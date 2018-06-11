package com.easyweigh.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.helpers.DirectoryChooserDialog;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by Michael on 30/06/2016.
 */
@SuppressWarnings("ALL")
public class ExportActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btnExport;
    Intent mIntent;
    File file;
    String path;
    Handler _handler;
    private int progressStatus = 0;
    int count = 0;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    ArcProgress arcProgress;
    private ProgressBar progressBar;
    private TextView textView,txtFNo;
    private Button pickFrom,pickTo;
    EditText etFrom,etTo,etFarmerNo;
    String  fromDate,toDate,farmerNo,batchNo;
    String  condition = " _id > 0 ";
    String  condition1 = " _id > 0 ";
    AlertDialog b;
    private Button btnSearchReceipt;
    public SimpleCursorAdapter ca;
    static SharedPreferences prefs;
    SQLiteDatabase db;

    private Button btnFilter;
    ListView listReciepts;
    String BatchDate;
    TextView textBatchNo, textBatchDate, textDelNo, textStatus;
    String cond;
    SearchView searchView;
    int closed=1;
    int closed1 = 1;
    int cloudid = 0;
    String DelNo;
    String error;
    String BatchNo;
    String FileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_export);
        setupToolbar();
        initializer();
        _handler = new Handler();
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


    public void initializer() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        FileName=format2.format(cal.getTime());
        prefs = PreferenceManager.getDefaultSharedPreferences(ExportActivity.this);
        dbhelper = new DBHelper(getApplicationContext());
        db = dbhelper.getReadableDatabase();
        btnExport = (Button) findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnExport.setEnabled(false);

                    showSearchReceipt();
                btnExport.setEnabled(true);


            }


        });
        btnFilter = (Button) findViewById(R.id.btnFilter);
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
                textBatchNo = (TextView) selectedView.findViewById(R.id.tv_reciept);
                textBatchDate = (TextView) selectedView.findViewById(R.id.tv_date);
                textDelNo = (TextView) selectedView.findViewById(R.id.tv_number);
                Log.d("Accounts", "Selected Account Id : " + textBatchNo.getText().toString());
                showRecieptDetails();
            }
        });
        String selectQuery = "SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE SignedOff=1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() <= 0) {
            Toast.makeText(ExportActivity.this, "No Batch Dispatched to Export !!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        cursor.close();
        showSearchReceipt();
    }
    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Batches");
        etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
        etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
        etTo.setVisibility(View.GONE);
        etFarmerNo = (EditText) dialogView.findViewById(R.id.edtFarmerNo);
        TextView To= (TextView) dialogView.findViewById(R.id.to);
        To.setVisibility(View.GONE);
        Date date = new Date(getDate());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
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
        pickTo.setVisibility(View.GONE);
        pickTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment2();
                newFragment.show(getFragmentManager(), "datePicker");

            }
        });


        btnSearchReceipt = (Button) dialogView.findViewById(R.id.btn_SearchReceipt);
        btnSearchReceipt.setVisibility(View.VISIBLE);
        btnSearchReceipt.setText("SEARCH BATCH");
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
                    condition += " and  " + Database.BatchDate + " = '" + fromDate + "'";
                if (closed > 0)
                    condition += " and  " + Database.Closed + " = '" + closed + "'";
                    condition += " and  " + Database.SignedOff + "='" + closed + "'";

                //getSearch();
                ca.getFilter().filter(condition.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String reciept = constraint.toString();
                        return dbhelper.SearchBatchByDate(reciept);
                    }
                });

                b.dismiss();
            }
        });



        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

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
    public void showRecieptDetails() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_listclosedbatches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("All Weighment Receipts");
        dbhelper = new DBHelper(this);
        db = dbhelper.getReadableDatabase();

        BatchNo = textBatchNo.getText().toString();
        String dbtBatchOn = textBatchDate.getText().toString() + " 00:00:00";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fmt.parse(dbtBatchOn);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        BatchDate = format1.format(date);
        if (BatchDate.length() > 0)
            cond += " and  " + Database.CollDate + " = '" + BatchDate + "'";

        if (BatchNo.length() > 0)
            cond += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";

        searchView = (SearchView) dialogView.findViewById(R.id.searchView);
        searchView.setQueryHint("Search Farmer No ...");
        searchView.setVisibility(View.GONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String FarmerNo = constraint.toString();
                        return dbhelper.SearchSpecificOnR(FarmerNo, cond);

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
                        String FarmerNo = constraint.toString();
                        return dbhelper.SearchOnR(FarmerNo);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });


        Cursor accounts = db.rawQuery("select * from " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "'", null);
        TextView txtStatus = (TextView) dialogView.findViewById(R.id.textStatus);

        if (accounts.getCount() == 0) {
            txtStatus.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.GONE);
        } else {
            //Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}


            final DecimalFormat df = new DecimalFormat("#0.0#");
            Cursor c = db.rawQuery("select " +
                    "" + Database.DataCaptureDevice +
                    ",COUNT(" + Database.ROW_ID + ")" +
                    ",SUM(" + Database.Tareweight + ")" +
                    ",SUM(" + Database.Quantity + ")" +
                    " from FarmersProduceCollection WHERE "
                    + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "'", null);
            if (c != null) {

                c.moveToFirst();
                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText("Weighments: " + df.format(c.getDouble(1)) + "\n" +
                        "Net Weight: " + df.format(c.getDouble(3)) + " Kgs.");

            }
            c.close();

        }
        while (accounts.moveToNext()) {
            String from[] = {Database.ROW_ID, Database.FarmerNo, Database.Quantity};
            int to[] = {R.id.txtAccountId, R.id.tv_number, R.id.tv_phone};


            ca = new SimpleCursorAdapter(dialogView.getContext(), R.layout.z_list, accounts, from, to);

            ListView listfarmers = (ListView) dialogView.findViewById(R.id.lvUsers);
            listfarmers.setAdapter(ca);
            listfarmers.setTextFilterEnabled(true);
            //db.close();
            //dbhelper.close();
        }


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


            }
        });
        dialogBuilder.setNegativeButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLUE);
        b.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        b.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);

        b.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#4285F4'>Are you sure you want to export csv ?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                b.dismiss();
                                new ExportFileAsync().execute();

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                b.dismiss();

                            }
                        });
                final AlertDialog alert2 = builder.create();
                alert2.show();
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (wantToCloseDialog)
                    b.dismiss();
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

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
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
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
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
            etTo.setText(format1.format(chosenDate));
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

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Data Export ...");
                mProgressDialog.setMessage("Exporting data from file ...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
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
            btnExport.setVisibility(View.GONE);
            listReciepts.setVisibility(View.GONE);
            btnFilter.setVisibility(View.GONE);

            File dbFile=getDatabasePath(DBHelper.DB_NAME);
            //DBHelper dbhelper = new DBHelper(getApplicationContext());
            File exportDir = new File(Environment.getExternalStorageDirectory()+ "/" + "/Easyweigh/Exports/");
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

             file = new File(exportDir, "Transactions.csv");
        }

        @Override
        protected String doInBackground(String... aurl) {

            try
            {
                BatchNo = textBatchNo.getText().toString();
                DelNo = textDelNo.getText().toString();
                String dbtBatchOn =textBatchDate.getText().toString()+" 00:00:00";
                String dbtBatchOn1 =textBatchDate.getText().toString();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = fmt.parse(dbtBatchOn);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                BatchDate=format1.format(date);

                if (dbtBatchOn1.length() > 0)
                    condition += " and  " + Database.BatchDate + " = '" + dbtBatchOn1 + "'";

                if (BatchNo.length() > 0)
                    condition += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";

                if (closed1 > 0)
                    condition += " and  " + Database.Closed + " = '" + closed1 + "'";
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + condition + " and SignedOff=1", null);
                count=curCSV.getCount();

                //csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext())
                {
                    //Which column you want to exprort
                    String arrStr[] ={curCSV.getString(0),curCSV.getString(1),curCSV.getString(2), curCSV.getString(3)
                    ,curCSV.getString(4),curCSV.getString(5), curCSV.getString(6)
                    ,curCSV.getString(7),curCSV.getString(8), curCSV.getString(9)
                    ,curCSV.getString(10),curCSV.getString(11), curCSV.getString(12)
                    ,curCSV.getString(13),curCSV.getString(14), curCSV.getString(15)
                    ,curCSV.getString(16),curCSV.getString(17), curCSV.getString(18)};
                    csvWrite.writeNext(arrStr);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }
                curCSV.close();
                Cursor curCSV2 = db.rawQuery("select * from " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                        + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "'  and " + Database.CloudID + " ='" + cloudid + "'", null);
                count=count+curCSV2.getCount();
                //csvWrite.writeNext(curCSV2.getColumnNames());
                while(curCSV2.moveToNext())
                {
                    //Which column you want to exprort
                    String arrStr[] ={curCSV2.getString(0),curCSV2.getString(1),curCSV2.getString(2), curCSV2.getString(3)
                            ,curCSV2.getString(4),curCSV2.getString(5), curCSV2.getString(6)
                            ,curCSV2.getString(7),curCSV2.getString(8), curCSV2.getString(9)
                            ,curCSV2.getString(10),curCSV2.getString(11), curCSV2.getString(12)
                            ,curCSV2.getString(13),curCSV2.getString(14), curCSV2.getString(15)
                            ,curCSV2.getString(16),curCSV2.getString(17), curCSV2.getString(18)};
                    csvWrite.writeNext(arrStr);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }
                csvWrite.close();

                curCSV2.close();
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

            AlertDialog.Builder builder = new AlertDialog.Builder(ExportActivity.this);
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
    class ExportFileAsync2 extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
            arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
            arcProgress.setProgress(0);

            textView = (TextView) findViewById(R.id.textView1);
            btnExport.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... aurl) {


            File dbFile=getDatabasePath(DBHelper.DB_NAME);
            //DBHelper dbhelper = new DBHelper(getApplicationContext());
            File exportDir = new File(Environment.getExternalStorageDirectory()+ "/" + "/Easyweigh/Masters/");
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, "Batches.csv");
            File file2 = new File(exportDir, "ProduceCollection.csv");
            try
            {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME ,null);
                count=curCSV.getCount();
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext())
                {
                    //Which column you want to exprort
                    String arrStr[] ={curCSV.getString(0),curCSV.getString(1),curCSV.getString(2), curCSV.getString(3)
                            ,curCSV.getString(4),curCSV.getString(5), curCSV.getString(6)
                            ,curCSV.getString(7),curCSV.getString(8), curCSV.getString(9)
                            ,curCSV.getString(10),curCSV.getString(11), curCSV.getString(12)
                            ,curCSV.getString(13),curCSV.getString(14), curCSV.getString(15)
                            ,curCSV.getString(16),curCSV.getString(17), curCSV.getString(18)};
                    csvWrite.writeNext(arrStr);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }
                csvWrite.close();
                curCSV.close();
                file2.createNewFile();
                CSVWriter csvWrite1 = new CSVWriter(new FileWriter(file2));
                Cursor curCSV2 = db.rawQuery("SELECT * FROM " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME ,null);
                count=count+curCSV2.getCount();
                csvWrite1.writeNext(curCSV2.getColumnNames());
                while(curCSV2.moveToNext())
                {
                    //Which column you want to exprort
                    String arrStr[] ={curCSV2.getString(0),curCSV2.getString(1),curCSV2.getString(2), curCSV2.getString(3)
                            ,curCSV2.getString(4),curCSV2.getString(5), curCSV2.getString(6)
                            ,curCSV2.getString(7),curCSV2.getString(8), curCSV2.getString(9)
                            ,curCSV2.getString(10),curCSV2.getString(11), curCSV2.getString(12)
                            ,curCSV2.getString(13),curCSV2.getString(14), curCSV2.getString(15)
                            ,curCSV2.getString(16),curCSV2.getString(17), curCSV2.getString(18)};
                    csvWrite1.writeNext(arrStr);

                    progressStatus++;
                    publishProgress("" + progressStatus);
                }
                csvWrite1.close();
                curCSV2.close();
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
            finish();
            //mIntent = new Intent(ExportActivity.this,MainActivity.class);
            //startActivity(mIntent);

            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText(count+" Records Exported successfully");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            //Toast.makeText(ExportMasterActivity.this, "Data Exported successfully!!", Toast.LENGTH_LONG).show();
        }
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

            Cursor accounts = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                    + Database.Closed + " ='" + closed + "' and SignedOff=1", null);
            if (accounts.getCount() > 0) {
                String from [] = {  Database.ROW_ID,Database.DeliveryNoteNumber, Database.DataDevice , Database.BatchNumber, Database.BatchDate};
                int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date};


                ca  = new SimpleCursorAdapter(this,R.layout.batch_list, accounts,from,to);

                ListView listfarmers= (ListView) this.findViewById( R.id.lvReciepts);

                listfarmers.setAdapter(ca);
                listfarmers.setTextFilterEnabled(true);
                db.close();
                //dbhelper.close();
            }
            else{

                new NoReceipt().execute();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void onBackPressed() {
        //Display alert message when back button has been pressed


        finish();
        /*btnExport.setVisibility(View.VISIBLE);
        mIntent = new Intent(ExportActivity.this,MainActivity.class);
       startActivity(mIntent);*/
        return;
    }
    private String getDate(){

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

    private class Restart extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(Void... params)
        {
            finish();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result)
        {



            mIntent = new Intent(getApplicationContext(),ExportActivity.class);
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
                Thread.sleep(50);
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
            text.setText("No Batches Found To Export");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
}