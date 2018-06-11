/*****************************************************************************
 * Copyright (c) 2014 Laird Technologies. All Rights Reserved.
 * 
 * The information contained herein is property of Laird Technologies.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/

package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.connector.P25ConnectionException;
import com.easyweigh.connector.P25Connector;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.printerdata.PocketPos;
import com.easyweigh.printerutils.DataConstants;
import com.easyweigh.printerutils.DateUtil;
import com.easyweigh.printerutils.FontDefine;
import com.easyweigh.printerutils.Printer;
import com.easyweigh.printerutils.Util;
import com.easyweigh.services.WeighingService;
import com.easyweigh.trancell.BleBaseActivity;
import com.easyweigh.trancell.WeighBaseActivity;
import com.easyweigh.vsp.serialdevice.SerialManager;
import com.easyweigh.vsp.serialdevice.SerialManagerUiCallback;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class AgentScaleSerialWeighActivity extends WeighBaseActivity implements
		SerialManagerUiCallback
{
	public Toolbar toolbar;
	private Button mBtnSend;
	private ScrollView mScrollViewConsoleOutput;
	private EditText mInputBox;
	private TextView mValueConsoleOutputTv;
	private TextView mValueRxCounterTv;
	private TextView mValueTxCounterTv;

	private SerialManager mSerialManager;

	private boolean isPrefClearTextAfterSending = false;
	private boolean isPrefSendCR = true;

	SearchView searchView;
	public SimpleCursorAdapter ca;
	DBHelper dbhelper;
	SQLiteDatabase db;
	DecimalFormat formatter;
	ListView listFarmers;
	String accountId;
	TextView textAccountId,tv_number,txtFarmer;
	static SharedPreferences mSharedPrefs;
	static SharedPreferences prefs;
	Boolean success = true;
	static TextView tvMemberName,tvShowMemberNo,tvShowGrossTotal,tvWeighingAccumWeigh,tvWeighingTareWeigh,
			tvUnitsCount,tvShowTotalKgs,tvGross,tvNetWeightAccepted,tvGrossAccepted,txtKgs,txtScaleConn,txtPrinterConn;
	EditText etShowGrossTotal;
	static TextView tvsavedReading,tvSavedNet,tvSavedTare,tvSavedUnits,tvSavedTotal;
	Typeface font;
	String SessionDate,SessionTime,SessionDevice,SessionAgentNo,SessionBags,SessionNet,SessionTare,SessionBatchID,SessionClerkID,SessionWarehouseID,SessionProduceCode,SessionVarietyCode,SessionGradeCode;
	String MobileNo,ClerkName,GrossWeight,TotalKGs,Date,TotalKg;
	int recieptNo=1;
	int SNo=1;
	int SessNo=1;
	static String SessionID,SessionNo,ReceiptNo,ColDate,BatchDate,Time, DataDevice,BatchNo,Agent,FarmerNo,FarmerName,MTDKGS,Kgs,Tare;
	String WorkerNo, FieldClerk,ProduceCode;
	String VarietyCode,GradeCode, RouteCode, ShedCode;
	String TransporterCode,GrossTotal,TareWeight,UnitCount;
	String UnitPrice, RecieptNo,BagCount,produce, Warehouse,NetWeight,Accummulated;
	String newGross,newNet,newTare;
	String AgentName;
	public static String rFarmerNo,rFarmerPhone,rAccummulated;

	public static String cachedDeviceAddress;
	WeighingService resetConn;
	static Double myGross=0.0;
	int weighmentCounts = 1;

	public static ProgressDialog mConnectingDlg;
	public static BluetoothAdapter mBluetoothAdapter;
	public static P25Connector mConnector;

	String ScaleConn;
	public static AlertDialog weigh;
	public static Button btnReconnect;
	static Double netweight=0.0;
	String weighingSession = "";
	Button btn_accept,btn_next,btn_print,btn_reconnect;
	LinearLayout lt_accept,lt_nprint;
	static boolean stopRefreshing = false;

	static double  setTareWeight = 0.0;
	static Context _ctx;
	static Activity _activity;
	public static final String TRANCELL_TI500 = "TI-500";
	private Timer timer;
	SimpleDateFormat dateTimeFormat;
	static int SessionBgs;
	String farmers,farmerNo,manageid;
	ArrayList<String> farmerdata=new ArrayList<String>();
	ArrayAdapter<String> farmeradapter;
	Spinner spFarmer;
	View rootView;
	Button btnBack;
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_listagents_weigh_serial);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mSerialManager = new SerialManager(this, this);
		setBleBaseDeviceManager(mSerialManager);

		//initialiseDialogAbout(getResources().getString(R.string.about_serial));
		initialiseDialogFoundDevices("VSP");
		initializer();
		_ctx = this;
		_activity = this;
	}


	@Override
	public void bindViews()
	{
		super.bindViews();

		mBtnSend = (Button) findViewById(R.id.btnSend);
		mScrollViewConsoleOutput = (ScrollView) findViewById(R.id.scrollViewConsoleOutput);
		mInputBox = (EditText) findViewById(R.id.inputBox);
		mValueConsoleOutputTv = (TextView) findViewById(R.id.valueConsoleOutputTv);
		mValueRxCounterTv = (TextView) findViewById(R.id.valueRxCounterTv);
		mValueTxCounterTv = (TextView) findViewById(R.id.valueTxCounterTv);
	}
	private static void showToast(String message) {
		Toast.makeText(_activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	private static void showUnsupported() {
		showToast("Bluetooth is unsupported by this device");

	}
	private static void showConnected() {
		showToast("Printer Connected");


	}

	private static void showDisonnected() {
		showToast("Printer Disconnected");

	}

	private static void connect() {


		///BluetoothDevice device = mDeviceList.get(mDeviceSp.getSelectedItemPosition());
		String mDevice = prefs.getString("mDevice", "");
		BluetoothDevice device =mBluetoothAdapter.getRemoteDevice(mDevice);


		if (device.getBondState() == BluetoothDevice.BOND_NONE) {
			try {
				createBond(device);
			} catch (Exception e) {
				showToast("Failed to pair device");

				return;
			}
		}

		try {
			if (!mConnector.isConnected()) {
				mConnector.connect(device);
			} else {
				mConnector.disconnect();

				//showDisonnected();
			}
		} catch (P25ConnectionException e) {
			e.printStackTrace();
		}
	}

	private static void createBond(BluetoothDevice device) throws Exception {

		try {
			Class<?> cl 	= Class.forName("android.bluetooth.BluetoothDevice");
			Class<?>[] par 	= {};

			Method method 	= cl.getMethod("createBond", par);

			method.invoke(device);

		} catch (Exception e) {
			e.printStackTrace();

			throw e;
		}
	}

	private void sendData(byte[] bytes) {
		try {
			mConnector.sendData(bytes);
		} catch (P25ConnectionException e) {
			e.printStackTrace();
		}
	}
	private static final BroadcastReceiver pReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state 	= intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				if (state == BluetoothAdapter.STATE_ON) {
					//showEnabled();
				} else if (state == BluetoothAdapter.STATE_OFF) {
					//showDisabled();
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				//mDeviceList = new ArrayList<BluetoothDevice>();

				// mProgressDlg.show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// mProgressDlg.dismiss();

				// updateDeviceList();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				//mDeviceList.add(device);

				showToast("Found device " + device.getName());
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

				if (state == BluetoothDevice.BOND_BONDED) {
					showToast("Paired");

					connect();
				}
			}
		}
	};
	public void disableBT(){
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.disable();
		}
	}
	public void enableBT(){
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.enable();
		}
	}
	public void initializer(){

		txtKgs = (TextView) this.findViewById(R.id.txtKGS);
		dbhelper = new DBHelper(getApplicationContext());
		db = dbhelper.getReadableDatabase();
		formatter = new DecimalFormat("0000");
		dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(AgentScaleSerialWeighActivity.this);
		prefs = PreferenceManager.getDefaultSharedPreferences(AgentScaleSerialWeighActivity.this);
		txtPrinterConn = (TextView) this.findViewById(R.id.txtPrinterConn);
		btnReconnect = (Button) this.findViewById(R.id.btnReconnect);
		btnReconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				connect();
			}
		});


		        SharedPreferences.Editor edit = prefs.edit();
				edit.remove("Gross");
				edit.remove("Net");
				edit.commit();

				showWeigh();
		        //FarmerList();
		rootView = findViewById(R.id.rootLayout);
		txtFarmer = (TextView) this.findViewById(R.id.txtFarmer);
		txtFarmer.setText(prefs.getString("Farmer","Farmer:"));
		txtFarmer.setTextColor(Color.BLACK);
		searchView=(SearchView) findViewById(R.id.searchView);
		searchView.setQueryHint("Search Farmer No ...");
		//searchView.requestFocus();
		searchView.setQuery("", false);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {

						String fNo = query.toString();
				Cursor d = dbhelper.SearchOneFarmer(fNo);
				if(d.getCount()>0){
					String Farmer=d.getString(0)+"-"+d.getString(1);
					txtFarmer.setTextColor(Color.BLACK);
					txtFarmer.setText(Farmer);
					farmerNo=d.getString(0);
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("Farmer", Farmer);
					edit.commit();
					edit.putString("FarmerNo", farmerNo);
					edit.commit();
				    }
				else{
					farmerNo="";
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("Farmer", "Farmer:");
					edit.commit();
					edit.putString("FarmerNo",farmerNo);
					edit.commit();
					txtFarmer.setTextColor(Color.RED);
					txtFarmer.setText("Farmer Not Found!!");
				}


				// Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

						//String farmerNo = newText.toString();
						//dbhelper.SearchFarmer(farmerNo);


				//Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
				return false;
			}
		});


	}
	@Override
	public void setListeners()
	{
		super.setListeners();

		mBtnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// to send data to module
				String data = mInputBox.getText().toString();
				if (data != null) {
					mBtnSend.setEnabled(false);
					if (mValueConsoleOutputTv.getText().length() <= 0) {
						mValueConsoleOutputTv.append(">");
					} else {
						mValueConsoleOutputTv.append("\n\n>");
					}

					if (isPrefSendCR == true) {
						mSerialManager.startDataTransfer(data + "\r");
					} else if (isPrefSendCR == false) {
						mSerialManager.startDataTransfer(data);
					}

					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

					inputManager.hideSoftInputFromWindow(getCurrentFocus()
									.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					if (isPrefClearTextAfterSending == true) {
						mInputBox.setText("");
					} else {
						// do not clear the text from the editText
					}
				}
			}
		});
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onStart() {
		super.onStart();
	
	}

	public void onResume() {
		super.onResume();
		rootView.requestFocus();
		searchView.clearFocus();

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
			// go back to milkers activity
			//Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
		} else {
			/*if (mConnector != null) {
				txtPrinterConn.setVisibility(View.VISIBLE);

			}*/
		}
		setTareWeight = Double.parseDouble( mSharedPrefs.getString("tareWeight", "0"));


	}


	public void showWeigh() {
		tvMemberName = (TextView) findViewById(R.id.tvMemberNameShow);
		tvShowMemberNo = (TextView) findViewById(R.id.tvShowMemberNo);
		
		spFarmer = (Spinner) findViewById(R.id.spFarmer);


		tvShowMemberNo.setText(prefs.getString("agentCode", ""));
		tvMemberName.setText(prefs.getString("agentName", ""));

		tvWeighingAccumWeigh = (TextView) findViewById(R.id.tvWeighingAccumWeigh);
		tvWeighingAccumWeigh.setTypeface(font);

		tvWeighingTareWeigh = (TextView) findViewById(R.id.tvWeighingTareWeigh);
		tvWeighingTareWeigh.setTypeface(font);
		tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));

		tvShowTotalKgs = (TextView) findViewById(R.id.tvShowTotalKgs);
		tvShowTotalKgs.setTypeface(font);

		tvsavedReading = (TextView) findViewById(R.id.tvvGross);
		tvSavedNet = (TextView) findViewById(R.id.tvvTotalKgs);
		tvSavedTare = (TextView) findViewById(R.id.tvTareWeight);
		tvSavedUnits = (TextView) findViewById(R.id.tvvcount);
		tvSavedTotal = (TextView) findViewById(R.id.tvAccumWeight);

		tvUnitsCount = (TextView) findViewById(R.id.tvUnitsCount);
		tvUnitsCount.setTypeface(font);
		tvUnitsCount.setText(String.valueOf(weighmentCounts));
		tvShowGrossTotal = (TextView) findViewById(R.id.tvShowGrossTotal);
		tvShowGrossTotal.setTypeface(font);
		tvShowGrossTotal.setText("0.0");
		tvGrossAccepted = (TextView) findViewById(R.id.tvGrossAccepted);
		tvGrossAccepted.setTypeface(font);
		tvNetWeightAccepted = (TextView) findViewById(R.id.tvNetWeightAccepted);
		tvNetWeightAccepted.setTypeface(font);

		lt_accept=(LinearLayout)findViewById(R.id.lt_accept);
		lt_nprint=(LinearLayout)findViewById(R.id.lt_nprint);
		btn_accept = (Button) findViewById(R.id.btn_accept);
		btn_next = (Button) findViewById(R.id.btn_next);
		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBleBaseDeviceManager.disconnect();
				finish();


			}
		});
		btn_print = (Button) findViewById(R.id.btn_print);
		btn_reconnect = (Button) findViewById(R.id.btn_reconnect);
		btn_reconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				connect();


			}
		});
		btn_reconnect.setVisibility(View.GONE);
		btn_accept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {


				if(tvShowGrossTotal.getText().equals("0.0")) {
					Context context = getApplicationContext();
					LayoutInflater inflater = getLayoutInflater();
					View customToastroot = inflater.inflate(R.layout.red_toast, null);
					TextView text = (TextView) customToastroot.findViewById(R.id.toast);
					text.setText("Gross Total Cannot be 0.0, Please Request For Gross Reading");
					Toast customtoast = new Toast(context);
					customtoast.setView(customToastroot);
					customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
					customtoast.setDuration(Toast.LENGTH_LONG);
					customtoast.show();

					return;

				}
				netweight=Double.parseDouble(tvShowTotalKgs.getText().toString());
				if(netweight<=0.5) {
					Context context = getApplicationContext();
					LayoutInflater inflater = getLayoutInflater();
					View customToastroot = inflater.inflate(R.layout.red_toast, null);
					TextView text = (TextView) customToastroot.findViewById(R.id.toast);
					text.setText("Unacceptable Net Weight! should be greater than 0.5");
					Toast customtoast = new Toast(context);
					customtoast.setView(customToastroot);
					customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
					customtoast.setDuration(Toast.LENGTH_LONG);
					customtoast.show();
					//Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
					return;

				}


				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
				// Setting Dialog Title
				LayoutInflater inflater = getLayoutInflater();
				final View dialogView = inflater.inflate(R.layout.dialog_grossweight, null);
				dialogBuilder.setView(dialogView);
				dialogBuilder.setTitle("Accept Reading?");
				// Setting Dialog Message
				//dialogBuilder.setMessage("Are you sure you want to accept the gross reading?");
				tvGross = (TextView) dialogView.findViewById(R.id.txtGross);
				tvGross.setTypeface(font);

				if(tvShowGrossTotal.getText().equals("0.0")) {

					tvGross.setText("0 KG");

				}
				else{

					tvGross.setText(tvShowTotalKgs.getText().toString() +" KG");
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("Gross", tvShowGrossTotal.getText().toString());
					edit.commit();
					edit.putString("Net", tvShowTotalKgs.getText().toString());
					edit.commit();
					edit.putString("Accum", tvWeighingAccumWeigh.getText().toString());
					edit.commit();

				}


				///Setting Negative "Yes" Button
				dialogBuilder.setNegativeButton("YES",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {

                                // Check if no view has focus:
								searchView.setVisibility(View.GONE);
								View view = getCurrentFocus();
								if (view != null) {
									InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
								}

								dbhelper = new DBHelper(getApplicationContext());
								SQLiteDatabase db = dbhelper.getReadableDatabase();
								formatter = new DecimalFormat("0000");
								Date BatchD = null;
								try {
									BatchD = dateTimeFormat.parse(prefs.getString("AgentBatchON", "").toString() + " 00:00:00");
								} catch (ParseException e) {
									e.printStackTrace();
								}
								SimpleDateFormat format0 = new SimpleDateFormat("yyyyMMdd");
								BatchDate=format0.format(BatchD);
								BatchNo=prefs.getString("BatchNumb", "");
								DataDevice = mSharedPrefs.getString("terminalID", "").toString()+BatchDate+BatchNo;
								Calendar cal = Calendar.getInstance();
								java.util.Date date = new Date(getDate());
								SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
								SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
								ColDate = format1.format(date);
								Time= format2.format(cal.getTime());
								FarmerNo=prefs.getString("FarmerNo","");;
								Agent=tvShowMemberNo.getText().toString();
								ProduceCode=prefs.getString("produceCode", "");
								UnitPrice=prefs.getString("unitPrice", "");
								Warehouse = prefs.getString("warehouseCode", " ");
								TareWeight= tvWeighingTareWeigh.getText().toString();
								Accummulated=prefs.getString("Accum", "");
								UnitCount=tvUnitsCount.getText().toString();
								newNet=prefs.getString("Net", "");
								newGross=prefs.getString("Gross", "");
								final DecimalFormat df = new DecimalFormat("#0.0#");
								Double Accum=0.0,NewAccum=0.0;
								Accum = Double.parseDouble(newNet)+Double.parseDouble(Accummulated);
								//NewAccum=Accum+Double.parseDouble(rAccummulated);

								Double Gross=0.0, Net=0.0,Tare=0.0;
								Net = Accum;
								Tare = Double.parseDouble(tvUnitsCount.getText().toString())*Double.parseDouble(tvWeighingTareWeigh.getText().toString());
								Gross = Net+Tare;
								GrossTotal=df.format(Gross);
								NetWeight=df.format(Net);
								newTare=df.format(Tare);

								SharedPreferences.Editor edit = prefs.edit();
								edit.putString("textReciept", DataDevice + RecieptNo);
								edit.commit();
								edit.putString("textTransDate", ColDate);
								edit.commit();
								edit.putString("textTransTime", Time);
								edit.commit();
								edit.putString("textTerminal", DataDevice);
								edit.commit();
								edit.putString("textFarmerNo", FarmerNo);
								edit.commit();
								edit.putString("textName", tvMemberName.getText().toString());
								edit.commit();
								edit.putString("textTrip", BatchNo);
								edit.commit();
								edit.putString("textBags", UnitCount);
								edit.commit();
								edit.putString("textGrossWt", GrossTotal);
								edit.commit();
								edit.putString("textTareWt", newTare);
								edit.commit();
								edit.putString("textNetWt", NetWeight);
								edit.commit();


								Cursor sessionid = db.rawQuery("select * from ASessionTbl ", null);
								if (sessionid.getCount() > 0) {
									Cursor c = db.rawQuery("select MAX(_id) from ASessionTbl", null);
									if (c != null) {

										c.moveToFirst();

										SNo = Integer.parseInt(c.getString(0)) + 1;
										SessionID = String.valueOf(SNo);

									}
									c.close();
								} else {
									SessionID = String.valueOf(SNo);

								}
								Cursor session = db.rawQuery("select * from ASessionTbl where " + Database.ASessionDevice + " = '" + DataDevice + "'", null);
								if (session.getCount() > 0) {
									Cursor c1 = db.rawQuery("select MAX(areceiptno) from ASessionTbl where " + Database.ASessionDevice + " = '" + DataDevice + "'", null);
									if (c1 != null) {

										c1.moveToFirst();

										SessNo = Integer.parseInt(c1.getString(0)) + 1;
										SessionNo = formatter.format(SessNo);

									}
									c1.close();
								} else {
									SessionNo = formatter.format(SessNo);

								}

								SessionDate=ColDate;
								SessionTime=Time;
								SessionDevice=DataDevice;
								SessionAgentNo=Agent;
								SessionBags=UnitCount;
								SessionNet=NetWeight;
								SessionTare=newTare;
								SessionBatchID=BatchNo;
								SessionClerkID=prefs.getString("user", "");
								SessionWarehouseID=Warehouse;
								SessionProduceCode=ProduceCode;
								SessionVarietyCode="";
								SessionGradeCode="";

								SessionBgs = Integer.parseInt(tvUnitsCount.getText().toString());
								if (SessionBgs == 1) {
									edit.putString("SessionID", SessionID);
									edit.commit();
									edit.putString("SessionNo", SessionNo);
									edit.commit();

									dbhelper.AddASession(SessionID, SessionDate, SessionTime, SessionDevice, SessionAgentNo
											,SessionBags, SessionNet, SessionTare, SessionNo,SessionBatchID,SessionClerkID
											,SessionWarehouseID,SessionProduceCode,SessionVarietyCode,SessionGradeCode);

								}else{
									SessionID=prefs.getString("SessionID","");
									SessionNo=prefs.getString("SessionNo","");
									edit.putString("textReciept", DataDevice + SessionNo);
									edit.commit();
									ContentValues value = new ContentValues();
									value.put( Database.ASessionTime,SessionTime);
									value.put( Database.ASessionBags,SessionBags);
									value.put( Database.ASessionNet,SessionNet);
									value.put( Database.ASessionTare,SessionTare);

									long row = db.update(Database.ASESSION_TABLE_NAME, value,
											"_id = ?", new String[] { SessionID });

									// db.close();
									if (row > 0){
										// Toast.makeText(getApplicationContext(), "Updated Total KGs Successfully!",Toast.LENGTH_LONG).show();
									}
									else{
										Toast.makeText(getApplicationContext(), "Sorry! Could not update Session!",
												Toast.LENGTH_LONG).show();
									}
								}

								dbhelper.AddAgentTrans(ColDate, Time, DataDevice, BatchNo, Agent, FarmerNo,
										ProduceCode, newNet, TareWeight, UnitCount,UnitPrice, SessionNo, Warehouse);


								Context context=getApplicationContext();
								LayoutInflater inflater=getLayoutInflater();
								View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
								TextView text = (TextView) customToastroot.findViewById(R.id.toast);
								text.setText("Saved Successfully: " + newGross + " Kg\nRecieptNo: " + DataDevice + SessionNo + "\nAgentNo: " + Agent);
								Toast customtoast=new Toast(context);
								customtoast.setView(customToastroot);
								customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
								customtoast.setDuration(Toast.LENGTH_LONG);
								//customtoast.show();

								tvShowGrossTotal.setVisibility(View.GONE);
								tvShowTotalKgs.setVisibility(View.GONE);

								tvGrossAccepted.setVisibility(View.VISIBLE);
								tvGrossAccepted.setText(newGross);
								tvNetWeightAccepted.setVisibility(View.VISIBLE);

								tvNetWeightAccepted.setText(newNet);
								tvWeighingAccumWeigh.setText(df.format(Accum));

								lt_accept.setVisibility(View.GONE);
								lt_nprint.setVisibility(View.VISIBLE);

								tvsavedReading.setText("Saved Reading");
								tvSavedNet.setText("Saved Net");
								tvSavedTare.setText("Saved Tare");
								tvSavedUnits.setText("Saved Units");
								tvSavedTotal.setText("Saved Total");

								tvsavedReading.setTextColor(getResources().getColor(R.color.colorPinkDark));
								tvSavedNet.setTextColor(getResources().getColor(R.color.colorPinkDark));
								tvSavedTare.setTextColor(getResources().getColor(R.color.colorPinkDark));
								tvSavedUnits.setTextColor(getResources().getColor(R.color.colorPinkDark));
								tvSavedTotal.setTextColor(getResources().getColor(R.color.colorPinkDark));



							}
						});
				// Setting Positive "NO" Button
				dialogBuilder.setPositiveButton("NO",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {


							}
						});
				// Showing Alert Message
				dialogBuilder.show();
			}
		});
		btn_next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				myGross=Double.parseDouble(tvShowTotalKgs.getText().toString());
				if (myGross>0){
					Context context = AgentScaleSerialWeighActivity.this;
					LayoutInflater inflater = AgentScaleSerialWeighActivity.this.getLayoutInflater();
					View customToastroot = inflater.inflate(R.layout.red_toast, null);
					TextView text = (TextView) customToastroot.findViewById(R.id.toast);
					text.setText("Please Remove Load!\nTo Continue ...");
					Toast customtoast = new Toast(context);
					customtoast.setView(customToastroot);
					customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
					customtoast.setDuration(Toast.LENGTH_LONG);
					customtoast.show();
					return;
				}
				searchView.setVisibility(View.GONE);
				lt_accept.setVisibility(View.VISIBLE);
				lt_nprint.setVisibility(View.GONE);

				tvShowGrossTotal.setVisibility(View.VISIBLE);
				tvShowTotalKgs.setVisibility(View.VISIBLE);

				tvGrossAccepted.setVisibility(View.GONE);
				tvNetWeightAccepted.setVisibility(View.GONE);

				weighmentCounts = weighmentCounts+1;
				tvUnitsCount.setText(String.valueOf(weighmentCounts));

				tvsavedReading.setText("Reading");
				tvSavedNet.setText("Net Weight");
				tvSavedTare.setText("Tare Weight");
				tvSavedUnits.setText("Units Count");
				tvSavedTotal.setText("Total Kgs");

				tvsavedReading.setTextColor(Color.BLACK);
				tvSavedNet.setTextColor(Color.BLACK);
				tvSavedTare.setTextColor(Color.BLACK);
				tvSavedUnits.setTextColor(Color.BLACK);
				tvSavedTotal.setTextColor(Color.BLACK);





			}
		});

		btn_print.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSharedPrefs.getString("scaleVersion", "").toString().equals(TRANCELL_TI500)){
					//afterWeighigAlertDialog.dismiss();
					if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
						// go back to milkers activity
						Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();


						/*if(!mSharedPrefs.getBoolean("enableSMS", false)==true) {
							// go back to milkers activity
							Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();
							return;
						} else {
							SMS();
						}*/
						return;
					} else {
						//check scale version before printing


						/*if(!mSharedPrefs.getBoolean("enableSMS", false)==true) {
							// go back to milkers activity
							Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();
						} else {
							SMS();
						}*/

						if (mSharedPrefs.getString("Language", "Eng").toString().equals("Eng")) {


							if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
								PrintGenericReceipt();

							}
							else if (mSharedPrefs.getString("receiptTemplates", "Detailed").toString().equals("Detailed")) {
								PrintDetailedReceipt();

							}
							else if (mSharedPrefs.getString("receiptTemplates", "Simple").toString().equals("Simple")) {
								PrintSimpleReceipt();

							}
							else{

								PrintDetailedReceipt();
							}
						}
						//dialog.cancel();
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Reprint Receipt?</font>"))
								.setCancelable(false)
								.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {

										if (mSharedPrefs.getString("Language", "Eng").toString().equals("Eng")) {


											if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
												PrintGenericReceipt();

											} else if (mSharedPrefs.getString("receiptTemplates", "Detailed").toString().equals("Detailed")) {
												PrintDetailedReceipt();

											} else if (mSharedPrefs.getString("receiptTemplates", "Simple").toString().equals("Simple")) {
												PrintSimpleReceipt();

											} else {

												PrintDetailedReceipt();
											}
										}
									}
								})
								.setPositiveButton("No", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
										mBleBaseDeviceManager.disconnect();
										finish();
										weighmentCounts = 1;

									}
								});
						final AlertDialog alert2 = builder.create();
						alert2.show();
						alert2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
						alert2.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
						alert2.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
						alert2.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
						alert2.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (mSharedPrefs.getString("Language", "Eng").toString().equals("Eng")) {


									if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
										PrintGenericReceipt();

									}
									else if (mSharedPrefs.getString("receiptTemplates", "Detailed").toString().equals("Detailed")) {
										PrintDetailedReceipt();

									}
									else if (mSharedPrefs.getString("receiptTemplates", "Simple").toString().equals("Simple")) {
										PrintSimpleReceipt();

									}
									else{

										PrintDetailedReceipt();
									}
								}
								Boolean wantToCloseDialog = false;
								//Do stuff, possibly set wantToCloseDialog to true then...
								if (wantToCloseDialog)
									alert2.dismiss();
								//else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
							}
						});

					}



				}
			}
		});



	}
	private void FarmerList() {
		farmerdata.clear();

		SQLiteDatabase db= dbhelper.getReadableDatabase();
		Cursor c=db.rawQuery("select FFarmerNo,FFarmerName from farmers", null);
		if(c!=null)
		{
			if(c.moveToFirst())
			{
				do{
					farmers=c.getString(c.getColumnIndex("FFarmerName"));
					farmerdata.add(farmers);

				}while(c.moveToNext());
			}
		}
		c.close();
		db.close();
		dbhelper.close();

		farmeradapter=new ArrayAdapter<String>(AgentScaleSerialWeighActivity.this,R.layout.spinner_item,farmerdata);
		farmeradapter.setDropDownViewResource(R.layout.spinner_item);
		farmeradapter.notifyDataSetChanged();
		spFarmer.setAdapter(farmeradapter);
		spFarmer.setSelection(prefs.getInt("spinnerSelection", 0));
		spFarmer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String farmerName = parent.getItemAtPosition(position).toString();
				int selectedPosition =position;
				SQLiteDatabase db = dbhelper.getReadableDatabase();
				Cursor c = db.rawQuery("select FFarmerNo from farmers where FFarmerName= '" + farmerName + "'", null);
				if (c != null) {
					c.moveToFirst();
					farmerNo = c.getString(c.getColumnIndex("FFarmerNo"));


				}
				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("FFarmerNo", farmerNo);
				edit.commit();
				edit.putInt("spinnerSelection", selectedPosition);
				edit.apply();
				c.close();
				db.close();
				dbhelper.close();

				TextView tv = (TextView) view;
				if (position % 2 == 1) {
					// Set the item background color
					tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
				} else {
					// Set the alternate item background color
					tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	@Override
	public void onUiVspServiceFound(final boolean found)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (found) {
					mBtnSend.setEnabled(true);

					if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
						// go back to milkers activity
						//Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
					} else {
						mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
						if (mBluetoothAdapter == null) {
							showUnsupported();
						} else {
							mConnectingDlg = new ProgressDialog(_ctx.getApplicationContext());

							mConnectingDlg.setMessage("Printer Connecting...");
							mConnectingDlg.setCancelable(false);

							mConnector = new P25Connector(new P25Connector.P25ConnectionListener() {

								@Override
								public void onStartConnecting() {
									//mConnectingDlg.show();
									Context context = _ctx.getApplicationContext();
									LayoutInflater inflater = _activity.getLayoutInflater();
									View customToastroot = inflater.inflate(R.layout.blue_toast, null);
									TextView text = (TextView) customToastroot.findViewById(R.id.toast);
									text.setText("Connecting Printer ...");
									Toast customtoast = new Toast(context);
									customtoast.setView(customToastroot);
									customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
									customtoast.setDuration(Toast.LENGTH_LONG);
									customtoast.show();
									btnReconnect.setVisibility(View.GONE);
									// Toast.makeText(_ctx.getApplicationContext(), "Connecting Printer ...", Toast.LENGTH_LONG).show();
								}

								@Override
								public void onConnectionSuccess() {
									// mConnectingDlg.dismiss();

									// showConnected();
									txtPrinterConn.setVisibility(View.VISIBLE);
									btnReconnect.setVisibility(View.GONE);

								}


								@Override
								public void onConnectionFailed(String error) {
									// mConnectingDlg.dismiss();


									btnReconnect.setVisibility(View.VISIBLE);
									txtPrinterConn.setVisibility(View.GONE);

								}

								@Override
								public void onConnectionCancelled() {

									//  mConnectingDlg.dismiss();

									btnReconnect.setVisibility(View.VISIBLE);
									txtPrinterConn.setVisibility(View.GONE);

								}

								@Override
								public void onDisconnected() {

									// showDisonnected();

									Context context = _ctx.getApplicationContext();
									LayoutInflater inflater = _activity.getLayoutInflater();
									View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
									TextView text = (TextView) customToastroot.findViewById(R.id.toast);
									text.setText("Printer Disconnected");
									Toast customtoast = new Toast(context);
									customtoast.setView(customToastroot);
									customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
									customtoast.setDuration(Toast.LENGTH_LONG);
									customtoast.show();
									txtPrinterConn.setVisibility(View.GONE);

								}
							});
							connect();
							IntentFilter filter = new IntentFilter();

							filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
							filter.addAction(BluetoothDevice.ACTION_FOUND);
							filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
							filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
							filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

							_activity.registerReceiver(pReceiver, filter);
						}
					}
				} else {
					mBtnSend.setEnabled(false);
				}
			}
		});
	}

	@Override
	public void onUiSendDataSuccess(final String dataSend)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//mValueConsoleOutputTv.append(dataSend);
				//txtKgs.setText(dataSend);
