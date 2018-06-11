package com.easyweigh.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.easyweigh.helpers.Delivary;
import com.easyweigh.printerdata.PocketPos;
import com.easyweigh.printerutils.DataConstants;
import com.easyweigh.printerutils.DateUtil;
import com.easyweigh.printerutils.FontDefine;
import com.easyweigh.printerutils.Printer;
import com.easyweigh.printerutils.Util;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Created by Michael on 30/06/2016.
 */
public class DeliveryReportActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    ListView listReciepts;
    Boolean success = true;
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

    String  fromDate,toDate,farmerNo;
    String  condition = " _id > 0 ";
    AlertDialog b;
    SQLiteDatabase db;
    Cursor accounts;
    DeliveryArrayAdapter ArrayAdapter;
    String TransporterCode,FactoryCode,Transporter,Factory;
    Delivary student;
    DeliveryArrayAdapter.StudentWrapper StudentWrapper = null;
    ArrayList<Delivary> students = new ArrayList<Delivary>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listbatch);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Delivery Report");

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
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryReportActivity.this);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Restart().execute();
            }
        });


        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
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
        final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Deliveries");
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

                if (fromDate.length() > 0)
                    condition += " and  " + Database.FdDate + " >= '" + fromDate + "'";

                if (toDate.length() > 0)
                    condition += " and  " + Database.FdDate + " <= '" + toDate + "'";

                if (farmerNo.length() > 0)
                    condition += " and  " + Database.FdDNoteNum + " = '" + farmerNo + "'";

                getdata();
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

                    contentSb.append("  "+accounts1.getString(accounts1.getColumnIndex(Database.DeliveryNoteNumber))+"     "
                            + accounts1.getString(accounts1.getColumnIndex(Database.TotalWeights))+ "\n");

                }

            }
            contentSb.append("  -----------------------------\n");
            contentSb.append("  TOTAL KGS         " + count.getString(count.getColumnIndex(Database.FdFieldWt))+ "\n");

            contentSb.append("  -----------------------------\n");
            contentSb.append("  You were served by,\n  " + prefs.getString("fullname", "") + "\n");
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

            System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
            offset += content1Byte.length;

            byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);

            sendData(senddata);

        }

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
                        printDelivary();

                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }





    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata(){

        try {

            SQLiteDatabase db= dbhelper.getReadableDatabase();
            ArrayList<Delivary> arraylist = new ArrayList<Delivary>();

            accounts = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " where "+condition+"", null);
            if (accounts.getCount() > 0) {
                while(accounts.moveToNext()) {

                    arraylist.add(new Delivary(accounts.getString(accounts.getColumnIndex(Database.ROW_ID)),accounts.getString(accounts.getColumnIndex(Database.FdDNoteNum)),
                            accounts.getString(accounts.getColumnIndex(Database.FdDate)),
                            accounts.getString(accounts.getColumnIndex(Database.FdFieldWt))));
                }

                ArrayAdapter = new DeliveryArrayAdapter(DeliveryReportActivity.this, R.layout.delivery_list, arraylist);
                listReciepts= (ListView) this.findViewById( R.id.lvReciepts);

                listReciepts.setAdapter(ArrayAdapter);
                ArrayAdapter.notifyDataSetChanged();
                listReciepts.setTextFilterEnabled(true);

                //db.close();
                //dbhelper.close();


            }

            else {

                new NoReceipt().execute();
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

                    //connect();
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

            mIntent = new Intent(getApplicationContext(),DeliveryReportActivity.class);
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
            text.setText("No Deliveries Found!");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
    public class DeliveryArrayAdapter extends ArrayAdapter<Delivary> {

        Context context;
        int layoutResourceId;
        ArrayList<Delivary> students = new ArrayList<Delivary>();

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

            Delivary student = students.get(position);
            StudentWrapper.number.setText(student.getName());
            StudentWrapper.deldate.setText(student.getAge());
            StudentWrapper.totalkgs.setText(student.getAddress());


            StudentWrapper.print.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listReciepts.performItemClick(listReciepts.getAdapter().getView(position, null, null), position, listReciepts.getAdapter().getItemId(0));
                    StudentWrapper.number.getText().toString();
                    //Toast.makeText(getApplicationContext(), StudentWrapper.number.getText().toString(), Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("DNoteNo", StudentWrapper.number.getText().toString());
                    edit.commit();
                    dbhelper = new DBHelper(context);
                    db = dbhelper.getReadableDatabase();
                    if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                        // go back to milkers activity
                        Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                    return;
                    }
                    printReceipt();
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


}
