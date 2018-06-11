package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.connector.P25ConnectionException;
import com.easyweigh.connector.P25Connector;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.helpers.InputFilterMinMax;
import com.easyweigh.printerdata.PocketPos;
import com.easyweigh.printerutils.DataConstants;
import com.easyweigh.printerutils.DateUtil;
import com.easyweigh.printerutils.FontDefine;
import com.easyweigh.printerutils.Printer;
import com.easyweigh.printerutils.Util;
import com.easyweigh.services.WeighingService;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Michael on 30/06/2016.
 */
public class FarmerScaleWeighActivity extends AppCompatActivity {
    public Toolbar toolbar;
    Intent mIntent;
    static DBHelper dbhelper;
    ListView listFarmers;

    String accountId;
    TextView textAccountId;
    Boolean success = true;
    static TextView tvStability,tvError,tvMemberName,tvShowMemberNo,tvShowGrossTotal,tvWeighingAccumWeigh,tvWeighingTareWeigh,
             tvUnitsCount,tvShowTotalKgs,tvGross,tvNetWeightAccepted,tvGrossAccepted,tvTareAccepted,txtKgs,txtScaleConn,txtPrinterConn;
    EditText etShowGrossTotal,etQuality;
    static TextView tvsavedReading,tvSavedNet,tvSavedTare,tvSavedUnits,tvSavedTotal;
    Typeface font;
    String sheds;

    SearchView searchView;
    public SimpleCursorAdapter ca;

    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String DR_150 = "DR-150";
    public static final String WEIGH_AND_TARE = "Discrete";
    public static final String FILLING = "Incremental";
    public static final String MANUAL = "Manual";
    private Snackbar snackbar;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;


    public static final String DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String SCALE_VERSION = "scaleVersion";

    public static final int TARE_SCALE = 0;
    public static final int ZERO_SCALE = 12;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_STATE_CHANGE_PRINTER = 11;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_WRITE = 3;
    public static final int READING_PROBE = 6;

    public static final int REQUEST_DEVICEADDRESS = 101;

    public static final int PRINT_DELIVERY = 102;
    public static final int PRINT_Z_REPORT = 103;

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static final String TOAST = "toast";
    public static final int COMPLETE_FAILURE = 404;

     static Double GROSS_KG = 0.0;

    public static final String RECEIPT_NO = "receipt_no";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String TERMINAL = "terminal";
    public static final String FARMER_NO = "farmer_no";
    public static final String NAME = "name";
    public static final String ROUTE = "route";
    public static final String SHED = "shed";
    public static final String BATCH = "batch";
    public static final String GROSS_WEIGHT = "gross_weight";
    public static final String TARE_WEIGHT = "tare_weight";
    public static final String NET_WEIGHT = "net_weight";
     public static final String UNIT_PRICE = "unit_price";
    public static final String AMOUNT = "amount";
    public static final String CLERK_NAME = "clerk_name";
    public static final String COMPANY = "message_header";
    public static final String ADDRESS_LINE1 = "address_line1";
    public static final String ADDRESS_LINE2 = "address_line2";

    public static final String BATCH_DATE = "batch_date";
    public static final String BATCH_NO = "batchNo";
    public static final String BATCH_OPEN_DATE = "openDate";
    public static final String BATCH_TOTAL = "batchTotal";
    public static final String BATCH_CLOSE_DATE = "closeDate";
    public static final String BATCH_DISPATCH_DATE = "dispatchDate";
    public static final String BATCH__WEIGHINGPERIOD = "weighingPeriod";
    public static final String BATCH_DELIVERY_NOTE_NO = "deliveryNoteNo";
    public static final String BATCH_VEHICLE_NO = "vehicleNo";
    public static final String BATCH_USER_ID = "userID";
    public static final String BATCH_OPEN_TIME = "openTime";
    public static final String BATCH_CLOSE_TIME = "closeTime";
    public static final String BATCH_DISPATCH_TIME = "dispatchTime";
    public static final String TOTAL_WEIGHMENTS = "totalWeighments";
    public static final String FACTORY_CODE = "factoryCode";

    public static final String SELECTED_DATE = "selectedDate";
    public static final String SELECTED_BATCH = "selectedBatch";

    static String SessionID,SessionReceipt,SessionDate,SessionTime,SessionDevice,SessionFarmerNo,SessionBags,SessionNet,SessionTare,SessionRoute;


    int weighmentCounts = 1;
    int weighmentIndex = 0;

    static Context _ctx;
    static Activity _activity;

    static double weighments[] = null ;

    int gotConsignmentUniqueid = 0;

    static boolean stopRefreshing = false;
    double grossWeight = 0.0, totalGrossWeight = 0.0;
    static double tareWeight = 0.0, setTareWeight = 0.0,totalTareWeight = 0.0;
    static double netWeight = 0.0,totalNetWeight = 0.0,rtotalTareWeight = 0.0;


    static boolean stableReading = false;
    boolean sameFarmer = false;

    static int loopingIndex = 0;





    public static String cachedDeviceAddress;



    SimpleDateFormat mDateFormat ;
    // Name of the connected device
    private static String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    //private BluetoothAdapter mBluetoothAdapter = null;

    public static final String TAG = "Weighing";
    public static String DEVICE_TYPE = "device_type";


    static ProgressDialog mProcessDialog;




    private static Messenger mWeighingService;

    private static boolean mWeighingServiceBound;
    private boolean mPrintingServiceBounnd;



    boolean firstWeighment = false;

    TextView tv_number;

    //UUID for creating sessions
    UUID uuid = UUID.randomUUID();
    String weighingSession = "";
    static Button btn_accept,btn_next,btn_print,btn_reconnect;
    LinearLayout lt_accept,lt_nprint;
    private ProgressBar mProgress;
    Message msg;
    Bundle b;

    int recieptNo=1;
    int SNo=1;
    int SessNo=1;
    static String SessionNo,ReceiptNo,ReferenceNo,BatchSerial,Quality,ColDate,Time,BatchDate, DataDevice,BatchNo,Agent,FarmerNo,FarmerName,MTDKGS,Kgs,Tare;
    String WorkerNo, FieldClerk,ProduceCode;
    String VarietyCode,GradeCode, RouteCode, ShedCode,ShedName;
    String TransporterCode,GrossTotal,TareWeight,UnitCount,LeafQuality;
    String UnitPrice, RecieptNo,BagCount,produce, CanSerial,NetWeight,Accummulated;
    String newGross,newNet,newTare,initialPrice;
    // socket represents the open connection.
    BluetoothSocket mBTSocket   = null;
    WeighingService resetConn;
    SQLiteDatabase db;
    DecimalFormat formatter;
    static Double myGross=0.0;

    public static ProgressDialog mConnectingDlg;
    public static BluetoothAdapter mBluetoothAdapter;
    public static P25Connector mConnector;

    String ScaleConn;
    public static AlertDialog weigh;
    public static AlertDialog dialog_quality;
    public static Button btnReconnect;
    static Double netweight=0.0;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat dateTimeFormat1;
    static int SessionBgs;
    static String[] ScaleError;
    static String errorNo;

    public static String rFarmerNo,rFarmerName,rFarmerPhone,rAccummulated;
    String smsurl;
    String username,password,senderid,textmessage;
    String retval = "";
    String error_code="100";
    String error_desc="";
    String success_desc="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        firstWeighment = true; //ensure receipt no is not incremented on first weighment
        weighingSession = uuid.toString();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(FarmerScaleWeighActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(FarmerScaleWeighActivity.this);
        //send print request
        resetConn=new WeighingService();
        msg = Message.obtain(null, WeighingService.PRINT_REQUEST);


        try {
            setContentView(R.layout.activity_listfarmers_weigh);
            _ctx = this;
            _activity = this;

            //first get scale version
            if (mSharedPrefs.getString("scaleVersion", "Manual").toString().equals(MANUAL)) {
                resetConn.stop();
                //Toast.makeText(getBaseContext(), "You are using Manual Scale", Toast.LENGTH_LONG).show();
            }
            else {

                cachedDeviceAddress = pref.getString("address", "");
               // Toast.makeText(getBaseContext(), "Current Scale:"+cachedDeviceAddress, Toast.LENGTH_LONG).show();
                if (WeighingService.mState != WeighingService.STATE_CONNECTED) {
                    mProcessDialog = new ProgressDialog(this);
                    mProcessDialog.setTitle("Please Wait");
                    mProcessDialog.setMessage("Attempting Connection to Scale ...");
                    mProcessDialog.setCancelable(false);
                    mProcessDialog.show();
                }

                //Not yet connected to service
                mWeighingServiceBound = false;
                mPrintingServiceBounnd = false;


                try {
                    try {
                        gotConsignmentUniqueid = Integer.valueOf(mSharedPrefs.getString("consignmentUniqueID", "0"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    new Date();
                    mDateFormat = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

				/*cumulativeWeight = mDbHelper.getCumultiveWeight(tvMemberNo.getText().toString(),
						String.valueOf(gotConsignmentUniqueid));*/
                } catch (Exception e) {
                    Log.e(TAG, "Getting Cumaltive " + e.toString());
                    e.printStackTrace();
                }


                registerReceiver(mReceiver, initIntentFilter());

                if (mSharedPrefs.getString("weighingAlgorithm", "Incremental").toString().equals(FILLING)) {
                    // tvTareWeight.setText("0.0");
                    //first connection so we send tare command
                    mHandler.sendEmptyMessage(FarmerScaleWeighActivity.TARE_SCALE);
                } else {
                    mHandler.sendEmptyMessage(FarmerScaleWeighActivity.ZERO_SCALE);
                    //setTareWeight = Double.parseDouble(mSharedPrefs.getString("tareWeight", "1.2"));
                    //tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));
                }

                //weighmentCounts = mSharedPrefs.getInt("weighmentCounts", 0);
                weighmentCounts=1;
                if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                    // go back to milkers activity
                    //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                    mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();
                }

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage().toString());
        }

        setupToolbar();
        initializer();
        setupSnackBar();
    }
    private static void showToast(String message) {
        Toast.makeText(_ctx.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_farmerW);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    public void setupSnackBar() {
        snackbar = Snackbar.make(findViewById(R.id.ParentLayoutFarmers), getString(R.string.ScaleError), Snackbar.LENGTH_LONG);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#FF5252"));
    }
    public void initializer(){


        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        formatter = new DecimalFormat("0000");
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateTimeFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "LCDM2B__.TTF");
        txtKgs = (TextView) this.findViewById(R.id.txtKGS);
        txtKgs.setText("0.0");
        txtKgs.setVisibility(View.GONE);
        txtScaleConn = (TextView) this.findViewById(R.id.txtScaleConn);
        ScaleConn=txtScaleConn.getText().toString();

        txtPrinterConn = (TextView) this.findViewById(R.id.txtPrinterConn);
        btnReconnect = (Button) this.findViewById(R.id.btnReconnect);
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                connect();
            }
        });

