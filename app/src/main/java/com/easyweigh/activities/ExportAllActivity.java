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
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.easyweigh.helpers.Delivary;
import com.easyweigh.printerdata.PocketPos;
import com.easyweigh.printerutils.DataConstants;
import com.easyweigh.printerutils.DateUtil;
import com.easyweigh.printerutils.FontDefine;
import com.easyweigh.printerutils.Printer;
import com.easyweigh.printerutils.Util;
import com.github.lzyzsd.circleprogress.ArcProgress;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by Michael on 30/06/2016.
 */
public class ExportAllActivity extends AppCompatActivity {
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

    private Button btnSearchReceipt,btnFilter;
    private Button pickFrom,pickTo;

    String  fromDate,toDate,farmerNo;
    String  condition = " _id > 0 ";
    AlertDialog b;
    SQLiteDatabase db;

    DeliveryArrayAdapter ArrayAdapter;
        private int progressStatus = 0;
    int count = 0;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    ArcProgress arcProgress;
    private ProgressBar progressBar;
    private TextView textView,txtFNo;
    String  TicketNo,DNoteNo,DelDate,Factory,Transporter,Vehicle,ArrivalTime,FieldWt ,GrossWt,TareWt,
            RejectWt,QualityScore, DepartureTime,CoPrefix,InternalSerial,UserIdentifier;

    String batchNo, deviceID, stringOpenDate, deliveryNoteNo, userID, userID2, stringOpenTime, weighingSession,
            closedb, stringCloseTime, factory, tractorNo, trailerNo,TransporterCode,DelivaryNo,Co_prefix,Current_User,Quality;
    
    String ColDate, Time, DataDevice, BatchNO, Agent, FarmerNo;
    String WorkerNo, FieldClerk, ProduceCode;
    String VarietyCode, GradeCode, RouteCode, ShedCode;
    String  GrossTotal, TareWeight, UnitCount;
    String UnitPrice, RecieptNo, CanSerial, NetWeight, UsedCard;
    
    SimpleDateFormat dateTimeFormat;
    SimpleDateFormat timeFormat;
    SimpleDateFormat dateFormat;
    SimpleDateFormat dateOnlyFormat;
    SimpleDateFormat BatchDateFormat;

    String DeliveryInfo;
    String BatchDate,BatchSerial;
    TextView textBatchNo, textBatchDate, textDelNo, textStatus;
    String cond;

