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
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Michael on 30/06/2016.
 */
public class FarmerManualWeighActivity extends AppCompatActivity {
    public Toolbar toolbar;
    Intent mIntent;
    DBHelper dbhelper;
    Button btn_svFarmer;
    ListView listFarmers;
    EditText et_farmerno, et_cardno, et_idno,et_mobileno,et_farmername;
    String s_farmerno, s_cardno, s_idno,s_mobileno,s_farmername, s_managedfarm,s_producetokg;
    Spinner   sp_fshed, sp_managedfarm;
    String accountId;
    TextView textAccountId;
    Boolean success = true;
     static TextView tvMemberName,tvShowMemberNo,tvShowGrossTotal,tvWeighingAccumWeigh,tvWeighingTareWeigh,
             tvUnitsCount,tvShowTotalKgs,tvGross,txtScaleConn,txtPrinterConn;
    static TextView tvsavedReading,tvSavedNet,tvSavedTare,tvSavedUnits,tvSavedTotal;
    EditText etShowGrossTotal;
    Typeface font;
    String sheds,shedid,manageid;
    ArrayList<String> sheddata=new ArrayList<String>();
    ArrayAdapter<String> shedadapter;
    SearchView searchView,searchView1;
    public SimpleCursorAdapter ca;

    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String WEIGH_AND_TARE = "Discrete";
    public static final String FILLING = "Incremental";
    public static final String MANUAL = "Manual";
    private Snackbar snackbar;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;

    int weighmentCounts = 1;
    static double setTareWeight = 0.0;
    public static String rFarmerNo,rFarmerPhone,rAccummulated;
    boolean firstWeighment = false;

    //UUID for creating sessions
    UUID uuid = UUID.randomUUID();
    String weighingSession = "";
    Button btn_accept,btn_next,btn_print;
    LinearLayout lt_accept,lt_nprint;