//				txtKgs.setText(dataSend.replace("\n", "").replace("\r", ""));
				mValueTxCounterTv.setText("" + mSerialManager.getTxCounter());
				mScrollViewConsoleOutput.smoothScrollTo(0,
						mValueConsoleOutputTv.getBottom());
			}
		});
	}

	@Override
	public void onUiReceiveData(final String dataReceived,final String Reading)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String GrossReading = dataReceived.replaceAll(" ", "").replaceAll("kg", "").replaceAll("GR", "").replaceAll("\n", "").replaceAll("\r", "");
				String G1, G2, G3, s1, s2;
				Double s3;
				G1 = dataReceived.replaceAll(" ", "");
				G2 = G1.replaceAll("kg", "").replaceAll("GR", "");
				G3 = G2.replaceAll("\r\n", "");
				// s3=Double.parseDouble(G3);

				mValueConsoleOutputTv.append(GrossReading);
				mValueRxCounterTv.setText("" + mSerialManager.getRxCounter());

				//txtKgs.setText(dataReceived);
				double value;
				final DecimalFormat df = new DecimalFormat("#0.0#");
				try {


					value = new Double(Reading);
					SharedPreferences.Editor edit = prefs.edit();
					edit.putString("Reading", String.valueOf(value));
					edit.commit();
					//System.out.println(value);}
				} catch (NumberFormatException e) {
					//value = 0; // your default value
					value = Double.parseDouble(prefs.getString("Reading", "0"));
				}

				Double NetWeight = 0.0;
				NetWeight = value - setTareWeight;
				//mValueConsoleOutputTv.append(df.format(value));
				tvShowGrossTotal.setText(df.format(value));
				tvShowTotalKgs.setText(df.format(NetWeight));

				mScrollViewConsoleOutput.smoothScrollTo(0,
						mValueConsoleOutputTv.getBottom());
			}
		});
	}
	@Override
	public void onUiConnected()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mBtnScan.setText(R.string.btn_disconnect);


				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								String data = "P";
								if (data != null) {
									mBtnSend.setEnabled(false);
									if (mValueConsoleOutputTv.getText().length() <= 0) {
										//mValueConsoleOutputTv.append(">");
									} else {
										//mValueConsoleOutputTv.append("\n\n>");
									}

									if (isPrefSendCR == true) {
										try {
											mSerialManager.startDataTransfer(data);

											Thread.sleep(250);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}


									} else if (isPrefSendCR == false) {

										try {
											mSerialManager.startDataTransfer(data);

											Thread.sleep(250);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}

									/*InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

									inputManager.hideSoftInputFromWindow(getCurrentFocus()
													.getWindowToken(),
											InputMethodManager.HIDE_NOT_ALWAYS);*/

									if (isPrefClearTextAfterSending == true) {
										mInputBox.setText("");
									} else {
										// do not clear the text from the editText
									}
								}
							}
						});
					}
				}, 6000, 1000);

			}
		});
		invalidateUI();
	}
	@Override
	public void onUiUploaded()
	{
		mBtnSend.setEnabled(true);
	}

	@Override
	protected void loadPref()
	{
		super.loadPref();
		isPrefClearTextAfterSending = mSharedPreferences.getBoolean(
				"pref_clear_text_after_sending", false);
		isPrefSendCR = mSharedPreferences.getBoolean(
				"pref_append_/r_at_end_of_data", true);
	}
	@Override
	public void onBackPressed()
	{
		mBleBaseDeviceManager.disconnect();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
			// go back to milkers activity
			//Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
			//return;
		} else {

			if (mBluetoothAdapter == null) {
				// Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
			}
			else{
				if (mBluetoothAdapter.isDiscovering()) {
					mBluetoothAdapter.cancelDiscovery();
				}

			}

			if (mConnector == null) {
				//Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
			}
			else{
				try {
					mConnector.disconnect();
					_activity.unregisterReceiver(pReceiver);
				} catch (P25ConnectionException e) {
					e.printStackTrace();
				}

			}

	}
	}

	public void PrintGenericReceipt() {
		accountId = SessionID;
		dbhelper = new DBHelper(this);
		SQLiteDatabase db= dbhelper.getReadableDatabase();
		Cursor p=db.rawQuery("select MpCode,MpDescription from Produce", null);
		if(p!=null)
		{
			if(p.moveToFirst())
			{
				do{
					produce=p.getString(p.getColumnIndex("MpDescription"));

				}while(p.moveToNext());
			}
		}
		long milis		= System.currentTimeMillis();
		String date		= DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
		String time		= DateUtil.timeMilisToString(milis, "hh:mm a");
		String titleStr	= "\n"+produce+"RECEIPT" + "\n"
				+mSharedPrefs.getString("company_name", "").toString() + "\n"
				+"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
				mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
				Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

		StringBuilder contentSb	= new StringBuilder();


		Double grossWeight=0.0;
		final DecimalFormat df = new DecimalFormat("#0.0#");
		final DecimalFormat df1 = new DecimalFormat("#0.00#");



		Cursor session = db.query(Database.ASESSION_TABLE_NAME, null,
				" _id = ?", new String[]{accountId}, null, null, null);

		if (session.moveToFirst()) {
			SharedPreferences.Editor edit = prefs.edit();
			SessionNo=formatter.format(session.getInt(8));
			ReceiptNo=session.getString(session.getColumnIndex(Database.ASessionDevice))+SessionNo;
			ColDate=session.getString(session.getColumnIndex(Database.ASessionDate));
			Time=session.getString(session.getColumnIndex(Database.ASessionTime));
			DataDevice=mSharedPrefs.getString("terminalID", "");
			Agent=session.getString(session.getColumnIndex(Database.ASessionAgentNo));
			AgentName=prefs.getString("agentName", "");
			UnitCount=session.getString(session.getColumnIndex(Database.ASessionBags));
			grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionTare)))
					+Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionNet)));
			GrossTotal=df.format(grossWeight);

			TareWeight=session.getString(session.getColumnIndex(Database.ASessionTare));
			NetWeight=session.getString(session.getColumnIndex(Database.ASessionNet));

			contentSb.append("  \n-----------------------------\n");

			contentSb.append("  RECEIPT    : " + ReceiptNo + "\n");
			contentSb.append("  DATE       : " + ColDate + "\n");
			contentSb.append("  TIME       : " + Time + "\n");
			contentSb.append("  TERMINAL   : " + DataDevice + "\n");
			contentSb.append("  AGENT NO   : " +Agent + "\n");
			contentSb.append("   " + AgentName + "\n");
			contentSb.append("  WAREHOUSE  : " + prefs.getString("whName", "") + "\n");
			// contentSb.append("  -----------------------------\n");
			//contentSb.append("  REF.ID     : KGS  TIME\r\n");
			//contentSb.append("  -----------------------------\n");

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
			//startManagingCursor(accounts);
			Cursor account  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
					+ Database.AgentNo + " ='" + Agent + "'" +
					"and "+ Database.ACaptureTime +" <='" + Time + "' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ UnitCount +"'",null) ;
			if (account.getCount() > 0) {
				int count=0;
				while(account.moveToNext()) {
					// update view
					count=count+1;
					BagCount = String.valueOf(count);
					Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.AQuantity))));
					Tare = account.getString(account.getColumnIndex(Database.ATareweight));
					Time = account.getString(account.getColumnIndex(Database.ACaptureTime));
					edit.putString("ReceiptNo", BagCount);
					edit.commit();
					edit.putString("Kgs", Kgs);
					edit.commit();
					edit.putString("Tare", Tare);
					edit.commit();
					edit.putString("Time", Time);
					edit.commit();
					// contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
				}
			}
			account.close();




			contentSb.append("  -----------------------------\n");
			contentSb.append("  BAGS       : " + UnitCount+ "\n");
			contentSb.append("  GROSS WT   : " + df1.format(Double.parseDouble(GrossTotal)) + "\n");
			contentSb.append("  TARE WT    : " + df1.format(Double.parseDouble(TareWeight)) + "\n");
			contentSb.append("  NET WT     : " + df1.format(Double.parseDouble(NetWeight)) + "\n");
			//contentSb.append("  MTD KGS    : " + MTDKGS + "\n");

			contentSb.append("  -----------------------------\n");
			contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
			// contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
			contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
			contentSb.append("\n");
			contentSb.append("\n");


			byte[] titleByte	= Printer.printfont(titleStr, FontDefine.FONT_32PX, FontDefine.Align_CENTER,
					(byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

			byte[] content1Byte	= Printer.printfont(contentSb.toString(), FontDefine.FONT_32PX, FontDefine.Align_LEFT,
					(byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);




			byte[] totalByte	= new byte[titleByte.length+content1Byte.length];

			int offset = 0;
			System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
			offset += titleByte.length;

			System.arraycopy(content1Byte, 0, totalByte,offset , content1Byte.length);
			offset += content1Byte.length;

			byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

			sendData(senddata);

		}
		session.close();
		// db.close();
		//dbhelper.close();

	}
	public void PrintDetailedReceipt() {

		accountId = SessionID;
		dbhelper = new DBHelper(this);
		SQLiteDatabase db= dbhelper.getReadableDatabase();
		Cursor p=db.rawQuery("select MpCode,MpDescription from Produce", null);
		if(p!=null)
		{
			if(p.moveToFirst())
			{
				do{
					produce=p.getString(p.getColumnIndex("MpDescription"));

				}while(p.moveToNext());
			}
		}
		long milis		= System.currentTimeMillis();
		String date		= DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
		String time		= DateUtil.timeMilisToString(milis, "hh:mm a");
		String titleStr	= "\n"+produce+"RECEIPT" + "\n"
				+mSharedPrefs.getString("company_name", "").toString() + "\n"
				+"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
				mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
				Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

		StringBuilder contentSb	= new StringBuilder();


		Double grossWeight=0.0;
		final DecimalFormat df = new DecimalFormat("#0.0#");
		final DecimalFormat df1 = new DecimalFormat("#0.00#");


     	Cursor session = db.query(Database.ASESSION_TABLE_NAME, null,
				" _id = ?", new String[]{accountId}, null, null, null);

		if (session.moveToFirst()) {
			SharedPreferences.Editor edit = prefs.edit();
			SessionNo=formatter.format(session.getInt(8));
			ReceiptNo=session.getString(session.getColumnIndex(Database.ASessionDevice))+SessionNo;
			ColDate=session.getString(session.getColumnIndex(Database.ASessionDate));
			Time=session.getString(session.getColumnIndex(Database.ASessionTime));
			DataDevice=mSharedPrefs.getString("terminalID", "");
			Agent=session.getString(session.getColumnIndex(Database.ASessionAgentNo));
			AgentName=prefs.getString("agentName", "");
			UnitCount=session.getString(session.getColumnIndex(Database.ASessionBags));

			grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionTare)))
					+Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionNet)));
			GrossTotal=df.format(grossWeight);

			TareWeight=session.getString(session.getColumnIndex(Database.ASessionTare));
			NetWeight=session.getString(session.getColumnIndex(Database.ASessionNet));

			contentSb.append("  \n-----------------------------\n");

			contentSb.append("  RECEIPT    : " + ReceiptNo + "\n");
			contentSb.append("  DATE       : " + ColDate + "\n");
			contentSb.append("  TIME       : " + Time + "\n");
			contentSb.append("  TERMINAL   : " + DataDevice + "\n");
			contentSb.append("  AGENT NO   : " + Agent + "\n");
			contentSb.append("   " + AgentName + "\n");
			contentSb.append("  WAREHOUSE  : " + prefs.getString("whName", "") + "\n");
			contentSb.append("  -----------------------------\n");
			contentSb.append("  REF.ID     : KGS  TIME\r\n");
			contentSb.append("  -----------------------------\n");

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
			//startManagingCursor(accounts);
			Cursor account  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
					+ Database.AgentNo + " ='" + Agent + "'" +
					"and "+ Database.ACaptureTime +" <='" + Time + "' and "+ Database.AReceiptNo +"='"+ SessionNo +"' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ UnitCount +"'",null) ;
			if (account.getCount() > 0) {
				int count=0;
				while(account.moveToNext()) {

					count=count+1;
					BagCount = String.valueOf(count);

					Kgs = df1.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.AQuantity))));
					Tare = account.getString(account.getColumnIndex(Database.ATareweight));
					Time = account.getString(account.getColumnIndex(Database.ACaptureTime));

					edit.putString("Kgs", Kgs);
					edit.commit();
					edit.putString("Tare", Tare);
					edit.commit();
					edit.putString("Time", Time);
					edit.commit();

					contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
				}
			}
			account.close();



			contentSb.append("  -----------------------------\n");
			contentSb.append("  BAGS       : " + UnitCount+ "\n");
			contentSb.append("  GROSS WT   : " + df1.format(Double.parseDouble(GrossTotal)) + "\n");
			contentSb.append("  TARE WT    : " + df1.format(Double.parseDouble(TareWeight)) + "\n");
			contentSb.append("  NET WT     : " + df1.format(Double.parseDouble(NetWeight)) + "\n");
			//contentSb.append("  MTD KGS    : " + MTDKGS + "\n");

			contentSb.append("  -----------------------------\n");
			contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
			// contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
			contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
			contentSb.append("\n");
			contentSb.append("\n");


			byte[] titleByte	= Printer.printfont(titleStr, FontDefine.FONT_32PX, FontDefine.Align_CENTER,
					(byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

			byte[] content1Byte	= Printer.printfont(contentSb.toString(), FontDefine.FONT_32PX, FontDefine.Align_LEFT,
					(byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);




			byte[] totalByte	= new byte[titleByte.length+content1Byte.length];

			int offset = 0;
			System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
			offset += titleByte.length;

			System.arraycopy(content1Byte, 0, totalByte,offset , content1Byte.length);
			offset += content1Byte.length;

			byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

			sendData(senddata);

		}
		session.close();
		// db.close();
		//dbhelper.close();

	}
	public void PrintSimpleReceipt() {
		accountId = SessionID;
		dbhelper = new DBHelper(this);
		SQLiteDatabase db= dbhelper.getReadableDatabase();
		Cursor p=db.rawQuery("select MpCode,MpDescription from Produce", null);
		if(p!=null)
		{
			if(p.moveToFirst())
			{
				do{
					produce=p.getString(p.getColumnIndex("MpDescription"));

				}while(p.moveToNext());
			}
		}
		long milis		= System.currentTimeMillis();
		String date		= DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
		String time		= DateUtil.timeMilisToString(milis, "hh:mm a");
		String titleStr	= "\n"+produce+"RECEIPT" + "\n"
				+mSharedPrefs.getString("company_name", "").toString() + "\n"
				+"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
				mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
				Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

		StringBuilder contentSb	= new StringBuilder();


		Double grossWeight=0.0;
		final DecimalFormat df = new DecimalFormat("#0.0#");
		final DecimalFormat df1 = new DecimalFormat("#0.00#");



		Cursor session = db.query(Database.ASESSION_TABLE_NAME, null,
				" _id = ?", new String[]{accountId}, null, null, null);

		if (session.moveToFirst()) {
			SharedPreferences.Editor edit = prefs.edit();
			SessionNo=formatter.format(session.getInt(8));
			ReceiptNo=session.getString(session.getColumnIndex(Database.ASessionDevice))+SessionNo;
			ColDate=session.getString(session.getColumnIndex(Database.ASessionDate));
			Time=session.getString(session.getColumnIndex(Database.ASessionTime));
			DataDevice=mSharedPrefs.getString("terminalID", "");
			Agent=session.getString(session.getColumnIndex(Database.ASessionAgentNo));
			AgentName=prefs.getString("agentName", "");
			UnitCount=session.getString(session.getColumnIndex(Database.ASessionBags));
			grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionTare)))
					+Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionNet)));
			GrossTotal=df.format(grossWeight);

			TareWeight=session.getString(session.getColumnIndex(Database.ASessionTare));
			NetWeight=session.getString(session
					.getColumnIndex(Database.ASessionNet));
			contentSb.append("  \n-----------------------------\n");

			contentSb.append("  RECEIPT    : " + ReceiptNo + "\n");
			contentSb.append("  DATE       : " + ColDate + "\n");
			contentSb.append("  TIME       : " + Time + "\n");
			contentSb.append("  TERMINAL   : " + DataDevice + "\n");
			contentSb.append("  AGENT NO   : " +Agent + "\n");
			contentSb.append("   " + AgentName + "\n");
			contentSb.append("  WAREHOUSE  : " + prefs.getString("whName", "") + "\n");
			// contentSb.append("  -----------------------------\n");
			//contentSb.append("  REF.ID     : KGS  TIME\r\n");
			//contentSb.append("  -----------------------------\n");

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
			//startManagingCursor(accounts);
			Cursor account  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
					+ Database.AgentNo + " ='" + Agent + "'" +
					"and "+ Database.ACaptureTime +" <='" + Time + "' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ UnitCount +"'",null) ;
			if (account.getCount() > 0) {
				int count=0;
				while(account.moveToNext()) {
					// update view
					count=count+1;
                    /*BagCount = account.getString(account.getColumnIndex(Database.DataCaptureDevice))+ account.getString(account
                            .getColumnIndex(Database.ReceiptNo));*/

					BagCount = String.valueOf(count);

					Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.AQuantity))));
					Tare = account.getString(account.getColumnIndex(Database.ATareweight));
					Time = account.getString(account.getColumnIndex(Database.ACaptureTime));
					edit.putString("ReceiptNo", BagCount);
					edit.commit();
					edit.putString("Kgs", Kgs);
					edit.commit();
					edit.putString("Tare", Tare);
					edit.commit();
					edit.putString("Time", Time);
					edit.commit();
					// contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
				}
			}
			account.close();



			contentSb.append("  -----------------------------\n");
			contentSb.append("  BAGS       : " + UnitCount+ "\n");
			//contentSb.append("  GROSS WT   : " + df1.format(Double.parseDouble(GrossTotal)) + "\n");
			//contentSb.append("  TARE WT    : " + df1.format(Double.parseDouble(TareWeight)) + "\n");
			contentSb.append("  NET WT     : " + df1.format(Double.parseDouble(NetWeight)) + "\n");
			//contentSb.append("  MTD KGS    : " + MTDKGS + "\n");
			contentSb.append("  -----------------------------\n");
			contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
			// contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
			contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
			contentSb.append("\n");
			contentSb.append("\n");


			byte[] titleByte	= Printer.printfont(titleStr, FontDefine.FONT_32PX, FontDefine.Align_CENTER,
					(byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

			byte[] content1Byte	= Printer.printfont(contentSb.toString(), FontDefine.FONT_32PX, FontDefine.Align_LEFT,
					(byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);




			byte[] totalByte	= new byte[titleByte.length+content1Byte.length];

			int offset = 0;
			System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
			offset += titleByte.length;

			System.arraycopy(content1Byte, 0, totalByte,offset , content1Byte.length);
			offset += content1Byte.length;

			byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

			sendData(senddata);

		}
		session.close();
		// db.close();
		//dbhelper.close();

	}
	/*public void SMS() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Send Sms?");
		dialogBuilder.setMessage("Do you want to send an SMS receipt?");
		dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				RecieptNo=prefs.getString("textReciept", "");
				MobileNo=prefs.getString("rFarmerPhone", "");
				FarmerNo=prefs.getString("textFarmerNo", "");
				NetWeight=prefs.getString("textNetWt", "");
				TotalKg=prefs.getString("textTotalKgs", "");
				Date=prefs.getString("textTransDate", "");

				if (MobileNo.equals("")) {
					Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
					return;
				}
				if (FarmerNo.equals("")) {
					Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
					return;
				}
				Sendsms(MobileNo,
						"ReceiptNo:" + RecieptNo +
								"\n" + "FarmerNo:" + FarmerNo +
								"\n" + "Collected: " + NetWeight + " Kgs." +
								"\n" + "Total Kgs: " + TotalKg + " Kgs." +
								"\n" + "On Date " + Date +
								"\n" + "Thank you!!");
				Toast.makeText(getBaseContext(), "Message Sent Successfully!!", Toast.LENGTH_LONG).show();

			}
		});
		dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();

			}
		});
		AlertDialog b = dialogBuilder.create();
		b.show();

	}

	private void Sendsms(String address,String message) {
		// TODO Auto-generated method stub
		SmsManager smsMgr = SmsManager.getDefault();
		smsMgr.sendTextMessage(address, null, message, null, null);

	}*/
	private String getDate(){

		//A string to hold the current date
		String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

		//Return the current date
		return currentDateTimeString;
	}
}