package com.easyweigh.services;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.easyweigh.activities.AgentScaleEW15WeighActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;


public class AgentWeighingService extends Service {
	@SuppressLint("NewApi")
	// Debugging
	private static final String TAG = "WeighingService";
	private static final boolean D = true;

	// Name for the SDP record when creating server socket
	private static final String NAME_SECURE = "WeighingSecure";

	// Unique UUID for this application
	private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

	// Member fields
	//private final BluetoothAdapter mAdapter;
	//private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	public static int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;       // we're doing nothing
	public static final int STATE_LISTEN = 1;     // now listening for incoming connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	// Message codes received from the UI client.
	// Register client with this service.
	public static final int MSG_REG_CLIENT = 200;
	// Unregister client from this service.
	public static final int MSG_UNREG_CLIENT = 201;

	public static final int RETRY = 500;
	public static final int RECONNECT = 501;

	public static final int PRINT_REQUEST = 503;
	public static final int CANCEL_PRINT_REQUEST = 504;

	public static final int INIT_WEIGHING = 505;

	public static final int ZERO_SCALE = 506;
	public static final int TARE_SCALE = 507;

	public static final int PRINT_DELIVERY = 508;
	public static final int PRINT_Z_REPORT = 509;

	public static final int READING_PROBE = 510;

	public static final int DISCONNECT = 511;
	public static final String EASYWEIGH_VERSION_15 = "EW15";
	public static final String EASYWEIGH_VERSION_11 = "EW11";

	public static final String WEIGH_AND_TARE = "Discrete";
	public static final String FILLING = "Incremental";

	private int mChannelId;

	private Messenger mClient;
	private BluetoothDevice mDevice;
	private BluetoothAdapter mBluetoothAdapter;
	ProgressDialog mProcessDialog;

	String deviceAddress, scaleVersion;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.

	byte[] printRequestBytes,cancelPrintRequestBytes;
	String printRequest,cancelPrintRequest;

	SharedPreferences mSharedPrefs;
	private static final String MY_DB = "com.octagon.easyweigh_preferences";

	boolean readingProbed = false;

	boolean printingInProgress = false;
	BluetoothSocket mmSocket=null;
	private String mSocketType;
	InputStream mmInStream;
	OutputStream mmOutStream;
	BluetoothDevice mmDevice;
	/**
	 * Constructor. Prepares a new AgentScaleEW15WeighActivity session.
	 * @param //context  The UI Activity Context
	 * @param //handler  A Handler to send messages back to the UI Activity
	 */

	// Handles events sent by {@link HealthHDPActivity}.
	@SuppressLint("HandlerLeak")
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Register UI client to this service so the client can receive messages.
			case MSG_REG_CLIENT:
				Log.d(TAG, "Activity client registered");
				mClient = msg.replyTo;

				Log.i(TAG, "Got Address [" + AgentScaleEW15WeighActivity.getAddress() + "]");
				//String deviceAddress = AgentScaleEW15WeighActivity.getAddress().toString();

				if (deviceAddress != null && deviceAddress.length() > 0) { //Check if we have a valid bluetooth Mac Address
					try {//Attempting to connect using got address
						Log.i(TAG, "mState is " + mState);
						if (mState != STATE_CONNECTED) {
							mDevice = mBluetoothAdapter.getRemoteDevice(AgentScaleEW15WeighActivity.getAddress());
							connect(mDevice,false);	
						}

					} catch (Exception e) { //we failed so fire device list activity
						Log.e(TAG, "Error During Connection " + e.getMessage().toString());
					}
				} else { //if we do not have one the get one via DeviceListActivity
					//Send a message to UI activity to request DeviceListActivity
					Log.i(TAG, "Sending Message to UI activity");
					try {
						Message msg2 = Message.obtain(null, AgentScaleEW15WeighActivity.REQUEST_DEVICEADDRESS);
						tumaMessage(msg2);	
					} catch (Exception e2) {
						Log.e(TAG, "Error Sending Message " + e2.getMessage().toString());
					}
				}

				break;
				// Unregister UI client from this service.
			case MSG_UNREG_CLIENT:
				mClient = null;
				break;
			case RETRY:
				try {

					deviceAddress = msg.getData().getString(AgentScaleEW15WeighActivity.EXTRA_DEVICE_ADDRESS);

					mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
					connect(mDevice,false);

				} catch (Exception e) {
					Log.e(TAG, "Unable to reach client to request address " + e.getMessage());
				}
				break;
			case RECONNECT:
				deviceAddress = msg.getData().getString(AgentScaleEW15WeighActivity.EXTRA_DEVICE_ADDRESS);

