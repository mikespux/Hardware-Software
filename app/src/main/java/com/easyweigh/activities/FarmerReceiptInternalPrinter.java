package com.easyweigh.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.aidlprinter.service.utils.BitmapUtils;
import com.easyweigh.aidlprinter.service.utils.BytesUtil;
import com.easyweigh.aidlprinter.service.utils.MemInfo;
import com.easyweigh.aidlprinter.service.utils.ThreadPoolManager;
import com.easyweigh.connector.P25Connector;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.printerdata.PocketPos;
import com.easyweigh.printerutils.DataConstants;
import com.easyweigh.printerutils.DateUtil;
import com.easyweigh.printerutils.FontDefine;
import com.easyweigh.printerutils.Printer;
import com.easyweigh.printerutils.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

public class FarmerReceiptInternalPrinter extends AppCompatActivity {
	private static final String TAG = "PrinterTestDemo";
	public Toolbar toolbar;


	DBHelper dbhelper;
	ListView listReciepts;
	String accountId;
	TextView textAccountId;
	Boolean success = true;
	TextView textCompanyName,textPoBox,textReciept,textTransDate,textTransTime,textTerminal,textFarmerNo,textName,
			textRoute,textShed,textTrip,textBags,textGrossWt,textTareWt,textNetWt,textTotalKgs,textClerk;
	SearchView searchView;
	public SimpleCursorAdapter ca;
	Intent mIntent;
	static SharedPreferences mSharedPrefs;
	static SharedPreferences prefs;
	EditText etFrom,etTo,etFarmerNo;
	private ProgressDialog mProgressDlg;
	private ProgressDialog mConnectingDlg;
	private BluetoothAdapter mBluetoothAdapter;
	private P25Connector mConnector;
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	private Button mConnectBtn;
	private Button mPrintBtn;
	private Button btnSearchReceipt,btnFilter;
	private Spinner mDeviceSp;
	private Button pickFrom,pickTo;
	String strCompanyName,strPoBox,strReciept,strTransDate,strTransTime,strTerminal,strFarmerNo,strName,
			strRoute,strShed,strTrip,strBags,strGrossWt,strTareWt,strNetWt,strTotalKgs,strClerk;
	String  fromDate,toDate,farmerNo;
	String  condition = " _id > 0 ";
	String  cond = " _id > 0 ";
	AlertDialog b;
	AlertDialog alert;
	DecimalFormat formatter;
	String SessionNo,DataDevice;
	String ReceiptNo,BagCount,produce;
	String Kgs,Tare,Time;
	private  TextView info;
	
	private IWoyouService woyouService;
	private byte[] inputCommand ;
    
	private final int RUNNABLE_LENGHT = 11;
	
	private Random random = new Random();
	
	private ICallback callback = null;
	
	private ServiceConnection connService = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