        listFarmers = (ListView) this.findViewById(R.id.lvUsers);
        listFarmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                tv_number = (TextView) selectedView.findViewById(R.id.tv_number);

                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                //first get scale version

                myGross = Double.parseDouble(txtKgs.getText().toString());
                if (myGross > 0) {
                    Context context = FarmerScaleWeighActivity.this;
                    LayoutInflater inflater = FarmerScaleWeighActivity.this.getLayoutInflater();
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

                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("Gross");
                edit.remove("Net");
                edit.commit();
                if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {

                    // Toast.makeText(getBaseContext(), "Quality not enabled on settings", Toast.LENGTH_LONG).show();
                    showWeighDialog();
                }else{
                  showQualityDialog();
                 }

                      }
        });



        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setQueryHint("Search Farmer No ...");
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String farmerNo = constraint.toString();
                        return dbhelper.SearchSpecific(farmerNo);

                    }
                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String farmerNo = constraint.toString();
                        return dbhelper.SearchFarmer(farmerNo);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });



    }
    public void showQualityDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_quality, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("------- Enter Quality -------");
        accountId = textAccountId.getText().toString();

        etQuality = (EditText) dialogView.findViewById(R.id.etQuality);
        //etQuality.setFilters(new InputFilter[]{ new InputFilterMinMax("1.0", "100.0")});
        btn_next = (Button) dialogView.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(etQuality.getText().toString().isEmpty()) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Quality Cannot be Empty");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                    return;

                }
                double quality;
                final DecimalFormat df = new DecimalFormat("#0.0#");
                try {


                    quality = new Double(etQuality.getText().toString());
                    //System.out.println(value);}
                } catch (NumberFormatException e) {
                    quality = 0; // your default value
                }

                if(quality<=0.0 || quality>100.0)
                {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Quality Cannot be 0.0 or greater than 100.0");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("Quality", etQuality.getText().toString());
                edit.commit();


               showWeighDialog();
               dialog_quality.dismiss();
            }
        });
        dialogBuilder.setNegativeButton("   ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        dialogBuilder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                //getdata();
                // weighmentCounts = 1;

            }
        });
        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        dialog_quality= dialogBuilder.create();
        dialog_quality.show();
       /* dialog_quality.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (wantToCloseDialog)
                    weigh.dismiss();

            }
        });*/

    }

    public void showWeighDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_weigh, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("------- Weigh Produce -------");
        accountId = textAccountId.getText().toString();
        tvError = (TextView) dialogView.findViewById(R.id.tvError);
        tvStability = (TextView) dialogView.findViewById(R.id.tvStability);
        tvMemberName = (TextView) dialogView.findViewById(R.id.tvMemberNameShow);
        tvShowMemberNo = (TextView) dialogView.findViewById(R.id.tvShowMemberNo);

        tvWeighingAccumWeigh = (TextView) dialogView.findViewById(R.id.tvWeighingAccumWeigh);
        tvWeighingAccumWeigh.setTypeface(font);

        tvWeighingTareWeigh = (TextView) dialogView.findViewById(R.id.tvWeighingTareWeigh);
        tvWeighingTareWeigh.setTypeface(font);
        tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));

        tvShowTotalKgs = (TextView) dialogView.findViewById(R.id.tvShowTotalKgs);
        tvShowTotalKgs.setTypeface(font);

        tvsavedReading = (TextView) dialogView.findViewById(R.id.tvvGross);
        tvSavedNet = (TextView) dialogView.findViewById(R.id.tvvTotalKgs);
        tvSavedTare = (TextView) dialogView.findViewById(R.id.tvTareWeight);
        tvSavedUnits = (TextView) dialogView.findViewById(R.id.tvvcount);
        tvSavedTotal = (TextView) dialogView.findViewById(R.id.tvAccumWeight);

        tvUnitsCount = (TextView) dialogView.findViewById(R.id.tvUnitsCount);
        tvUnitsCount.setTypeface(font);
        tvUnitsCount.setText(String.valueOf(weighmentCounts));

        tvShowGrossTotal = (TextView) dialogView.findViewById(R.id.tvShowGrossTotal);
        tvShowGrossTotal.setTypeface(font);

        tvGrossAccepted = (TextView) dialogView.findViewById(R.id.tvGrossAccepted);
        tvGrossAccepted.setTypeface(font);

        tvTareAccepted = (TextView) dialogView.findViewById(R.id.tvTareAccepted);
        tvTareAccepted.setTypeface(font);

        tvNetWeightAccepted = (TextView) dialogView.findViewById(R.id.tvNetWeightAccepted);
        tvNetWeightAccepted.setTypeface(font);

        etShowGrossTotal = (EditText) dialogView.findViewById(R.id.etShowGrossTotal);
        etShowGrossTotal.setTypeface(font);

        lt_accept=(LinearLayout)dialogView.findViewById(R.id.lt_accept);
        lt_nprint=(LinearLayout)dialogView.findViewById(R.id.lt_nprint);
        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(etShowGrossTotal.getText().length()>0){
                    calculateResult();

                }
                if(etShowGrossTotal.getText().length()==0) {
                    tvShowTotalKgs.setText("0.0");
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        };

        etShowGrossTotal.addTextChangedListener(textWatcher);

         btn_accept = (Button) dialogView.findViewById(R.id.btn_accept);
        btn_next = (Button) dialogView.findViewById(R.id.btn_next);
        btn_print = (Button) dialogView.findViewById(R.id.btn_print);

        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
            if(!mSharedPrefs.getBoolean("enableSMS", false)==true) {

                //Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();

            }else{
                btn_print.setText("Send Sms");
            }
        }

        btn_reconnect = (Button) dialogView.findViewById(R.id.btn_reconnect);
        btn_reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    connect();


            }
        });
        btn_reconnect.setVisibility(View.GONE);
        btn_accept.setOnClickListener(new View.OnClickListener() {
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
                    //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
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
                if(!checkIfAllEqual()){
                    tvStability.setVisibility(View.VISIBLE);
                    stopRefreshing = false;
                    return;
                }else{
                    tvStability.setVisibility(View.GONE);
                }
                if (mSharedPrefs.getString("weighingAlgorithm", "Incremental").toString().equals(FILLING)) {
                    // tvTareWeight.setText("0.0");
                    //first connection so we send tare command
                    mHandler.sendEmptyMessage(FarmerScaleWeighActivity.TARE_SCALE);
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
                    edit.putString("tareAcc", tvWeighingTareWeigh.getText().toString());
                    edit.commit();

                }
                ///Setting Negative "Yes" Button
                dialogBuilder.setNegativeButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dbhelper = new DBHelper(getApplicationContext());
                                SQLiteDatabase db = dbhelper.getReadableDatabase();
                                formatter = new DecimalFormat("0000");
                                Date BatchD = null;
                                try {
                                    BatchD = dateTimeFormat.parse(prefs.getString("BatchON", "").toString() + " 00:00:00");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                SimpleDateFormat format0 = new SimpleDateFormat("yyyyMMdd");
                                BatchDate = format0.format(BatchD);
                                BatchNo = prefs.getString("BatchNumber", "");
                                DataDevice = mSharedPrefs.getString("terminalID", "").toString() + BatchDate + BatchNo;
                                Calendar cal = Calendar.getInstance();
                                Date date = new Date(getDate());
                                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                                SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                                ColDate = format1.format(BatchD);
                                Time = format1.format(date)+" "+format2.format(cal.getTime());
                                FarmerNo = tvShowMemberNo.getText().toString();
                                WorkerNo = "";
                                Agent = "";
                                FieldClerk = prefs.getString("user", "");
                                ProduceCode = prefs.getString("produceCode", "");
                                VarietyCode = prefs.getString("varietyCode", "");
                                GradeCode = prefs.getString("gradeCode", "");
                                UnitPrice = prefs.getString("unitPrice", "");
                                RouteCode = prefs.getString("routeCode", "");
                                ShedCode = prefs.getString("shedCode", "");
                                CanSerial = prefs.getString("canSerial", " ");
                                TareWeight = prefs.getString("tareAcc", "");
                                Accummulated = prefs.getString("Accum", "");
                                UnitCount = tvUnitsCount.getText().toString();
                                newNet = prefs.getString("Net", "");
                                newGross = prefs.getString("Gross", "");

                                final DecimalFormat df = new DecimalFormat("#0.0#");
                                Double Accum = 0.0, NewAccum = 0.0;
                                Accum = Double.parseDouble(newNet) + Double.parseDouble(Accummulated);
                                NewAccum = Accum + Double.parseDouble(rAccummulated);

                               /* Cursor count = db.rawQuery("select * from FarmersProduceCollection where " + Database.CollDate + " = '" + ColDate + "' and " +
                                        "" + Database.BatchNumber + " = '" + BatchNo + "'", null);
                                if (count.getCount() > 0) {
                                    Cursor c = db.rawQuery("select MAX(ReceiptNo) from FarmersProduceCollection  where " + Database.CollDate + " = '" + ColDate + "' and " +
                                            "" + Database.BatchNumber + " = '" + BatchNo + "'", null);
                                    if (c != null) {

                                        c.moveToFirst();

                                        recieptNo = Integer.parseInt(c.getString(0)) + 1;
                                        RecieptNo = formatter.format(recieptNo);

                                    }
                                    c.close();
                                } else {
                                    RecieptNo = formatter.format(recieptNo);

                                }*/

                                Double Gross = 0.0, Net = 0.0, Tare = 0.0;
                                Net = Accum;
                                Tare = Double.parseDouble(tvUnitsCount.getText().toString()) * Double.parseDouble(prefs.getString("tareAcc", ""));
                                Gross = Net + Tare;
                                GrossTotal = df.format(Gross);
                                NetWeight = df.format(Net);
                                newTare = df.format(Tare);

                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("textTotalKgs", df.format(NewAccum));
                                edit.commit();

                                Cursor session = db.rawQuery("select * from SessionTbl", null);
                                if (session.getCount() > 0) {

                                    Cursor c = db.rawQuery("select MAX(_id) from SessionTbl", null);
                                    if (c != null) {

                                        c.moveToFirst();

                                        SNo = Integer.parseInt(c.getString(0)) + 1;
                                        SessionID = String.valueOf(SNo);

                                    }
                                    c.close();
                                }else
                                {
                                    SessionID = String.valueOf(SNo);
                                }

                                Cursor sessiona = db.rawQuery("select * from SessionTbl where " + Database.SessionDevice + " = '" + DataDevice + "'", null);
                                if (sessiona.getCount() > 0) {
                                    Cursor c1 = db.rawQuery("select MAX("+Database.SessionCounter+") from SessionTbl where " + Database.SessionDevice + " = '" + DataDevice + "'", null);
                                    if (c1 != null) {

                                        c1.moveToFirst();

                                        SessNo = Integer.parseInt(c1.getString(0)) + 1;
                                        SessionNo = formatter.format(SessNo);

                                    }
                                    c1.close();

                                } else {
                                    SessionNo = formatter.format(SessNo);
                                }

                                SessionDate = ColDate;
                                SessionTime = Time;
                                SessionDevice = DataDevice;
                                SessionFarmerNo = FarmerNo;
                                SessionBags = UnitCount;
                                SessionNet = NetWeight;
                                SessionTare = newTare;
                                SessionRoute = RouteCode;

                                SessionReceipt=DataDevice+SessionNo;
                                ReferenceNo=DataDevice+SessionNo;
                                BatchSerial=DataDevice;
                                if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                                    Quality="0";
                                }else{
                                    Quality=prefs.getString("Quality", "0");;
                                }


                                ContentValues values = new ContentValues();
                                values.put(Database.F_PRODUCE_KG_TODATE, df.format(NewAccum));
                                long rows = db.update(Database.FARMERS_TABLE_NAME, values,
                                        "FFarmerNo = ?", new String[]{FarmerNo});

                                if (rows > 0) {
                                    // Toast.makeText(getApplicationContext(), "Updated Total KGs Successfully!",Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Sorry! Could not update Total KGs!",
                                            Toast.LENGTH_LONG).show();
                                }

                                SessionBgs = Integer.parseInt(tvUnitsCount.getText().toString());
                                if (SessionBgs == 1) {
                                    edit.putString("SessionID", SessionID);
                                    edit.commit();
                                    edit.putString("SessionNo", SessionNo);
                                    edit.commit();

                                    dbhelper.AddSession(SessionID, SessionDate, SessionTime, SessionDevice, SessionFarmerNo, SessionBags, SessionNet, SessionTare, SessionRoute, SessionNo,SessionReceipt);

                                } else {

                                    SessionID = prefs.getString("SessionID", "");
                                    SessionNo = prefs.getString("SessionNo", "");

                                    edit.putString("textReciept", DataDevice + SessionNo);
                                    edit.commit();
                                    ContentValues value = new ContentValues();
                                    value.put(Database.SessionTime, SessionTime);
                                    value.put(Database.SessionBags, SessionBags);
                                    value.put(Database.SessionNet, SessionNet);
                                    value.put(Database.SessionTare, SessionTare);

                                    long row = db.update(Database.SESSION_TABLE_NAME, value,
                                            "_id = ?", new String[]{SessionID});

                                    // db.close();
                                    if (row > 0) {
                                        // Toast.makeText(getApplicationContext(), "Updated Total KGs Successfully!",Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Sorry! Could not update Session!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                               Cursor s = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                                         + Database.FarmerNo + "='" + FarmerNo + "'" +
                                        " and " + Database.LoadCount + "='" + UnitCount + "' and " + Database.DataCaptureDevice + "='" + DataDevice + "'" +
                                        " and " + Database.ReceiptNo + "='" + SessionNo + "'", null);
                                if (s.getCount()>0) {

                                    Toast.makeText(getApplicationContext(), "Weighment Already Saved!",Toast.LENGTH_LONG).show();
                                    dialog.cancel();
                                    tvShowGrossTotal.setVisibility(View.GONE);
                                    tvShowTotalKgs.setVisibility(View.GONE);
                                    tvWeighingTareWeigh.setVisibility(View.GONE);

                                    tvGrossAccepted.setVisibility(View.VISIBLE);
                                    tvGrossAccepted.setText(newGross);
                                    tvTareAccepted.setVisibility(View.VISIBLE);
                                    tvTareAccepted.setText(prefs.getString("tareAcc", ""));
                                    tvNetWeightAccepted.setVisibility(View.VISIBLE);

                                    tvNetWeightAccepted.setText(newNet);
                                    tvWeighingAccumWeigh.setText(df.format(Accum));

                                    lt_accept.setVisibility(View.GONE);
                                    lt_nprint.setVisibility(View.VISIBLE);
                                    weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);

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
                                    return;
                                   }
                                else
                                {
                                dbhelper.AddFarmerTrans(ColDate, Time, DataDevice, BatchNo, Agent, FarmerNo,
                                        WorkerNo, FieldClerk, ProduceCode,
                                        VarietyCode, GradeCode, RouteCode, ShedCode,
                                        newNet, TareWeight, UnitCount,
                                        UnitPrice, SessionNo, ReferenceNo,BatchSerial,Quality,CanSerial);

                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                                 dialog.cancel();
                                }

                                Context context = getApplicationContext();
                                LayoutInflater inflater = getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                text.setText("Saved Successfully: " + newGross + " Kg\nRecieptNo: " + DataDevice + SessionNo + "\nFarmerNo: " + FarmerNo);
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_LONG);
                                // customtoast.show();

                                tvShowGrossTotal.setVisibility(View.GONE);
                                tvShowTotalKgs.setVisibility(View.GONE);
                                tvWeighingTareWeigh.setVisibility(View.GONE);

                                tvGrossAccepted.setVisibility(View.VISIBLE);
                                tvGrossAccepted.setText(newGross);
                                tvTareAccepted.setVisibility(View.VISIBLE);
                                tvTareAccepted.setText(prefs.getString("tareAcc", ""));
                                tvNetWeightAccepted.setVisibility(View.VISIBLE);

                                tvNetWeightAccepted.setText(newNet);
                                tvWeighingAccumWeigh.setText(df.format(Accum));

                                lt_accept.setVisibility(View.GONE);
                                lt_nprint.setVisibility(View.VISIBLE);
                                weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);

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
                                dialog.cancel();

                            }
                        });
                // Showing Alert Message
                dialogBuilder.show();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                                myGross=Double.parseDouble(txtKgs.getText().toString());
                                if (myGross>0){
                                    Context context = FarmerScaleWeighActivity.this;
                                    LayoutInflater inflater = FarmerScaleWeighActivity.this.getLayoutInflater();
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

                                lt_accept.setVisibility(View.VISIBLE);
                                lt_nprint.setVisibility(View.GONE);

                                tvShowGrossTotal.setVisibility(View.VISIBLE);
                                 tvWeighingTareWeigh.setVisibility(View.VISIBLE);
                                tvShowTotalKgs.setVisibility(View.VISIBLE);

                                tvGrossAccepted.setVisibility(View.GONE);
                                  tvTareAccepted.setVisibility(View.GONE);
                                tvNetWeightAccepted.setVisibility(View.GONE);

                                weighmentCounts = weighmentCounts+1;
                                tvUnitsCount.setText(String.valueOf(weighmentCounts));
                                weigh.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                             weigh.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                     public void onClick(View v) {

                        int count;
                        count = Integer.parseInt(tvUnitsCount.getText().toString());
                        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                            // go back to milkers activity
                           // Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                            weighmentCounts = 1;
                            weigh.dismiss();
                        }
                        {
                        if (count > 1) {
                            tvShowGrossTotal.setVisibility(View.GONE);
                            tvWeighingTareWeigh.setVisibility(View.GONE);
                            tvShowTotalKgs.setVisibility(View.GONE);

                            tvGrossAccepted.setVisibility(View.VISIBLE);
                            tvGrossAccepted.setText(newGross);
                            tvTareAccepted.setVisibility(View.VISIBLE);
                            tvTareAccepted.setText(prefs.getString("tareAcc", ""));
                            tvNetWeightAccepted.setVisibility(View.VISIBLE);

                            tvNetWeightAccepted.setText(newNet);
                            tvWeighingAccumWeigh.setText(prefs.getString("textTotalKgs", ""));
                            tvUnitsCount.setText(String.valueOf(weighmentCounts-1));
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
                        else{
                            weighmentCounts = 1;
                            weigh.dismiss();

                        }
                        Boolean wantToCloseDialog = false;
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            weigh.dismiss();}
                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });
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

        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check scale version before printing
                if(mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_15)||
                   mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_11)||
                   mSharedPrefs.getString("scaleVersion", "").toString().equals(DR_150))
                {

                if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                    if(!mSharedPrefs.getBoolean("enableSMS", false)==true) {

                       //Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();

                    } else {

                        if (mSharedPrefs.getString("SMSModes", "BS").toString().equals("BS")) {
                            if (rFarmerPhone.equals("")) {
                                Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (rFarmerNo.equals("")) {
                                Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
                                return;
                            }

                            new WEBSMS().execute();
                        }
                        else{
                           // rFarmerPhone="+254715881439";
                            rAccummulated=prefs.getString("textTotalKgs", "");
                            if (rFarmerPhone.equals("")) {
                                Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (rFarmerNo.equals("")) {
                                Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
                                return;
                            }
                            textmessage=SessionReceipt +" for "+rFarmerNo+"," + rFarmerName +
                                    " weighed " + UnitCount +" bags of Net weight: " +NetWeight+ " Kg on " +ColDate+
                                    ". Month to Date weight: " + rAccummulated + " Kg.";

                            Sendsms(rFarmerPhone, textmessage);
                            weigh.dismiss();
                            weighmentCounts = 1;
                            Context context=getApplicationContext();
                            LayoutInflater inflater=getLayoutInflater();
                            View customToastroot =inflater.inflate(R.layout.blue_toast, null);
                            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                            text.setText("Message Sent Successfully!!");
                            Toast customtoast=new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getBaseContext(), "Message Sent Successfully!!", Toast.LENGTH_LONG).show();
                        }
                     return;
                    }
                    // go back to milkers activity
                    Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                    return;
                }
                else {


                       if (mSharedPrefs.getString("Language", "Eng").toString().equals("Eng")) {


                           if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
                               PrintGenericReceipt();

                           }
                          else if (mSharedPrefs.getString("receiptTemplates", "Detailed").toString().equals("Detailed")) {
                               PrintDetailedReceipt();

                           }
                           else if (mSharedPrefs.getString("receiptTemplates", "Simple-Detailed").toString().equals("Simple-Detailed")) {
                               PrintSimpleDetailedReceipt();

                           }
                           else if (mSharedPrefs.getString("receiptTemplates", "Simple").toString().equals("Simple")) {
                               PrintSimpleReceipt();

                           }

                           else{

                               PrintDetailedReceipt();
                           }
                         }
                       else if (mSharedPrefs.getString("Language", "Kiswa").toString().equals("Kiswa")) {
                           if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
                               PrintGenKiswReceipt();

                           }

                       }

                                        AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                                        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Reprint Receipt?</font>"))
                                                .setCancelable(false)
                                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                    }
                                                })
                                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        weigh.dismiss();
                                                        weighmentCounts = 1;

                                                        if(!mSharedPrefs.getBoolean("enableSMS", false)==true) {

                                                           //Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();

                                                        } else {

                                                            if (mSharedPrefs.getString("SMSModes", "BS").toString().equals("BS")) {
                                                                if (rFarmerPhone.equals("")) {
                                                                    Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
                                                                    return;
                                                                }
                                                                if (rFarmerNo.equals("")) {
                                                                    Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
                                                                    return;
                                                                }

                                                                new WEBSMS().execute();
                                                            }
                                                            else{
                                                                 //rFarmerPhone="+254715881439";
                                                                rAccummulated=prefs.getString("textTotalKgs", "");
                                                                if (rFarmerPhone.equals("")) {
                                                                    Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
                                                                    return;
                                                                }
                                                                if (rFarmerNo.equals("")) {
                                                                    Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
                                                                    return;
                                                                }
                                                                textmessage=SessionReceipt +" for "+rFarmerNo+"," + rFarmerName +
                                                                        " weighed " + UnitCount +" bags of Net weight: " +NetWeight+ " Kg on " +ColDate+
                                                                        ". Month to Date weight: " + rAccummulated + " Kg.";

                                                                Sendsms(rFarmerPhone, textmessage);
                                                                Context context=getApplicationContext();
                                                                LayoutInflater inflater=getLayoutInflater();
                                                                View customToastroot =inflater.inflate(R.layout.blue_toast, null);
                                                                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                                                text.setText("Message Sent Successfully!!");
                                                                Toast customtoast=new Toast(context);
                                                                customtoast.setView(customToastroot);
                                                                customtoast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                                                                customtoast.setDuration(Toast.LENGTH_LONG);
                                                                customtoast.show();
                                                                //Toast.makeText(getBaseContext(), "Message Sent Successfully!!", Toast.LENGTH_LONG).show();
                                                            }

                                                        }

                                                    }
                                                });
                                        final AlertDialog alert2 = builder.create();
                                        alert2.show();
                                        alert2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                                        alert2.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
                                        alert2.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                                        alert2.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);
                                        alert2.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (mSharedPrefs.getString("Language", "Eng").toString().equals("Eng")) {


                                                    if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
                                                        PrintGenericReceipt();

                                                    }
                                                    else if (mSharedPrefs.getString("receiptTemplates", "Detailed").toString().equals("Detailed")) {
                                                        PrintDetailedReceipt();

                                                    }
                                                    else if (mSharedPrefs.getString("receiptTemplates", "Simple-Detailed").toString().equals("Simple-Detailed")) {
                                                        PrintSimpleDetailedReceipt();

                                                    }
                                                    else if (mSharedPrefs.getString("receiptTemplates", "Simple").toString().equals("Simple")) {
                                                        PrintSimpleReceipt();

                                                    }

                                                    else{

                                                        PrintDetailedReceipt();
                                                    }
                                                }
                                                else if (mSharedPrefs.getString("Language", "Kiswa").toString().equals("Kiswa")) {
                                                    if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
                                                        PrintGenKiswReceipt();

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

                //first get scale version
        if(mSharedPrefs.getString("scaleVersion", "Manual").toString().equals(MANUAL)) {
            etShowGrossTotal.setVisibility(View.VISIBLE);
            tvShowGrossTotal.setVisibility(View.GONE);
        }
        else {

            etShowGrossTotal.setVisibility(View.GONE);
            tvShowGrossTotal.setVisibility(View.VISIBLE);
        }

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.FARMERS_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            tvShowMemberNo.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNO)));
            tvMemberName.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNAME)));

           // tvWeighingAccumWeigh.setText(account.getString(account.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));

            rFarmerNo=account.getString(account.getColumnIndex(Database.F_FARMERNO));
            rFarmerName=account.getString(account.getColumnIndex(Database.F_FARMERNAME));
            rFarmerPhone=account.getString(account.getColumnIndex(Database.F_MOBILENUMBER));
            rAccummulated=account.getString(account.getColumnIndex(Database.F_PRODUCE_KG_TODATE));

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("rFarmerNo", rFarmerNo);
            edit.commit();

            edit.putString("rFarmerPhone", rFarmerPhone);
            edit.commit();





        }
        account.close();
        db.close();
       dbhelper.close();

        dialogBuilder.setNegativeButton("   ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        dialogBuilder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                //getdata();
               // weighmentCounts = 1;

            }
        });
        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        weigh= dialogBuilder.create();
        weigh.show();
        weigh.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (wantToCloseDialog)
                    weigh.dismiss();

            }
        });
        weigh.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        weigh.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
        weigh.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#FA0703'>Are you sure want to close this window?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                weighmentCounts = 1;
                                int count,count2;
                                count=Integer.parseInt(tvUnitsCount.getText().toString());
                                if(count<=1){
                                    weigh.dismiss();

                                }
                                if(SessionBags!=null){
                                    count2=Integer.parseInt(SessionBags);
                                    if(count2>=1){
                                        //dbhelper.AddSession(SessionID,SessionDate,SessionTime,SessionDevice,SessionFarmerNo,SessionBags,SessionNet,SessionTare,SessionRoute,SessionNo);
                                        weigh.dismiss();

                                    }
                                }

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        });
                final AlertDialog btnback = builder.create();
                btnback.show();

                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (wantToCloseDialog)
                    weigh.dismiss();
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
    }
    // The function called to calculate and display the result of the multiplication
    private void calculateResult() throws NumberFormatException {
        // Gets the two EditText controls' Editable values
        Editable editableValue1 = etShowGrossTotal.getText();

        // Initializes the double values and result
        double value1 = 0.0, value2 = 0.0,  result;

        // If the Editable values are not null, obtains their double values by parsing

        value1 = Double.parseDouble(editableValue1.toString());




        // Calculates the result
        result = value1-setTareWeight ;

        // Displays the calculated result
        tvShowTotalKgs.setText(String.valueOf(result));

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata(){

        try {

            SQLiteDatabase db= dbhelper.getReadableDatabase();
            Cursor accounts = db.query( true, Database.FARMERS_TABLE_NAME,null,null,null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.F_FARMERNO , Database.F_FARMERNAME, Database.F_SHED, Database.F_MOBILENUMBER};
            int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_name,R.id.tv_shed,R.id.tv_phone};


            ca  = new SimpleCursorAdapter(this,R.layout.user_list, accounts,from,to);

            ListView listfarmers= (ListView) this.findViewById( R.id.lvUsers);
            listfarmers.setAdapter(ca);
            listfarmers.setTextFilterEnabled(true);
            db.close();
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case TARE_SCALE:
                    if (mWeighingServiceBound) {
                        try {
                            //first get scale version
                            if(mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                                if(mSharedPrefs.getString("weighingAlgorithm", "Incremental").toString().equals(FILLING)) {
                                    Message msg2 = Message.obtain(null, WeighingService.TARE_SCALE);
                                    mWeighingService.send(msg2);
                                }
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case ZERO_SCALE:
                    if (mWeighingServiceBound) {
                        /*try {
                            //first get scale version
                            if(mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                                if(mSharedPrefs.getString("weighingAlgorithm", "Discrete").toString().equals(WEIGH_AND_TARE)) {
                                    Message msg2 = Message.obtain(null, WeighingService.ZERO_SCALE);
                                    mWeighingService.send(msg2);
                                }
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }*/
                    }
                    break;

                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case WeighingService.STATE_CONNECTED:
                            FarmerScaleWeighActivity.mProcessDialog.setMessage("Connected to Scale");
                            //Toast.makeText(_ctx.getApplicationContext(), "Connected ...", Toast.LENGTH_SHORT).show();
                            txtScaleConn.setText("Scale Connected");
                            if(txtScaleConn.getText().toString().equals("Scale Connected")){

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
                                                Context context=_ctx.getApplicationContext();
                                                LayoutInflater inflater=_activity.getLayoutInflater();
                                                View customToastroot =inflater.inflate(R.layout.blue_toast, null);
                                                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                                text.setText("Connecting Printer ...");
                                                Toast customtoast=new Toast(context);
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

                                                Context context=_ctx.getApplicationContext();
                                                LayoutInflater inflater=_activity.getLayoutInflater();
                                                View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
                                                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                                text.setText("Printer Disconnected");
                                                Toast customtoast=new Toast(context);
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

                            }
                            try {
                                if(mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                                    if(mSharedPrefs.getString("weighingAlgorithm", "Incremental").toString().equals(FILLING)) {
                                        Message msg2 = Message.obtain(null, WeighingService.TARE_SCALE);
                                        mWeighingService.send(msg2);
                                    }
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }


                            break;
                        case WeighingService.STATE_CONNECTING:
                            mProcessDialog.setMessage("Attempting Connection to scale");

                            //Toast.makeText(getApplicationContext(), "Connecting ...", Toast.LENGTH_SHORT).show();
                            break;
                        case WeighingService.STATE_LISTEN:
                        case WeighingService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_STATE_CHANGE_PRINTER:
                    switch (msg.arg1) {
                        case WeighingService.STATE_CONNECTED:
                            FarmerScaleWeighActivity.mProcessDialog.setMessage("Connected to Printer");
                            break;
                        case WeighingService.STATE_CONNECTING:
                            mProcessDialog.setMessage("Attempting Connection to printer");
                            break;
                        case WeighingService.STATE_LISTEN:
                        case WeighingService.STATE_NONE:
                            break;
                    }
                    break;
                case REQUEST_DEVICEADDRESS: //Device Address Not found call DeviceListActivity
                    if(mProcessDialog!=null && mProcessDialog.isShowing()) {
                        mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                    }
                    if (mSharedPrefs.getString("scaleVersion", "Manual").toString().equals(MANUAL)) {

                       // Toast.makeText(getBaseContext(), "You are using Manual Scale", Toast.LENGTH_LONG).show();
                    }
                    else {
                    _activity.finish();
                    Intent intentDeviceList = new Intent(_ctx.getApplicationContext(), DeviceListActivity.class);
                    _activity.startActivityForResult(intentDeviceList, 1);
                    }
                    break;
                case MESSAGE_READ:
                    try {
                        txtScaleConn.setText("Scale Connected");

                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);

                        Log.i(TAG, "Returned Message" + readMessage);



                        //Convert message to ascii byte array
                        byte[] messageBytes = stringToBytesASCII(readMessage);

                        String thisWeighment = "";
                        if (mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                            thisWeighment = getReading(messageBytes,readMessage);
                            totalTareWeight = setTareWeight + tareWeight;
                        } else {
                            thisWeighment = getReading(messageBytes);
                        }

                        final DecimalFormat df = new DecimalFormat("#0.0#");
                        Double myDouble = 0.0;

                        //thisWeighment = newFormatReading[0]; //overriding weighment
                        //tareWeight = Double.parseDouble(newFormatReading[1]);

                        Log.i(TAG, "New Format Reading is " + thisWeighment);
                        Log.i(TAG, "Weighment is " + thisWeighment);
                        Log.i(TAG, "Tare Weight is " + tareWeight);
                        Log.i(TAG, "Error No " + errorNo);

                        if(thisWeighment != null && !thisWeighment.isEmpty())  {
                            myDouble = Double.parseDouble(thisWeighment);
                            prefs = PreferenceManager.getDefaultSharedPreferences(_ctx.getApplicationContext());
                            SharedPreferences.Editor edit = prefs.edit();
                        if(errorNo==null){}else {
                            if (errorNo.equals("100") || myDouble == 100.0) {

                                Context context = _ctx.getApplicationContext();
                                LayoutInflater inflater = _activity.getLayoutInflater();
                                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                text.setText("OVER WEIGHT");
                                Toast customtoast = new Toast(context);
                                customtoast.setView(customToastroot);
                                customtoast.setGravity(Gravity.TOP | Gravity.TOP, 0, 0);
                                customtoast.setDuration(Toast.LENGTH_LONG);
                                //  customtoast.show();
                                tvWeighingTareWeigh.setText("-");
                                tvShowGrossTotal.setText("-");
                                tvShowTotalKgs.setText("-");
                                tvError.setVisibility(View.VISIBLE);
                                tvStability.setVisibility(View.GONE);
                                tvMemberName.setVisibility(View.GONE);
                                btn_accept.setEnabled(false);

                                return;
                            } else {
                                tvError.setVisibility(View.GONE);
                                tvMemberName.setVisibility(View.VISIBLE);
                                btn_accept.setEnabled(true);
                            }
                        }
                            if (stopRefreshing != true) {
                                GROSS_KG=myDouble;
                                txtKgs.setText(df.format(GROSS_KG));
                                edit.putString("tvGross", df.format(myDouble));
                                edit.commit();

                                tvShowGrossTotal.setText(df.format(myDouble));
                                if (mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {

                                    tvWeighingTareWeigh.setText(String.valueOf(tareWeight));
                                    netWeight = Double.parseDouble(tvShowGrossTotal.getText().toString());
                                    df.setRoundingMode(RoundingMode.HALF_EVEN);
                                    df.format(netWeight);
                                }
                                else{
                                    tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));
                                    netWeight = Double.parseDouble(tvShowGrossTotal.getText().toString()) - setTareWeight;
                                    df.setRoundingMode(RoundingMode.HALF_EVEN);
                                    df.format(netWeight);
                                }

                                if (netWeight <= 0.0) {
                                    edit.putString("tvNetWeight", "0.0");
                                    edit.commit();
                                    tvShowTotalKgs.setText("0.0");
                                    tvStability.setVisibility(View.GONE);
                                }else{
                                    edit.putString("tvNetWeight",df.format(netWeight).toString());
                                    edit.commit();
                                    tvShowTotalKgs.setText(df.format(netWeight).toString());
                                }

                            } else {
                                /*if (tvMessage.getText().toString().equals("REMOVE!") || tvMessage.getText().toString().equals("UNSTABLE!")
                                        || tvMessage.getText().toString().equals("PLEASE REMOVE LOAD!")) {*/
                                   // if(myDouble <= 0) { //yes the weight has been off loaded
                                        stopRefreshing = false;
                                tvStability.setVisibility(View.GONE);
                                        //tvMessage.setText("");
                                        //tvMessage.setVisibility(View.GONE);
                                        Context context = _activity;
                                        LayoutInflater inflater = _activity.getLayoutInflater();
                                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                        text.setText("Reading Unstable ...");
                                        Toast customtoast = new Toast(context);
                                        customtoast.setView(customToastroot);
                                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                        customtoast.setDuration(Toast.LENGTH_LONG);
                                        //customtoast.show();
                                    //}
                                //}
                            }
                        }

                        new Thread() {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    //check for stable reading
                                    String stableReadingCounter = mSharedPrefs.getString("stabilityReadingCounter", "3");
                                    String milliSeconds=mSharedPrefs.getString("milliSeconds", "300");
                                    if(stableReadingCounter.equals("")){
                                        stableReadingCounter="3";
                                    }
                                    if(milliSeconds.equals("")){
                                        milliSeconds="300";
                                    }

                                    weighments = new double[Integer.valueOf(stableReadingCounter)];
                                    for (int i = 0; i<Integer.valueOf(stableReadingCounter);i++) {
                                        weighments[i] = Double.parseDouble(df.format(netWeight));
                                        sleep(Integer.valueOf(milliSeconds));
                                        if (stableReading = true) {
                                            sleep(10);
                                        } else {
                                            //sleep(Integer.valueOf(mSharedPrefs.getString("milliSeconds", "500")));
                                            sleep(10);
                                        }
                                    }
                                    //sleep(1); //interval of 500 milliseconds
                                    //return;
                                }
                                catch (InterruptedException localInterruptedException)
                                {
                                    localInterruptedException.printStackTrace();
                                    return;
                                }
                            }
                        }
                                .start();
                        loopingIndex++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }

                    break;
                case READING_PROBE:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    //Convert message to ascii byte array
                    byte[] messageBytes = stringToBytesASCII(readMessage);

                    String thisWeighment = "";
                    if (mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                        thisWeighment = getReading(messageBytes,readMessage);
                        totalTareWeight = setTareWeight + tareWeight;
                    } else {
                        thisWeighment = getReading(messageBytes);
                    }

                    if(!thisWeighment.equals("0.0")) {
                        //resend tare command

                        Message msg2 = Message.obtain(null, WeighingService.TARE_SCALE);
                        Message msg3 = Message.obtain(null, WeighingService.ZERO_SCALE);
                        try {
                            if(mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                                if(mSharedPrefs.getString("weighingAlgorithm", "Incremental").toString().equals(FILLING)) {
                                    mWeighingService.send(msg2);
                                }
                            }
                           // mWeighingService.send(msg3);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    mProcessDialog.setMessage("Connected to " + mConnectedDeviceName);

                    if(mProcessDialog!=null && mProcessDialog.isShowing()) {
                        mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                    }

                    // now send R for 3 seconds

                    try {
                        Message msg2 = Message.obtain(null, WeighingService.INIT_WEIGHING);
                        mWeighingService.send(msg2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                case MESSAGE_TOAST:
                    //mProcessDialog.setMessage("Unable To Connect to Device");
                    if(mProcessDialog==null) {
                        mProcessDialog = new ProgressDialog(_ctx.getApplicationContext());
                    }

                    mProcessDialog.setMessage(msg.getData().getString(TOAST));

                    if(mProcessDialog!=null && mProcessDialog.isShowing()) {
                        mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                    }
                    if (msg.getData().getString(TOAST).equals("Unable to connect scale")) {
                        Context context=_ctx.getApplicationContext();
                        LayoutInflater inflater=_activity.getLayoutInflater();
                        View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText(msg.getData().getString(TOAST));
                        Toast customtoast=new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        if (mBluetoothAdapter != null) {
                            if (mBluetoothAdapter.isDiscovering()) {
                                mBluetoothAdapter.cancelDiscovery();
                            }
                        }

                        if (mConnector != null) {
                            try {
                                mConnector.disconnect();
                            } catch (P25ConnectionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (msg.getData().getString(TOAST).equals("Scale Disconnected")) {

                    Context context=_ctx.getApplicationContext();
                    LayoutInflater inflater=_activity.getLayoutInflater();
                    View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText(msg.getData().getString(TOAST));
                    Toast customtoast=new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                        if (mBluetoothAdapter != null) {
                            if (mBluetoothAdapter.isDiscovering()) {
                                mBluetoothAdapter.cancelDiscovery();
                            }
                        }

                        if (mConnector != null) {
                            try {
                                mConnector.disconnect();
                            } catch (P25ConnectionException e) {
                                e.printStackTrace();
                            }
                        }
                        txtScaleConn.setVisibility(View.GONE);
                       // SessionSave();

                    }
                   // mProcessDialog.show();
                   // Toast.makeText(_ctx.getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_LONG).show();
                    new Thread() {
                        @Override
                        public void run()
                        {
                            try
                            {
                                sleep(3000);
                                return;
                            }
                            catch (InterruptedException localInterruptedException)
                            {
                                localInterruptedException.printStackTrace();
                                return;
                            }
                            finally
                            {
                                _activity.finish();

                            }
                        }
                    }
                            .start();
                    break;
                case COMPLETE_FAILURE:
                    //Something is terribly wrong
                    Toast.makeText(_ctx.getApplicationContext(),"Something is terribly Wrong",Toast.LENGTH_SHORT).show();
                    _activity.finish();
                    break;
            }
        }
    };
    private final Handler printerHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {

                    }
                    break;
                case MESSAGE_READ:

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    mProcessDialog.setMessage("Connected to " + mConnectedDeviceName);

                    if(mProcessDialog!=null && mProcessDialog.isShowing()) {
                        mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
                    }

                    break;
                case MESSAGE_TOAST:
                    mProcessDialog.setMessage(msg.getData().getString(TOAST));
                    if(mProcessDialog!=null)mProcessDialog.show();

                    new Thread() {
                        @Override
                        public void run()
                        {
                            try
                            {
                                sleep(3000);
                                return;
                            }
                            catch (InterruptedException localInterruptedException)
                            {
                                localInterruptedException.printStackTrace();
                                return;
                            }
                            finally
                            {
                                finish();
                            }
                        }
                    }
                            .start();
                    break;
                case COMPLETE_FAILURE:
                    //Something is terribly wrong
                    Toast.makeText(getApplicationContext(),"Something is terribly Wrong",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };
    private static final Messenger mMessenger = new Messenger(mHandler);
    @SuppressWarnings("unused")
    private Messenger mPrinterMessenger = new Messenger(printerHandler);




    public static byte[] stringToBytesASCII(String str) {
        byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

    static void showPrintDialog(final String message, final String dialogTitle) {
        new AlertDialog.Builder(_ctx)
                .setTitle(dialogTitle)
                .setMessage(message)
                .setNegativeButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    static String getReading(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        for (int i = 0; i < data.length; ++ i) {
            if (data[i] < 6) {
                throw new IllegalArgumentException();
            } else if (data[i] >= 46 && data[i] <=57) {
                sb.append((char) data[i]); //I believe this is an accurate reading
            }
        }
        //return sb.toString();
        return sb.toString().replaceAll("I", "");
    }

    static String getReading(byte[] data,String message) {
        String returnValue = "";
        try {
            for (int i = 0; i < data.length; ++ i) {
                if (message.length() >= 10 && message.length()<= 11) {
                    throw new IllegalArgumentException();
                } else if (data[i] >= 46 && data[i] <=57) {
                    String parts[] = message.trim().split(",");

                    returnValue = parts[0];
                    if (parts.length==2) {
                        tareWeight = Double.parseDouble(parts[1]);
                    }
                    String readMessage= new String(data);
                    ScaleError = readMessage.trim().split(",");
                    errorNo = ScaleError[1];
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    // Sets up communication with {@link WeighingService}
    private static ServiceConnection scaleConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mWeighingServiceBound = true;

            Bundle myBundle = new Bundle();
            myBundle.putInt(DEVICE_TYPE,1);

            Message msg = Message.obtain(null, WeighingService.MSG_REG_CLIENT);

            msg.setData(myBundle);

            msg.replyTo = mMessenger;

            mWeighingService = new Messenger(service);
            //mPrinterService = new Messenger(service);

            try {
                mWeighingService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to register client to service.");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWeighingService = null;

            mWeighingServiceBound = false;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
            // go back to milkers activity
            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
       if (mConnector != null) {
            txtPrinterConn.setVisibility(View.VISIBLE);

        }
        }

        sameFarmer = false;


        setTareWeight = Double.parseDouble( mSharedPrefs.getString("tareWeight", "0"));
        stopRefreshing = false;

        if (mWeighingServiceBound) {
            Message msg = Message.obtain(null, WeighingService.READING_PROBE);
            Bundle b = new Bundle();

            try {
                weighingSession = uuid.toString();
                weighmentCounts = 1;

                firstWeighment = false;
                stopRefreshing=false;

                msg.setData(b);
                mWeighingService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                weighmentCounts = 1;
                tvUnitsCount.setText(String.valueOf(weighmentCounts));

            }catch(Exception e) {
                e.printStackTrace();


            }
        } else {
            initialize();
        }
		/*if (!mPrintingServiceBounnd) {
			initialize();
		}*/
    }

    public static void initialize() {
        Log.d(TAG, "setup Service()");

        try {
            Intent intent = new Intent(FarmerScaleWeighActivity._ctx, WeighingService.class);
            //Intent printingIntent = new Intent(this,PrintingService.class);

            Bundle myBundle = new Bundle();
            Log.i(TAG, "Am Passing this address to Service " + cachedDeviceAddress);
            myBundle.putString(EXTRA_DEVICE_ADDRESS, cachedDeviceAddress);

            Log.i(TAG, "Scale Version " + mSharedPrefs.getString("scaleVersion", "EW15"));
            //get scale version
            if (mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                myBundle.putString(SCALE_VERSION, EASYWEIGH_VERSION_15);
            }
            else if(mSharedPrefs.getString("scaleVersion", "EW11").toString().equals(EASYWEIGH_VERSION_11)){
                myBundle.putString(SCALE_VERSION, EASYWEIGH_VERSION_11);
            }
            else if(mSharedPrefs.getString("scaleVersion", "DR-150").toString().equals(DR_150)) {
                myBundle.putString(SCALE_VERSION, DR_150);
            }

            intent.putExtras(myBundle); //add Bundle to intent

            _ctx.startService(intent);

            _ctx .bindService(intent, scaleConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage().toString());
        }
    }

    // Intent filter and broadcast receive to handle Bluetooth on event.
    private IntentFilter initIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) ==
                        BluetoothAdapter.STATE_ON) {
                    initialize();
                }
            }
        }
    };
    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from WeighingActivity service and Unregister receiver
        try {
            if (mWeighingServiceBound) unbindService(scaleConnection);
            unregisterReceiver(mReceiver);
            unregisterReceiver(pReceiver);
            if(mProcessDialog!=null && mProcessDialog.isShowing()) {
                mProcessDialog.dismiss(); //Dismiss the dialog since I know it's visible
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(FarmerScaleWeighActivity.this);
                    cachedDeviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    cachedDeviceAddress =pref.getString("address", "");
                    if (cachedDeviceAddress == null) {
                        Toast.makeText(getBaseContext(), "Please select scale....", Toast.LENGTH_LONG).show();
                       // finish();
                    } else {
                        //Send Message to Servive with new address

                        Message msg = Message.obtain(null, WeighingService.RETRY);
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_DEVICE_ADDRESS,cachedDeviceAddress);

                        try {
                            msg.setData(bundle);
                            mWeighingService.send(msg);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Unable to register client to service.");
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                try {
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a chat session
                        //initialize();

                        //	cachedDeviceAddress = mDbHelper.getLastUsedDevice();

                        Message msg = Message.obtain(null, WeighingService.RETRY);
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_DEVICE_ADDRESS,cachedDeviceAddress);

                        if(cachedDeviceAddress != null || !cachedDeviceAddress.isEmpty()) { //first check we have an address
                            try {
                                mProcessDialog.setMessage("Attempting Connection ...");
                                mProcessDialog.show();
                                msg.setData(bundle);
                                mWeighingService.send(msg);
                            } catch (RemoteException e) {
                                Log.w(TAG, "Unable to register client to service.");
                                e.printStackTrace();
                            }
                        } else { //Try Each and every other address in DB
                            mProcessDialog.setMessage("Unable to connect to Default Device");
                             FarmerScaleWeighActivity.this.finish();
                            Intent intentDeviceList = new Intent(getApplicationContext(), DeviceListActivity.class);
                            startActivityForResult(intentDeviceList, 1);
                        }

                    } else {
                        // User did not enable Bluetooth or an error occurred
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                        //mProcessDialog.dismiss();
                        finish();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "After Request BT " + e.toString());
                }

                break;
        }
    }
    public static String getAddress() {
        return cachedDeviceAddress;
    }

    public boolean checkIfAllEqual() {
		/*if (weighments.length == 0) {
			return false;
		} else {*/
        boolean returnValue = false;
        //double first = weighments[0];
        int stableReadingCounter = 0;
        String first,second = null;
        for (int i=0;i <weighments.length;i++){
            if(i==0) {
                if(weighments[i]==weighments[i+1]) {
                    stableReadingCounter++;
                }
            }else {
                Log.i(TAG, "Comparing " + weighments[i] + " with " + weighments[i-1] );
                first = String.valueOf(weighments[i]);
                second = String.valueOf(weighments[i-1]);

                //if(weighments[i]. weighments[i+1]){
                if(first.equals(second)) {
                    stableReadingCounter++;
                }else{
                    stableReadingCounter--;
                }
            }
        }

		/*for (double element : weighments) {
			if (element != first) {
				Log.i(TAG, "Element " + element);
				Log.i(TAG, weighments.length + " readings are not equal");
				unStableReadingCounter ++;
				stableReadingCounter--;
			}else {
				stableReadingCounter++;
			}
		}*/

        Log.i(TAG,"Counter is " + stableReadingCounter);
        if (stableReadingCounter==weighments.length) {
            returnValue = true;
            stableReading = true;
            Log.i(TAG, weighments.length + " readings are equal");
        }
        return returnValue;
        //}
    }
    private boolean OldcheckIfAllEqual() {
        int stableReadingCounter = 0;
        for (int i = 0; i < weighments.length; i++) {
            if (i != 0) {
                Log.i(TAG, "Comparing " + weighments[i] + " with " + weighments[i - 1]);
                if (String.valueOf(weighments[i]).equals(String.valueOf(weighments[i - 1]))) {
                    stableReadingCounter++;
                } else {
                    stableReadingCounter--;
                }
            } else if (weighments[i] == weighments[i + 1]) {
                stableReadingCounter++;
            }
        }
        Log.i(TAG, "Counter is " + stableReadingCounter);
        if (stableReadingCounter != weighments.length) {
            return false;
        }
        stableReading = true;
        Log.i(TAG, new StringBuilder(String.valueOf(weighments.length)).append(" readings are equal").toString());
        return true;
    }

    private boolean icheckIfAllEqual() {
        int trueCounter = 0;
        for(int i = 0; i<weighments.length; i++){
            for(int j=i + 1; j<weighments.length; j++){
                if(weighments[i] == weighments[j]){
                    //Log.i(TAG,  weighments[i] + " is not equal to " + weighments[j]);
                    trueCounter++;
                }
            }
        }

        if (trueCounter == weighments.length) {
            Log.i(TAG, weighments.length + " readings are equal");
            return true;
        }else {
            Log.i(TAG, "Returning false for obvious reasons");
            return false;
        }
    }

    private boolean  checkIfAboveZero() {
        int myCounter = 0;
        for(int k = 0; k < weighments.length; k++){
            if (weighments[k] <= 0.0) {
                myCounter++;
            }
        }

        if (myCounter != weighments.length) {
            Log.i(TAG, "checkIfAboveZero is false");
            return false;
        } else {		//All readings are less than zero
            Log.i(TAG, "checkIfAboveZero is true");
            return true;
        }
    }

    /*boolean flag = false;
        for (int i = 0;i < weighments.length;i++) {
            if (weighments[0] != weighments[i]) {
                flag = true;
            }
        }
        return flag;*/

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
    public void PrintGenericReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Receipt");
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
        String titleStr	= "\n"+produce+" RECEIPT" + "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        StringBuilder contentSb	= new StringBuilder();


        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        Date ReceiptDate = null;
        Date ReceiptTime = null;


        Cursor session = db.query(Database.SESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);

        if (session.moveToFirst()) {

            SharedPreferences.Editor edit = prefs.edit();

            SessionNo=session.getString(session.getColumnIndex(Database.SessionCounter));

            String[] allColumns = new String[] {Database.F_FARMERNAME,Database.F_PRODUCE_KG_TODATE};
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns,Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                FarmerName=c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                MTDKGS=c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
            }

            RouteCode=session.getString(session.getColumnIndex(Database.SessionRoute));
            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;

            try {
                ReceiptDate = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ColDate=format1.format(ReceiptDate);
            try {
                ReceiptTime = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Time=format2.format(ReceiptTime);

            DataDevice=mSharedPrefs.getString("terminalID", "");
            FarmerNo=session.getString(session.getColumnIndex(Database.SessionFarmerNo));

            //UnitCount=session.getString(session.getColumnIndex(Database.SessionBags));
            //grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare))) +Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
            //GrossTotal=df.format(grossWeight);
            //TareWeight=session.getString(session.getColumnIndex(Database.SessionTare));
            //NetWeight=session.getString(session.getColumnIndex(Database.SessionNet));

            contentSb.append("  \n-----------------------------\n");
            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + ColDate + "\n");
            contentSb.append("  TIME       : " + Time + "\n");
            contentSb.append("  TERMINAL   : " + DataDevice + "\n");
            contentSb.append("  FARMER NO  : " +FarmerNo + "\n");
            contentSb.append("   " + FarmerName + "\n");
            contentSb.append("  ROUTE      : " + RouteCode + "\n");
            contentSb.append("  SHED       : " + ShedCode + "\n");

           // contentSb.append("  -----------------------------\n");
           // contentSb.append("  REF.ID     : KGS  TIME\r\n");
           // contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + FarmerNo + "'" +
                    " and "+ Database.CollDate +" ='" + ColDate + "'" +
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime))+ "'" +
                    " and "+ Database.DataCaptureDevice +"='"+ SessionDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ UnitCount +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                double netweight=0.0;
                double tare=0.0;
                while(account.moveToNext()) {

                    count=count+1;
                    netweight=netweight+Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity)));
                    tare=tare+Double.parseDouble(account.getString(account.getColumnIndex(Database.Tareweight)));

                    BagCount = String.valueOf(count);
                    Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity))));
                    Tare = account.getString(account.getColumnIndex(Database.Tareweight));
                    Time = account.getString(account.getColumnIndex(Database.CaptureTime));
                    LeafQuality= account.getString(account.getColumnIndex(Database.Quality));

                    edit.putString("ReceiptNo", BagCount);
                    edit.commit();
                    edit.putString("Kgs", Kgs);
                    edit.commit();
                    edit.putString("Tare", Tare);
                    edit.commit();
                    edit.putString("Time", Time);
                    edit.commit();
                    //contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                }

                NetWeight=df.format(netweight);
                TareWeight=df.format(tare);
                GrossTotal=df.format(netweight+tare);
                UnitCount=String.valueOf(count);
            }

            account.close();
            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + UnitCount+ "\n");
            contentSb.append("  GROSS WT   : " + GrossTotal + "\n");
            contentSb.append("  TARE WT    : " + TareWeight + "\n");
            contentSb.append("  NET WT     : " + NetWeight + "\n");
            contentSb.append("  MTD KGS    : " + MTDKGS + "\n");
            if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                //contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }else{
                contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }

            contentSb.append("  -----------------------------\n");
            contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
            // contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
            contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
            contentSb.append("\n");
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
    public void PrintGenKiswReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details_kiswa, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("STAKABADTHI YA MAUZO");
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
        String titleStr	= "\nSTAKABADTHI YA MAUZO\n"+produce+ "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        StringBuilder contentSb	= new StringBuilder();


        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        Date ReceiptDate = null;
        Date ReceiptTime = null;


        Cursor session = db.query(Database.SESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);

        if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();
            SessionNo=session.getString(session.getColumnIndex(Database.SessionCounter));
            String[] allColumns = new String[] {Database.F_FARMERNAME,Database.F_PRODUCE_KG_TODATE};
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns,Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                FarmerName=c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                MTDKGS=c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
            }

            RouteCode=session.getString(session.getColumnIndex(Database.SessionRoute));

            Cursor shedname=db.rawQuery("select MccName from CollectionCenters where MccNo= '"+ShedCode+"' ", null);
            if(shedname!=null)
            {
                shedname.moveToFirst();
                ShedName=shedname.getString(shedname.getColumnIndex("MccName"));
            }
            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;

            try {
                ReceiptDate = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ColDate=format1.format(ReceiptDate);
            try {
                ReceiptTime = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Time=format2.format(ReceiptTime);
            DataDevice=mSharedPrefs.getString("terminalID", "");
            FarmerNo=session.getString(session.getColumnIndex(Database.SessionFarmerNo));
            initialPrice = mSharedPrefs.getString("buyingPrice", "").toString();

            //UnitCount=session.getString(session.getColumnIndex(Database.SessionBags));
            //grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare)))+Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
            //GrossTotal=df.format(grossWeight);
            //TareWeight=session.getString(session.getColumnIndex(Database.SessionTare));
            //NetWeight=session.getString(session.getColumnIndex(Database.SessionNet));

            contentSb.append("  \n-----------------------------\n");
            contentSb.append("  NAMBA: " + ReceiptNo + "\n");
            contentSb.append("  TAREHE     : " + ColDate + "\n");
            contentSb.append("  MUDA       : " + Time + "\n");
            contentSb.append("  MZANI      : " + DataDevice + "\n");
            contentSb.append("  MKULIMA    : " +FarmerNo + "\n");
            contentSb.append("   " + FarmerName + "\n");
            contentSb.append("  NJIA       : " + RouteCode + "\n");
            contentSb.append("  KITUO      : " + ShedName + "\n");
            //contentSb.append("  -----------------------------\n");
            //contentSb.append("  REF.ID     : KGS  TIME\r\n");
            //contentSb.append("  -----------------------------\n");


            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + FarmerNo + "'" +
                    " and "+ Database.CollDate +" ='" + ColDate + "'" +
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime))+ "'" +
                    " and "+ Database.DataCaptureDevice +"='"+ SessionDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ UnitCount +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                double netweight=0.0;
                double tare=0.0;
                while(account.moveToNext()) {

                    count=count+1;
                    netweight=netweight+Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity)));
                    tare=tare+Double.parseDouble(account.getString(account.getColumnIndex(Database.Tareweight)));

                    BagCount = String.valueOf(count);
                    Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity))));
                    Tare = account.getString(account.getColumnIndex(Database.Tareweight));
                    LeafQuality= account.getString(account.getColumnIndex(Database.Quality));
                    try {
                        ReceiptTime = dateTimeFormat.parse(account.getString(account.getColumnIndex(Database.CaptureTime)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Time = format2.format(ReceiptTime);


                    edit.putString("ReceiptNo", BagCount);
                    edit.commit();
                    edit.putString("Kgs", Kgs);
                    edit.commit();
                    edit.putString("Tare", Tare);
                    edit.commit();
                    edit.putString("Time", Time);
                    edit.commit();
                    //contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                }

                NetWeight=df.format(netweight);
                TareWeight=df.format(tare);
                GrossTotal=df.format(netweight+tare);
                UnitCount=String.valueOf(count);
            }
            account.close();


            contentSb.append("  -----------------------------\n");

            contentSb.append("  BEI YA AWALI   : " +initialPrice + "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("  MIFUKO         : " + UnitCount+ "\n");
           // contentSb.append("  JUMLA YA KILO  : " + GrossTotal + "\n");
          // contentSb.append("  MFUKO NA UMANDE: " + TareWeight + "\n");
            contentSb.append("  UZITO HALISI   : " + NetWeight + "\n");
            //contentSb.append("  NYONGEZA YA UZITO    : " + MTDKGS + "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("  JINA LA KARANI: " + prefs.getString("fullname", "") + "\n");
            // contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
            //contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
            contentSb.append("\n");
            contentSb.append("\n");
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
        //db.close();
        //dbhelper.close();

    }
    public void PrintDetailedReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Receipt");
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
        String titleStr	= "\n"+produce+" RECEIPT" + "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        StringBuilder contentSb	= new StringBuilder();


        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        Date ReceiptDate = null;
        Date ReceiptTime = null;


        Cursor session = db.query(Database.SESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();

            SessionNo=session.getString(session.getColumnIndex(Database.SessionCounter));

            String[] allColumns = new String[] {Database.F_FARMERNAME,Database.F_PRODUCE_KG_TODATE};
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns,Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                FarmerName=c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                MTDKGS=c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
            }

            RouteCode=session.getString(session.getColumnIndex(Database.SessionRoute));
            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;

            try {
                ReceiptDate = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ColDate=format1.format(ReceiptDate);
            try {
                ReceiptTime = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Time=format2.format(ReceiptTime);

            DataDevice=mSharedPrefs.getString("terminalID", "");
            FarmerNo=session.getString(session.getColumnIndex(Database.SessionFarmerNo));

            //UnitCount=session.getString(session.getColumnIndex(Database.SessionBags));
            //grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare))) +Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
           // GrossTotal=df.format(grossWeight);
           // TareWeight=session.getString(session.getColumnIndex(Database.SessionTare));
           // NetWeight=session.getString(session.getColumnIndex(Database.SessionNet));

            contentSb.append("  \n-----------------------------\n");
            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + ColDate + "\n");
            contentSb.append("  TIME       : " + Time + "\n");
            contentSb.append("  TERMINAL   : " + DataDevice + "\n");
            contentSb.append("  FARMER NO  : " +FarmerNo + "\n");
            contentSb.append("   " + FarmerName + "\n");
            contentSb.append("  ROUTE      : " + RouteCode + "\n");
            contentSb.append("  SHED       : " + ShedCode + "\n");
            contentSb.append("  -----------------------------\n");
            contentSb.append("  REF.ID     : KGS  TIME\r\n");
            contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + FarmerNo + "'" +
                    " and "+ Database.CollDate +" ='" + ColDate + "'" +
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime))+ "'" +
                    " and "+ Database.DataCaptureDevice +"='"+ SessionDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ UnitCount +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                double netweight=0.0;
                double tare=0.0;
                while(account.moveToNext()) {

                    count=count+1;
                    netweight=netweight+Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity)));
                    tare=tare+Double.parseDouble(account.getString(account.getColumnIndex(Database.Tareweight)));

                    BagCount = String.valueOf(count);
                    Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity))));
                    Tare = account.getString(account.getColumnIndex(Database.Tareweight));
                    LeafQuality= account.getString(account.getColumnIndex(Database.Quality));
                    try {
                        ReceiptTime = dateTimeFormat1.parse(account.getString(account.getColumnIndex(Database.CaptureTime)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Time = format2.format(ReceiptTime);

                    edit.putString("ReceiptNo", BagCount);
                    edit.commit();
                    edit.putString("Kgs", Kgs);
                    edit.commit();
                    edit.putString("Tare", Tare);
                    edit.commit();
                    edit.putString("Time", Time);
                    edit.commit();
                    contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                }

                NetWeight=df.format(netweight);
                TareWeight=df.format(tare);
                GrossTotal=df.format(netweight+tare);
                UnitCount=String.valueOf(count);
            }

            account.close();

            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + UnitCount+ "\n");
            contentSb.append("  GROSS WT   : " + GrossTotal + "\n");
            contentSb.append("  TARE WT    : " + TareWeight + "\n");
            contentSb.append("  NET WT     : " + NetWeight + "\n");
            contentSb.append("  MTD KGS    : " + MTDKGS + "\n");
            if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                //contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }else{
                contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }
            contentSb.append("  -----------------------------\n");
            contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
            contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
            contentSb.append("\n");
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
        //db.close();
        //dbhelper.close();

    }

    public void PrintSimpleDetailedReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Receipt");
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
        String titleStr	= "\n"+produce+" RECEIPT" + "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        StringBuilder contentSb	= new StringBuilder();


        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        Date ReceiptDate = null;
        Date ReceiptTime = null;


        Cursor session = db.query(Database.SESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();

            SessionNo=session.getString(session.getColumnIndex(Database.SessionCounter));

            String[] allColumns = new String[] {Database.F_FARMERNAME,Database.F_PRODUCE_KG_TODATE};
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns,Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                FarmerName=c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                MTDKGS=c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
            }

            RouteCode=session.getString(session.getColumnIndex(Database.SessionRoute));
            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;

            try {
                ReceiptDate = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ColDate=format1.format(ReceiptDate);
            try {
                ReceiptTime = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Time=format2.format(ReceiptTime);

            DataDevice=mSharedPrefs.getString("terminalID", "");
            FarmerNo=session.getString(session.getColumnIndex(Database.SessionFarmerNo));

            //UnitCount=session.getString(session.getColumnIndex(Database.SessionBags));
            //grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare))) +Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
            // GrossTotal=df.format(grossWeight);
            // TareWeight=session.getString(session.getColumnIndex(Database.SessionTare));
            // NetWeight=session.getString(session.getColumnIndex(Database.SessionNet));

            contentSb.append("  \n-----------------------------\n");
            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + ColDate + "\n");
            contentSb.append("  TIME       : " + Time + "\n");
            contentSb.append("  TERMINAL   : " + DataDevice + "\n");
            contentSb.append("  FARMER NO  : " +FarmerNo + "\n");
            contentSb.append("   " + FarmerName + "\n");
            contentSb.append("  ROUTE      : " + RouteCode + "\n");
            contentSb.append("  SHED       : " + ShedCode + "\n");
            contentSb.append("  -----------------------------\n");
            contentSb.append("  REF.ID     : KGS  TIME\r\n");
            contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + FarmerNo + "'" +
                    " and "+ Database.CollDate +" ='" + ColDate + "'" +
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime))+ "'" +
                    " and "+ Database.DataCaptureDevice +"='"+ SessionDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ UnitCount +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                double netweight=0.0;
                double tare=0.0;
                while(account.moveToNext()) {

                    count=count+1;
                    netweight=netweight+Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity)));
                    tare=tare+Double.parseDouble(account.getString(account.getColumnIndex(Database.Tareweight)));

                    BagCount = String.valueOf(count);
                    Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity))));
                    Tare = account.getString(account.getColumnIndex(Database.Tareweight));
                    LeafQuality= account.getString(account.getColumnIndex(Database.Quality));
                    try {
                        ReceiptTime = dateTimeFormat1.parse(account.getString(account.getColumnIndex(Database.CaptureTime)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Time = format2.format(ReceiptTime);

                    edit.putString("ReceiptNo", BagCount);
                    edit.commit();
                    edit.putString("Kgs", Kgs);
                    edit.commit();
                    edit.putString("Tare", Tare);
                    edit.commit();
                    edit.putString("Time", Time);
                    edit.commit();
                    contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                }

                NetWeight=df.format(netweight);
                TareWeight=df.format(tare);
                GrossTotal=df.format(netweight+tare);
                UnitCount=String.valueOf(count);
            }

            account.close();

            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + UnitCount+ "\n");
            contentSb.append("  GROSS WT   : " + GrossTotal + "\n");
            contentSb.append("  TARE WT    : " + TareWeight + "\n");
            contentSb.append("  NET WT     : " + NetWeight + "\n");
            //contentSb.append("  MTD KGS    : " + MTDKGS + "\n");
            if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                //contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }else{
                contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }
            contentSb.append("  -----------------------------\n");
            contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
            contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
            contentSb.append("\n");
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
        //db.close();
        //dbhelper.close();

    }
    public void PrintSimpleReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Receipt");
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
        String titleStr	= "\n"+produce+" RECEIPT" + "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        StringBuilder contentSb	= new StringBuilder();

        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        Date ReceiptDate = null;
        Date ReceiptTime = null;


        Cursor session = db.query(Database.SESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);

        if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();
            SessionNo=session.getString(session.getColumnIndex(Database.SessionCounter));
            String[] allColumns = new String[] {Database.F_FARMERNAME,Database.F_PRODUCE_KG_TODATE};
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns,Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                FarmerName=c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                MTDKGS=c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
            }

            RouteCode=session.getString(session.getColumnIndex(Database.SessionRoute));
            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;

            try {
                ReceiptDate = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ColDate=format1.format(ReceiptDate);
            try {
                ReceiptTime = dateTimeFormat1.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Time=format2.format(ReceiptTime);

            DataDevice=mSharedPrefs.getString("terminalID", "");
            FarmerNo=session.getString(session.getColumnIndex(Database.SessionFarmerNo));
            //UnitCount=session.getString(session.getColumnIndex(Database.SessionBags));
            //grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare))) +Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
            //GrossTotal=df.format(grossWeight);
            //TareWeight=session.getString(session.getColumnIndex(Database.SessionTare));
            //NetWeight=session.getString(session.getColumnIndex(Database.SessionNet));

            contentSb.append("  \n-----------------------------\n");
            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + ColDate + "\n");
            contentSb.append("  TIME       : " + Time + "\n");
            contentSb.append("  TERMINAL   : " + DataDevice + "\n");
            contentSb.append("  FARMER NO  : " +FarmerNo + "\n");
            contentSb.append("   " + FarmerName + "\n");
            contentSb.append("  ROUTE      : " + RouteCode + "\n");
            contentSb.append("  SHED       : " + ShedCode + "\n");
            //contentSb.append("  -----------------------------\n");
            //contentSb.append("  REF.ID     : KGS  TIME\r\n");
            //contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + FarmerNo + "'" +
                    " and "+ Database.CollDate +" ='" + ColDate + "'" +
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime))+ "'" +
                    " and "+ Database.DataCaptureDevice +"='"+ SessionDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ UnitCount +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                double netweight=0.0;
                double tare=0.0;

                while(account.moveToNext()) {

                    count=count+1;
                    netweight=netweight+Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity)));
                    tare=tare+Double.parseDouble(account.getString(account.getColumnIndex(Database.Tareweight)));

                    BagCount = String.valueOf(count);
                    Kgs = df.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.Quantity))));
                    Tare = account.getString(account.getColumnIndex(Database.Tareweight));
                    Time = account.getString(account.getColumnIndex(Database.CaptureTime));
                    LeafQuality= account.getString(account.getColumnIndex(Database.Quality));

                    edit.putString("ReceiptNo", BagCount);
                    edit.commit();
                    edit.putString("Kgs", Kgs);
                    edit.commit();
                    edit.putString("Tare", Tare);
                    edit.commit();
                    edit.putString("Time", Time);
                    edit.commit();
                    //contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                }

                NetWeight=df.format(netweight);
                TareWeight=df.format(tare);
                GrossTotal=df.format(netweight+tare);
                UnitCount=String.valueOf(count);
            }

            account.close();




            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + UnitCount+ "\n");
            //contentSb.append("  GROSS WT   : " + GrossTotal + "\n");
            //contentSb.append("  TARE WT    : " + TareWeight + "\n");
            contentSb.append("  NET WT     : " + NetWeight + "\n");
            if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                //contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }else{
                contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }

           // contentSb.append("  MTD KGS    : " + MTDKGS + "\n");

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
    private void Sendsms(String address,String message) {
        // TODO Auto-generated method stub
        SmsManager smsMgr = SmsManager.getDefault();
        smsMgr.sendTextMessage(address, null, message, null, null);

    }
    private class WEBSMS extends AsyncTask<Void, Void, String>
    {    @Override
    protected void onPreExecute()
    {
    }
        @Override
        protected String doInBackground(Void... params)
        {  makePostRequest();
            return "";
        }
        @Override
        protected void onPostExecute(String result)
        {
            if(Integer.parseInt(error_code)<0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText(error_desc);
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
               // Toast.makeText(getApplicationContext(), error_desc, Toast.LENGTH_LONG).show();
                return;
            }else{
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.blue_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText(success_desc);
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getApplicationContext(), success_desc,Toast.LENGTH_LONG).show();
            }

        }
    }
    private void makePostRequest() {
        smsurl="http://messaging.advantasms.com/sendsms.jsp?";
        username=mSharedPrefs.getString("prefUser", "octagon");
        password=mSharedPrefs.getString("prefPass", "octagon");
        senderid=mSharedPrefs.getString("SenderID", "OCTAGON");
        //rFarmerPhone="+254715881439";
        rAccummulated=prefs.getString("textTotalKgs", "");
        textmessage=SessionReceipt +" for "+rFarmerNo+"," + rFarmerName +
                " weighed " + UnitCount +" bags of Net weight: " +NetWeight+ " Kg on " +ColDate+
                ". Month to Date weight: " + rAccummulated + " Kg.";

        String requestUrl="user="+username+"&password="+password+
                "&senderid="+senderid+
                "&mobiles="+rFarmerPhone+
                "&sms="+textmessage;


        try{

            URL url = new URL(smsurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", "" +Integer.toString(requestUrl.getBytes().length));
            connection.setUseCaches (false);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

            wr.writeBytes(requestUrl);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                retval += decodedString;
            }
            in.close();
            System.out.println(retval);
            String sampleXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+retval;
            JSONObject jsonObj = null;

            try {
                jsonObj = XML.toJSONObject(sampleXml);
                success_desc= "Message Sent Successfully To "+jsonObj.getJSONObject("smslist").getJSONObject("sms").getString("mobile-no");
            }
            catch (JSONException e) {
                Log.e("JSON exception", e.getMessage());
                error_code = jsonObj.getJSONObject("smslist").getJSONObject("error").getString("error-code");
                error_desc = jsonObj.getJSONObject("smslist").getJSONObject("error").getString("error-description");
                e.printStackTrace();
            }

            Log.d("XML", sampleXml);

            Log.d("JSON", jsonObj.toString());

            Log.d("VALUE", error_code+ ":"+error_desc );

            connection.disconnect();


        }
        catch(Exception ex)
        {
            System.out.print(ex);
        }
    }


    private String getDate(){

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }


}