    String MobileNo,ClerkName,GrossWeight,TotalKGs,Date,TotalKg;
    int recieptNo=1;
    String BatchSerial,Quality,ReferenceNo,ColDate,Time, DataDevice,BatchNo,Agent,FarmerNo;
    String WorkerNo, FieldClerk,ProduceCode;
    String VarietyCode,GradeCode, RouteCode, ShedCode;
    String TransporterCode,GrossTotal,TareWeight,UnitCount;
    String UnitPrice, RecieptNo, CanSerial,NetWeight,Accummulated;
    String newGross,newNet,newTare;
    DecimalFormat formatter;
    String ScaleConn;
    public static AlertDialog weigh;
    public static Button btnReconnect;
    public static ProgressDialog mConnectingDlg;
    public static BluetoothAdapter mBluetoothAdapter;
    public static P25Connector mConnector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        firstWeighment = true; //ensure receipt no is not incremented on first weighment
        weighingSession = uuid.toString();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(FarmerManualWeighActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(FarmerManualWeighActivity.this);
            setContentView(R.layout.activity_listfarmers_weigh);

        if (mSharedPrefs.getString("scaleVersion", "Manual").toString().equals(MANUAL)) {
            Toast.makeText(getBaseContext(), "You are using Manual Scale", Toast.LENGTH_LONG).show();
        }


                //weighmentCounts = mSharedPrefs.getInt("weighmentCounts", 0);
                weighmentCounts=1;


        setupToolbar();
        initializer();
        setupSnackBar();
        if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
            // go back to milkers activity
            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                showUnsupported();
            } else {
                mConnectingDlg = new ProgressDialog(getApplicationContext());

                mConnectingDlg.setMessage("Printer Connecting...");
                mConnectingDlg.setCancelable(false);

                mConnector = new P25Connector(new P25Connector.P25ConnectionListener() {

                    @Override
                    public void onStartConnecting() {
                        //mConnectingDlg.show();
                        Context context=getApplicationContext();
                        LayoutInflater inflater=getLayoutInflater();
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

                        Context context=getApplicationContext();
                        LayoutInflater inflater=getLayoutInflater();
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

               registerReceiver(pReceiver, filter);
            }
        }

    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private  void showUnsupported() {
        showToast("Bluetooth is unsupported by this device");

    }
    private  void showConnected() {
        showToast("Printer Connected");


    }

    private  void showDisonnected() {
        showToast("Printer Disconnected");

    }

    private  void connect() {


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
    private final BroadcastReceiver pReceiver = new BroadcastReceiver() {
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

        txtScaleConn = (TextView) this.findViewById(R.id.txtScaleConn);
        txtScaleConn.setVisibility(View.GONE);
        ScaleConn=txtScaleConn.getText().toString();

        txtPrinterConn = (TextView) this.findViewById(R.id.txtPrinterConn);
        btnReconnect = (Button) this.findViewById(R.id.btnReconnect);
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                connect();
            }
        });
        dbhelper = new DBHelper(getApplicationContext());
        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "LCDM2B__.TTF");
        listFarmers = (ListView) this.findViewById(R.id.lvUsers);
        listFarmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                //first get scale version
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("Gross");
                edit.remove("Net");
                edit.commit();
                showWeighDialog();
                setTareWeight = Double.parseDouble(mSharedPrefs.getString("tareWeight", "0.0"));
                tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));

            }
        });

        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setQueryHint("Search Farmer No ...");
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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


                return true;
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

    public void showWeighDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_weigh, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("------- Weigh Produce -------");
        accountId = textAccountId.getText().toString();

        tvMemberName = (TextView) dialogView.findViewById(R.id.tvMemberNameShow);
        tvShowMemberNo = (TextView) dialogView.findViewById(R.id.tvShowMemberNo);

        tvWeighingAccumWeigh = (TextView) dialogView.findViewById(R.id.tvWeighingAccumWeigh);
        tvWeighingAccumWeigh.setTypeface(font);

        tvWeighingTareWeigh = (TextView) dialogView.findViewById(R.id.tvWeighingTareWeigh);
        tvWeighingTareWeigh.setTypeface(font);
        tvWeighingTareWeigh.setText(String.valueOf(setTareWeight));

        tvShowTotalKgs = (TextView) dialogView.findViewById(R.id.tvShowTotalKgs);
        tvShowTotalKgs.setTypeface(font);



        //tvShowTotalKgs.setText(prefs.getString("tvNetWeight", ""));
        tvUnitsCount = (TextView) dialogView.findViewById(R.id.tvUnitsCount);
        tvUnitsCount.setTypeface(font);

        //weighmentCounts = mSharedPrefs.getInt("weighmentCounts", 0);
       // weighmentCounts =Integer.parseInt( tvUnitsCount.getText().toString())+weighmentCounts;
        tvUnitsCount.setText(String.valueOf(weighmentCounts));

        tvShowGrossTotal = (TextView) dialogView.findViewById(R.id.tvShowGrossTotal);
        tvShowGrossTotal.setTypeface(font);
        // tvShowGrossTotal.setText(prefs.getString("tvGross", ""));

        etShowGrossTotal = (EditText) dialogView.findViewById(R.id.etShowGrossTotal);
        etShowGrossTotal.setTypeface(font);
        lt_accept = (LinearLayout) dialogView.findViewById(R.id.lt_accept);
        lt_nprint = (LinearLayout) dialogView.findViewById(R.id.lt_nprint);

        tvsavedReading = (TextView) dialogView.findViewById(R.id.tvvGross);
        tvSavedNet = (TextView) dialogView.findViewById(R.id.tvvTotalKgs);
        tvSavedTare = (TextView) dialogView.findViewById(R.id.tvTareWeight);
        tvSavedUnits = (TextView) dialogView.findViewById(R.id.tvvcount);
        tvSavedTotal = (TextView) dialogView.findViewById(R.id.tvAccumWeight);

        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("etGross", etShowGrossTotal.getText().toString());
        edit.commit();

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
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etShowGrossTotal.getText().length()==0) {
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Enter Gross Reading");
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
                dialogBuilder.setMessage("Are you sure you want to accept the gross reading?");
                tvGross = (TextView) dialogView.findViewById(R.id.txtGross);
                tvGross.setTypeface(font);
                if(etShowGrossTotal.getText().length()==0) {

                    tvGross.setText("0 KG");

                }
                else{
                    tvGross.setText(tvShowTotalKgs.getText().toString() +" KG");



                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("Gross", etShowGrossTotal.getText().toString());
                    edit.commit();
                    edit.putString("Net", tvShowTotalKgs.getText().toString());
                    edit.commit();
                    edit.putString("Accum", tvWeighingAccumWeigh.getText().toString());
                    edit.commit();
                }


                // Setting Positive "Yes" Button
                dialogBuilder.setNegativeButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dbhelper = new DBHelper(getApplicationContext());
                                SQLiteDatabase db = dbhelper.getReadableDatabase();
                                formatter = new DecimalFormat("000000");
                                DataDevice = mSharedPrefs.getString("terminalID", "").toString();
                                BatchNo=prefs.getString("BatchNumber", "");
                                Calendar cal = Calendar.getInstance();
                                Date date = new Date(getDate());
                                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                                SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                                ColDate = format1.format(date);
                                Time= format2.format(cal.getTime());
                                FarmerNo=tvShowMemberNo.getText().toString();
                                WorkerNo="";
                                Agent="";
                                FieldClerk=prefs.getString("user", "");
                                ProduceCode=prefs.getString("produceCode", "");
                                VarietyCode=prefs.getString("varietyCode", "");
                                GradeCode=prefs.getString("gradeCode", "");
                                UnitPrice=prefs.getString("unitPrice", "");

                                RouteCode=prefs.getString("routeCode", "");
                                ShedCode=prefs.getString("shedCode", "");
                                CanSerial = prefs.getString("canSerial", " ");
                                TareWeight= tvWeighingTareWeigh.getText().toString();
                                Accummulated=prefs.getString("Accum", "");
                                UnitCount=tvUnitsCount.getText().toString();
                                newNet=prefs.getString("Net", "");
                                newGross=prefs.getString("Gross", "");
                                final DecimalFormat df = new DecimalFormat("#0.0#");
                                Double Accum=0.0,NewAccum=0.0;
                                Accum = Double.parseDouble(newNet)+Double.parseDouble(Accummulated);
                                NewAccum=Accum+Double.parseDouble(rAccummulated);
                                Cursor count = db.rawQuery("select * from FarmersProduceCollection", null);
                                if (count.getCount() > 0) {
                                    Cursor c = db.rawQuery("select MAX(ReceiptNo) from FarmersProduceCollection", null);
                                    if (c != null) {

                                        c.moveToFirst();

                                        recieptNo = Integer.parseInt(c.getString(0)) + 1;
                                        RecieptNo = formatter.format(recieptNo);

                                    }
                                    c.close();
                                } else {
                                    RecieptNo = formatter.format(recieptNo);

                                }
                                Double Gross=0.0, Net=0.0,Tare=0.0;
                                Net = Accum;
                                Tare = Double.parseDouble(tvUnitsCount.getText().toString())*Double.parseDouble(tvWeighingTareWeigh.getText().toString());
                                Gross = Net+Tare;
                                GrossTotal=df.format(Gross);
                                NetWeight=df.format(Net);
                                newTare=df.format(Tare);
                                BatchSerial=DataDevice;
                                if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                                    Quality="0.0";
                                    return;
                                }else{
                                    Quality=prefs.getString("Quality", "0.0");;
                                }

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
                                edit.putString("textRoute", RouteCode);
                                edit.commit();
                                edit.putString("textShed", ShedCode);
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
                                edit.putString("textTotalKgs", df.format(NewAccum));
                                edit.commit();

                                ContentValues values = new ContentValues();
                                values.put(Database.F_PRODUCE_KG_TODATE, df.format(NewAccum));
                                long rows = db.update(Database.FARMERS_TABLE_NAME, values,
                                        "FFarmerNo = ?", new String[] { FarmerNo });

                                db.close();
                                if (rows > 0){
                                    // Toast.makeText(getApplicationContext(), "Updated Total KGs Successfully!",Toast.LENGTH_LONG).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Sorry! Could not update Total KGs!",
                                            Toast.LENGTH_LONG).show();}

                                dbhelper.AddFarmerTrans(ColDate, Time, DataDevice, BatchNo, Agent, FarmerNo,
                                        WorkerNo, FieldClerk, ProduceCode,
                                        VarietyCode, GradeCode, RouteCode, ShedCode,
                                         newNet, TareWeight, UnitCount,
                                        UnitPrice, RecieptNo,ReferenceNo,BatchSerial,Quality,CanSerial
                                );
                                //customtoast.show();

                                etShowGrossTotal.setEnabled(false);

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
                // Setting Negative "NO" Button
                dialogBuilder.setPositiveButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
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
               /* AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                // Setting Dialog Title
                dialogBuilder.setTitle("Next Reading?");
                // Setting Dialog Message
                dialogBuilder.setMessage("Are you sure you want to take the next reading?");

                // Setting Positive "Yes" Button
                dialogBuilder.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {*/
                                etShowGrossTotal.setEnabled(true);
                                etShowGrossTotal.setText("");
                                etShowGrossTotal.setHint("0.0");
                                lt_accept.setVisibility(View.VISIBLE);
                                lt_nprint.setVisibility(View.GONE);
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
                           /* }
                        });
                // Setting Negative "NO" Button
                dialogBuilder.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
                                dialog.cancel();
                            }
                        });
                // Showing Alert Message
                dialogBuilder.show();*/

            }
        });

        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //afterWeighigAlertDialog.dismiss();
                if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                    // go back to milkers activity
                    Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {

                    if(mSharedPrefs.getString("scaleVersion", "").toString().equals(MANUAL)){
                        //showPrintDialog();
                        //showPrintDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                        builder.setMessage(Html.fromHtml("<font color='#4285F4'>Are you sure you want to print receipt ?</font>"))
                                .setCancelable(false)
                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        printSingleReceipt();
                                        dialog.cancel();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                                        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Reprint Receipt?</font>"))
                                                .setCancelable(false)
                                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        printSingleReceipt();
                                                    }
                                                })
                                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        weigh.dismiss();
                                                        weighmentCounts = 1;

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
                                                printSingleReceipt();
                                                Boolean wantToCloseDialog = false;
                                                //Do stuff, possibly set wantToCloseDialog to true then...
                                                if (wantToCloseDialog)
                                                    alert2.dismiss();
                                                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                                            }
                                        });
                                    }
                                })
                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert1 = builder.create();
                        alert1.show();
                        alert1.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                        alert1.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
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
            //tvWeighingAccumWeigh.setText(account.getString(account.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));

            rFarmerNo=account.getString(account.getColumnIndex(Database.F_FARMERNO));
            rFarmerPhone=account.getString(account.getColumnIndex(Database.F_MOBILENUMBER));
            rAccummulated=account.getString(account.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
           // SharedPreferences.Editor edit = prefs.edit();
            edit.putString("rFarmerNo", rFarmerNo);
            edit.commit();

            edit.putString("rFarmerPhone", rFarmerPhone);
            edit.commit();





        }
        account.close();
        db.close();
        dbhelper.close();



        dialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass

                weighmentCounts = 1;


            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                weighmentCounts = 1;
                dialog.dismiss();

            }
        });
        weigh = dialogBuilder.create();
        weigh.show();
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
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
            // go back to milkers activity
            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
            if (mConnector != null) {
                txtPrinterConn.setVisibility(View.VISIBLE);

            }
        }
    }



    @Override
    public void onBackPressed() {
        //do nothing
        finish();
       // resetConn.stop();
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
            // go back to milkers activity
            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
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
        super.onPause();

    }
    @Override
    public void onDestroy() {
        if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
            // go back to milkers activity
            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
            unregisterReceiver(pReceiver);
        }
        super.onDestroy();
    }
    private void printSingleReceipt() {
        long milis		= System.currentTimeMillis();
        String date		= DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
        String time		= DateUtil.timeMilisToString(milis, "hh:mm a");
        String titleStr	= "\n*** RECEIPT SLIP ***" + "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        StringBuilder contentSb	= new StringBuilder();


        contentSb.append("  \n-----------------------------\n");

        contentSb.append("  RECEIPT    : " + prefs.getString("textReciept", "")+ "\n");
        contentSb.append("  DATE       : " + prefs.getString("textTransDate", "") + "\n");
        contentSb.append("  TIME       : " + prefs.getString("textTransTime", "") + "\n");
        contentSb.append("  TERMINAL   : " + prefs.getString("textTerminal", "") + "\n");
        contentSb.append("  FARMER NO  : " + prefs.getString("textFarmerNo", "") + "\n");
        contentSb.append("  NAME       :\n  " + prefs.getString("textName", "") + "\n");
        contentSb.append("  ROUTE      : " + prefs.getString("textRoute", "") + "\n");
        contentSb.append("  SHED       : " + prefs.getString("textShed", "") + "\n");
        //contentSb.append("  TRIP       : " + prefs.getString("textTrip", "") + "\n");
        contentSb.append("  -----------------------------\n");
        //contentSb.append("  CAN        : " + msg.getData().getString(FarmerScaleWeighActivity.CAN) + "\r\n");
        contentSb.append("  BAGS       : " + prefs.getString("textBags", "")+ "\n");
        contentSb.append("  GROSS WT   : " + prefs.getString("textGrossWt", "") + "\n");
        contentSb.append("  TARE WT    : " + prefs.getString("textTareWt", "") + "\n");
        contentSb.append("  NET WT     : " + prefs.getString("textNetWt", "") + "\n");
        contentSb.append("  MTD KGS    : " + prefs.getString("textTotalKgs", "") + "\n");
        //contentSb.append("  UNIT PRICE : " + prefs.getString("mDevice", "") + "\r\n");
        //contentSb.append("  AMOUNT     : " + prefs.getString("mDevice", "") + "\r\n");
        contentSb.append("  -----------------------------\n");
        contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
        // contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
        contentSb.append("  "+Util.nameLeftValueRightJustify("www.octagon.co.ke ", " 25471855111", DataConstants.RECEIPT_WIDTH) + "\n");
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
    public void SMS() {
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

    }
    private String getDate(){

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }
}
