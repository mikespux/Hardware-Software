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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.easyweigh.fragments.TabsFragment;
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
import java.util.Set;

/**
 * Created by Michael on 30/06/2016.
 */
public class BatchRecieptsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    ListView listReciepts;
    String BatchNo,BatchDate;
    TextView textBatchNo,textBatchDate,textDelNo;
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
    String  cond;
    int closed=1;
    AlertDialog b;
    SQLiteDatabase db;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    EditText Trailer,Tractor;
    String strTractor,strTrailer;
    String BatchSerial="";
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
        getSupportActionBar().setTitle(R.string.nav_item_batches);

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
        prefs = PreferenceManager.getDefaultSharedPreferences(BatchRecieptsActivity.this);

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

        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        searchView.setQueryHint("Search Batch No ...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String recieptNo = constraint.toString();
                        return dbhelper.SearchSpecificBatch(recieptNo);

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
                        return dbhelper.SearchBatch(recieptNo);

                    }
                });

                return false;
            }
        });
       searchView.requestFocus();

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

    public void showSearchReceipt() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Batches");
        etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
        etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
        etFarmerNo = (EditText) dialogView.findViewById(R.id.edtFarmerNo);

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
                    condition += " and  " + Database.BatchDate + " >= '" + fromDate + "'";

                if (toDate.length() > 0)
                    condition += " and  " + Database.BatchDate + " <= '" + toDate + "'";

                /*if (closed > 0)
                    condition += " and  " + Database.Closed + " = '" + closed + "'";*/

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
    public void showRecieptDetails() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_updateclosedbatches, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("All Weighment Receipts");
        dbhelper = new DBHelper(this);
         db= dbhelper.getReadableDatabase();

        Trailer = (EditText) dialogView.findViewById(R.id.etTrailer);
        Tractor = (EditText) dialogView.findViewById(R.id.etTractor);
        BatchNo = textBatchNo.getText().toString();
        String dbtBatchOn =textBatchDate.getText().toString()+" 00:00:00";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fmt.parse(dbtBatchOn);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        BatchDate=format1.format(date);
        if (BatchDate.length() > 0)
            cond += " and  " + Database.CollDate + " = '" + BatchDate + "'";

        if (BatchNo.length() > 0)
            cond += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";

        searchView=(SearchView) dialogView.findViewById(R.id.searchView);
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
                        return dbhelper.SearchSpecificOnR(FarmerNo,cond);

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
        Cursor batchinfo = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                + Database.DeliveryNoteNumber + " ='" + textDelNo.getText().toString() + "'", null);
        while ( batchinfo.moveToNext()) {
            Tractor.setText(batchinfo.getString(batchinfo
                    .getColumnIndex(Database.Tractor)));
            Trailer.setText(batchinfo.getString(batchinfo
                    .getColumnIndex(Database.Trailer)));
            BatchSerial=batchinfo.getString(batchinfo
                    .getColumnIndex(Database.DeliveryNoteNumber));
        }

        Cursor accounts = db.rawQuery("select * from " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                + Database.BatchSerial + " ='" + BatchSerial + "'", null);
         TextView txtStatus=(TextView)dialogView.findViewById(R.id.textStatus);

        if ( accounts.getCount() == 0 )
        {
            txtStatus.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.GONE);}
        else {
            //Toast.makeText(this, "records found", Toast.LENGTH_LONG).show();}

//+ Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "'
                final DecimalFormat df = new DecimalFormat("#0.0#");
            final DecimalFormat df1 = new DecimalFormat("##");
                Cursor c = db.rawQuery("select " +
                        ""+Database.DataCaptureDevice+
                        ",COUNT(" + Database.ROW_ID+ ")"+
                        ",SUM(" + Database.Tareweight+ ")"+
                        ",SUM(" + Database.Quantity+ ")"+
                        " from FarmersProduceCollection WHERE "
                        + Database.BatchSerial + " ='" + BatchSerial + "'", null);
                if (c != null) {

                    c.moveToFirst();
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Weighments: "+df1.format(c.getDouble(1))+"\n"+
                                      "Net Weight: "+df.format(c.getDouble(3)) +" Kgs.");

                }
                c.close();

        }
        while ( accounts.moveToNext()) {
            String from[] = {Database.ROW_ID, Database.FarmerNo, Database.Quantity};
            int to[] = {R.id.txtAccountId, R.id.tv_number, R.id.tv_phone};


            ca = new SimpleCursorAdapter(dialogView.getContext(), R.layout.z_list, accounts, from, to);

            ListView listfarmers = (ListView) dialogView.findViewById(R.id.lvUsers);
            listfarmers.setAdapter(ca);
            listfarmers.setTextFilterEnabled(true);
           // db.close();
            //dbhelper.close();
        }






        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


            }
        });

        dialogBuilder.setNegativeButton("Update PlateNo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

       /*dialogBuilder.setNegativeButton("Delete Batch", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                deleteBatch();

            }
        });*/
        final AlertDialog b = dialogBuilder.create();
        b.show();
        b.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
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
                strTractor=Tractor.getText().toString();
                strTractor = strTractor.replace(",","");
                strTrailer=Trailer.getText().toString();
                strTrailer = strTrailer.replace(",","");

                ContentValues values = new ContentValues();
                values.put(Database.Tractor,strTractor);
                values.put(Database.Trailer,strTrailer);
                long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                        "DeliveryNoteNumber = ?", new String[]{textDelNo.getText().toString()});
                if (rows > 0) {
                    Toast.makeText(getApplicationContext(), "Vehicle and Trailer Updated Successfully", Toast.LENGTH_LONG).show();
                }
                b.dismiss();

            }
        });
    }

    public void deleteBatch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Html.fromHtml("<font color='#4285F4'>Are you sure you want to delete this batch?</font>"))
                .setCancelable(false)
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Date date = new Date(getDate());
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("BatchON", format1.format(date));
                        edit.commit();

                        edit.remove("DeliverNoteNumber");
                        edit.commit();
                        deleteCurrentAccount();



                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

    }



    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, "DeliveryNoteNumber=?", new String[]{ textDelNo.getText().toString()});

            if ( rows == 1) {
                Toast.makeText(this, "Batch Deleted Successfully!", Toast.LENGTH_LONG).show();
                String dbtBatchOn =textBatchDate.getText().toString()+" 00:00:00";
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = fmt.parse(dbtBatchOn);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                BatchDate=format1.format(date);
                int rows1 = db.delete(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME,
                        Database.CollDate + "=? AND " + Database.BatchNumber + "=? ", new String[]{BatchDate, textBatchNo.getText().toString()}
                );

                if ( rows1 == 1) {
                    Toast.makeText(this, "Transactions Deleted Successfully!", Toast.LENGTH_LONG).show();

                }
                else{
                  //  Toast.makeText(this, "Could not delete Transactions!", Toast.LENGTH_LONG).show();
                }
                dbhelper.close();
                getdata();
            }
            else
                Toast.makeText(this, "Could not delete Batch!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
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
            //Cursor accounts = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, Database.Closed + "='" + closed + "'", null, null, null, null, null, null);
            Cursor accounts = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
            String from [] = {  Database.ROW_ID,Database.DeliveryNoteNumber, Database.DataDevice , Database.BatchNumber, Database.BatchDate};
            int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date};


            ca  = new SimpleCursorAdapter(this,R.layout.batch_list, accounts,from,to);

            ListView listfarmers= (ListView) this.findViewById( R.id.lvReciepts);

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
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result)
        {



            mIntent = new Intent(getApplicationContext(),BatchRecieptsActivity.class);
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
            text.setText("No Batches Found");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
        }
    }
}
