package com.easyweigh.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
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

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Created by Michael on 30/06/2016.
 */
public class AgentRecieptsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    ListView listReciepts,listbags;
    String accountId;
    TextView textAccountId;
    Boolean success = true;
     TextView textCompanyName,textPoBox,textReciept,textTransDate,textTransTime,textTerminal,textFarmerNo,textName,
    textRoute,textShed,textTrip,textBags,textGrossWt,textTareWt,textNetWt,textTotalKgs,textClerk;
    SearchView searchView;
    public SimpleCursorAdapter ca,cb;
    Intent mIntent;
    static SharedPreferences mSharedPrefs;
    static SharedPreferences prefs;
    EditText etFrom,etTo,etAgentNo;
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
    String  fromDate,toDate,agentNo;
    String  condition = " _id > 0 ";
    String  cond = " _id > 0 ";
    AlertDialog b;
    AlertDialog alert;
    DecimalFormat formatter;
    String SessionNo,DataDevice;
    String ReceiptNo,BagCount,produce;
    String Kgs,Tare,Time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listreciepts);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agent Receipts");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer(){
        mDeviceSp 			= (Spinner) findViewById(R.id.sp_device);
        mConnectBtn			= (Button) findViewById(R.id.btnConnect);
        btnFilter			= (Button) findViewById(R.id.btnFilter);
        mPrintBtn			= (Button) findViewById(R.id.btnPrint);
        mPrintBtn.setVisibility(View.GONE);
        formatter = new DecimalFormat("0000");
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(AgentRecieptsActivity.this);
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
            }
        });

        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        

        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
            // go back to milkers activity
            Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
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

                        updateDeviceList();
                    }
                }


                mProgressDlg = new ProgressDialog(this);

                mProgressDlg.setMessage("Scanning...");
                mProgressDlg.setCancelable(false);
                mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        mBluetoothAdapter.cancelDiscovery();
                    }
                });

                mConnectingDlg = new ProgressDialog(this);

                mConnectingDlg.setMessage("Connecting...");
                mConnectingDlg.setCancelable(false);

                mConnector = new P25Connector(new P25Connector.P25ConnectionListener() {

                    @Override
                    public void onStartConnecting() {
                        mConnectingDlg.show();
                        mConnectBtn.setVisibility(View.GONE);
                    }

                    @Override
                    public void onConnectionSuccess() {
                        mConnectingDlg.dismiss();

                        showConnected();
                    }


                    @Override
                    public void onConnectionFailed(String error) {
                        mConnectingDlg.dismiss();
                        mConnectBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onConnectionCancelled() {

                        mConnectingDlg.dismiss();
                        mConnectBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onDisconnected() {

                        // showDisconnected();
                    }
                });
                connect();

            }
            //connect/disconnect
            mConnectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    connect();
                }
            });

            //print demo text
            mPrintBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //showGeneralRecieptDetails();


                }
            });

            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

            registerReceiver(mReceiver, filter);
        }
            showSearchReceipt();

    }


    private void showUnsupported() {
        showToast("Bluetooth is unsupported by this device");

        mConnectBtn.setVisibility(View.VISIBLE);

        mPrintBtn.setVisibility(View.GONE);

    }

    private void showConnected() {
        showToast("Printer Connected");

        mConnectBtn.setVisibility(View.GONE);
       btnFilter.setVisibility(View.VISIBLE);
        mPrintBtn.setVisibility(View.GONE);

    }

    private void showDisconnected() {
        showToast("Printer Disconnected");

        mConnectBtn.setVisibility(View.VISIBLE);
        btnFilter.setVisibility(View.VISIBLE);
        mPrintBtn.setVisibility(View.GONE);

    }
    @Override
    public void onPause() {
        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
            // go back to milkers activity
            // Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
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
        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
            // go back to milkers activity
            // Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
        } else {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_agtsearch_reciept, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Receipt");
        etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
        etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
        etAgentNo = (EditText) dialogView.findViewById(R.id.edtAgentNo);

        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        etFrom.setText(format1.format(date));
        etTo.setText(format1.format(date));

        pickFrom =(Button)dialogView.findViewById(R.id.btnFrom);
        pickFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });

        pickTo =(Button)dialogView.findViewById(R.id.btnTo);
        pickTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment2();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });


        btnSearchReceipt = (Button) dialogView.findViewById(R.id.btn_SearchReceipt);
        btnSearchReceipt.setVisibility(View.VISIBLE);
        btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDate = etFrom.getText().toString();
                toDate = etTo.getText().toString();
                agentNo = etAgentNo.getText().toString();

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("fromDate", fromDate);
                edit.commit();
                edit.putString("toDate", toDate);
                edit.commit();
                edit.putString("agentNo", agentNo);
                edit.commit();
                if (agentNo.equals("")) {

                    Context context = AgentRecieptsActivity.this;
                    LayoutInflater inflater = AgentRecieptsActivity.this.getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Enter Agent No ...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(FarmerRecieptsActivity.this, "Please Enter Farmer No", Toast.LENGTH_LONG).show();


                    return;
                }
                if (fromDate.length() > 0)
                    condition += " and  " + Database.ASessionDate + " >= '" + fromDate + "'";

                if (toDate.length() > 0)
                    condition += " and  " + Database.ASessionDate + " <= '" + toDate + "'";

                if (agentNo.length() > 0)
                    condition += " and  " + Database.ASessionAgentNo + " = '" + agentNo + "'";

                if ( fromDate.length() > 0)
                    cond += " and  " + Database.ACollDate + " >= '" + fromDate + "'";

                if ( toDate.length() > 0)
                    cond += " and  " + Database.ACollDate + " <= '" + toDate + "'";

                if ( agentNo.length() > 0)
                    cond += " and  " + Database.AgentNo + " = '" + agentNo + "'";

                //getSearch();
                ca.getFilter().filter(condition.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String reciept = constraint.toString();
                        return dbhelper.SearchARecieptByDate(reciept);
                    }
                });

                b.dismiss();
            }
        });



        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                   // Toast.makeText(FarmerRecieptsActivity.this, "Please Click Search to proceed", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });
        dialogBuilder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                //getdata();
              finish();
            }
        });
        b = dialogBuilder.create();
        b.show();

    }
    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get( Calendar.HOUR );
            int minute = c.get( Calendar.MINUTE );

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            // edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());

            // Create a Date variable/object with user chosen date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
            etFrom.setText(format1.format(chosenDate));
        }
    }
    @SuppressLint("ValidFragment")
    public class DatePickerFragment2 extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            // edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
            etTo.setText(format1.format(chosenDate));
        }
    }
    public void PrintGenericReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_agent_transaction_details, null);
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

        StringBuilder contentSb	= new StringBuilder();

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
        listbags = (ListView) dialogView.findViewById(R.id.listbags);
        listbags.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        final DecimalFormat df1 = new DecimalFormat("#0.00#");

        // SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor session = db.query(Database.ASESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();
            SessionNo=formatter.format(session.getInt(8));
            String[] allColumns = new String[] {Database.AGT_NAME};
            Cursor c = db.query(Database.AGENT_TABLE_NAME, allColumns,Database.AGT_ID + "='" + session.getString(session
                            .getColumnIndex(Database.ASessionAgentNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                textName.setText(c.getString(c.getColumnIndex(Database.AGT_NAME)));

            }





            textReciept.setText(session.getString(session
                    .getColumnIndex(Database.ASessionDevice))+SessionNo);
            DataDevice=(session.getString(session
                    .getColumnIndex(Database.ASessionDevice)));
            textTransDate.setText(session.getString(session
                    .getColumnIndex(Database.ASessionDate)));
            textTransTime.setText(session.getString(session
                    .getColumnIndex(Database.ASessionTime)));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session
                    .getColumnIndex(Database.ASessionAgentNo)));
            textBags.setText(session.getString(session
                    .getColumnIndex(Database.ASessionBags)));
            Cursor s  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.AgentNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.ACaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.ADataCaptureDevice +"='" + DataDevice +"' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if(s!=null)
            {
                String from[] = {Database.ROW_ID,Database.ALoadCount, Database.AQuantity, Database.ACaptureTime};
                int to[] = {R.id.txtAccountId, R.id.txtBagNo, R.id.txtBagW, R.id.txtTime};


                cb = new SimpleCursorAdapter(this, R.layout.baglist, s, from, to);
                //cb.notifyDataSetChanged();
                listbags.setAdapter(cb);
                s.moveToFirst();
                String[] allColumn = new String[] {Database.WH_NAME};
                Cursor w = db.query(Database.WAREHOUSE_TABLE_NAME, allColumn,Database.WH_ID + "='" + s.getString(s.getColumnIndex(Database.AContainer)) + "'", null, null, null, null,
                        null);
                if (w != null) {
                    w.moveToFirst();

                    textRoute.setText(w.getString(w.getColumnIndex(Database.WH_NAME)));

                }



            }

            grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionTare)))
                    +Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionNet)));
            textGrossWt.setText(df.format(grossWeight));

            textTareWt.setText(session.getString(session
                    .getColumnIndex(Database.ASessionTare)));
            textNetWt.setText(session.getString(session
                    .getColumnIndex(Database.ASessionNet)));
            ReceiptNo=session.getString(session.getColumnIndex(Database.ASessionDevice))+SessionNo;
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  RECEIPT    : " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  TIME       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  TERMINAL   : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  AGENT NO   : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  WAREHOUSE  : " + textRoute.getText().toString() + "\n");
            // contentSb.append("  SHED       : " + textShed.getText().toString() + "\n");
            //contentSb.append("  -----------------------------\n");
            //contentSb.append("  REF.ID     : KGS  TIME\r\n");
            //contentSb.append("  -----------------------------\n");

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
            //startManagingCursor(accounts);
            Cursor account  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.AgentNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.ACaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.ADataCaptureDevice +"='" + DataDevice +"' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                while(account.moveToNext()) {
                    // update view
                    count=count+1;
                    BagCount = String.valueOf(count);
                /*ReceiptNo = account.getString(account.getColumnIndex(Database.DataCaptureDevice))+ account.getString(account
                        .getColumnIndex(Database.ReceiptNo));*/


                    Kgs = df1.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.AQuantity))));
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
                    //contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                }
            }
            account.close();




            contentSb.append("  -----------------------------\n");
            contentSb.append("  BAGS       : " + textBags.getText().toString()+ "\n");
            contentSb.append("  GROSS WT   : " + df1.format(Double.parseDouble(textGrossWt.getText().toString())) + "\n");
           // contentSb.append("  TARE WT    : " + df1.format(Double.parseDouble(textTareWt.getText().toString())) + "\n");
            contentSb.append("  NET WT     : " +df1.format(Double.parseDouble(textNetWt.getText().toString())) + "\n");
            //contentSb.append("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n");

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
            if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                // go back to milkers activity
                // Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
            } else {
                sendData(senddata);
            }

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
        b.show();
    }
    public void PrintDetailedReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_agent_transaction_details, null);
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

        StringBuilder contentSb	= new StringBuilder();

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
        listbags = (ListView) dialogView.findViewById(R.id.listbags);
        listbags.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        final DecimalFormat df1 = new DecimalFormat("#0.00#");

       // SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor session = db.query(Database.ASESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();
            SessionNo=formatter.format(session.getInt(8));
            String[] allColumns = new String[] {Database.AGT_NAME};
            Cursor c = db.query(Database.AGENT_TABLE_NAME, allColumns,Database.AGT_ID + "='" + session.getString(session
                            .getColumnIndex(Database.ASessionAgentNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                textName.setText(c.getString(c.getColumnIndex(Database.AGT_NAME)));

            }





            textReciept.setText(session.getString(session
                    .getColumnIndex(Database.ASessionDevice))+SessionNo);
            DataDevice=(session.getString(session
                    .getColumnIndex(Database.ASessionDevice)));
            textTransDate.setText(session.getString(session
                    .getColumnIndex(Database.ASessionDate)));
            textTransTime.setText(session.getString(session
                    .getColumnIndex(Database.ASessionTime)));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session
                    .getColumnIndex(Database.ASessionAgentNo)));
            textBags.setText(session.getString(session
                    .getColumnIndex(Database.ASessionBags)));
            Cursor s  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.AgentNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.ACaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.ADataCaptureDevice +"='" + DataDevice +"' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if(s!=null)
            {
                String from[] = {Database.ROW_ID,Database.ALoadCount, Database.AQuantity, Database.ACaptureTime};
                int to[] = {R.id.txtAccountId, R.id.txtBagNo, R.id.txtBagW, R.id.txtTime};


                cb = new SimpleCursorAdapter(this, R.layout.baglist, s, from, to);
                //cb.notifyDataSetChanged();
                listbags.setAdapter(cb);
                s.moveToFirst();
                String[] allColumn = new String[] {Database.WH_NAME};
                Cursor w = db.query(Database.WAREHOUSE_TABLE_NAME, allColumn,Database.WH_ID + "='" + s.getString(s.getColumnIndex(Database.AContainer)) + "'", null, null, null, null,
                        null);
                if (w != null) {
                    w.moveToFirst();

                    textRoute.setText(w.getString(w.getColumnIndex(Database.WH_NAME)));

                }



            }

            grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionTare)))
                    +Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionNet)));
            textGrossWt.setText(df.format(grossWeight));

            textTareWt.setText(session.getString(session
                    .getColumnIndex(Database.ASessionTare)));
            textNetWt.setText(session.getString(session
                    .getColumnIndex(Database.ASessionNet)));
            ReceiptNo=session.getString(session.getColumnIndex(Database.ASessionDevice))+SessionNo;
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  RECEIPT    : " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  TIME       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  TERMINAL   : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  AGENT NO   : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  WAREHOUSE  : " + textRoute.getText().toString() + "\n");
           // contentSb.append("  SHED       : " + textShed.getText().toString() + "\n");
            contentSb.append("  -----------------------------\n");
            contentSb.append("  REF.ID     : KGS  TIME\r\n");
            contentSb.append("  -----------------------------\n");

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
        //startManagingCursor(accounts);
            Cursor account  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.AgentNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.ACaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.ADataCaptureDevice +"='" + DataDevice +"' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
        if (account.getCount() > 0) {
            int count=0;
            while(account.moveToNext()) {
                // update view
                count=count+1;
                BagCount = String.valueOf(count);
                /*ReceiptNo = account.getString(account.getColumnIndex(Database.DataCaptureDevice))+ account.getString(account
                        .getColumnIndex(Database.ReceiptNo));*/


                Kgs = df1.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.AQuantity))));
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
                contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
            }
        }
            account.close();




            contentSb.append("  -----------------------------\n");
            contentSb.append("  BAGS       : " + textBags.getText().toString()+ "\n");
            contentSb.append("  GROSS WT   : " + df1.format(Double.parseDouble(textGrossWt.getText().toString())) + "\n");
            contentSb.append("  TARE WT    : " + df1.format(Double.parseDouble(textTareWt.getText().toString())) + "\n");
            contentSb.append("  NET WT     : " +df1.format(Double.parseDouble(textNetWt.getText().toString())) + "\n");
            //contentSb.append("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n");

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
            if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                // go back to milkers activity
               // Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
            } else {
            sendData(senddata);
            }

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
        b.show();
    }
    public void PrintSimpleReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_agent_transaction_details, null);
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

        StringBuilder contentSb	= new StringBuilder();

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
        listbags = (ListView) dialogView.findViewById(R.id.listbags);
        listbags.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        final DecimalFormat df1 = new DecimalFormat("#0.00#");

        // SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor session = db.query(Database.ASESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();
            SessionNo=formatter.format(session.getInt(8));
            String[] allColumns = new String[] {Database.AGT_NAME};
            Cursor c = db.query(Database.AGENT_TABLE_NAME, allColumns,Database.AGT_ID + "='" + session.getString(session
                            .getColumnIndex(Database.ASessionAgentNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                textName.setText(c.getString(c.getColumnIndex(Database.AGT_NAME)));

            }





            textReciept.setText(session.getString(session
                    .getColumnIndex(Database.ASessionDevice))+SessionNo);
            DataDevice=(session.getString(session
                    .getColumnIndex(Database.ASessionDevice)));
            textTransDate.setText(session.getString(session
                    .getColumnIndex(Database.ASessionDate)));
            textTransTime.setText(session.getString(session
                    .getColumnIndex(Database.ASessionTime)));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session
                    .getColumnIndex(Database.ASessionAgentNo)));
            textBags.setText(session.getString(session
                    .getColumnIndex(Database.ASessionBags)));
            Cursor s  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.AgentNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.ACaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.ADataCaptureDevice +"='" + DataDevice +"' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if(s!=null)
            {
                String from[] = {Database.ROW_ID,Database.ALoadCount, Database.AQuantity, Database.ACaptureTime};
                int to[] = {R.id.txtAccountId, R.id.txtBagNo, R.id.txtBagW, R.id.txtTime};


                cb = new SimpleCursorAdapter(this, R.layout.baglist, s, from, to);
                //cb.notifyDataSetChanged();
                listbags.setAdapter(cb);
                s.moveToFirst();
                String[] allColumn = new String[] {Database.WH_NAME};
                Cursor w = db.query(Database.WAREHOUSE_TABLE_NAME, allColumn,Database.WH_ID + "='" + s.getString(s.getColumnIndex(Database.AContainer)) + "'", null, null, null, null,
                        null);
                if (w != null) {
                    w.moveToFirst();

                    textRoute.setText(w.getString(w.getColumnIndex(Database.WH_NAME)));

                }



            }

            grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionTare)))
                    +Double.parseDouble(session.getString(session.getColumnIndex(Database.ASessionNet)));
            textGrossWt.setText(df.format(grossWeight));

            textTareWt.setText(session.getString(session
                    .getColumnIndex(Database.ASessionTare)));
            textNetWt.setText(session.getString(session
                    .getColumnIndex(Database.ASessionNet)));
            ReceiptNo=session.getString(session.getColumnIndex(Database.ASessionDevice))+SessionNo;
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  RECEIPT    : " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  TIME       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  TERMINAL   : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  AGENT NO   : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  WAREHOUSE  : " + textRoute.getText().toString() + "\n");
            // contentSb.append("  SHED       : " + textShed.getText().toString() + "\n");
           // contentSb.append("  -----------------------------\n");
            //contentSb.append("  REF.ID     : KGS  TIME\r\n");
            //contentSb.append("  -----------------------------\n");

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
            //startManagingCursor(accounts);
            Cursor account  = db.rawQuery("SELECT * FROM " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.AgentNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.ACaptureTime +" <='" + textTransTime.getText().toString() + "' and "+ Database.ADataCaptureDevice +"='" + DataDevice +"' and "+ Database.AReceiptNo +"='" + SessionNo +"' ORDER BY ACaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                while(account.moveToNext()) {
                    // update view
                    count=count+1;
                    BagCount = String.valueOf(count);
                /*ReceiptNo = account.getString(account.getColumnIndex(Database.DataCaptureDevice))+ account.getString(account
                        .getColumnIndex(Database.ReceiptNo));*/


                    Kgs = df1.format(Double.parseDouble(account.getString(account.getColumnIndex(Database.AQuantity))));
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
                    //contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                }
            }
            account.close();




            contentSb.append("  -----------------------------\n");
            contentSb.append("  BAGS       : " + textBags.getText().toString()+ "\n");
            //contentSb.append("  GROSS WT   : " + df1.format(Double.parseDouble(textGrossWt.getText().toString())) + "\n");
           // contentSb.append("  TARE WT    : " + df1.format(Double.parseDouble(textTareWt.getText().toString())) + "\n");
            contentSb.append("  NET WT     : " +df1.format(Double.parseDouble(textNetWt.getText().toString())) + "\n");
            //contentSb.append("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n");

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
            if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                // go back to milkers activity
                // Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
            } else {
                sendData(senddata);
            }

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

                    PrintSimpleReceipt();
                }


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
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
            Cursor accounts = db.query(true, Database.ASESSION_TABLE_NAME, null, null, null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
            String from [] = {  Database.ROW_ID,Database.ASessionAgentNo, Database.ASessionDevice , Database.ASessionBags, Database.ASessionDate};
            int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date};


            ca  = new SimpleCursorAdapter(this,R.layout.receipt_agent_list, accounts,from,to);

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
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getSearch(){

        try {

            SQLiteDatabase db= dbhelper.getReadableDatabase();
            //Cursor accounts = db.query(true, Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null, null, null, null, null, null, null, null);
            Cursor accounts=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
           /* if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD){
                ca.swapCursor(accounts);
            } else {
                ca.changeCursor(accounts);
            }*/
            if ( accounts.getCount() == 0 )
                Toast.makeText(this, "no records", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();
            while ( accounts.moveToNext()) {
                String from[] = {Database.ROW_ID, Database.FarmerNo, Database.DataCaptureDevice, Database.ReceiptNo, Database.CollDate};
                int to[] = {R.id.txtAccountId, R.id.tv_number, R.id.tv_device, R.id.tv_reciept, R.id.tv_date};


                ca = new SimpleCursorAdapter(this, R.layout.receipt_list, accounts, from, to);

                ListView listfarmers = (ListView) this.findViewById(R.id.lvReciepts);
                //  listfarmers.invalidateViews();
                ca.notifyDataSetChanged();
                listfarmers.setAdapter(ca);
                // listfarmers.setTextFilterEnabled(true);
                db.close();
                dbhelper.close();
            }

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private String[] getArray(ArrayList<BluetoothDevice> data) {
        String[] list = new String[0];

        if (data == null) return list;

        int size	= data.size();
        list		= new String[size];

        for (int i = 0; i < size; i++) {
            list[i] = data.get(i).getName();
        }

        return list;
    }
    private void updateDeviceList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, getArray(mDeviceList));

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        mDeviceSp.setAdapter(adapter);
        mDeviceSp.setSelection(0);
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

                updateDeviceList();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);

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
    private String getDate(){

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        // mIntent = new Intent(FarmerDetailsActivity.this,MainActivity.class);
        //startActivity(mIntent);
        return;
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
            finish();

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result)
        {

            //dialog.dismiss();

            mIntent = new Intent(getApplicationContext(),AgentRecieptsActivity.class);
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


            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
