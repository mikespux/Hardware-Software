package com.easyweigh.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.activities.AgentBatchRecieptsActivity;
import com.easyweigh.activities.AgentDeliveryEditActivity;
import com.easyweigh.activities.BatchRecieptsActivity;
import com.easyweigh.activities.DeliveryEditActivity;
import com.easyweigh.activities.ExportAllActivity;
import com.easyweigh.activities.ExportAllAgentActivity;
import com.easyweigh.activities.UploadActivity;
import com.easyweigh.activities.UploadAgentActivity;
import com.easyweigh.activities.UploadAllActivity;
import com.easyweigh.connector.P25ConnectionException;
import com.easyweigh.connector.P25Connector;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.helpers.CustomList;
import com.easyweigh.helpers.Delivary;
import com.easyweigh.printerdata.PocketPos;
import com.easyweigh.printerutils.DataConstants;
import com.easyweigh.printerutils.DateUtil;
import com.easyweigh.printerutils.FontDefine;
import com.easyweigh.printerutils.Printer;
import com.easyweigh.printerutils.Util;
import com.easyweigh.services.WeighingService;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;


public class DelivaryFragment extends Fragment {

    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;
    private TextView dateDisplay,txtCompanyInfo,dtpBatchOn,textClock,txtBatchNo;
    Button btnDispatch,btnPrint,btnComplete;

    static SharedPreferences mSharedPrefs,prefs;
    DBHelper dbhelper;
    SQLiteDatabase db;
    private CircleProgress circle_progress;
    private DonutProgress donutProgress;
    private Timer timer;
    TextView txtUndelivered;
    private int progress = 0;
    Button btnCloseBatch;
    String stTicketNo,stGroswt,stTarewt,stNet,stRejectwt,stQuality;
    String ClosingTime,NoOfWeighments,TotalWeights,Factory,TransporterCode,strTractor,strTrailer,SignedOff,SignedOffTime
            ,BatchSession,BatchCount,Dispatched,FactoryCode;
    String BatchOn,DNumber;
    Spinner spinnerFactory;
    EditText Trailer,Tractor,etDeliveryNo,etTicketNo,etGroswt,etTarewt,etNet,etRejectwt,etQuality;
    int BatchNo=1;
    int maxBatch;
    DecimalFormat formatter;

    String factorys;
    String factoryid=null;
    ArrayList<String> factorydata=new ArrayList<String>();
    ArrayAdapter<String> factoryadapter;