				if (deviceAddress.equals("") || deviceAddress.equals(null)) {
					//Complete failure
					Message totalFailure  = Message.obtain(null, AgentScaleEW15WeighActivity.COMPLETE_FAILURE);
					tumaMessage(totalFailure);
				} else {
					mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
					connect(mDevice,false);
				}
				break;
			case PRINT_REQUEST:
				try {
					ArrayList<String> stringsToPrint = new ArrayList<String>();

					printRequest = String.valueOf((char) 16);

					printRequestBytes = printRequest.getBytes();
					write(printRequestBytes);

					String emptyLine = "  " + "\r\n";
					//now send data to print
					byte[] send = emptyLine.getBytes();
					write(send);	

					//lala kidogo
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.COMPANY) + "\r\n");
					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.ADDRESS_LINE1) + "\r\n");
					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.ADDRESS_LINE2) + "\r\n");

					stringsToPrint.add("  ----------------------------------\r\n");

					stringsToPrint.add("  RECEIPT    : " + msg.getData().getString(AgentScaleEW15WeighActivity.RECEIPT_NO) + "\r\n");
					stringsToPrint.add("  DATE       : " + msg.getData().getString(AgentScaleEW15WeighActivity.DATE) + "\r\n");
					stringsToPrint.add("  TIME       : " + msg.getData().getString(AgentScaleEW15WeighActivity.TIME) + "\r\n");
					stringsToPrint.add("  TERMINAL   : " + msg.getData().getString(AgentScaleEW15WeighActivity.TERMINAL) + "\r\n");
					stringsToPrint.add("  FARMER NO  : " + msg.getData().getString(AgentScaleEW15WeighActivity.FARMER_NO) + "\r\n");
					stringsToPrint.add("  NAME       : " + msg.getData().getString(AgentScaleEW15WeighActivity.NAME) + "\r\n");
					stringsToPrint.add("  ROUTE      : " + msg.getData().getString(AgentScaleEW15WeighActivity.ROUTE) + "\r\n");
					stringsToPrint.add("  SHED       : " + msg.getData().getString(AgentScaleEW15WeighActivity.SHED) + "\r\n");
					stringsToPrint.add("  BATCH      : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH) + "\r\n");
					stringsToPrint.add("  ----------------------------------\r\n");
					//stringsToPrint.add("  CAN        : " + msg.getData().getString(AgentScaleEW15WeighActivity.CAN) + "\r\n");
					stringsToPrint.add("  GROSS WT   : " + msg.getData().getString(AgentScaleEW15WeighActivity.GROSS_WEIGHT) + "\r\n");
					stringsToPrint.add("  TARE WT    : " + msg.getData().getString(AgentScaleEW15WeighActivity.TARE_WEIGHT) + "\r\n");
					stringsToPrint.add("  NET WT     : " + msg.getData().getString(AgentScaleEW15WeighActivity.NET_WEIGHT) + "\r\n");
					//stringsToPrint.add("  TOTAL KGS  : " + msg.getData().getString(AgentScaleEW15WeighActivity.TOTAL_WEIGHT) + "\r\n");
					stringsToPrint.add("  UNIT PRICE : " + msg.getData().getString(AgentScaleEW15WeighActivity.UNIT_PRICE) + "\r\n");
					stringsToPrint.add("  AMOUNT     : " + msg.getData().getString(AgentScaleEW15WeighActivity.AMOUNT) + "\r\n");
					stringsToPrint.add("  -----------------------------------\r\n");
					stringsToPrint.add("  CLERK NAME : " + msg.getData().getString(AgentScaleEW15WeighActivity.CLERK_NAME) +"\r\n");
					stringsToPrint.add("\r\n");
					stringsToPrint.add("\r\n");

					//lala kidogo
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					//Before We Print Send Print Command

					for (String str:stringsToPrint) {
						printingInProgress = true;

						printRequest = String.valueOf((char) 16);

						printRequestBytes = printRequest.getBytes();
						write(printRequestBytes);

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						//now send data to print
						send = str.getBytes();
						write(send);	
					}

					printingInProgress = false;
					/*printRequest = String.valueOf((char) 16);

					printRequestBytes = printRequest.getBytes();
					write(printRequestBytes);*/

					/*String message;
					message = "     *** RECEIPT SLIP ***\n";
					message+=  "     " + mSharedPrefs.getString("companyName", "").toString() + "\n";
					message+=  "     " + mSharedPrefs.getString("AddressLine1", "").toString() + "\n";
					message+=  "     " + mSharedPrefs.getString("AddressLine2", "").toString() + "\n";
					message+="\n";
					message+="  RECEIPT  : " + msg.getData().getString(AgentScaleEW15WeighActivity.RECEIPT_NO) + "\n\n";
					message+="  DATE     : " + msg.getData().getString(AgentScaleEW15WeighActivity.DATE) + "\n\n";
					message+="  TIME     : "+ msg.getData().getString(AgentScaleEW15WeighActivity.TIME) + "\n\n";
					message+="  TERMINAL : " + msg.getData().getString(AgentScaleEW15WeighActivity.TERMINAL) + "\n\n";
					message+="  FARMER NO: " + msg.getData().getString(AgentScaleEW15WeighActivity.FARMER_NO) + "\n\n";
					message+="  NAME     : " + msg.getData().getString(AgentScaleEW15WeighActivity.NAME) + "\n\n";
					message+="  ROUTE    : " + msg.getData().getString(AgentScaleEW15WeighActivity.ROUTE) + "\n\n";
					message+="  SHED     : " + msg.getData().getString(AgentScaleEW15WeighActivity.SHED) + "\n\n";
					message+="  BATCH    : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH) + "\n\n";
					message+="  ---------------------------------\n\n";
					message+="  CAN      : " + msg.getData().getString(AgentScaleEW15WeighActivity.CAN) + "\n\n";
					message+="  GROSS WT : " + msg.getData().getString(AgentScaleEW15WeighActivity.GROSS_WEIGHT) + "\n\n";
					message+="  TARE WT  : " + msg.getData().getString(AgentScaleEW15WeighActivity.TARE_WEIGHT) + "\n\n";
					message+="  NET WT   : " + msg.getData().getDouble(AgentScaleEW15WeighActivity.NET_WEIGHT) + "\n\n";
					message+="  TOTAL KGS: " + msg.getData().getString(AgentScaleEW15WeighActivity.TOTAL_WEIGHT) + "\n\n";
					message+="  ---------------------------------\n\n";
					message+="  CLERK NAME: " + msg.getData().getString(AgentScaleEW15WeighActivity.CLERK_NAME) +"\n\n";
					message+="\n\n";
					message+="\n\n";*/

					/*try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					//now send data to print
					byte[] send = message.getBytes();
					write(send);	*/				
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					//send cancel print request

					cancelPrintRequest = String.valueOf((char) 17);

					cancelPrintRequestBytes = cancelPrintRequest.getBytes();
					write(cancelPrintRequestBytes);

					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case CANCEL_PRINT_REQUEST:
				String cancelPrintRequest = String.valueOf((char) 17);

				byte[] cancelPrintRequestBytes = cancelPrintRequest.getBytes();
				write(cancelPrintRequestBytes);

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				break;
			case TARE_SCALE:
				new Thread("TaringThread")
				{
					@Override
					public void run()
					{
						try
						{
							String message = "T";
							byte[] send = message.getBytes();
							write(send);
							sleep(250); 
							//return;
						} catch (Exception e) {
							e.printStackTrace();
						}
						finally
						{
							interrupt();
						}
					}
				}
				.start();
				break;
			case PRINT_DELIVERY:
				try {
					ArrayList<String> stringsToPrint = new ArrayList<String>();

					printRequest = String.valueOf((char) 16);

					printRequestBytes = printRequest.getBytes();
					write(printRequestBytes);

					String emptyLine = "  " + "\r\n";
					//now send data to print
					byte[] send = emptyLine.getBytes();
					write(send);	

					//lala kidogo
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.COMPANY) + "\r\n");
					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.ADDRESS_LINE1) + "\r\n");
					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.ADDRESS_LINE2) + "\r\n");
					stringsToPrint.add("-------------------------------\r\n");
					stringsToPrint.add(" NUMBER           : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH_DELIVERY_NOTE_NO) + "\r\n");
					stringsToPrint.add(" DATE             : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH_DATE) + "\r\n");
					stringsToPrint.add(" TIME             : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH_CLOSE_TIME) + "\r\n");
					stringsToPrint.add(" TERMINAL         : " + msg.getData().getString(AgentScaleEW15WeighActivity.TERMINAL) + "\r\n");
					stringsToPrint.add(" FACTORY          : " + msg.getData().getString(AgentScaleEW15WeighActivity.FACTORY_CODE) + "\r\n");
					stringsToPrint.add(" BATCH NO         : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH_NO) + "\r\n");
					stringsToPrint.add(" TOTAL WT         : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH_TOTAL) + "\r\n");
					stringsToPrint.add(" TOTAL RECCORDS   : " + msg.getData().getString(AgentScaleEW15WeighActivity.TOTAL_WEIGHMENTS) + "\r\n");
					stringsToPrint.add(" SESSION          : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH__WEIGHINGPERIOD) + "\r\n");
					stringsToPrint.add(" TRUCK            : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH_VEHICLE_NO) + "\r\n");
					stringsToPrint.add("-------------------------------\r\n");
					stringsToPrint.add(" CLERK NAME       : " + msg.getData().getString(AgentScaleEW15WeighActivity.BATCH_USER_ID) + "\r\n");
					stringsToPrint.add("\r\n");
					stringsToPrint.add("\r\n");

					for (String str:stringsToPrint) {
						printingInProgress = true;

						printRequest = String.valueOf((char) 16);

						printRequestBytes = printRequest.getBytes();
						write(printRequestBytes);

						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						//now send data to print
						send = str.getBytes();
						write(send);	
					}

					printingInProgress = false;

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					//send cancel print request

					cancelPrintRequest = String.valueOf((char) 17);

					cancelPrintRequestBytes = cancelPrintRequest.getBytes();
					write(cancelPrintRequestBytes);

					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				break;
			case PRINT_Z_REPORT:
				/*SQLDataHelper mDbHelper = null;

				try {
					String selectedBatch,selectedDate,terminal;

					mDbHelper = new SQLDataHelper(getApplicationContext());
					mDbHelper.open();


					selectedBatch = msg.getData().getString(AgentScaleEW15WeighActivity.SELECTED_BATCH);
					selectedDate = msg.getData().getString(AgentScaleEW15WeighActivity.SELECTED_DATE);
					terminal = msg.getData().getString(AgentScaleEW15WeighActivity.TERMINAL);

					int batchUniqueID = 0; 
					batchUniqueID = mDbHelper.getBatchUniqueID(selectedDate, selectedBatch);

					Cursor batchInfo = mDbHelper.getBatchInfo(batchUniqueID);

					Cursor records  = mDbHelper.getRecordsPrint(batchUniqueID);

					ArrayList<String> stringsToPrint = new ArrayList<String>();

					printRequest = String.valueOf((char) 16);

					printRequestBytes = printRequest.getBytes();
					write(printRequestBytes);

					String emptyLine = "  " + "\r\n";
					//now send data to print
					byte[] send = emptyLine.getBytes();
					write(send);	

					//lala kidogo
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//stringsToPrint.add("                 " + msg.getData().getString(AgentScaleEW15WeighActivity.MESSAGE_HEADER) + "\r\n");
					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.COMPANY) + "\r\n");
					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.ADDRESS_LINE1) + "\r\n");
					stringsToPrint.add("  " + msg.getData().getString(AgentScaleEW15WeighActivity.ADDRESS_LINE2) + "\r\n");
					stringsToPrint.add("-------------------------------\r\n");
					stringsToPrint.add(" DATE          : " + batchInfo.getString(batchInfo.getColumnIndex(SQLConstants.ConsignmentTable.CONSIGNMENT_DATE)) + "\r\n");
					stringsToPrint.add(" TERMINAL      : " + terminal + "\r\n");
					stringsToPrint.add(" CONSIGNMENT   : " + batchInfo.getString(batchInfo.getColumnIndex(SQLConstants.ConsignmentTable.CONSIGNMENT_NUMBER)) + "\r\n");

					if(mSharedPrefs.getBoolean("enableShifts", false)) {
						stringsToPrint.add(" PERIOD        : " + batchInfo.getString(batchInfo.getColumnIndex(SQLConstants.ConsignmentTable.WEIGHING_PERIOD)) + "\r\n");
					}

					stringsToPrint.add("-------------------------------\r\n");
					stringsToPrint.add(" FARMER NO     KGS\r\n");
					stringsToPrint.add("-------------------------------\r\n");
					stringsToPrint.add("==============================\r\n");
					stringsToPrint.add(" TOTAL          " + batchInfo.getString(batchInfo.getColumnIndex(SQLConstants.ConsignmentTable.TOTAL_WEIGHT)) + "\r\n");
					String batchUserID  = batchInfo.getString(batchInfo.getColumnIndex(SQLConstants.ConsignmentTable.USERID));

					stringsToPrint.add(" Weighed By: " + batchUserID + "\r\n");

					//stringsToPrint.add(" " + mDbHelper.getClerkByID(batchUserID) +"\r\n");
					stringsToPrint.add("\r\n");
					stringsToPrint.add("\r\n");

					for (String str:stringsToPrint) {
						printingInProgress = true;

						printRequest = String.valueOf((char) 16);

						printRequestBytes = printRequest.getBytes();
						write(printRequestBytes);

						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						//now send data to print
						send = str.getBytes();
						write(send);	
					}

					printingInProgress = false;

					//now open cursor and loop through records
					for (records.moveToFirst(); !records.isAfterLast(); records.moveToNext()) {
						/*message+= " " + records.getString(records.getColumnIndex(SQLConstants.PurchaseTable.FARMER_NUMBER))  + "     " +
								records.getString(records.getColumnIndex("TOTALWEIGHT")) + "\r\n";*//*
						stringsToPrint.add(" " + records.getString(records.getColumnIndex(SQLConstants.PurchaseTable.FARMER_NUMBER))  + "     " +
								records.getString(records.getColumnIndex("TOTALWEIGHT")) + "\r\n");
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mDbHelper.close();
					cancelPrintRequest = String.valueOf((char) 17);

					cancelPrintRequestBytes = cancelPrintRequest.getBytes();
					write(cancelPrintRequestBytes);

					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
*/
				break;
			case ZERO_SCALE:
					new Thread("TaringThread")
					{
						@Override
						public void run()
						{
							try
							{
								String message = "Z";
								byte[] send = message.getBytes();
								write(send);
								sleep(250);
								//return;
							} catch (Exception e) {
								e.printStackTrace();
							}
							finally
							{
								interrupt();
							}
						}
					}
							.start();
					break;
			case READING_PROBE:
				String message = "R";
				byte[] send = message.getBytes();

				try {
					readingProbed = true;
					write(send);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Make sure Bluetooth and health profile are available on the Android device.  Stop service
	 * if they are not available.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "AgentScaleEW15WeighActivity Service is running.");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mSharedPrefs = this.getSharedPreferences(MY_DB,
				Context.MODE_PRIVATE);

		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			// Bluetooth adapter isn't available.  The client of the service is supposed to
			// verify that it is available and activate before invoking this service.
			AgentWeighingService.this.stop();
			return;
		} 
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			Bundle b = intent.getExtras();
			deviceAddress = b.getString(AgentScaleEW15WeighActivity.EXTRA_DEVICE_ADDRESS);
			scaleVersion = b.getString(AgentScaleEW15WeighActivity.SCALE_VERSION);

		} catch (Exception e) {
			Log.e(TAG, "Failure on Bundle " + e.toString());
		}
		return START_STICKY;	
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return mMessenger.getBinder();
	}

	private void tumaMessage(Message msg) {
		if (mClient == null) {
			Log.d(TAG,"No Clients Registered");
			return;
		}

		try {
			mClient.send(msg);
		} catch (RemoteException e) {
			// Unable to reach client.
			e.printStackTrace();
		}
	}

	/**
	 * Set the current state of the chat connection
	 * @param state  An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		//mHandler.obtainMessage(AgentScaleEW15WeighActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
		Message msg = Message.obtain(null, AgentScaleEW15WeighActivity.MESSAGE_STATE_CHANGE, state, -1);
		tumaMessage(msg);
	}

	/**
	 * Return the current connection state. */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume() */
	public synchronized void start() {
		if (D) Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		setState(STATE_LISTEN);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * @param device  The BluetoothDevice to connect
	 * @param secure Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(BluetoothDevice device, boolean secure) {
		if (D) Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device, secure);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * @param socket  The BluetoothSocket on which the connection was made
	 * @param device  The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice
			device, final String socketType) {
		if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		//Message msg = mHandler.obtainMessage(AgentScaleEW15WeighActivity.MESSAGE_DEVICE_NAME);
		Message msg  = Message.obtain(null, AgentScaleEW15WeighActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(AgentScaleEW15WeighActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		tumaMessage(msg);

		setState(STATE_CONNECTED);

		if(mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
			Log.i(TAG, "Algo is " + mSharedPrefs.getString("weighingAlgorithm", "Incremental"));
			if(mSharedPrefs.getString("weighingAlgorithm", "Incremental").toString().equals(FILLING)) { 
				Log.i(TAG, "Sending tare command");
				new Thread("TaringThread")
				{
					@Override
					public void run()
					{
						try
						{
							String message = "T";
							byte[] send = message.getBytes();
							write(send);
							sleep(250); 
							//return;
						} catch (Exception e) {
							e.printStackTrace();
						}
						finally
						{
							interrupt();
						}
					}
				}
				.start();
			}
		}

		new Thread("WeighingThread")
		{
			@Override
			public void run()
			{
				try
				{
					//get scale version
					if(scaleVersion.equals(AgentScaleEW15WeighActivity.EASYWEIGH_VERSION_11)) {
						//  sending R to get readings from BT Scale
						while (true) {
							String message = "R" + "\r" + "\n";
							byte[] send = message.getBytes();
							if(!printingInProgress)
								write(send);
							sleep(100); 
							//return;
						}
					} else if (scaleVersion.equals(AgentScaleEW15WeighActivity.EASYWEIGH_VERSION_15)) {
						//  sending R to get readings from BT Scale
						while (true) {
							String message = "R";
							byte[] send = message.getBytes();
							if(!printingInProgress)
								write(send);
							sleep(1000); 
							//return;
						}	
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				finally
				{
					//mProcessDialog.cancel();
				}
			}
		}
		.start();
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		setState(STATE_NONE);

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mBluetoothAdapter != null) {

			mBluetoothAdapter.cancelDiscovery();
		}
		connectionFailed();
		AgentWeighingService.this.stopSelf();
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * @param out The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED) return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
		//Message msg = mHandler.obtainMessage(AgentScaleEW15WeighActivity.MESSAGE_TOAST);
		Message msg = Message.obtain(null, AgentScaleEW15WeighActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(AgentScaleEW15WeighActivity.TOAST, "Unable to connect scale");
		msg.setData(bundle);
		tumaMessage(msg);
		//mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		AgentWeighingService.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		//Message msg = mHandler.obtainMessage(AgentScaleEW15WeighActivity.MESSAGE_TOAST);
		Message msg = Message.obtain(null, AgentScaleEW15WeighActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(AgentScaleEW15WeighActivity.TOAST, "Scale Disconnected");
		msg.setData(bundle);
		tumaMessage(msg);

		setState(STATE_NONE);
		// Start the service overs to restart listening mode

	}
	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread {



		public ConnectThread(BluetoothDevice device, boolean secure) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				if (secure) {
					try {
						Method m = device.getClass().getMethod("createRfcommSocket", int.class);
						tmp = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
					} catch (Exception e) {
						Log.e(TAG, "Error at HTC/createRfcommSocket: " + e);
						e.printStackTrace();
					}
				} else {
					tmp = device.createRfcommSocketToServiceRecord(
							MY_UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
			}
			mmSocket = tmp;
		}

		@Override
		public void run() {
			Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			//mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + mSocketType +
							" socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (AgentWeighingService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {


		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		@Override
		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					//bytes = mmInStream.read(buffer, 0, buffer.length);
					// Send the obtained bytes to the UI Activity
					/*mHandler.obtainMessage(AgentScaleEW15WeighActivity.MESSAGE_READ, bytes, -1, buffer)
					.sendToTarget();*/
					Message msg = Message.obtain(null, AgentScaleEW15WeighActivity.MESSAGE_READ, bytes, -1, buffer);
					tumaMessage(msg);
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * @param buffer  The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				Message msg = null;
				if (readingProbed) {
					msg = Message.obtain(null, AgentScaleEW15WeighActivity.READING_PROBE, -1, -1, buffer);
				} else {
					msg = Message.obtain(null, AgentScaleEW15WeighActivity.MESSAGE_WRITE, -1, -1, buffer);
				}
				// Share the sent message back to the UI Activity

				tumaMessage(msg);
				readingProbed = false;
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}


}

