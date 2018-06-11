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
import android.content.ContentValues;
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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
import com.easyweigh.fragments.DelivaryFragment;
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
public class DeliveryEditActivity extends AppCompatActivity {
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
    Cursor accounts;
    DeliveryArrayAdapter ArrayAdapter;
    String TransporterCode,FactoryCode,Transporter,Factory;
    String accountId;
    EditText Trailer,Tractor,etDeliveryNo,etTicketNo,etGroswt,etTarewt,etNet,etRejectwt,etQuality;
    Button btnCloseBatch;
    SQLiteDatabase db;
    String stDelNo,stTicketNo,stGroswt,stTarewt,stNet,stRejectwt,stQuality;
    Double netweight;
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
        prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryEditActivity.this);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Restart().execute();
            }
        });


        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        searchView.requestFocus();

        showSearchReceipt();
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

                ArrayAdapter = new DeliveryArrayAdapter(DeliveryEditActivity.this, R.layout.delivery_list, arraylist);
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

            mIntent = new Intent(getApplicationContext(),DeliveryEditActivity.class);
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
                StudentWrapper.id = (TextView) item.findViewById(R.id.txtAccountId);
                StudentWrapper.number = (TextView) item.findViewById(R.id.tv_number);
                StudentWrapper.deldate = (TextView) item.findViewById(R.id.tv_date);
                StudentWrapper.totalkgs = (TextView) item.findViewById(R.id.txtTotalKgs);
                StudentWrapper.print = (Button) item.findViewById(R.id.btnPrint);

                item.setTag(StudentWrapper);
            } else {
                StudentWrapper = (StudentWrapper) item.getTag();
            }

            student = students.get(position);
            StudentWrapper.id.setText(student.getID());
            StudentWrapper.number.setText(student.getName());
            StudentWrapper.deldate.setText(student.getAge());
            StudentWrapper.totalkgs.setText(student.getAddress());


            StudentWrapper.print.setText("View");
            StudentWrapper.print.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listReciepts.performItemClick(listReciepts.getAdapter().getView(position, null, null), position, listReciepts.getAdapter().getItemId(0));
                    StudentWrapper.id.getText().toString();
                    //Toast.makeText(getApplicationContext(), StudentWrapper.id.getText().toString(), Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("_id", StudentWrapper.id.getText().toString());
                    edit.commit();

                    dbhelper = new DBHelper(context);
                    db = dbhelper.getReadableDatabase();

                    showUpdateDeliveryDialog();
                }
            });




            return item;

        }

        private class StudentWrapper {
            TextView id;
            TextView number;
            TextView deldate;
            TextView totalkgs;
            Button print;

        }

    }
    public void showUpdateDeliveryDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_delivery, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Deliveries");
        accountId = prefs.getString("_id", "");

        etDeliveryNo = (EditText) dialogView.findViewById(R.id.etDeliveryNo);
        etTicketNo = (EditText) dialogView.findViewById(R.id.etTicketNo);
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

        dbhelper = new DBHelper(this);
        db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.Fmr_FactoryDeliveries, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            etDeliveryNo.setText(account.getString(account
                    .getColumnIndex(Database.FdDNoteNum)));
            etTicketNo.setText(account.getString(account
                    .getColumnIndex(Database.FdWeighbridgeTicket)));
            etGroswt.setText(account.getString(account
                    .getColumnIndex(Database.FdGrossWt)));
            etTarewt.setText(account.getString(account
                    .getColumnIndex(Database.FdTareWt)));
            etRejectwt.setText(account.getString(account
                    .getColumnIndex(Database.FdRejectWt)));
            etQuality.setText(account.getString(account
                    .getColumnIndex(Database.FdQualityScore)));



        }
        account.close();
        //db.close();
        //dbhelper.close();



        btnCloseBatch = (Button) dialogView.findViewById(R.id.btnCloseBatch);

        btnCloseBatch = (Button) dialogView.findViewById(R.id.btnCloseBatch);
        btnCloseBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etTicketNo.length() <= 0) {
                    etTicketNo.setError("Enter Ticket No.");
                    return;
                }
                if (etGroswt.length() <= 0) {
                    etGroswt.setError("Enter GrossWT.");
                    return;
                }
                if (etTarewt.length() <= 0) {
                    etTarewt.setError("Enter TareWT.");
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
                builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Update Delivery?</font>"))
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String DNoteNum = prefs.getString("_id", "");


                                Cursor count = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
                                        + Database.ROW_ID + " ='" + DNoteNum + "'", null);

                                if (count.getCount() > 0) {
                                    stDelNo = etDeliveryNo.getText().toString();
                                    stTicketNo = etTicketNo.getText().toString();
                                    stGroswt = etGroswt.getText().toString();
                                    stTarewt = etTarewt.getText().toString();
                                    stNet = etNet.getText().toString();
                                    stRejectwt = etRejectwt.getText().toString();
                                    stQuality = etQuality.getText().toString();

                                    netweight = Double.parseDouble(stNet.toString());
                                    if (netweight <= 0) {
                                        Context context = getApplicationContext();
                                        LayoutInflater inflater = getLayoutInflater();
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



                                    ContentValues values = new ContentValues();
                                    values.put(Database.FdDNoteNum, stDelNo);
                                    values.put(Database.FdWeighbridgeTicket, stTicketNo);
                                    values.put(Database.FdGrossWt, stGroswt);
                                    values.put(Database.FdTareWt, stTarewt);
                                    values.put(Database.FdRejectWt, stRejectwt);
                                    values.put(Database.FdQualityScore, stQuality);

                                    long rows = db.update(Database.Fmr_FactoryDeliveries, values,
                                            Database.ROW_ID + " = ?", new String[]{DNoteNum});

                                    ContentValues value = new ContentValues();
                                    value.put(Database.DelivaryNO, etDeliveryNo.getText().toString());
                                    long rows1 = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, value,
                                            Database.DelivaryNO + " = ?", new String[]{etDeliveryNo.getText().toString()});

                                    if (rows1 >0 && rows > 0) {
                                        Toast.makeText(getApplicationContext(), "Delivery Updated Successfully !!", Toast.LENGTH_LONG).show();
                                        b.dismiss();
                                        getdata();
                                    }

                                                   /* getApplicationContext().finish();
                                                    mIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(mIntent);*/


                                } else {
                                    Context context = getApplicationContext();
                                    LayoutInflater inflater =getLayoutInflater();
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
               // deleteUser();

            }
        });
        /*dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateUsers();
                getdata();



            }
        });*/
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

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
}