    int closed=1;
    int closed1 = 1;
    int cloudid = 0;
    String DelNo;
    String error;
    String BatchNo;
    String FileName;
    File file;
    Button btnExport;
    Cursor curCSV;
    Cursor curCSV2;
    Cursor accounts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Export CSV");

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
        btnFilter			= (Button) findViewById(R.id.btnFilter);
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        BatchDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        prefs = PreferenceManager.getDefaultSharedPreferences(ExportAllActivity.this);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Restart().execute();
            }
        });


        showSearchReceipt();
    }



    @Override
    public void onPause() {


        super.onPause();
    }

    @Override
    public void onDestroy() {

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















    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }

    public void exportReceipt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to export csv?")
                .setCancelable(false)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //printDelivary();
                        new ExportFileAsync().execute();

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

                ArrayAdapter = new DeliveryArrayAdapter(ExportAllActivity.this, R.layout.export_list, arraylist);
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

            mIntent = new Intent(getApplicationContext(),ExportAllActivity.class);
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
            text.setText("No Data Found To Export!");
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
            StudentWrapper StudentWrapper = null;

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

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("DNoteNo", student.getName());
            edit.commit();
            edit.putString("DeliveryDate",student.getAge());
            edit.commit();
            StudentWrapper.print.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dbhelper = new DBHelper(context);
                    db = dbhelper.getReadableDatabase();
                    listReciepts.performItemClick(listReciepts.getAdapter().getView(position, null, null), position, listReciepts.getAdapter().getItemId(0));
                    exportReceipt();
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

    class ExportFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
            arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
            arcProgress.setProgress(0);
            arcProgress.setVisibility(View.VISIBLE);
            textView = (TextView) findViewById(R.id.textView1);
            listReciepts.setVisibility(View.GONE);
            btnFilter.setVisibility(View.GONE);

            File dbFile=getDatabasePath(DBHelper.DB_NAME);
            //DBHelper dbhelper = new DBHelper(getApplicationContext());
            File exportDir = new File(Environment.getExternalStorageDirectory()+ "/" + "/Easyweigh/Exports/");
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            String dbtBatchOn =prefs.getString("DeliveryDate", "")+" 00:00:00";
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            Date date1 = null;
            try {
                date1 = fmt.parse(dbtBatchOn);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yyyy");
            String DelDate=format1.format(date1);
            String fileName ="DELNO-"+prefs.getString("DNoteNo", "")+"-ON-"+DelDate+".csv";
           //Transactions.csv
            file = new File(exportDir, fileName);
        }

        @Override
        protected String doInBackground(String... aurl) {

            try {
                DelNo = prefs.getString("DNoteNo", "");

                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                accounts = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
                        + Database.FdDNoteNum + " ='" + DelNo + "'", null);

                count = accounts.getCount();
                if (accounts.getCount() > 0) {
                    accounts.moveToFirst();
                    while (!accounts.isAfterLast()) {
                        TicketNo = accounts.getString(accounts.getColumnIndex(Database.FdWeighbridgeTicket));
                        DNoteNo = accounts.getString(accounts.getColumnIndex(Database.FdDNoteNum));
                        DelDate = accounts.getString(accounts.getColumnIndex(Database.FdDate));
                        Factory = accounts.getString(accounts.getColumnIndex(Database.FdFactory));

                        if (accounts.getString(accounts.getColumnIndex(Database.FdTransporter)) == null) {
                            Transporter = "";
                        } else {
                            Transporter = accounts.getString(accounts.getColumnIndex(Database.FdTransporter));
                        }
                        Vehicle = accounts.getString(accounts.getColumnIndex(Database.FdVehicle));
                        ArrivalTime = accounts.getString(accounts.getColumnIndex(Database.FdArrivalTime));
                        FieldWt = accounts.getString(accounts.getColumnIndex(Database.FdFieldWt));
                        GrossWt = accounts.getString(accounts.getColumnIndex(Database.FdGrossWt));
                        TareWt = accounts.getString(accounts.getColumnIndex(Database.FdTareWt));

                        if (accounts.getString(accounts.getColumnIndex(Database.FdRejectWt)).equals("")) {
                            RejectWt = "0";
                        } else {
                            RejectWt = accounts.getString(accounts.getColumnIndex(Database.FdRejectWt));
                        }
                        if (accounts.getString(accounts.getColumnIndex(Database.FdQualityScore)).equals("")) {
                            QualityScore = "0";
                        } else {
                            QualityScore = accounts.getString(accounts.getColumnIndex(Database.FdQualityScore));
                        }

                        DepartureTime = accounts.getString(accounts.getColumnIndex(Database.FdDepartureTime));

                        CoPrefix = mSharedPrefs.getString("company_prefix", "").toString();
                        InternalSerial = mSharedPrefs.getString("terminalID", "").toString();
                        UserIdentifier = prefs.getString("user", "");


                        String DeliveryInfo[] = {"1",
                                TicketNo,
                                DNoteNo,
                                DelDate,
                                Factory,
                                Transporter,
                                Vehicle,
                                ArrivalTime,
                                FieldWt,
                                GrossWt,
                                TareWt,
                                RejectWt,
                                QualityScore,
                                DepartureTime,
                                CoPrefix,
                                InternalSerial,
                                UserIdentifier +"\r\n"
                        };
                        accounts.moveToNext();
                        csvWrite.writeNext(DeliveryInfo);

                        progressStatus++;
                        publishProgress("" + progressStatus);


                    }


                    curCSV = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + Database.DelivaryNO + " ='" + DelNo + "' and SignedOff=1", null);
                    count = curCSV.getCount();

                    //csvWrite.writeNext(curCSV.getColumnNames());
                    while (curCSV.moveToNext()) {
                        
                        Date openTime = dateTimeFormat.parse(curCSV.getString(curCSV.getColumnIndex(Database.BatchDate)).toString() +
                                " " +
                                curCSV.getString(curCSV.getColumnIndex(Database.OpeningTime)).toString());
                        Date closeTime = dateTimeFormat.parse(curCSV.getString(curCSV.getColumnIndex(Database.BatchDate)).toString() +
                                " " +
                                curCSV.getString(curCSV.getColumnIndex(Database.ClosingTime)).toString());
                        batchNo = curCSV.getString(curCSV.getColumnIndex(Database.BatchNumber));
                        deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
                        stringOpenDate = dateFormat.format(openTime);
                        deliveryNoteNo = curCSV.getString(curCSV.getColumnIndex(Database.DeliveryNoteNumber));
                        userID = curCSV.getString(curCSV.getColumnIndex(Database.Userid));
                        stringOpenTime = timeFormat.format(openTime);
                        if (curCSV.getString(curCSV.getColumnIndex(Database.BatchSession))==null){
                            weighingSession = "1";
                        }else{
                            weighingSession=curCSV.getString(curCSV.getColumnIndex(Database.BatchSession));
                        }
                        closedb = curCSV.getString(curCSV.getColumnIndex(Database.Closed));
                        stringCloseTime = timeFormat.format(closeTime);

                        BatchDate= BatchDateFormat.format(closeTime);

                        factory = curCSV.getString(curCSV.getColumnIndex(Database.Factory));
                        if (curCSV.getString(curCSV.getColumnIndex(Database.Transporter)) ==null){
                            TransporterCode="";
                        }else{
                            TransporterCode=curCSV.getString(curCSV.getColumnIndex(Database.Transporter));
                        }
                        tractorNo = curCSV.getString(curCSV.getColumnIndex(Database.Tractor));
                        trailerNo = curCSV.getString(curCSV.getColumnIndex(Database.Trailer));

                        if (curCSV.getString(curCSV.getColumnIndex(Database.DelivaryNO))==null){
                            DelivaryNo="";
                        }else{
                            DelivaryNo=curCSV.getString(curCSV.getColumnIndex(Database.DelivaryNO));
                        }
                        Co_prefix=mSharedPrefs.getString("company_prefix", "").toString();
                        Current_User=prefs.getString("user", "");
                        BatchSerial = curCSV.getString(curCSV.getColumnIndex(Database.DeliveryNoteNumber));

                     
                        //Which column you want to export
                        String Batches[] = {"2", 
                                batchNo,
                                deviceID,
                                stringOpenDate,
                                deliveryNoteNo,
                                userID,
                                stringOpenTime,
                                weighingSession,
                                closedb, 
                                stringCloseTime,
                                factory,
                                tractorNo,
                                trailerNo,
                                DelivaryNo,
                                TransporterCode,
                                Co_prefix,
                                Current_User+"\r\n"};
                        csvWrite.writeNext(Batches);
                        BatchNo=curCSV.getString(6);
                        progressStatus++;
                        publishProgress("" + progressStatus);

                      // + Database.BatchNumber + " ='" + BatchNo + "' and "+ Database.CollDate + " ='" + stringOpenDate + "'"

                    curCSV2 = db.rawQuery("select * from " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                            + Database.BatchSerial + " ='" + BatchSerial + "'", null);
                    count = count + curCSV2.getCount();
                    //csvWrite.writeNext(curCSV2.getColumnNames());
                    while (curCSV2.moveToNext()) {
                        //Which column you want to export
                        ColDate = curCSV2.getString(curCSV2.getColumnIndex(Database.CollDate));
                       // ColDate = stringOpenDate;
                        //Time= curCSV2.getString(curCSV2.getColumnIndex(Database.CollDate))+" "
                        Time=curCSV2.getString(curCSV2.getColumnIndex(Database.CaptureTime));
                        BatchNo=curCSV2.getString(curCSV2.getColumnIndex(Database.BatchNumber));
                        DataDevice =  mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);;

                       if (curCSV2.getString(curCSV2.getColumnIndex(Database.DataSource))==null){
                            Agent="";
                        }else{
                            Agent=curCSV2.getString(curCSV2.getColumnIndex(Database.DataSource));
                        }
                        FarmerNo=curCSV2.getString(curCSV2.getColumnIndex(Database.FarmerNo));
                        WorkerNo=curCSV2.getString(curCSV2.getColumnIndex(Database.WorkerNo));
                        userID2=curCSV2.getString(curCSV2.getColumnIndex(Database.FieldClerk));
                        ProduceCode=curCSV2.getString(curCSV2.getColumnIndex(Database.DeliveredProduce));
                        VarietyCode=curCSV2.getString(curCSV2.getColumnIndex(Database.ProduceVariety));
                        GradeCode=curCSV2.getString(curCSV2.getColumnIndex(Database.ProduceGrade));
                        RouteCode=curCSV2.getString(curCSV2.getColumnIndex(Database.SourceRoute));
                        ShedCode=curCSV2.getString(curCSV2.getColumnIndex(Database.BuyingCenter));
                        NetWeight=curCSV2.getString(curCSV2.getColumnIndex(Database.Quantity));
                        TareWeight=curCSV2.getString(curCSV2.getColumnIndex(Database.Tareweight));
                        UnitCount=curCSV2.getString(curCSV2.getColumnIndex(Database.LoadCount));
                        UnitPrice=curCSV2.getString(curCSV2.getColumnIndex(Database.UnitPrice));
                        CanSerial = curCSV2.getString(curCSV2.getColumnIndex(Database.Container));
                        RecieptNo =curCSV2.getString(curCSV2.getColumnIndex(Database.DataCaptureDevice))+curCSV2.getString(curCSV2.getColumnIndex(Database.ReceiptNo));
                        UsedCard=curCSV2.getString(curCSV2.getColumnIndex(Database.UsedSmartCard));
                        Co_prefix=mSharedPrefs.getString("company_prefix", "").toString();
                        Current_User=prefs.getString("user", "");

                        if (curCSV2.getString(curCSV2.getColumnIndex(Database.Quality)).equals("0")){
                            Quality=QualityScore;
                        }else{
                            Quality=curCSV2.getString(curCSV2.getColumnIndex(Database.Quality));
                        }



                        String Produces[] = {"3",
                                ColDate,
                                DataDevice,
                                Time,
                                userID2,
                                ProduceCode,
                                RouteCode,
                                ShedCode,
                                FarmerNo,
                                NetWeight  ,
                                TareWeight,
                                UnitCount,
                                CanSerial,
                                RecieptNo,
                                UsedCard,
                                VarietyCode ,
                                GradeCode,
                                UnitPrice,
                                Co_prefix,
                                Agent,
                                Current_User,
                                Quality+"\r\n"};
                        csvWrite.writeNext(Produces);

                        progressStatus++;
                        publishProgress("" + progressStatus);
                    }


                        //curCSV2.close();

                        //curCSV.close();


                        //csvWrite.close();
                    }
                    curCSV.close();
                    //curCSV2.close();
                    accounts.close();
                    csvWrite.close();
                }

            }
                catch(Exception sqlEx)
                {
                    Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
                }




            return null;

        }
        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setMax(count);
            arcProgress.setBottomText("EXPORTING ...");
            textView.setText(Integer.parseInt(progress[0]) + "/" + count+" Records");
        }

        @Override
        protected void onPostExecute(String unused) {
            // dismissDialog(DIALOG_DOWNLOAD_PROGRESS);

            //mIntent = new Intent(ExportActivity.this,MainActivity.class);
            //startActivity(mIntent);

            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText(count + " Records Exported successfully");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            finish();
            AlertDialog.Builder builder = new AlertDialog.Builder(ExportAllActivity.this);
            builder.setMessage(Html.fromHtml("<font color='#000000'>Do you want to locate the exported file ?</font>"))
                    .setCancelable(false)
                    .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            openFolder();

                        }
                    }).setPositiveButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    dialog.cancel();
                    finish();

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);

            // b.dismiss();
            //Toast.makeText(ExportMasterActivity.this, "Data Exported successfully!!", Toast.LENGTH_LONG).show();
        }
    }
    public void openFolder()
    {
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+ "/" + "/Easyweigh/Masters/");
        intent.setDataAndType(uri, "text/csv");
        startActivity(Intent.createChooser(intent, "Open folder"));*/
        Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+ "/" + "/Easyweigh/Masters/");
        chooser.addCategory(Intent.CATEGORY_OPENABLE);
        chooser.setDataAndType(uri, "text/csv");
        // startActivity(chooser);
        try {
            // startActivityForResult(chooser, SELECT_FILE);
            startActivity(Intent.createChooser(chooser, "Open folder"));
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
