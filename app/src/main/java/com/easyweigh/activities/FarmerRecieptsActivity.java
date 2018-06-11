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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Michael on 30/06/2016.
 */
public class FarmerRecieptsActivity extends AppCompatActivity {
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
            strRoute,strShed,strTrip,strBags,strGrossWt,strTareWt,strNetWt,strTotalKgs,strClerk,ShedName;
    String  fromDate,toDate,farmerNo;
    String  condition = " _id > 0 ";
    String  cond = " _id > 0 ";
    AlertDialog b;
    AlertDialog alert;
    DecimalFormat formatter;
    String SessionNo,DataDevice;
    String ReceiptNo,BagCount,produce,LeafQuality,initialPrice;
    String Kgs,Tare,Time;

    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat timeFormat;
    TextView NoReceiptFound;

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
        getSupportActionBar().setTitle(R.string.nav_item_farmers);

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
        dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        NoReceiptFound=(TextView) findViewById(R.id.tvNoreceipt);
        mDeviceSp 			= (Spinner) findViewById(R.id.sp_device);
        mConnectBtn			= (Button) findViewById(R.id.btnConnect);
        btnFilter			= (Button) findViewById(R.id.btnFilter);
        mPrintBtn			= (Button) findViewById(R.id.btnPrint);
        formatter = new DecimalFormat("0000");
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(FarmerRecieptsActivity.this);
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
                else if (mSharedPrefs.getString("Language", "Kiswa").toString().equals("Kiswa"))
                {
                    if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
                        PrintGenKiswaReceipt();

                    }

                }
                else {
                    PrintDetailedReceipt();
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

                    showGeneralRecieptDetails();



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
        mPrintBtn.setVisibility(View.VISIBLE);

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
        final View dialogView = inflater.inflate(R.layout.dialog_search_receipt, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Receipt");
        etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
        etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
        etFarmerNo = (EditText) dialogView.findViewById(R.id.edtFarmerNo);

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
                farmerNo = etFarmerNo.getText().toString();

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("fromDate", fromDate);
                edit.commit();
                edit.putString("toDate", toDate);
                edit.commit();
                edit.putString("farmerNo", farmerNo);
                edit.commit();
                if (farmerNo.equals("")) {

                    Context context = FarmerRecieptsActivity.this;
                    LayoutInflater inflater = FarmerRecieptsActivity.this.getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Enter Farmer No ...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(FarmerRecieptsActivity.this, "Please Enter Farmer No", Toast.LENGTH_LONG).show();


                    return;
                }
                if (fromDate.length() > 0)
                    condition += " and  " + Database.SessionDate + " >= '" + fromDate + "'";

                if (toDate.length() > 0)
                    condition += " and  " + Database.SessionDate + " <= '" + toDate + "'";

                if (farmerNo.length() > 0)
                    condition += " and  " + Database.SessionFarmerNo + " = '" + farmerNo + "'";

                if ( fromDate.length() > 0)
                    cond += " and  " + Database.CollDate + " >= '" + fromDate + "'";

                if ( toDate.length() > 0)
                    cond += " and  " + Database.CollDate + " <= '" + toDate + "'";

                if ( farmerNo.length() > 0)
                    cond += " and  " + Database.FarmerNo + " = '" + farmerNo + "'";

                getSearch();

               /* ca.getFilter().filter(condition.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String reciept = constraint.toString();
                        return dbhelper.SearchRecieptByDate(reciept);
                    }
                });*/

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
        String time		= DateUtil.timeMilisToString(milis, "HH:mm a");
        String titleStr	= "\n"+produce+" RECEIPT" + "\n"
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

        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


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
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns, Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                textName.setText(c.getString(c.getColumnIndex(Database.F_FARMERNAME)));
                textTotalKgs.setText(c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));
            }

            textRoute.setText(session.getString(session.getColumnIndex(Database.SessionRoute)));
            textReciept.setText(session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo);
            try {
                ReceiptDate = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransDate.setText(format1.format(ReceiptDate));
            try {
                ReceiptTime = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransTime.setText(format2.format(ReceiptTime));
            DataDevice=(session.getString(session
                    .getColumnIndex(Database.SessionDevice)));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session
                    .getColumnIndex(Database.SessionFarmerNo)));
            textBags.setText(session.getString(session
                    .getColumnIndex(Database.SessionBags)));

            grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare)))
                    +Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
            textGrossWt.setText(df.format(grossWeight));

            textTareWt.setText(session.getString(session
                    .getColumnIndex(Database.SessionTare)));
            textNetWt.setText(session.getString(session
                    .getColumnIndex(Database.SessionNet)));

            Cursor s  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "' and "+ Database.DataCaptureDevice +"='" + DataDevice +"' and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if(s!=null)
            {
                s.moveToFirst();

                textShed.setText(s.getString(s.getColumnIndex(Database.BuyingCenter)));


            }

            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  TIME       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  TERMINAL   : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  FARMER NO  : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  ROUTE      : " + textRoute.getText().toString() + "\n");
            contentSb.append("  SHED       : " + textShed.getText().toString() + "\n");
          //  contentSb.append("  -----------------------------\n");
            //contentSb.append("  REF.ID     : KGS  TIME\r\n");
            //contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'"+
                    " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "'" +
                    " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);

            if (account.getCount() > 0) {
                int count=0;

                while(account.moveToNext()) {
                    // update view
                    count=count+1;
                    BagCount = String.valueOf(count);
                /*ReceiptNo = account.getString(account.getColumnIndex(Database.DataCaptureDevice))+ account.getString(account
                        .getColumnIndex(Database.ReceiptNo));*/

                    Kgs = account.getString(account.getColumnIndex(Database.Quantity));
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
            }
            account.close();




            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + textBags.getText().toString()+ "\n");
            contentSb.append("  GROSS WT   : " + textGrossWt.getText().toString() + "\n");
            contentSb.append("  TARE WT    : " + textTareWt.getText().toString() + "\n");
            contentSb.append("  NET WT     : " +textNetWt.getText().toString() + "\n");
            contentSb.append("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n");
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

                    PrintGenericReceipt();
                }


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public void PrintGenKiswaReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details_kiswa, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("STAKABADTHI YA MAUZO");
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
        String time		= DateUtil.timeMilisToString(milis, "HH:mm a");
        String titleStr	= "\nSTAKABADTHI YA MAUZO\n"+produce+"\n"
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

        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


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
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns, Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                textName.setText(c.getString(c.getColumnIndex(Database.F_FARMERNAME)));
                textTotalKgs.setText(c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));
            }

            textRoute.setText(session.getString(session.getColumnIndex(Database.SessionRoute)));
            textReciept.setText(session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo);
            try {
                ReceiptDate = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransDate.setText(format1.format(ReceiptDate));
            try {
                ReceiptTime = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransTime.setText(format2.format(ReceiptTime));
            DataDevice=(session.getString(session.getColumnIndex(Database.SessionDevice)));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session.getColumnIndex(Database.SessionFarmerNo)));
            textBags.setText(session.getString(session.getColumnIndex(Database.SessionBags)));

            grossWeight=Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionTare)))
                    +Double.parseDouble(session.getString(session.getColumnIndex(Database.SessionNet)));
            textGrossWt.setText(df.format(grossWeight));

            textTareWt.setText(session.getString(session.getColumnIndex(Database.SessionTare)));
            textNetWt.setText(session.getString(session.getColumnIndex(Database.SessionNet)));
            initialPrice = mSharedPrefs.getString("buyingPrice", "").toString();

            Cursor s  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "' and "+ Database.DataCaptureDevice +"='" + DataDevice +"' and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if(s!=null)
            {
                s.moveToFirst();
                String shedcode=s.getString(s.getColumnIndex(Database.BuyingCenter));
                Cursor shedname=db.rawQuery("select MccName from CollectionCenters where MccNo= '"+shedcode+"' ", null);
                if(shedname!=null)
                {
                    shedname.moveToFirst();
                    textShed.setText(shedname.getString(shedname.getColumnIndex("MccName")));
                }


            }
            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  NAMBA: " + ReceiptNo + "\n");
            contentSb.append("  TAREHE     : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  MUDA       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  MZANI      : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  MKULIMA    : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  NJIA       : " + textRoute.getText().toString() + "\n");
            contentSb.append("  KITUO      : " + textShed.getText().toString() + "\n");
            //  contentSb.append("  -----------------------------\n");
            //contentSb.append("  REF.ID     : KGS  TIME\r\n");
            //contentSb.append("  -----------------------------\n");

        /*Cursor account = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null,
                " FarmerNo = ?", new String[] { textFarmerNo.getText().toString() }, null, null, null);*/
            //startManagingCursor(accounts);
            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    "and "+ Database.CaptureTime +" <='" +session.getString(session.getColumnIndex(Database.SessionTime)) + "' and "+ Database.DataCaptureDevice +"='" + DataDevice +"' and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if(s!=null)
            {
                s.moveToFirst();

                textShed.setText(s.getString(s.getColumnIndex(Database.BuyingCenter)));


            }
            if (account.getCount() > 0) {
                int count=0;

                while(account.moveToNext()) {
                    count=count+1;
                    BagCount = String.valueOf(count);
                    Kgs = account.getString(account.getColumnIndex(Database.Quantity));
                    Tare = account.getString(account.getColumnIndex(Database.Tareweight));
                    Time = account.getString(account.getColumnIndex(Database.CaptureTime));
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

            contentSb.append("  BEI YA AWALI   : " +initialPrice + "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("  MIFUKO         : " + textBags.getText().toString()+ "\n");
            //contentSb.append("  JUMLA YA KILO  : " + textGrossWt.getText().toString() + "\n");
            //contentSb.append("  MFUKO NA UMANDE: " + textTareWt.getText().toString() + "\n");
            contentSb.append("  UZITO HALISI   : " +textNetWt.getText().toString() + "\n");
            //contentSb.append("  NYONGEZA YA UZITO    : " + textTotalKgs.getText().toString() + "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("  JINA LA KARANI: " + prefs.getString("fullname", "") + "\n");
            // contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
           // contentSb.append("  "+Util.nameLeftValueRightJustify("Simu ya mhudumu Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
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


            }
        });
        dialogBuilder.setNegativeButton("Print", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
                    // go back to milkers activity
                    Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                    //check scale version before printing

                    PrintGenKiswaReceipt();
                }


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
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
        String time		= DateUtil.timeMilisToString(milis, "HH:mm a");
        String titleStr	= "\n"+produce+" RECEIPT" + "\n"
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

        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


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

                textName.setText(c.getString(c.getColumnIndex(Database.F_FARMERNAME)));
                textTotalKgs.setText(c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));
            }

            textRoute.setText(session.getString(session.getColumnIndex(Database.SessionRoute)));

            textReciept.setText(session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo);
            DataDevice=(session.getString(session.getColumnIndex(Database.SessionDevice)));
            try {
                ReceiptDate = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransDate.setText(format1.format(ReceiptDate));
            try {
                ReceiptTime = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransTime.setText(format2.format(ReceiptTime));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session.getColumnIndex(Database.SessionFarmerNo)));
            textBags.setText(session.getString(session.getColumnIndex(Database.SessionBags)));

            Cursor s  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "'" +
                    " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
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
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  TIME       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  TERMINAL   : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  FARMER NO  : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  ROUTE      : " + textRoute.getText().toString() + "\n");
            contentSb.append("  SHED       : " + textShed.getText().toString() + "\n");
            contentSb.append("  -----------------------------\n");
            contentSb.append("  REF.ID     : KGS  TIME\r\n");
            contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'"+
                    " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "'" +
                    " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
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
                    try {
                        ReceiptTime = dateTimeFormat.parse(account.getString(account.getColumnIndex(Database.CaptureTime)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Time = format2.format(ReceiptTime);
                    LeafQuality= account.getString(account.getColumnIndex(Database.Quality));

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

                strNetWt=df.format(netweight);
                strTareWt=df.format(tare);
                strGrossWt=df.format(netweight+tare);
                strBags=String.valueOf(count);
            }
            account.close();
            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + strBags+ "\n");
            contentSb.append("  GROSS WT   : " + strGrossWt + "\n");
            contentSb.append("  TARE WT    : " + strTareWt + "\n");
            contentSb.append("  NET WT     : " + strNetWt + "\n");
            contentSb.append("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n");
            if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                //contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }else
                {
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
            if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
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


            }
        });
        dialogBuilder.setNegativeButton("Print", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

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
    public void PrintSimpleDetailedReceipt() {
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
        String time		= DateUtil.timeMilisToString(milis, "HH:mm a");
        String titleStr	= "\n"+produce+" RECEIPT" + "\n"
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

        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


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

                textName.setText(c.getString(c.getColumnIndex(Database.F_FARMERNAME)));
                textTotalKgs.setText(c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));
            }

            textRoute.setText(session.getString(session.getColumnIndex(Database.SessionRoute)));

            textReciept.setText(session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo);
            DataDevice=(session.getString(session.getColumnIndex(Database.SessionDevice)));
            try {
                ReceiptDate = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransDate.setText(format1.format(ReceiptDate));
            try {
                ReceiptTime = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransTime.setText(format2.format(ReceiptTime));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session.getColumnIndex(Database.SessionFarmerNo)));
            textBags.setText(session.getString(session.getColumnIndex(Database.SessionBags)));

            Cursor s  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "'" +
                    " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
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
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  TIME       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  TERMINAL   : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  FARMER NO  : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  ROUTE      : " + textRoute.getText().toString() + "\n");
            contentSb.append("  SHED       : " + textShed.getText().toString() + "\n");
            contentSb.append("  -----------------------------\n");
            contentSb.append("  REF.ID     : KGS  TIME\r\n");
            contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'"+
                    " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "'" +
                    " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
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
                    try {
                        ReceiptTime = dateTimeFormat.parse(account.getString(account.getColumnIndex(Database.CaptureTime)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Time = format2.format(ReceiptTime);
                    LeafQuality= account.getString(account.getColumnIndex(Database.Quality));

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

                strNetWt=df.format(netweight);
                strTareWt=df.format(tare);
                strGrossWt=df.format(netweight+tare);
                strBags=String.valueOf(count);
            }
            account.close();
            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + strBags+ "\n");
            contentSb.append("  GROSS WT   : " + strGrossWt + "\n");
            contentSb.append("  TARE WT    : " + strTareWt + "\n");
            contentSb.append("  NET WT     : " + strNetWt + "\n");
            if(!mSharedPrefs.getBoolean("enableQuality", false)==true) {
                //contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }else{
            contentSb.append("  QUALITY    : " + LeafQuality + "\n");
            }
            //contentSb.append("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n");

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
            if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
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


            }
        });
        dialogBuilder.setNegativeButton("Print", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
                    // go back to milkers activity
                    Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                    //check scale version before printing

                    PrintSimpleDetailedReceipt();
                }


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public void PrintSimpleReceipt() {
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
        String time		= DateUtil.timeMilisToString(milis, "HH:mm a");
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

        textClerk = (TextView) dialogView.findViewById(R.id.textClerk);
        textClerk.setText(prefs.getString("user", ""));


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

                textName.setText(c.getString(c.getColumnIndex(Database.F_FARMERNAME)));
                textTotalKgs.setText(c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));
            }

            textRoute.setText(session.getString(session.getColumnIndex(Database.SessionRoute)));


            textReciept.setText(session.getString(session
                    .getColumnIndex(Database.SessionDevice))+SessionNo);
            DataDevice=(session.getString(session
                    .getColumnIndex(Database.SessionDevice)));
            try {
                ReceiptDate = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransDate.setText(format1.format(ReceiptDate));
            try {
                ReceiptTime = dateTimeFormat.parse(session.getString(session.getColumnIndex(Database.SessionTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textTransTime.setText(format2.format(ReceiptTime));
            textTerminal.setText(prefs.getString("terminalID", ""));
            textFarmerNo.setText(session.getString(session
                    .getColumnIndex(Database.SessionFarmerNo)));
            textBags.setText(session.getString(session
                    .getColumnIndex(Database.SessionBags)));

            Cursor s  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'" +
                    " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                    " and "+ Database.CaptureTime +" <='" +session.getString(session.getColumnIndex(Database.SessionTime))+ "'" +
                    " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
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
            contentSb.append("  \n-----------------------------\n");

            contentSb.append("  RECEIPT: " + ReceiptNo + "\n");
            contentSb.append("  DATE       : " + textTransDate.getText().toString() + "\n");
            contentSb.append("  TIME       : " + textTransTime.getText().toString() + "\n");
            contentSb.append("  TERMINAL   : " + textTerminal.getText().toString() + "\n");
            contentSb.append("  FARMER NO  : " +textFarmerNo.getText().toString() + "\n");
            contentSb.append("   " + textName.getText().toString() + "\n");
            contentSb.append("  ROUTE      : " + textRoute.getText().toString() + "\n");
            contentSb.append("  SHED       : " + textShed.getText().toString() + "\n");
            //  contentSb.append("  -----------------------------\n");
            //contentSb.append("  REF.ID     : KGS  TIME\r\n");
            //contentSb.append("  -----------------------------\n");

            Cursor account  = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                    + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'"+
                    " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                    " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "'" +
                    " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                    " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if (account.getCount() > 0) {
                int count=0;
                while(account.moveToNext()) {
                    // update view
                    count=count+1;
                    BagCount = String.valueOf(count);
                /*ReceiptNo = account.getString(account.getColumnIndex(Database.DataCaptureDevice))+ account.getString(account
                        .getColumnIndex(Database.ReceiptNo));*/


                    Kgs = account.getString(account.getColumnIndex(Database.Quantity));
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
            }
            account.close();




            contentSb.append("  -----------------------------\n");
            contentSb.append("  UNITS      : " + textBags.getText().toString()+ "\n");
            //contentSb.append("  GROSS WT   : " + textGrossWt.getText().toString() + "\n");
            //contentSb.append("  TARE WT    : " + textTareWt.getText().toString() + "\n");
            contentSb.append("  NET WT     : " +textNetWt.getText().toString() + "\n");
            //contentSb.append("  MTD KGS    : " + textTotalKgs.getText().toString() + "\n");
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

    public void showGeneralRecieptDetails() {

        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();

        fromDate = prefs.getString("fromDate", "");
        toDate = prefs.getString("toDate", "");
        farmerNo = prefs.getString("farmerNo", "");

        if ( fromDate.length() > 0)
            cond += " and  " + Database.CollDate + " >= '" + fromDate + "'";

        if ( toDate.length() > 0)
            cond += " and  " + Database.CollDate + " <= '" + toDate + "'";

        if ( farmerNo.length() > 0)
            cond += " and  " + Database.FarmerNo + " = '" + farmerNo + "'";

        Cursor account=db.rawQuery("select " +
                ""+Database.DataCaptureDevice+
                ","+Database.ReceiptNo+"" +
                ","+Database.CollDate+"" +
                ","+Database.CaptureTime+"" +
                ","+Database.DataCaptureDevice+"" +
                ","+Database.FarmerNo+"" +
                ","+Database.SourceRoute+"" +
                ","+Database.ReceiptNo+"" +
                ","+Database.BuyingCenter+"" +
                ","+Database.BatchNumber+"" +
                ",SUM(" + Database.Tareweight+ ")"+
                ",SUM(" + Database.Quantity+ ")"+
                ",COUNT(" + Database.FarmerNo+ ")"+
                " from FarmersProduceCollection where " + cond + "", null);


        if ( account.getCount() == 0 )
        {
            Toast.makeText(getApplication(), "no records to print", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            if (account.moveToFirst()) {

                strReciept = account.getString(account.getColumnIndex(Database.DataCaptureDevice))
                        + account.getString(account.getColumnIndex(Database.ReceiptNo));
                strTransDate = account.getString(account.getColumnIndex(Database.CollDate));
                strTransTime = account.getString(account.getColumnIndex(Database.CaptureTime));
                strTerminal = prefs.getString("terminalID", "");
                strFarmerNo = account.getString(account.getColumnIndex(Database.FarmerNo));
                strRoute = account.getString(account.getColumnIndex(Database.SourceRoute));
                strShed = account.getString(account.getColumnIndex(Database.BuyingCenter));
                //textShed.setText(s.getString(s.getColumnIndex(Database.BuyingCenter)));
                Cursor shedname=db.rawQuery("select MccName from CollectionCenters where MccNo ='"+strShed+"'", null);
                if(shedname.getCount()>0)
                {
                    shedname.moveToFirst();
                    ShedName=shedname.getString(shedname.getColumnIndex("MccName"));
                }
                strTrip = account.getString(account.getColumnIndex(Database.BatchNumber));
                strBags = String.valueOf(account.getInt(12));
                grossWeight = account.getDouble(10) + account.getDouble(11);
                strGrossWt = df.format(grossWeight);
                strTareWt = df.format(account.getDouble(10));
                strNetWt = df.format(account.getDouble(11));

                String[] allColumns = new String[]{Database.F_FARMERNAME, Database.F_PRODUCE_KG_TODATE};
                Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns, Database.F_FARMERNO + "='" + account.getString(account
                                .getColumnIndex(Database.FarmerNo)) + "'", null, null, null, null,
                        null);
                if (c != null) {
                    if ( c.getCount() == 0 )
                    {
                        Toast.makeText(getApplication(), Html.fromHtml("<font color='#FFFFFF'>No general receipt selected !!</font>"), Toast.LENGTH_LONG).show();
                        return;
                    }
                    c.moveToFirst();

                    strName = c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                    strTotalKgs = c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
                }

                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("textReciept", strReciept);
                edit.commit();
                edit.putString("textTransDate", strTransDate);
                edit.commit();
                edit.putString("textTransTime", strTransTime);
                edit.commit();
                edit.putString("textTerminal", strTerminal);
                edit.commit();
                edit.putString("textFarmerNo", strFarmerNo);
                edit.commit();
                edit.putString("textName", strName);
                edit.commit();
                edit.putString("textRoute", strRoute);
                edit.commit();
                edit.putString("textShed", ShedName);
                edit.commit();
                edit.putString("textTrip", strTrip);
                edit.commit();
                edit.putString("textBags", strBags);
                edit.commit();
                edit.putString("textGrossWt", strGrossWt);
                edit.commit();
                edit.putString("textTareWt", strTareWt);
                edit.commit();
                edit.putString("textNetWt", strNetWt);
                edit.commit();
                edit.putString("textTotalKgs", strTotalKgs);
                edit.commit();
            }
            account.close();
           //db.close();
           //dbhelper.close();


        }
        printReceipt();
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
                        if (mSharedPrefs.getString("Language", "Kiswa").toString().equals("Kiswa"))
                        {
                            if (mSharedPrefs.getString("receiptTemplates", "Generic").toString().equals("Generic")) {
                                printGenKisReceipt();

                            }

                        }else{
                        printGenReceipt();
                        }

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
            Cursor accounts= db.query(true, Database.SESSION_TABLE_NAME, null, null, null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
            String from [] = {  Database.ROW_ID,Database.SessionFarmerNo, Database.SessionReceipt , Database.SessionBags, Database.SessionDate};
            int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date};

            ca  = new SimpleCursorAdapter(this,R.layout.receipt_list, accounts,from,to);

            listReciepts= (ListView) this.findViewById( R.id.lvReciepts);
            listReciepts.setAdapter(ca);
            listReciepts.setTextFilterEnabled(true);
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
            Cursor accounts = db.query(true, Database.SESSION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
                String from [] = {  Database.ROW_ID,Database.SessionFarmerNo, Database.SessionReceipt , Database.SessionBags, Database.SessionDate};
                int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date};

                ca  = new SimpleCursorAdapter(this,R.layout.receipt_list, accounts,from,to);
                listReciepts= (ListView) this.findViewById( R.id.lvReciepts);
                listReciepts.setAdapter(ca);
                db.close();
                dbhelper.close();
            }
            else{
                listReciepts.setVisibility(View.GONE);
                NoReceiptFound.setVisibility(View.VISIBLE);
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


    private void printGenReceipt() {

        Double grossWeight=0.0;
        final DecimalFormat df = new DecimalFormat("#0.0#");
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
        Date ReceiptDate = null;
        Date ReceiptTime = null;
        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();

        fromDate = prefs.getString("fromDate", "");
        toDate = prefs.getString("toDate", "");
        farmerNo = prefs.getString("farmerNo", "");

        if ( fromDate.length() > 0)
            cond += " and  " + Database.CollDate + " >= '" + fromDate + "'";

        if ( toDate.length() > 0)
            cond += " and  " + Database.CollDate + " <= '" + toDate + "'";

        if ( farmerNo.length() > 0)
            cond += " and  " + Database.FarmerNo + " = '" + farmerNo + "'";

        Cursor account=db.rawQuery("select * from FarmersProduceCollection where " + cond + "", null);
        if ( account.getCount() == 0 )
        {
            Toast.makeText(getApplication(), "no records to print", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            if (account.moveToFirst()) {
                // update view
                strReciept = account.getString(account.getColumnIndex(Database.DataCaptureDevice))
                        + account.getString(account.getColumnIndex(Database.ReceiptNo));
                try {
                    ReceiptDate = dateTimeFormat.parse(account.getString(account.getColumnIndex(Database.CaptureTime)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                strTransDate = format1.format(ReceiptDate);
                try {
                    ReceiptTime = dateTimeFormat.parse(account.getString(account.getColumnIndex(Database.CaptureTime)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                strTransTime = format2.format(ReceiptTime);

                strTerminal = prefs.getString("terminalID", "");
                strFarmerNo = account.getString(account.getColumnIndex(Database.FarmerNo));
                strRoute = account.getString(account.getColumnIndex(Database.SourceRoute));
                strShed = account.getString(account.getColumnIndex(Database.BuyingCenter));
                Cursor shedname = db.rawQuery("select MccName from CollectionCenters where MccNo= '" + strShed + "' ", null);
                if (shedname != null) {
                    shedname.moveToFirst();

                    ShedName = shedname.getString(shedname.getColumnIndex("MccName"));
                }
                String[] allColumns = new String[]{Database.F_FARMERNAME, Database.F_PRODUCE_KG_TODATE};
                Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns, Database.F_FARMERNO + "='" + account.getString(account
                                .getColumnIndex(Database.FarmerNo)) + "'", null, null, null, null,
                        null);
                if (c != null) {
                    if (c.getCount() == 0) {
                        Toast.makeText(getApplication(), Html.fromHtml("<font color='#FFFFFF'>No general receipt selected !!</font>"), Toast.LENGTH_LONG).show();
                        return;
                    }
                    c.moveToFirst();

                    strName = c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                    strTotalKgs = c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
                }

                long milis = System.currentTimeMillis();
                String date = DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
                String time = DateUtil.timeMilisToString(milis, "HH:mm a");
                String titleStr = "\n*** RECEIPT SLIP ***" + "\n"
                        + mSharedPrefs.getString("company_name", "").toString() + "\n"
                        + "P.O. Box " + mSharedPrefs.getString("company_letterbox", "").toString() + "-" + mSharedPrefs.getString("company_postalcode", "").toString() + ", " +
                        mSharedPrefs.getString("company_postalname", "").toString() + "\n" +
                        Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n";

                StringBuilder contentSb = new StringBuilder();
                contentSb.append("  \n-----------------------------\n");

                contentSb.append("  RECEIPT:  SUMMARY\n");
                contentSb.append("  DATE       : " +strTransDate + "\n");
                contentSb.append("  TIME       : " + strTransTime + "\n");
                contentSb.append("  TERMINAL   : " + strTerminal + "\n");
                contentSb.append("  FARMER NO  : " + strFarmerNo + "\n");
                contentSb.append("   "+ strName + "\n");
                contentSb.append("  ROUTE      : " + strRoute + "\n");
                contentSb.append("  SHED       : " + strShed + "\n");
                contentSb.append("  -----------------------------\n");
                contentSb.append("  REF.ID     : KGS  TIME\r\n");
                contentSb.append("  -----------------------------\n");
                int count=0;
                double netweight=0.0;
                double tare=0.0;
                Cursor accounts=db.rawQuery("select * from FarmersProduceCollection where " + cond + "", null);
                    while(accounts.moveToNext()) {

                        count=count+1;
                        BagCount = String.valueOf(count);

                        Kgs = df.format(Double.parseDouble(accounts.getString(accounts.getColumnIndex(Database.Quantity))));
                        Tare = accounts.getString(accounts.getColumnIndex(Database.Tareweight));
                        try {
                            ReceiptTime = dateTimeFormat.parse(accounts.getString(accounts.getColumnIndex(Database.CaptureTime)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Time = format2.format(ReceiptTime);

                        contentSb.append("   " + BagCount + "           " + Kgs + "  " + Time + "\n");
                        netweight=netweight+Double.parseDouble(accounts.getString(accounts.getColumnIndex(Database.Quantity)));
                        tare=tare+Double.parseDouble(accounts.getString(accounts.getColumnIndex(Database.Tareweight)));
                     }
                strBags = String.valueOf(count);
                grossWeight = netweight + tare;
                strGrossWt = df.format(grossWeight);
                strTareWt = df.format(tare);
                strNetWt = df.format(netweight);

                contentSb.append("  -----------------------------\n");

                contentSb.append("  UNITS      : " +strBags + "\n");
                contentSb.append("  GROSS WT   : " + strGrossWt + "\n");
                contentSb.append("  TARE WT    : " + strTareWt + "\n");
                contentSb.append("  NET WT     : " + strNetWt + "\n");
                contentSb.append("  MTD KGS    : " + strTotalKgs + "\n");


                contentSb.append("  -----------------------------\n");
                contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
                contentSb.append("  " + Util.nameLeftValueRightJustify("Enquiry Call:", "" + mSharedPrefs.getString("company_posttel", "").toString(), DataConstants.RECEIPT_WIDTH) + "\n");
                contentSb.append("\n");
                contentSb.append("\n");


                byte[] titleByte = Printer.printfont(titleStr, FontDefine.FONT_32PX, FontDefine.Align_CENTER,
                        (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

                byte[] content1Byte = Printer.printfont(contentSb.toString(), FontDefine.FONT_32PX, FontDefine.Align_LEFT,
                        (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);


                byte[] totalByte = new byte[titleByte.length + content1Byte.length];

                int offset = 0;
                System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
                offset += titleByte.length;

                System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
                offset += content1Byte.length;

                byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

                sendData(senddata);
            }
           // account.close();

            //db.close();
            //dbhelper.close();
            }
    }
    private void printGenKisReceipt() {
        long milis		= System.currentTimeMillis();
        String date		= DateUtil.timeMilisToString(milis, "MMM dd, yyyy");
        String time		= DateUtil.timeMilisToString(milis, "HH:mm a");
        String titleStr	= "\nSTAKABADTHI YA MAUZO"+ "\n"
                +mSharedPrefs.getString("company_name", "").toString() + "\n"
                +"P.O. Box "+mSharedPrefs.getString("company_letterbox", "").toString()+"-" +mSharedPrefs.getString("company_postalcode", "").toString()+", "+
                mSharedPrefs.getString("company_postalname", "").toString() + "\n"+
                Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH)+ "\n";

        StringBuilder contentSb	= new StringBuilder();
        initialPrice = mSharedPrefs.getString("buyingPrice", "").toString();

        contentSb.append("  \n-----------------------------\n");
        contentSb.append("  NAMBA    : JUMLA\n");
        contentSb.append("  TAREHE   : " + prefs.getString("textTransDate", "") + "\n");
        contentSb.append("  MUDA     : " + prefs.getString("textTransTime", "") + "\n");
        contentSb.append("  MZANI    : " + prefs.getString("textTerminal", "") + "\n");
        contentSb.append("  MKULIMA  : " + prefs.getString("textFarmerNo", "") + "\n");
        contentSb.append("  " + prefs.getString("textName", "") + "\n");
        contentSb.append("  NJIA     : " + prefs.getString("textRoute", "") + "\n");
        contentSb.append("  KITUO    : " + prefs.getString("textShed", "") + "\n");
        contentSb.append("  -----------------------------\n");
        contentSb.append("  BEI YA AWALI   : " +initialPrice + "\n");
        contentSb.append("  -----------------------------\n");
        contentSb.append("  MIFUKO         : " + prefs.getString("textBags", "")+ "\n");
       // contentSb.append("  MFUKO NA UMANDE: " + prefs.getString("textTareWt", "") + "\n");
        contentSb.append("  UZITO HALISI   : " + prefs.getString("textNetWt", "") + "\n");
        contentSb.append("  -----------------------------\n");
        contentSb.append("  JINA LA KARANI: " + prefs.getString("fullname", "") + "\n");
        // contentSb.append("  PRINT DATE : "+Util.nameLeftValueRightJustify(date, time, DataConstants.RECEIPT_WIDTH) + "\n");
        //contentSb.append("  "+Util.nameLeftValueRightJustify("Enquiry Call:",""+mSharedPrefs.getString("company_posttel", "").toString() , DataConstants.RECEIPT_WIDTH) + "\n");
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
        return;
    }

    private class Restart extends AsyncTask<Void, Void, String>
    {

        @Override
        protected void onPreExecute()
        {
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
            mIntent = new Intent(getApplicationContext(),FarmerRecieptsActivity.class);
            startActivity(mIntent);
        }
    }
    private class NoReceipt extends AsyncTask<Void, Void, String>
    {

        @Override
        protected void onPreExecute()
        {

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