    String transporters;
    String transporterid=null;
    ArrayList<String> transporterdata=new ArrayList<String>();
    ArrayAdapter<String> transporteradapter;
    Spinner mc_ctransporter;
    AlertDialog b;
    WeighingService resetConn;
    ListView listReciepts;
    String Signedoff="1";
    int CLOSED = 1;
    int SIGNEDOFF=0;
    ListView list;
    String DeliveryNo;
    int dcount=1;
    String TicketNo;
    int tcount=1;
    String[]  web     = {
            "View Batches","Upload","Export Data","View Deliveries"
    };
    Integer[] imageId = {

            R.drawable.ic_list,
            R.drawable.ic_upload,
            R.drawable.ic_csv,
            R.drawable.ic_delivary
    };
    String DNoteNo,DelDate,Date,Transporter,Vehicle,ArrivalTime,FieldWt,DepartureTime;
    private ProgressDialog mConnectingDlg;
    private ProgressDialog mProgressDlg;
    private BluetoothAdapter mBluetoothAdapter;
    private P25Connector mConnector;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    String fromProd;
    Cursor accounts;
    DeliveryArrayAdapter ArrayAdapter;
    AlertDialog IncompleteDel;
    Double netweight;
    int accesslevel = 0;
    String user_level;
    Delivary student;
    String DelNoteNo;
    String deviceID;
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_deliver, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        initializer();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);

        return mView;
    }

    public void initializer() {
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        resetConn=new WeighingService();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        btnDispatch = (Button) mView.findViewById(R.id.btnDispatch);
        btnPrint =(Button)mView.findViewById(R.id.btnPrint);
        btnComplete =(Button)mView.findViewById(R.id.btnComplete);
        circle_progress = (CircleProgress)mView.findViewById(R.id.circle_progress);
        donutProgress = (DonutProgress) mView.findViewById(R.id.donut_progress);
        txtUndelivered=(TextView)mView.findViewById(R.id.txtUndelivered);
        txtUndelivered.setText("All Batches and Deliveries Completed");
        timer = new Timer();
        /*timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //circle_progress.setProgress(circle_progress.getProgress() + 1);
                        donutProgress.setProgress(donutProgress.getProgress() + 1);
                       // timer.cancel();
                    }
                });
            }
        }, 500, 100);*/


        CustomList adapter = new
                CustomList(getActivity(), web, imageId);
        list = (ListView) mView.findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                if (position == 1) {
                    //Intent myIntent = new Intent(Ali10Activity.this, Hassan.class);
                    //startActivity(myIntent);
                }
              fromProd = prefs.getString("FromProd", "");

                switch (position) {

                    case 0:

                        if(fromProd.equals("Agents")){
                            mIntent = new Intent(getActivity(), AgentBatchRecieptsActivity.class);
                            startActivity(mIntent);
                        }else
                        {
                            mIntent = new Intent(getActivity(), BatchRecieptsActivity.class);
                            startActivity(mIntent);
                        }


                        break;
                    case 1:
                        if (!checkList()) {
                            return;
                        }
                        if (!isInternetOn()) {
                            createNetErrorDialog();
                            return;
                        }
                        if(fromProd.equals("Agents")){
                            mIntent = new Intent(getActivity(), UploadAgentActivity.class);
                            startActivity(mIntent);
                    }else
                    { String selectQuery = "SELECT * FROM " + Database.Fmr_FactoryDeliveries + " WHERE FdStatus=0";
                        Cursor cursor = db.rawQuery(selectQuery, null);

                        if (cursor.moveToFirst()) {
                            Context context = getActivity();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                            text.setText("Complete Current Delivery First!");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            return;
                        }


                        mIntent = new Intent(getActivity(), UploadAllActivity.class);
                        startActivity(mIntent);
                    }
                        break;
                    case 2:
                        if(fromProd.equals("Agents")){
                            mIntent = new Intent(getActivity(), ExportAllAgentActivity.class);
                            startActivity(mIntent);
                        }else
                        {
                            mIntent = new Intent(getActivity(), ExportAllActivity.class);
                            startActivity(mIntent);
                        }
                        break;
                    case 3:
                        String username = prefs.getString("user", "");
                        Cursor d = dbhelper.getAccessLevel(username);
                        user_level = d.getString(accesslevel);
                        if(user_level.equals("2")){
                            Toast.makeText(getActivity(), "Please Contact Administrator To View Deliveries!", Toast.LENGTH_LONG).show();
                        }else{

                            if(fromProd.equals("Agents")){
                                mIntent = new Intent(getActivity(), AgentDeliveryEditActivity.class);
                                startActivity(mIntent);
                            }else
                            {
                                mIntent = new Intent(getActivity(), DeliveryEditActivity.class);
                                startActivity(mIntent);
                            }

                            break;
                        }



                    default:
                        break;
                }


            }
        });

       btnPrint.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                   // go back to milkers activity
                   Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                   return;
               } else {
                   if (prefs.getString("mDevice", "").toString().equals("")) {
                       // snackbar.show();
                       Context context = getActivity();
                       LayoutInflater inflater = getActivity().getLayoutInflater();
                       View customToastroot = inflater.inflate(R.layout.red_toast, null);
                       TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                       text.setText("Please Pair Printer...");
                       Toast customtoast = new Toast(context);
                       customtoast.setView(customToastroot);
                       customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                       customtoast.setDuration(Toast.LENGTH_LONG);
                       customtoast.show();
                       //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                       return;
                   }


                   mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                   if (mBluetoothAdapter == null) {
                       showUnsupported();
                   } else {
                       if (!mBluetoothAdapter.isEnabled()) {
                           showToast("Bluetooth Disabled");
                       } else {
                           //showToast("Bluetooth Enabled");

                           Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                           if (pairedDevices != null) {
                               mDeviceList.addAll(pairedDevices);


                           }
                       }


                       mProgressDlg = new ProgressDialog(getActivity());

                       mProgressDlg.setMessage("Scanning...");
                       mProgressDlg.setCancelable(false);
                       mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               dialog.dismiss();

                               mBluetoothAdapter.cancelDiscovery();
                           }
                       });

                       mConnectingDlg = new ProgressDialog(getActivity());

                       mConnectingDlg.setMessage("Connecting...");
                       mConnectingDlg.setCancelable(false);


                       mConnector = new P25Connector(new P25Connector.P25ConnectionListener() {

                           @Override
                           public void onStartConnecting() {
                               // mConnectingDlg.show();
                               Toast.makeText(getActivity(), "Connecting Printer ...", Toast.LENGTH_LONG).show();
                           }

                           @Override
                           public void onConnectionSuccess() {
                               //mConnectingDlg.dismiss();

                               showConnected();

                               String fromProd = prefs.getString("FromProd", "");
                               if(fromProd.equals("Agents")){
                                   printDelivaryAgent();
                               }else
                               {
                                   printDelivary();
                               }


                           }


                           @Override
                           public void onConnectionFailed(String error) {
                               //mConnectingDlg.dismiss();
                               Toast.makeText(getActivity(), "Printer Connection Failed ...", Toast.LENGTH_LONG).show();
                           }

                           @Override
                           public void onConnectionCancelled() {

                               //mConnectingDlg.dismiss();
                               Toast.makeText(getActivity(), "Printer Connection Failed ...", Toast.LENGTH_LONG).show();

                           }

                           @Override
                           public void onDisconnected() {

                               showDisconnected();

                           }
                       });
                       connect();

                   }


                   IntentFilter filter = new IntentFilter();

                   filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                   filter.addAction(BluetoothDevice.ACTION_FOUND);
                   filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                   filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                   filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

                   getActivity().registerReceiver(mReceiver, filter);



               }


           }
       });
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        } catch (P25ConnectionException e) {
                            e.printStackTrace();
                        }

                        //getActivity().unregisterReceiver(mReceiver);
                    }


                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_list_incomplete_delivery, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setCancelable(true);
                dialogBuilder.setTitle("Incomplete Delivery Details");

                listReciepts= (ListView) dialogView.findViewById(R.id.lvReciepts);
                getdata();
                dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString();

                    }
                });

                dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            //Toast.makeText(getActivity(), "Please Close Batch", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        return false;
                    }
                });

                IncompleteDel = dialogBuilder.create();
                IncompleteDel.show();
               // CompleteDelivery();

            }
        });
        btnDispatch.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                //pass
               /* String DelNo = "1";
                ContentValues values = new ContentValues();
                values.put(Database.BatCloudID, 0);
                values.put(Database.SignedOff, 0);
                long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                        Database.SignedOff + " = ?", new String[]{DelNo});

                if (rows > 0) {


                }*/
                String fromProd = prefs.getString("FromProd", "");
                if(fromProd.equals("Agents")){
                    String CLOSED = "1";
                    String SIGNEDOFF = "0";
                    Cursor count = db.rawQuery("select * from " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                            + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                    if (count.getCount() == 0) {
                        /*ContentValues values = new ContentValues();
                        values.put(Database.BatCloudID, 0);
                        long rows = db.update(Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                Database.BatchDate + " = ?", new String[]{"2017-02-23"});

                        if (rows > 0) {

                        }*/

                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Sorry! No Batches To Deliver!");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }
                    DispatchAgent();
                }
                else
                {
                    String CLOSED = "1";
                    String SIGNEDOFF = "0";
                    String OPEN="0";
                    Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                            + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                    if (count.getCount() == 0) {

                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Sorry! No Batches To Deliver!");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }
                    Cursor open = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                            + Database.Closed + " ='" + OPEN+ "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);
                    if (open.getCount() == 1) {

                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Sorry! Close Opened Batch first to Deliver");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }
                    Dispatch();
               }






        }
        });
        String fromProd = prefs.getString("FromProd", "");
        if(fromProd.equals("Agents")){

            Cursor batches = db.rawQuery("select * from " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

            if (batches.getCount() > 0){

                btnDispatch.setVisibility(View.VISIBLE);
                btnPrint.setVisibility(View.GONE);
                btnComplete.setVisibility(View.GONE);
                return;

            }
            String selectQuery = "SELECT * FROM " + Database.Agt_FactoryDeliveries + " WHERE FdStatus=0";
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {

                btnDispatch.setVisibility(View.GONE);
                btnPrint.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.VISIBLE);

            }


        }else
        {

            Cursor batches = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

            if (batches.getCount() > 0){

                btnDispatch.setVisibility(View.VISIBLE);
                btnPrint.setVisibility(View.GONE);
                btnComplete.setVisibility(View.GONE);
                return;

            }
            String selectQuery = "SELECT * FROM " + Database.Fmr_FactoryDeliveries + " WHERE FdStatus=0";
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {

                btnDispatch.setVisibility(View.GONE);
                btnPrint.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.VISIBLE);

            }

        }


    }

 public void Dispatch(){

     //new LogOut().execute();
     AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
     LayoutInflater inflater = getActivity().getLayoutInflater();
     final View dialogView = inflater.inflate(R.layout.dialog_dispatch_batch, null);
     dialogBuilder.setView(dialogView);
     dialogBuilder.setCancelable(false);
     dialogBuilder.setTitle("Batch Details");
     spinnerFactory = (Spinner) dialogView.findViewById(R.id.spinnerFactory);
     mc_ctransporter = (Spinner) dialogView.findViewById(R.id.mc_ctransporter);
     FactoryList();
     TransporterList();
     Trailer = (EditText) dialogView.findViewById(R.id.etTrailer);
     Tractor = (EditText) dialogView.findViewById(R.id.etTractor);
     etDeliveryNo = (EditText) dialogView.findViewById(R.id.etDeliveryNo);

     if (mSharedPrefs.getString("Language", "Kiswa").toString().equals("Kiswa"))
     {
         if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {

             etDeliveryNo.setEnabled(false);
             formatter = new DecimalFormat("0000");

             DeliveryNo=prefs.getString("dcount", null);
             if (DeliveryNo != null) {

                 dcount = Integer.parseInt(prefs.getString("dcount", "")) + 1;
                 DeliveryNo = formatter.format(dcount);

             }
             else{

                 DeliveryNo = formatter.format(dcount);
             }
             Date date = new Date(getDate());
             SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
             String dateDel = format.format(date);
             deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
             etDeliveryNo.setText(dateDel+deviceID+"D"+DeliveryNo);
         }
     }
     if(!mSharedPrefs.getBoolean("enableAutomaticDel", false)==true) {
         // go back to milkers activity
         //Toast.makeText(getActivity(), "Auto generated delivery not enabled", Toast.LENGTH_LONG).show();
         //return;
     } else {
         etDeliveryNo.setEnabled(false);
         formatter = new DecimalFormat("0000");

         DeliveryNo=prefs.getString("dcount", null);
         if (DeliveryNo != null) {

             dcount = Integer.parseInt(prefs.getString("dcount", "")) + 1;
             DeliveryNo = formatter.format(dcount);

         }
         else{

             DeliveryNo = formatter.format(dcount);
         }
         Date date = new Date(getDate());
         SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
         String dateDel = format.format(date);
         deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
         etDeliveryNo.setText(dateDel+deviceID+"D"+DeliveryNo);
     }

     btnCloseBatch = (Button) dialogView.findViewById(R.id.btnCloseBatch);
     btnCloseBatch.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

             if (Tractor.length()>7) {
                 Tractor.setError("Enter a Valid Number Plate");
                 return;
             }
             if (Trailer.length()>7) {
                 Trailer.setError("Enter a Valid Number Plate");
                 return;
             }
             if (etDeliveryNo.length()<=0) {
                 etDeliveryNo.setError("Enter Delivery No");
                 return;
             }

             Cursor checkDelNo =dbhelper.CheckDelNo(etDeliveryNo.getText().toString());
             //Check for duplicate id number
             if (checkDelNo.getCount() > 0) {
                 Context context = getActivity();
                 LayoutInflater inflater = getActivity().getLayoutInflater();
                 View customToastroot = inflater.inflate(R.layout.red_toast, null);
                 TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                 text.setText("Delivery Number Exists, type a new one");
                 Toast customtoast = new Toast(context);
                 customtoast.setView(customToastroot);
                 customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                 customtoast.setDuration(Toast.LENGTH_LONG);
                 customtoast.show();
                 return;
             }
             AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
             builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Deliver Batches?</font>"))
                     .setCancelable(false)
                     .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {

                             String CLOSED = "1";
                             String SIGNEDOFF = "0";
                             Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                                     + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                             if (count.getCount() > 0) {

                                 try {
                                     count.moveToFirst();
                                     Date openTime= dateTimeFormat.parse(count.getString(count.getColumnIndex(Database.BatchDate)).toString() + " 00:00:00");
                                     DelDate= dateFormat.format(openTime);
                                 } catch (ParseException e) {
                                     e.printStackTrace();
                                 }
                                 count.close();

                                 Factory = factoryid;
                                 TransporterCode = transporterid;
                                 strTractor = Tractor.getText().toString();
                                 strTractor = strTractor.replace(",","");
                                 strTrailer = Trailer.getText().toString();
                                 strTractor = strTractor.replace(",","");


                                 DNoteNo = etDeliveryNo.getText().toString();
                                 SharedPreferences.Editor edit = prefs.edit();
                                 edit.putString("DNoteNo", DNoteNo);
                                 edit.putString("dcount", DeliveryNo);
                                 edit.commit();
                                 Transporter = transporterid;
                                 Vehicle = Tractor.getText().toString();
                                 Vehicle = Vehicle.replace(",","");
                                 Date date = new Date(getDate());
                                 Calendar cal = Calendar.getInstance();
                                 SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                 SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                                 Date = format.format(date);
                                 ArrivalTime = Date+" "+format2.format(cal.getTime());



                                 final DecimalFormat df = new DecimalFormat("#0.0#");
                                 Cursor c = db.rawQuery("select " +
                                         "" + Database.DataDevice +
                                         ",COUNT(" + Database.ROW_ID + ")" +
                                         ",SUM(" + Database.TotalWeights + ")" +
                                         " from FarmersSuppliesConsignments WHERE "
                                         + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);
                                 if (c != null) {

                                     c.moveToFirst();
                                     FieldWt = df.format(c.getDouble(2));

                                 }
                                 c.close();

                                 ContentValues values = new ContentValues();
                                 values.put(Database.SignedOff, 1);
                                 values.put(Database.DelivaryNO, DNoteNo);
                                 values.put(Database.Factory, Factory);
                                 values.put(Database.Transporter, TransporterCode);
                                 values.put(Database.Tractor, strTractor);
                                 values.put(Database.Trailer, strTrailer);


                                 long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                         Database.SignedOff + " = ?", new String[]{SIGNEDOFF});
                                 dbhelper.AddDelivery(DNoteNo, DelDate, Factory, Transporter, Vehicle, ArrivalTime, FieldWt);
                                 if (rows > 0) {

                                     b.dismiss();
                                     donutProgress.setVisibility(View.VISIBLE);
                                     circle_progress.setVisibility(View.GONE);
                                     new CountDownTimer(1000, 100) {
                                         @Override
                                         public void onTick(long millisUntilFinished) {
                                             //this will be done every 1000 milliseconds ( 1 seconds )
                                             progress = (int) ((1000 - millisUntilFinished) / 5);
                                             donutProgress.setProgress(progress);
                                             txtUndelivered.setText("Delivering Batches ...");
                                             btnDispatch.setVisibility(View.GONE);
                                             btnPrint.setVisibility(View.VISIBLE);
                                             btnComplete.setVisibility(View.VISIBLE);
                                         }

                                         @Override
                                         public void onFinish() {
                                             //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                                             // pd.dismiss();
                                             donutProgress.setVisibility(View.GONE);
                                             circle_progress.setVisibility(View.VISIBLE);

                                             circle_progress.setProgress(0);
                                             circle_progress.setPrefixText("N");
                                             circle_progress.setSuffixText(" Batch");
                                             txtUndelivered.setText("All batches delivered");


                                         }

                                     }.start();
                                     //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
                                 } else {
                                     Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                                 }
                                                   /* getActivity().finish();
                                                    mIntent = new Intent(getActivity(), MainActivity.class);
                                                    startActivity(mIntent);*/


                             } else {
                                 Context context = getActivity();
                                 LayoutInflater inflater = getActivity().getLayoutInflater();
                                 View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                 TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                 text.setText("Sorry! No Batches To Deliver!");
                                 Toast customtoast = new Toast(context);
                                 customtoast.setView(customToastroot);
                                 customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                 customtoast.setDuration(Toast.LENGTH_LONG);
                                 customtoast.show();

                             }

                         }
                     })
                     .setPositiveButton("No", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             dialog.cancel();


                         }
                     });
             final AlertDialog alert2 = builder.create();
             alert2.show();


         }
     });

     dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int whichButton) {
             //do something with edt.getText().toString();

         }
     });

     dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
         @Override
         public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
             if (keyCode == KeyEvent.KEYCODE_BACK) {
                 //Toast.makeText(getActivity(), "Please Close Batch", Toast.LENGTH_LONG).show();
                 return true;
             }
             return false;
         }
     });

     b = dialogBuilder.create();
     b.show();


 }
    public void DispatchAgent(){

        //new LogOut().execute();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_dispatch_batch, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Batch Details");
        spinnerFactory = (Spinner) dialogView.findViewById(R.id.spinnerFactory);
        mc_ctransporter = (Spinner) dialogView.findViewById(R.id.mc_ctransporter);
        FactoryList();
        TransporterList();
        Trailer = (EditText) dialogView.findViewById(R.id.etTrailer);
        Tractor = (EditText) dialogView.findViewById(R.id.etTractor);
        etDeliveryNo = (EditText) dialogView.findViewById(R.id.etDeliveryNo);



        btnCloseBatch = (Button) dialogView.findViewById(R.id.btnCloseBatch);
        btnCloseBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Tractor.length()>7) {
                    Tractor.setError("Enter a Valid Number Plate");
                    return;
                }
                if (etDeliveryNo.length()<=0) {
                    etDeliveryNo.setError("Enter Delivery No");
                    return;
                }
                Cursor checkDelNo =dbhelper.CheckDelNoAgt(etDeliveryNo.getText().toString());
                //Check for duplicate id number
                if (checkDelNo.getCount() > 0) {
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Delivery Number Exists, type a new one");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Deliver Batches?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String CLOSED = "1";
                                String SIGNEDOFF = "0";
                                Cursor count = db.rawQuery("select * from " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                                        + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                                if (count.getCount() > 0) {


                                    try {
                                        count.moveToFirst();
                                        Date openTime= dateTimeFormat.parse(count.getString(count.getColumnIndex(Database.BatchDate)).toString() + " 00:00:00");
                                        DelDate= dateFormat.format(openTime);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    count.close();

                                    Factory = factoryid;
                                    TransporterCode = transporterid;
                                    strTractor = Tractor.getText().toString();
                                    strTrailer = Trailer.getText().toString();


                                    DNoteNo = etDeliveryNo.getText().toString();
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("ADNoteNo", DNoteNo);
                                    edit.commit();
                                    Transporter = transporterid;
                                    Vehicle = Tractor.getText().toString();
                                    Date date = new Date(getDate());
                                    Calendar cal = Calendar.getInstance();
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                                    Date = format.format(date);
                                    ArrivalTime = Date+" "+format2.format(cal.getTime());


                                    final DecimalFormat df = new DecimalFormat("#0.0#");
                                    Cursor c = db.rawQuery("select " +
                                            "" + Database.DataDevice +
                                            ",COUNT(" + Database.ROW_ID + ")" +
                                            ",SUM(" + Database.TotalWeights + ")" +
                                            " from AgentsSuppliesConsignments WHERE "
                                            + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);
                                    if (c != null) {

                                        FieldWt = df.format(c.getDouble(2));

                                    }
                                    c.close();

                                    ContentValues values = new ContentValues();
                                    values.put(Database.SignedOff, 1);
                                    values.put(Database.DelivaryNO, DNoteNo);
                                    values.put(Database.Factory, Factory);
                                    values.put(Database.Transporter, TransporterCode);
                                    values.put(Database.Tractor, strTractor);
                                    values.put(Database.Trailer, strTrailer);


                                    long rows = db.update(Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                            Database.SignedOff + " = ?", new String[]{SIGNEDOFF});
                                    dbhelper.AddAGTDelivery(DNoteNo, DelDate, Factory, Transporter, Vehicle, ArrivalTime, FieldWt);
                                    if (rows > 0) {

                                        b.dismiss();
                                        donutProgress.setVisibility(View.VISIBLE);
                                        circle_progress.setVisibility(View.GONE);
                                        new CountDownTimer(1000, 100) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {
                                                //this will be done every 1000 milliseconds ( 1 seconds )
                                                progress = (int) ((1000 - millisUntilFinished) / 5);
                                                donutProgress.setProgress(progress);
                                                txtUndelivered.setText("Delivering Batches ...");
                                                btnDispatch.setVisibility(View.GONE);
                                                btnPrint.setVisibility(View.VISIBLE);
                                                btnComplete.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onFinish() {
                                                //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                                                // pd.dismiss();
                                                donutProgress.setVisibility(View.GONE);
                                                circle_progress.setVisibility(View.VISIBLE);

                                                circle_progress.setProgress(0);
                                                circle_progress.setPrefixText("N");
                                                circle_progress.setSuffixText(" Batch");
                                                txtUndelivered.setText("All batches delivered");


                                            }

                                        }.start();
                                        //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                                    }
                                                   /* getActivity().finish();
                                                    mIntent = new Intent(getActivity(), MainActivity.class);
                                                    startActivity(mIntent);*/


                                } else {
                                    Context context = getActivity();
                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                    text.setText("Sorry! No Batches To Deliver!");
                                    Toast customtoast = new Toast(context);
                                    customtoast.setView(customToastroot);
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                    customtoast.show();

                                }

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();


                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();


            }
        });

        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();

            }
        });

        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //Toast.makeText(getActivity(), "Please Close Batch", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });

        b = dialogBuilder.create();
        b.show();


    }
    private void showUnsupported() {
        showToast("Bluetooth is unsupported by this device");


    }

    private void showConnected() {
        showToast("Printer Connected");


    }

    private void showDisconnected() {
        showToast("Printer Disconnected");


    }
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
    private boolean checkList() {

        try {
            if (this.mSharedPrefs.getBoolean("cloudServices", false)) {
                try {
                    if (this.mSharedPrefs.getString("internetAccessModes", null).toString().equals(null)) {
                        Toast.makeText(getActivity(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                        return false;

                    }
                    try {
                        if (this.mSharedPrefs.getString("licenseKey", null).equals(null) || this.mSharedPrefs.getString("licenseKey", null).equals(XmlPullParser.NO_NAMESPACE)) {
                            //this.checkListReturnValue = "License key not found!";
                            Toast.makeText(getActivity(), "License key not found!", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        try {
                            if (!this.mSharedPrefs.getString("portalURL", null).equals(null) && !this.mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
                                return true;
                            }
                            //this.checkListReturnValue = "Portal URL not configured!";
                            Toast.makeText(getActivity(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                            return false;
                        } catch (Exception e) {
                            //this.checkListReturnValue = "Portal URL not configured!";
                            Toast.makeText(getActivity(), "Portal URL not configured!", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } catch (Exception e2) {
                        //this.checkListReturnValue = "License key not found!";
                        Toast.makeText(getActivity(), "License key not found!", Toast.LENGTH_LONG).show();
                        return false;
                    }

                } catch (Exception e3) {
                    e3.printStackTrace();
                    //this.checkListReturnValue = "Cloud Services not enabled!";
                    Toast.makeText(getActivity(), "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            Toast.makeText(getActivity(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return false;


            //this.checkListReturnValue = "Cloud Services not enabled!";

        } catch (Exception e4) {
            e4.printStackTrace();
            //this.checkListReturnValue = "Cloud Services not enabled!";
            Toast.makeText(getActivity(), "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
            return false;
        }

    }
    private void connect() {
        if (mDeviceList == null || mDeviceList.size() == 0) {
            return;
        }

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

                showDisconnected();
            }
        } catch (P25ConnectionException e) {
            e.printStackTrace();
        }
    }
    private void createBond(BluetoothDevice device) throws Exception {

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
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state 	= intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    //  showToast("Bluetooth Enabled");
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    showToast("Bluetooth Disabled");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();

                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();


            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);

                showToast("Found device " + device.getName());
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED) {
                    showToast("Paired");

                    //connect();
                }
            }
        }
    };
    private void calculateResult() throws NumberFormatException {
        // Gets the two EditText controls' Editable values
        Editable editableValue1 = etGroswt.getText(),
                editableValue2 = etTarewt.getText();

        // Initializes the double values and result
        double value1 = 0.0,
                value2 = 0.0,
                result;

        // If the Editable values are not null, obtains their double values by parsing

        value1 = Double.parseDouble(editableValue1.toString());


        value2 = Double.parseDouble(editableValue2.toString());

        // Calculates the result
        result = value1 - value2;

        // Displays the calculated result
        etNet.setText(String.valueOf(result));
    }
    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE)
        ;

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to upload data. Please turn on mobile network or Wi-Fi in Settings.</font>"))
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setNegativeButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (mSharedPrefs.getString("internetAccessModes", "WF").toString().equals("WF")) {

                                    Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    startActivity(i);
                                }else{
                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivity(i);

                                }


                            }
                        }
                )
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getbatches();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getbatches() {
        String fromProd = prefs.getString("FromProd", "");
        if (fromProd.equals("Agents")) {

            try {
                int CLOSED = 1;
                int SIGNEDOFF = 0;

                SQLiteDatabase db = dbhelper.getReadableDatabase();
                final Cursor accounts = db.rawQuery("select * from " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                        + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                circle_progress.setProgress(accounts.getCount());
                circle_progress.setMax(10);
                if (accounts.getCount() == 0) {

                    txtUndelivered.setText("All batches delivered");
                    circle_progress.setPrefixText("N");
                    circle_progress.setSuffixText(" Batch");

                } else if (accounts.getCount() == 1) {

                    txtUndelivered.setText(accounts.getCount() + " batch not delivered");
                    circle_progress.setSuffixText(" batch");

                } else {
                    txtUndelivered.setText(accounts.getCount() + " batches not delivered");
                    circle_progress.setSuffixText(" batches");

                }
                // dbhelper.close();
            } catch (Exception ex) {
                Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
        else {

        try {
            int CLOSED = 1;
            int SIGNEDOFF = 0;

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            final Cursor accounts = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

            circle_progress.setProgress(accounts.getCount());
            circle_progress.setMax(10);
            if (accounts.getCount() == 0) {

                txtUndelivered.setText("All batches delivered");
                circle_progress.setPrefixText("N");
                circle_progress.setSuffixText(" Batch");

            } else if (accounts.getCount() == 1) {

                txtUndelivered.setText(accounts.getCount() + " batch not delivered");
                circle_progress.setSuffixText(" batch");

            } else {
                txtUndelivered.setText(accounts.getCount() + " batches not delivered");
                circle_progress.setSuffixText(" batches");

            }
            // dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    }


    private void TransporterList() {
        transporterdata.clear();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select tptID,tptName from transporter ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    transporters=c.getString(c.getColumnIndex("tptName"));
                    transporterdata.add(transporters);

                }while(c.moveToNext());
            }
        }


        transporteradapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,transporterdata);
        transporteradapter.setDropDownViewResource(R.layout.spinner_item);
        mc_ctransporter.setAdapter(transporteradapter);
        mc_ctransporter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String transporterName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                if (transporterName.equals("Select ...")) {
                    transporterid = " ";

                }
                Cursor c = db.rawQuery("select tptID from transporter where tptName= '" + transporterName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    transporterid = c.getString(c.getColumnIndex("tptID"));

                }
                c.close();
                // db.close();
                // dbhelper.close();
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

    private void FactoryList() {
        factorydata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select FryPrefix,FryTitle from factory ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    factorys=c.getString(c.getColumnIndex("FryTitle"));
                    factorydata.add(factorys);

                }while(c.moveToNext());
            }
        }


        factoryadapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,factorydata);
        factoryadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerFactory.setAdapter(factoryadapter);
        spinnerFactory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String factoryName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select FryPrefix from factory where FryTitle= '" + factoryName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    factoryid = c.getString(c.getColumnIndex("FryPrefix"));


                }
                c.close();
                // db.close();
                // dbhelper.close();
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
    //This is method to call the date and not accessible outside this class
    private String getDate(){

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }
    private void printDelivary() {
        long milis		= System.currentTimeMillis();
        String date		= DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
        String time		= DateUtil.timeMilisToString(milis, "hh:mm a");
        String titleStr	= "\n*** DELIVERY NOTE ***" + "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        String DNoteNum = prefs.getString("DNoteNo", "");

        Cursor count = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
                + Database.FdDNoteNum + " ='" + DNoteNum + "'", null);

        if (count.getCount() > 0) {


            StringBuilder contentSb	= new StringBuilder();
            count.moveToNext();
            if (count.getString(count.getColumnIndex(Database.FdTransporter))==null){
                Transporter="";
            }
            else{
                TransporterCode=count.getString(count.getColumnIndex(Database.FdTransporter));
                String[] allColumns = new String[] {Database.TPT_ID,Database.TPT_NAME};
                Cursor c = db.query(Database.TRANSPORTER_TABLE_NAME, allColumns,Database.TPT_ID + "='" + TransporterCode + "'", null, null, null, null,
                        null);
                if (c != null) {
                    c.moveToFirst();
                    Transporter=c.getString(c.getColumnIndex(Database.TPT_NAME));
                }
            }
            if (count.getString(count.getColumnIndex(Database.FdFactory))==null){
                Factory="";
            }else{
                FactoryCode=count.getString(count.getColumnIndex(Database.FdFactory));
                String[] allColumns = new String[] {Database.FRY_PREFIX,Database.FRY_TITLE};
                Cursor c1 = db.query(Database.FACTORY_TABLE_NAME, allColumns,Database.FRY_PREFIX + "='" + FactoryCode + "'", null, null, null, null,
                        null);
                if (c1 != null) {
                    c1.moveToFirst();

                    Factory=(c1.getString(c1.getColumnIndex(Database.FRY_TITLE)));

                }
            }
            contentSb.append("  \n-----------------------------\n");
            contentSb.append("  DELIVERY NO: " + count.getString(count.getColumnIndex(Database.FdDNoteNum)) + "\n");
            contentSb.append("  DATE       : " + count.getString(count.getColumnIndex(Database.FdDate)) + "\n");
            contentSb.append("  VEHICLE    : " + count.getString(count.getColumnIndex(Database.FdVehicle)) + "\n");
            contentSb.append("  FACTORY    : " + Factory + "\n");
            contentSb.append("  TRANSPORTER: " + Transporter + "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("    BATCH      " + "   TOTAL KGS "+ "\n");
            contentSb.append("  -----------------------------\n");
            Cursor accounts1 =  db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.DelivaryNO + " ='" + DNoteNum + "'", null);
            if (accounts1.getCount() > 0) {

                while (accounts1.moveToNext()) {

                    contentSb.append("  "+accounts1.getString(accounts1.getColumnIndex(Database.DeliveryNoteNumber))+"       "
                            + accounts1.getString(accounts1.getColumnIndex(Database.TotalWeights))+ "\n");

                }

            }
            contentSb.append("  -----------------------------\n");
            contentSb.append("  TOTAL KGS           " + count.getString(count.getColumnIndex(Database.FdFieldWt))+ "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
            // contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
            //contentSb.append("  "+Util.nameLeftValueRightJustify("www.octagon.co.ke ", " 25471855111", DataConstants.RECEIPT_WIDTH) + "\n");
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

            System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
            offset += content1Byte.length;

            byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

            sendData(senddata);

        }

    }
    public void printDelivaryAgent() {
        long milis		= System.currentTimeMillis();
        String date		= DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
        String time		= DateUtil.timeMilisToString(milis, "hh:mm a");
        String titleStr	= "\n*** DELIVERY NOTE ***" + "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        String DNoteNum = prefs.getString("ADNoteNo", "");

        Cursor count = db.rawQuery("select * from " + Database.Agt_FactoryDeliveries + " WHERE "
                + Database.FdDNoteNum + " ='" + DNoteNum + "'", null);

        if (count.getCount() > 0) {


            StringBuilder contentSb	= new StringBuilder();
            count.moveToNext();
            if (count.getString(count.getColumnIndex(Database.FdTransporter))==null){
                Transporter="";
            }
            else{
                TransporterCode=count.getString(count.getColumnIndex(Database.FdTransporter));
                String[] allColumns = new String[] {Database.TPT_ID,Database.TPT_NAME};
                Cursor c = db.query(Database.TRANSPORTER_TABLE_NAME, allColumns,Database.TPT_ID + "='" + TransporterCode + "'", null, null, null, null,
                        null);
                if (c != null) {
                    c.moveToFirst();
                    Transporter=c.getString(c.getColumnIndex(Database.TPT_NAME));
                }
            }
            if (count.getString(count.getColumnIndex(Database.FdFactory))==null){
                Factory="";
            }else{
                FactoryCode=count.getString(count.getColumnIndex(Database.FdFactory));
                String[] allColumns = new String[] {Database.FRY_PREFIX,Database.FRY_TITLE};
                Cursor c1 = db.query(Database.FACTORY_TABLE_NAME, allColumns,Database.FRY_PREFIX + "='" + FactoryCode + "'", null, null, null, null,
                        null);
                if (c1 != null) {
                    c1.moveToFirst();

                    Factory=(c1.getString(c1.getColumnIndex(Database.FRY_TITLE)));

                }
            }
            contentSb.append("  -----------------------------\n");
            contentSb.append("  DELIVERY NO: " + count.getString(count.getColumnIndex(Database.FdDNoteNum)) + "\n");
            contentSb.append("  DATE       : " + count.getString(count.getColumnIndex(Database.FdDate)) + "\n");
            contentSb.append("  VEHICLE    : " + count.getString(count.getColumnIndex(Database.FdVehicle)) + "\n");
            contentSb.append("  FACTORY    : " + Factory + "\n");
            contentSb.append("  TRANSPORTER: " + Transporter + "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("    BATCH      " + "   TOTAL KGS "+ "\n");
            contentSb.append("  -----------------------------\n");
            Cursor accounts1 =  db.rawQuery("select * from " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.DelivaryNO + " ='" + DNoteNum + "'", null);
            if (accounts1.getCount() > 0) {

                while (accounts1.moveToNext()) {

                    contentSb.append("  "+accounts1.getString(accounts1.getColumnIndex(Database.DeliveryNoteNumber))+"       "
                            + accounts1.getString(accounts1.getColumnIndex(Database.TotalWeights))+ "\n");

                }

            }
            contentSb.append("  -----------------------------\n");
            contentSb.append("  TOTAL KGS           " + count.getString(count.getColumnIndex(Database.FdFieldWt))+ "\n");

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

            System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
            offset += content1Byte.length;

            byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

            sendData(senddata);

        }

    }
    public void CompleteDelivery(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_complete_delivery, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Delivery Details");

        etTicketNo = (EditText) dialogView.findViewById(R.id.etTicketNo);
        if(!mSharedPrefs.getBoolean("enableAlphaNumeric", false)==true) {
          //Toast.makeText(getActivity(), "Alphanumeric not enabled on settings", Toast.LENGTH_LONG).show();

        } else {
            etTicketNo.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        if(!mSharedPrefs.getBoolean("enableAutomaticTicket", false)==true) {
            // go back to milkers activity
            //Toast.makeText(getActivity(), "Auto generated delivery not enabled", Toast.LENGTH_LONG).show();
            //return;
        } else {
            etTicketNo.setEnabled(false);
            formatter = new DecimalFormat("0000");

            TicketNo=prefs.getString("tcount", null);
            if (TicketNo != null) {

                tcount = Integer.parseInt(prefs.getString("tcount", "")) + 1;
                TicketNo = formatter.format(tcount);

            }
            else{

                TicketNo = formatter.format(tcount);
            }
            Date date = new Date(getDate());
            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
            String dateDel = format.format(date);
            deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);

            etTicketNo.setText(dateDel+deviceID+TicketNo);
        }
        etGroswt = (EditText) dialogView.findViewById(R.id.etGroswt);
        etTarewt = (EditText) dialogView.findViewById(R.id.etTarewt);
        etNet = (EditText) dialogView.findViewById(R.id.etNet);
        etRejectwt = (EditText) dialogView.findViewById(R.id.etRejectwt);
        etQuality = (EditText) dialogView.findViewById(R.id.etQuality);

        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(etGroswt.getText().length()>0 && etTarewt.getText().length()>0){
                    calculateResult();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        };

        // Adds the TextWatcher as TextChangedListener to both EditTexts
        etGroswt.addTextChangedListener(textWatcher);
        etTarewt.addTextChangedListener(textWatcher);

        btnCloseBatch = (Button) dialogView.findViewById(R.id.btnCloseBatch);
        btnCloseBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), prefs.getString("DNoteNo ", ""), Toast.LENGTH_LONG).show();
                if (etTicketNo.length() <=0) {
                    etTicketNo.setError("Enter Ticket No.");
                    return;
                }
                if (etGroswt.length()<=0) {
                    etGroswt.setError("Enter GrossWT.");
                    return;
                }
                if (etTarewt.length() <=0) {
                    etTarewt.setError("Enter TareWT.");
                    return;
                }
                Cursor checkTicket =dbhelper.CheckDelivery(etTicketNo.getText().toString());
                //Check for duplicate id number
                if (checkTicket.getCount() > 0) {
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Ticket Number Exists, type a new one");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Complete Delivery?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String DelNoteNum = prefs.getString("DelNoteNo", "");

                                Cursor count = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
                                        + Database.FdDNoteNum + " ='" + DelNoteNum + "'", null);

                                if (count.getCount() > 0) {

                                    stTicketNo = etTicketNo.getText().toString();
                                    stGroswt = etGroswt.getText().toString();
                                    stTarewt = etTarewt.getText().toString();
                                    stNet = etNet.getText().toString();
                                    stRejectwt = etRejectwt.getText().toString();
                                    stQuality = etQuality.getText().toString();

                                    netweight=Double.parseDouble(stNet.toString());
                                    if(netweight<=0) {
                                        Context context = getActivity();
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                        text.setText("Unacceptable Net Weight! should be greater than 0");
                                        Toast customtoast = new Toast(context);
                                        customtoast.setView(customToastroot);
                                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                        customtoast.setDuration(Toast.LENGTH_LONG);
                                        customtoast.show();
                                        //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                                        return;

                                    }

                                    Date date = new Date(getDate());
                                    Calendar cal = Calendar.getInstance();
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                                    Date = format.format(date);
                                    DepartureTime = Date+" "+format2.format(cal.getTime());
                                    ContentValues values = new ContentValues();
                                    values.put(Database.FdWeighbridgeTicket, stTicketNo);
                                    values.put(Database.FdGrossWt, stGroswt);
                                    values.put(Database.FdTareWt, stTarewt);
                                    values.put(Database.FdRejectWt, stRejectwt);
                                    values.put(Database.FdQualityScore, stQuality);
                                    values.put(Database.FdDepartureTime, DepartureTime);
                                    values.put(Database.FdStatus, 1);


                                    long rows = db.update(Database.Fmr_FactoryDeliveries, values,
                                            Database.FdDNoteNum + " = ?", new String[]{DelNoteNum});

                                    if (rows > 0)
                                    {
                                        SharedPreferences.Editor edit = prefs.edit();
                                        edit.putString("tcount", TicketNo);
                                        edit.commit();

                                        Toast.makeText(getActivity(), "Delivered Successfully !!", Toast.LENGTH_LONG).show();
                                        b.dismiss();
                                        getdata();
                                    }

                                                   /* getActivity().finish();
                                                    mIntent = new Intent(getActivity(), MainActivity.class);
                                                    startActivity(mIntent);*/


                                } else {
                                    Context context = getActivity();
                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                    text.setText("Sorry! No Deliveries!");
                                    Toast customtoast = new Toast(context);
                                    customtoast.setView(customToastroot);
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                    customtoast.show();

                                }

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();


                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();


            }
        });

        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();

            }
        });

        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //Toast.makeText(getActivity(), "Please Close Batch", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });

        b = dialogBuilder.create();
        b.show();
    }

    public void CompleteDeliveryAgt(){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_complete_delivery, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Delivery Details");

        etTicketNo = (EditText) dialogView.findViewById(R.id.etTicketNo);
        if(!mSharedPrefs.getBoolean("enableAlphaNumeric", false)==true) {
            //Toast.makeText(getActivity(), "Alphanumeric not enabled on settings", Toast.LENGTH_LONG).show();

        } else {
            etTicketNo.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        etGroswt = (EditText) dialogView.findViewById(R.id.etGroswt);
        etTarewt = (EditText) dialogView.findViewById(R.id.etTarewt);
        etNet = (EditText) dialogView.findViewById(R.id.etNet);
        etRejectwt = (EditText) dialogView.findViewById(R.id.etRejectwt);
        etQuality = (EditText) dialogView.findViewById(R.id.etQuality);

        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(etGroswt.getText().length()>0 && etTarewt.getText().length()>0){
                    calculateResult();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        };

        // Adds the TextWatcher as TextChangedListener to both EditTexts
        etGroswt.addTextChangedListener(textWatcher);
        etTarewt.addTextChangedListener(textWatcher);

        btnCloseBatch = (Button) dialogView.findViewById(R.id.btnCloseBatch);
        btnCloseBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), prefs.getString("ADNoteNo ", ""), Toast.LENGTH_LONG).show();
                if (etTicketNo.length() <=0) {
                    etTicketNo.setError("Enter Ticket No.");
                    return;
                }
                if (etGroswt.length()<=0) {
                    etGroswt.setError("Enter GrossWT.");
                    return;
                }
                if (etTarewt.length() <=0) {
                    etTarewt.setError("Enter TareWT.");
                    return;
                }

                Cursor checkTicket =dbhelper.CheckDeliveryAgt(etTicketNo.getText().toString());
                //Check for duplicate id number
                if (checkTicket.getCount() > 0) {
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Ticket Number Exists, type a new one");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Complete Delivery?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String DelNoteNum = prefs.getString("DelNoteNo", "");

                                Cursor count = db.rawQuery("select * from " + Database.Agt_FactoryDeliveries + " WHERE "
                                        + Database.FdDNoteNum + " ='" + DelNoteNum + "'", null);

                                if (count.getCount() > 0) {

                                    stTicketNo = etTicketNo.getText().toString();
                                    stGroswt = etGroswt.getText().toString();
                                    stTarewt = etTarewt.getText().toString();
                                    stNet = etNet.getText().toString();
                                    stRejectwt = etRejectwt.getText().toString();
                                    stQuality = etQuality.getText().toString();

                                    netweight=Double.parseDouble(stNet.toString());
                                    if(netweight<=0) {
                                        Context context = getActivity();
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                        text.setText("Unacceptable Net Weight! should be greater than 0");
                                        Toast customtoast = new Toast(context);
                                        customtoast.setView(customToastroot);
                                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                        customtoast.setDuration(Toast.LENGTH_LONG);
                                        customtoast.show();
                                        //Toast.makeText(getBaseContext(), "Please Enter Gross Reading", Toast.LENGTH_LONG).show();
                                        return;

                                    }

                                    Date date = new Date(getDate());
                                    Calendar cal = Calendar.getInstance();
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                                    Date = format.format(date);
                                    DepartureTime = Date+" "+format2.format(cal.getTime());
                                    ContentValues values = new ContentValues();
                                    values.put(Database.FdWeighbridgeTicket, stTicketNo);
                                    values.put(Database.FdGrossWt, stGroswt);
                                    values.put(Database.FdTareWt, stTarewt);
                                    values.put(Database.FdRejectWt, stRejectwt);
                                    values.put(Database.FdQualityScore, stQuality);
                                    values.put(Database.FdDepartureTime, DepartureTime);
                                    values.put(Database.FdStatus, 1);


                                    long rows = db.update(Database.Agt_FactoryDeliveries, values,
                                            Database.FdDNoteNum + " = ?", new String[]{DelNoteNum});

                                    if (rows > 0) {
                                        Toast.makeText(getActivity(), "Delivered Successfully !!", Toast.LENGTH_LONG).show();
                                        b.dismiss();
                                        getdata();
                                    }

                                                   /* getActivity().finish();
                                                    mIntent = new Intent(getActivity(), MainActivity.class);
                                                    startActivity(mIntent);*/


                                } else {
                                    Context context = getActivity();
                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                    text.setText("Sorry! No Deliveries!");
                                    Toast customtoast = new Toast(context);
                                    customtoast.setView(customToastroot);
                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                    customtoast.show();

                                }

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();


                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();


            }
        });

        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();

            }
        });

        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //Toast.makeText(getActivity(), "Please Close Batch", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });

        b = dialogBuilder.create();
        b.show();
    }
    public void getdata(){
        String fromProd = prefs.getString("FromProd", "");
        if(fromProd.equals("Agents")){
            try {

                SQLiteDatabase db= dbhelper.getReadableDatabase();
                ArrayList<Delivary> arraylist = new ArrayList<Delivary>();

                accounts = db.rawQuery("select * from " + Database.Agt_FactoryDeliveries + " WHERE FdStatus=0", null);
                if (accounts.getCount() > 0) {
                    while(accounts.moveToNext()) {

                        arraylist.add(new Delivary(accounts.getString(accounts.getColumnIndex(Database.ROW_ID)),accounts.getString(accounts.getColumnIndex(Database.FdDNoteNum)),
                                accounts.getString(accounts.getColumnIndex(Database.FdDate)),
                                accounts.getString(accounts.getColumnIndex(Database.FdFieldWt))));
                    }

                    ArrayAdapter = new DeliveryArrayAdapter(getActivity(), R.layout.complete_delivery_list, arraylist);


                    listReciepts.setAdapter(ArrayAdapter);
                    ArrayAdapter.notifyDataSetChanged();
                    listReciepts.setTextFilterEnabled(true);

                    //db.close();
                    //dbhelper.close();


                }

                else {


                    IncompleteDel.dismiss();

                    donutProgress.setVisibility(View.VISIBLE);
                    circle_progress.setVisibility(View.GONE);
                    new CountDownTimer(1000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //this will be done every 1000 milliseconds ( 1 seconds )
                            progress = (int) ((1000 - millisUntilFinished) / 5);
                            donutProgress.setProgress(progress);
                            txtUndelivered.setText("Processing Deliveries ...");
                            btnDispatch.setVisibility(View.VISIBLE);
                            btnPrint.setVisibility(View.GONE);
                            btnComplete.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFinish() {
                            //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                            // pd.dismiss();
                            donutProgress.setVisibility(View.GONE);
                            circle_progress.setVisibility(View.VISIBLE);

                            circle_progress.setProgress(0);
                            circle_progress.setPrefixText("N");
                            circle_progress.setSuffixText(" Batch");
                            txtUndelivered.setText("All Batches and Deliveries Completed");


                        }

                    }.start();
                    //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();


                }
            } catch (Exception ex) {
                Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            try {

                SQLiteDatabase db= dbhelper.getReadableDatabase();
                ArrayList<Delivary> arraylist = new ArrayList<Delivary>();

                accounts = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE FdStatus=0", null);
                if (accounts.getCount() > 0) {
                    while(accounts.moveToNext()) {

                        arraylist.add(new Delivary(accounts.getString(accounts.getColumnIndex(Database.ROW_ID)),accounts.getString(accounts.getColumnIndex(Database.FdDNoteNum)),
                                accounts.getString(accounts.getColumnIndex(Database.FdDate)),
                                accounts.getString(accounts.getColumnIndex(Database.FdFieldWt))));
                    }

                    ArrayAdapter = new DeliveryArrayAdapter(getActivity(), R.layout.complete_delivery_list, arraylist);


                    listReciepts.setAdapter(ArrayAdapter);
                    ArrayAdapter.notifyDataSetChanged();
                    listReciepts.setTextFilterEnabled(true);

                    //db.close();
                    //dbhelper.close();


                }

                else {


                    IncompleteDel.dismiss();

                    donutProgress.setVisibility(View.VISIBLE);
                    circle_progress.setVisibility(View.GONE);
                    new CountDownTimer(1000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //this will be done every 1000 milliseconds ( 1 seconds )
                            progress = (int) ((1000 - millisUntilFinished) / 5);
                            donutProgress.setProgress(progress);
                            txtUndelivered.setText("Processing Deliveries ...");
                            btnDispatch.setVisibility(View.VISIBLE);
                            btnPrint.setVisibility(View.GONE);
                            btnComplete.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFinish() {
                            //the progressBar will be invisible after 60 000 miliseconds ( 1 minute)
                            // pd.dismiss();
                            donutProgress.setVisibility(View.GONE);
                            circle_progress.setVisibility(View.VISIBLE);

                            circle_progress.setProgress(0);
                            circle_progress.setPrefixText("N");
                            circle_progress.setSuffixText(" Batch");
                            txtUndelivered.setText("All Batches and Deliveries Completed");


                        }

                    }.start();
                    //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();


                }
            } catch (Exception ex) {
                Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }



    }
    public class DeliveryArrayAdapter extends ArrayAdapter<Delivary> {

        Context context;
        int layoutResourceId;
        ArrayList<Delivary> students = new ArrayList<Delivary>();
        StudentWrapper StudentWrapper = null;

        public DeliveryArrayAdapter(Context context, int layoutResourceId,
                                    ArrayList<Delivary> studs) {
            super(context, layoutResourceId, studs);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.students = studs;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View item = convertView;


            if (item == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                item = inflater.inflate(layoutResourceId, parent, false);
                StudentWrapper = new StudentWrapper();
                StudentWrapper.number = (TextView) item.findViewById(R.id.tv_number);
                StudentWrapper.deldate = (TextView) item.findViewById(R.id.tv_date);
                StudentWrapper.totalkgs = (TextView) item.findViewById(R.id.txtTotalKgs);
                StudentWrapper.print = (Button) item.findViewById(R.id.btnPrint);

                item.setTag(StudentWrapper);
            } else {
                StudentWrapper = (StudentWrapper) item.getTag();
            }

            student = students.get(position);
            StudentWrapper.number.setText(student.getName());
            StudentWrapper.deldate.setText(student.getAge());
            StudentWrapper.totalkgs.setText(student.getAddress());


            StudentWrapper.print.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listReciepts.performItemClick(listReciepts.getAdapter().getView(position, null, null), position, listReciepts.getAdapter().getItemId(0));
                    StudentWrapper.number.getText().toString();
                    //Toast.makeText(getActivity(), StudentWrapper.number.getText().toString(), Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("DelNoteNo");
                    edit.commit();
                    edit.putString("DelNoteNo", StudentWrapper.number.getText().toString());
                    edit.commit();

                    dbhelper = new DBHelper(context);
                    db = dbhelper.getReadableDatabase();

                    String fromProd = prefs.getString("FromProd", "");

                    if(fromProd.equals("Agents")){
                        CompleteDeliveryAgt();
                    }
                    else
                    {

                        CompleteDelivery();
                    }




                }
            });




            return item;

        }

        private class StudentWrapper {
            TextView number;
            TextView deldate;
            TextView totalkgs;
            Button print;

        }

    }
    private class LogOut extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute()
        {
            dialog = ProgressDialog.show( getActivity(),
                    getString(R.string.please_wait),
                    "Disconnecting all devices ...",
                    true);
        }

        @Override
        protected String doInBackground(Void... params)
        {


            try {
                Thread.sleep(1000);
                resetConn.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result)
        {

            dialog.dismiss();


        }
    }


}