			woyouService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			woyouService = IWoyouService.Stub.asInterface(service);
		//	setButtonEnable(true);
		}
	};

	private final int MSG_TEST = 1;
	private long printCount = 0;
	
	@SuppressLint("HandlerLeak")
	Handler handler=new Handler(){
	    @Override
	    public void handleMessage(Message msg){
	    	if(msg.what == MSG_TEST){
	    		//testAll();
	    		long mm = MemInfo.getmem_UNUSED(FarmerReceiptInternalPrinter.this);
	    		if( mm < 100){
	    			handler.sendEmptyMessageDelayed(MSG_TEST, 20000);
	    		}else{
	    			handler.sendEmptyMessageDelayed(MSG_TEST, 800);
	    		}
	    		Log.i(TAG,"testAll: " + printCount + " Memory: " + mm);
	    	}
	    }
	};
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listreciepts);
		innitView();
		setupToolbar();
	}
	public void setupToolbar() {
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.nav_item_farmers);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
				try{
					unbindService(connService);
					handler.removeMessages(MSG_TEST);
				}catch(Exception e){}
				finally{
					finish();
				}
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
	}

	
	private void innitView() {

		mDeviceSp 			= (Spinner) findViewById(R.id.sp_device);
		mConnectBtn			= (Button) findViewById(R.id.btnConnect);
		mConnectBtn.setVisibility(View.GONE);
		btnFilter			= (Button) findViewById(R.id.btnFilter);
		mPrintBtn			= (Button) findViewById(R.id.btnPrint);
		formatter = new DecimalFormat("0000");
		dbhelper = new DBHelper(getApplicationContext());
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(FarmerReceiptInternalPrinter.this);
		String mDevice = prefs.getString("mDevice", "");
		// showToast(mDevice);
		btnFilter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Restart().execute();
			}
		});
		listReciepts = (ListView) this.findViewById(R.id.lvReciepts);
		listReciepts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
				textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
				Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
				if (mSharedPrefs.getString("Language", "Eng").toString().equals("Eng")) {


					if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
						//PrintGenericReceipt();
						PrintDetailedReceipt();
					}
					else if (mSharedPrefs.getString("receiptTemplates", "Detailed").toString().equals("Detailed")) {
						//PrintDetailedReceipt();

					}
					else if (mSharedPrefs.getString("receiptTemplates", "Simple").toString().equals("Simple")) {
						//PrintSimpleReceipt();

					}
					else{

						//PrintDetailedReceipt();
						PrintDetailedReceipt();
					}
				}
			}
		});

		searchView=(SearchView) findViewById(R.id.searchView);
		searchView.setVisibility(View.GONE);
		searchView.setQueryHint("Search Receipt No ...");

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				ca.getFilter().filter(query.toString());
				ca.setFilterQueryProvider(new FilterQueryProvider() {

					@Override
					public Cursor runQuery(CharSequence constraint) {
						String recieptNo = constraint.toString();
						return dbhelper.SearchSpecificReciept(recieptNo);

					}
				});

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				ca.getFilter().filter(newText.toString());
				ca.setFilterQueryProvider(new FilterQueryProvider() {

					@Override
					public Cursor runQuery(CharSequence constraint) {
						String recieptNo = constraint.toString();
						return dbhelper.SearchReciept(recieptNo);

					}
				});

				return false;
			}
		});
		searchView.requestFocus();

		new BitmapUtils(this);

		
		info = (TextView) findViewById(R.id.info);
		
		callback = new ICallback.Stub() {
			
			@Override
			public void onRunResult(final boolean success) throws RemoteException {
			}
			
			@Override
			public void onReturnString(final String value) throws RemoteException {
				Log.i(TAG,"printlength:" + value + "\n");						
			}
			
			@Override
			public void onRaiseException(int code, final String msg) throws RemoteException {
				Log.i(TAG,"onRaiseException: " + msg);
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						info.append("onRaiseException = " + msg + "\n");
					}});
				
			}
		};		
		
		//setButtonEnable(false);
		
		Intent intent=new Intent();
		intent.setPackage("woyou.aidlservice.jiuiv5");
		intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
		startService(intent);
		bindService(intent, connService, Context.BIND_AUTO_CREATE);
	}
	

	
	Bitmap mBitmap;


	



	
	//print character 
	public void printData(final byte[] bill){
		ThreadPoolManager.getInstance().executeTask(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					woyouService.sendRAWData(bill, callback);
					woyouService.lineWrap(3, callback);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}});		
	}

	public void PrintDetailedReceipt() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_transaction_details, null);
		dialogBuilder.setView(dialogView);
		dialogBuilder.setTitle("Receipt");
		accountId = textAccountId.getText().toString();
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

		final StringBuilder contentSb	= new StringBuilder();

		textCompanyName = (TextView) dialogView.findViewById(R.id.textCompanyName);
		textCompanyName.setText(mSharedPrefs.getString("company_name", "").toString());
		textPoBox = (TextView) dialogView.findViewById(R.id.textPoBox);
		textPoBox.setText(mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
				mSharedPrefs.getString("company_postalname", "").toString());
		textReciept = (TextView) dialogView.findViewById(R.id.textReciept);
		textTransDate = (TextView) dialogView.findViewById(R.id.textTransDate);
		textTransTime = (TextView) dialogView.findViewById(R.id.textTransTime);
		textTerminal = (TextView) dialogView.findViewById(R.id.textTerminal);
		textFarmerNo = (TextView) dialogView.findViewById(R.id.textFarmerNo);
		textName = (TextView) dialogView.findViewById(R.id.textName);

		textRoute = (TextView) dialogView.findViewById(R.id.textRoute);
		textShed = (TextView) dialogView.findViewById(R.id.textShed);
		textTrip = (TextView) dialogView.findViewById(R.id.textTrip);
		textBags = (TextView) dialogView.findViewById(R.id.textBags);
		textGrossWt = (TextView) dialogView.findViewById(R.id.textGrossWt);
		textTareWt = (TextView) dialogView.findViewById(R.id.textTareWt);
		textNetWt = (TextView) dialogView.findViewById(R.id.textNetWt);
		textTotalKgs = (TextView) dialogView.findViewById(R.id.textTotalKgs);

		textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
		textClerk.setText(prefs.getString("user", ""));


		Double grossWeight=0.0;
		final DecimalFormat df = new DecimalFormat("#0.0#");


		// SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor session = db.query(Database.SESSION_TABLE_NAME, null,
				" _id = ?", new String[]{accountId}, null, null, null);
		//startManagingCursor(accounts);
		if (session.moveToFirst()) {
			final SharedPreferences.Editor edit = prefs.edit();
			SessionNo=session.getString(session.getColumnIndex(Database.SessionCounter));
			String[] allColumns = new String[] {Database.F_FARMERNAME,Database.F_PRODUCE_KG_TODATE};
			Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns,Database.F_FARMERNO + "='" + session.getString(session
							.getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
					null);
			if (c != null) {
				c.moveToFirst();

				textName.setText(c.getString(c.getColumnIndex(Database.F_FARMERNAME)));
				textTotalKgs.setText(c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));
			}

			textRoute.setText(session.getString(session.getColumnIndex(Database.SessionRoute)));
			textShed.setText(session.getString(session.getColumnIndex(Database.SessionCounter)));

			textReciept.setText(session.getString(session
					.getColumnIndex(Database.SessionDevice))+SessionNo);
			DataDevice=(session.getString(session
					.getColumnIndex(Database.SessionDevice)));
			textTransDate.setText(session.getString(session
					.getColumnIndex(Database.SessionDate)));
			textTransTime.setText(session.getString(session
					.getColumnIndex(Database.SessionTime)));
			textTerminal.setText(prefs.getString("terminalID", ""));
			textFarmerNo.setText(session.getString(session
					.getColumnIndex(Database.SessionFarmerNo)));
			textBags.setText(session.getString(session
					.getColumnIndex(Database.SessionBags)));
			Cursor s  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
					+ Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
					"and "+ Database.CaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.DataCaptureDevice +"='" + DataDevice +"' and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
			if(s!=null)
			{
				s.moveToFirst();

				textShed.setText(s.getString(s.getColumnIndex(Database.BuyingCenter)));


			}

			grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare)))
					+Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
			textGrossWt.setText(df.format(grossWeight));

			textTareWt.setText(session.getString(session
					.getColumnIndex(Database.SessionTare)));
			textNetWt.setText(session.getString(session
					.getColumnIndex(Database.SessionNet)));
			ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;
			ThreadPoolManager.getInstance().executeTask(new Runnable() {

				@Override
				public void run() {

					try {
						woyouService.printTextWithFont("\n"+produce+"RECEIPT" + "\n"
								+mSharedPrefs.getString("company_name", "").toString() + "\n"
								+"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
								mSharedPrefs.getString("company_postalname", "").toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("  \n-----------------------------\n", "", 24, callback);

						woyouService.printTextWithFont("  RECEIPT    : " + ReceiptNo + "\n", "", 24, callback);
						woyouService.printTextWithFont("  DATE       : " + textTransDate.getText().toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("  TIME       : " + textTransTime.getText().toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("  TERMINAL   : " + textTerminal.getText().toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("  FARMER NO  : " + textFarmerNo.getText().toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("   " + textName.getText().toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("  ROUTE      : " + textRoute.getText().toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("  SHED       : " + textShed.getText().toString() + "\n", "", 24, callback);
						woyouService.printTextWithFont("  -----------------------------\n", "", 24, callback);
						woyouService.printTextWithFont("  REF.ID     : KGS  TIME\r\n", "", 24, callback);
						woyouService.printTextWithFont("  -----------------------------\n", "", 24, callback);

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
			//startManagingCursor(accounts);
						SQLiteDatabase db= dbhelper.getReadableDatabase();
			Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
					+ Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
					"and "+ Database.CaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.DataCaptureDevice +"='" + DataDevice +"' and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
			if (account.getCount() > 0) {
				int count=0;
				while(account.moveToNext()) {
					// update view
					count=count+1;
					BagCount = String.valueOf(count);
                /*ReceiptNo = account.getString(account.getColumnIndex(Database.DataCaptureDevice))+ account.getString(account
                        .getColumnIndex(Database.ReceiptNo));*/


					Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity))));
					Tare = account.getString(account.getColumnIndex(Database.Tareweight));
					Time = account.getString(account.getColumnIndex(Database.CaptureTime));

					woyouService.printTextWithFont("   " + BagCount + "           " + Kgs + "  " + Time + "\n", "", 24, callback);
				}
			}
			account.close();





						woyouService.printTextWithFont("  -----------------------------\n", "", 24, callback);

						woyouService.printTextWithFont("  UNITS      : " + textBags.getText().toString()+ "\n", "", 24, callback);

						woyouService.printTextWithFont("  GROSS WT   : " + textGrossWt.getText().toString() + "\n", "", 24, callback);

						woyouService.printTextWithFont("  TARE WT    : " + textTareWt.getText().toString() + "\n", "", 24, callback);

						woyouService.printTextWithFont("  NET WT     : " + textNetWt.getText().toString() + "\n", "", 24, callback);

						woyouService.printTextWithFont("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n", "", 24, callback);


						woyouService.printTextWithFont("  -----------------------------\n","", 24, callback);
						woyouService.printTextWithFont("  You were served by,\n  " + prefs.getString("fullname", "") + "\n","", 24, callback);
						woyouService.printTextWithFont("Enquiry Call:"+mSharedPrefs.getString("company_posttel", "") + "\n","", 24, callback);
						woyouService.printTextWithFont("\n","", 24, callback);
						woyouService.printTextWithFont("\n","", 24, callback);




					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});


		}
		session.close();
		db.close();
		dbhelper.close();






		dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//do something with edt.getText().toString();
				//  printReceipt();

			}
		});
		dialogBuilder.setNegativeButton("Print", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//pass
				//  updateFarmer();
				// getdata();
				if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
					// go back to milkers activity
					Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
				} else {
					//check scale version before printing

					PrintDetailedReceipt();
				}


			}
		});
		AlertDialog b = dialogBuilder.create();
		//b.show();
	}

	
	//print word
	public void printText(){
		ThreadPoolManager.getInstance().executeTask(new Runnable() {

			@Override
			public void run() {
				if (mBitmap == null) {
					mBitmap = BitmapFactory.decodeResource(getResources(), R.raw.test);
				}
				try {
					woyouService.printText("There were  ", callback);
					woyouService.printTextWithFont("28", "", 36, callback);
					woyouService.printText("in front of you\n", callback);
					for (int i = 24; i <= 48; i += 6) {
						woyouService.printTextWithFont("Sunmi", "", i, callback);
					}
					for (int i = 48; i >= 12; i -= 2) {
						woyouService.printTextWithFont("Sunmi", "", i, callback);
					}
					woyouService.lineWrap(1, callback);
					woyouService.printTextWithFont("ABCDEFGHIJKLMNOPQRSTUVWXYZ01234\n", "", 30, callback);
					woyouService.printTextWithFont("abcdefghijklmnopqrstuvwxyz56789\n", "", 30, callback);
					woyouService.printText("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\n", callback);
					woyouService.printText("abcdefghijklmnopqrstuvwxyz0123456789\n", callback);

					woyouService.lineWrap(2, callback);
					woyouService.setAlignment(1, callback);
					woyouService.printBitmap(mBitmap, callback);
					woyouService.setFontSize(24, callback);
					woyouService.printTextWithFont("Welcome start printer test\n", "", 35, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printTextWithFont("*****************************\n", "", 24, callback);
					woyouService.printTextWithFont("this is a normal line\n", "", 24, callback);
					woyouService.printTextWithFont("this line's font size is 30\n", "", 30, callback);
					woyouService.printTextWithFont("this line's font size is 36\n", "", 36, callback);
					woyouService.printTextWithFont("this line's font size is 42\n", "", 42, callback);
					//woyouService.printTextWithFont("*****************************\n", "", 24, callback);
					woyouService.sendRAWData(BytesUtil.initLine2(384), callback);
					woyouService.setAlignment(1, callback);
					woyouService.printBarCode("2015112910", 8, 100, 2, 2, callback);
					woyouService.printTextWithFont("\n\n", "", 24, callback);
					woyouService.setAlignment(1, callback);
					woyouService.printTextWithFont("***Completed***\n", "", 35, callback);
					for (int i = 0; i < 12; i++) {
						woyouService.sendRAWData(BytesUtil.initLine1(384, i), callback);
					}
					String[] s = woyouService.getServiceVersion().split("\\.");
					int ver = 10000 * Integer.parseInt(s[0])
							+ 100 * Integer.parseInt(s[1])
							+ Integer.parseInt(s[2]);

					//printOriginalText方法是1.7.6版本新加的，用于字符原宽度输出，即不作等宽处理
					//printOriginalText was added after 1.7.6
					if (ver >= 10706) {
						woyouService.setFontSize(36, callback);
						woyouService.printOriginalText("κρχκμνκλρκνκνμρτυφ\n", callback);
						woyouService.printOriginalText("http://www.sunmi.com\n", callback);
						woyouService.printOriginalText("this line's font size is 36\n這是一行36號字體\n", callback);
						woyouService.setFontSize(24, callback);
						woyouService.printOriginalText("κρχκμνκλρκνκνμρτυφ\n", callback);
						woyouService.printOriginalText("http://www.sunmi.com\n", callback);
						woyouService.printOriginalText("this is a normal line\n這是一行正常字體\n", callback);
					}

					Log.i("PrinterTestDemo", "version = " + woyouService.getServiceVersion() + "\nlength=" + s.length);

					woyouService.lineWrap(4, callback);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}
	
	//print table
	public void printTable(){
		ThreadPoolManager.getInstance().executeTask(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					woyouService.setAlignment(0, callback);
					woyouService.sendRAWData(new byte[]{0x1B, 0x21, 0x08}, callback);
					
					woyouService.setFontSize(24, callback);						
					String[] text = new String[4];
					int[] width = new int[] { 10, 6, 6, 8 };
					int[] align = new int[] { 0, 2, 2, 2 }; 
					text[0] = "Name";
					text[1] = "Quantity";
					text[2] = "Price";
					text[3] = "Total price";
					woyouService.printColumnsText(text, width, new int[] { 1, 2, 2, 2 }, callback);
					
					text[0] = "Strawberry yoghourt";
					text[1] = "4";
					text[2] = "12.00";
					text[3] = "48.00";
					woyouService.printColumnsText(text, width, align, callback);
					
					text[0] = "Yoghourt B";
					text[1] = "10";
					text[2] = "4.00";
					text[3] = "40.00";
					woyouService.printColumnsText(text, width, align, callback);
					
					text[0] = "Yogurt orange fruit cake"; // Text long, wrap
					text[1] = "100";
					text[2] = "16.00";
					text[3] = "1600.00";
					woyouService.printColumnsText(text, width, align, callback);
	
					text[0] = "Yogurt fruit sandwich";
					text[1] = "10";
					text[2] = "4.00";
					text[3] = "40.00";
					woyouService.printColumnsText(text, width, align, callback);
					
					woyouService.setFontSize(30, callback);	
					woyouService.setAlignment(0, callback);
					
					text = new String[3];
					width = new int[] { 10, 6, 8 };
					align = new int[] { 0, 2, 2 }; //
					
					text[0] = "Dish name";
					text[1] = "quantity";
					text[2] = "Total price";
					woyouService.printColumnsText(text, width, new int[] {1,2,2}, callback);
					
					text[0] = "Strawberry yoghurt pudding";
					text[1] = "4";
					text[2] = "48.00";
					woyouService.printColumnsText(text, width, align, callback);
					
					text[0] = "Yogurt fruit sandwich";
					text[1] = "10";
					text[2] = "40.00";
					woyouService.printColumnsText(text, width, align, callback);
					
					text[0] = "Yogurt orange fruit cake"; // 文字超长,换行
					text[1] = "100";
					text[2] = "1600.00";
					woyouService.printColumnsText(text, width, align, callback);
	
					text[0] = "Milk";
					text[1] = "10";
					text[2] = "40.00";
					woyouService.printColumnsText(text, width, align, callback);							
					woyouService.lineWrap(3, null);
					
					woyouService.sendRAWData(new byte[]{0x1B, 0x21, 0x00}, callback);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}						
			}});
	}
	

	

	//manual input print
	public void inputBytes(){
	      final EditText inputServer = new EditText(this);
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Server").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
	                .setNegativeButton("Cancel", null);
	        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface dialog, int which) {
	            	inputCommand = BytesUtil.getBytesFromHexString(inputServer.getText().toString());
	            	try{
	            		System.out.println(BytesUtil.getHexStringFromBytes(inputCommand));
	            		woyouService.sendRAWData(inputCommand, callback);
	            	} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            }
	        });
	        builder.show();						
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onStart() {
		super.onStart();
		getdata();
	}

	public void printReceipt() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to print this receipt?")
				.setCancelable(false)
				.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//printGenReceipt();;

					}
				})
				.setPositiveButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		alert = builder.create();
		alert.show();
	}





	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void getdata(){

		try {

			SQLiteDatabase db= dbhelper.getReadableDatabase();
			Cursor accounts = db.query(true, Database.SESSION_TABLE_NAME, null, null, null, null, null, null, null, null);
			if (accounts.getCount() > 0) {
				String from [] = {  Database.ROW_ID,Database.SessionFarmerNo, Database.SessionDevice , Database.SessionBags, Database.SessionDate};
				int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date};


				ca  = new SimpleCursorAdapter(this,R.layout.receipt_list, accounts,from,to);

				ListView listfarmers= (ListView) this.findViewById( R.id.lvReciepts);
				//ca.notifyDataSetChanged();
				listfarmers.setAdapter(ca);
				listfarmers.setTextFilterEnabled(true);
				db.close();
				dbhelper.close();
			}
			else{

				new NoReceipt().execute();
			}
		} catch (Exception ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private class Restart extends AsyncTask<Void, Void, String>
	{
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute()
		{
            /*dialog = ProgressDialog.show( FarmerRecieptsActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);*/
		}

		@Override
		protected String doInBackground(Void... params)
		{
			try{
				unbindService(connService);
				handler.removeMessages(MSG_TEST);
			}catch(Exception e){}
			finally{
				finish();
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result)
		{

			//dialog.dismiss();

			mIntent = new Intent(getApplicationContext(),FarmerRecieptsActivity.class);
			startActivity(mIntent);
		}
	}
	private class NoReceipt extends AsyncTask<Void, Void, String>
	{
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute()
		{
            /*dialog = ProgressDialog.show( FarmerRecieptsActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);*/
		}

		@Override
		protected String doInBackground(Void... params)
		{


			try{
				unbindService(connService);
				handler.removeMessages(MSG_TEST);
			}catch(Exception e){}
			finally {
				finish();
			}


			return "";
		}

		@Override
		protected void onPostExecute(String result)
		{

			//dialog.dismiss();
			finish();
			Context context=getApplicationContext();
			LayoutInflater inflater=getLayoutInflater();
			View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
			TextView text = (TextView) customToastroot.findViewById(R.id.toast);
			text.setText("No Receipts To Print");
			Toast customtoast=new Toast(context);
			customtoast.setView(customToastroot);
			customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
			customtoast.setDuration(Toast.LENGTH_LONG);
			customtoast.show();
		}
	}


}
