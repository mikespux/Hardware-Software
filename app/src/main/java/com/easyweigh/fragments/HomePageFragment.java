package com.easyweigh.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
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
import com.easyweigh.activities.ImportMasterActivity;
import com.easyweigh.activities.MainActivity;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class HomePageFragment extends Fragment {

    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;
    private TextView dateDisplay,txtCompanyInfo,dtpBatchOn,textClock,txtBatchNo;
    Button btnBatchOn,btnBatchOff,btnCloseBatch;
    String BatchDate,DeliverNoteNumber,DataDevice, BatchNumber, UserID, OpeningTime;
    String ClosingTime,NoOfWeighments,TotalWeights,Factory,strTractor,strTrailer,SignedOff,SignedOffTime
            ,BatchSession,BatchCount,Dispatched;
    String BatchOn,DNumber;
    static SharedPreferences mSharedPrefs,prefs;
    DBHelper dbhelper;
    SQLiteDatabase db;
    int BatchNo=1;
    DecimalFormat formatter;
    Spinner Spinnersession;
    String BSession;
    AlertDialog b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_homepage, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        initializer();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);

        return mView;
    }

    public void initializer(){
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
       formatter = new DecimalFormat("00");
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dateDisplay = (TextView)mView.findViewById(R.id.date);
        textClock = (TextView)mView.findViewById(R.id.textClock);
        dtpBatchOn= (TextView)mView.findViewById(R.id.dtpBatchOn);
        txtBatchNo= (TextView)mView.findViewById(R.id.txtBatchNo);
        txtCompanyInfo=(TextView)mView.findViewById(R.id.txtCompanyInfo);
        btnBatchOn = (Button) mView.findViewById(R.id.btnBatchOn);
        btnBatchOff = (Button) mView.findViewById(R.id.btnBatchOff);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("DeliverNoteNumber", txtBatchNo.getText().toString());
        edit.commit();

        //Setting TextView to the current date
        dateDisplay.setText(this.getDate());
        UserID=prefs.getString("user", "");
        String fromProd = prefs.getString("FromProd", "");
        if(fromProd.equals("Agents")){

            //  String selectQuery = "SELECT BatchDate,DeliveryNoteNumber FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Userid ='" + UserID + "' AND Closed =0";
            String selectQuery = "SELECT BatchDate,DeliveryNoteNumber FROM " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Closed =0";
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    BatchOn=(cursor.getString(0));
                    DNumber=(cursor.getString(1));
                } while (cursor.moveToNext());
                // SharedPreferences.Editor edit = prefs.edit();
                edit.putString("AgentBatchON", BatchOn);
                edit.commit();
                dtpBatchOn.setText(BatchOn);
                txtBatchNo.setText(DNumber);
                btnBatchOff.setVisibility(View.VISIBLE);
                btnBatchOn.setVisibility(View.GONE);
            }
            else{
                dtpBatchOn.setText(prefs.getString("basedate", ""));
                txtBatchNo.setText("No Batch Opened");
                btnBatchOn.setVisibility(View.VISIBLE);
                btnBatchOff.setVisibility(View.GONE);
                edit.putString("DeliverNoteNumb", txtBatchNo.getText().toString());
                edit.commit();
            }
            cursor.close();

        }else
        {

      //  String selectQuery = "SELECT BatchDate,DeliveryNoteNumber FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Userid ='" + UserID + "' AND Closed =0";
        String selectQuery = "SELECT BatchDate,DeliveryNoteNumber FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE Closed =0";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                BatchOn=(cursor.getString(0));
                DNumber=(cursor.getString(1));
            } while (cursor.moveToNext());
           // SharedPreferences.Editor edit = prefs.edit();
            edit.putString("BatchON", BatchOn);
            edit.commit();
            dtpBatchOn.setText(BatchOn);
            txtBatchNo.setText(DNumber);
            btnBatchOff.setVisibility(View.VISIBLE);
            btnBatchOn.setVisibility(View.GONE);
        }
        else{
            dtpBatchOn.setText(prefs.getString("basedate", ""));
            txtBatchNo.setText("No Batch Opened");
            btnBatchOn.setVisibility(View.VISIBLE);
            btnBatchOff.setVisibility(View.GONE);
            edit.putString("DeliverNoteNumber", txtBatchNo.getText().toString());
            edit.commit();
        }
        cursor.close();
       // db.close();
       }

        Calendar cal = Calendar.getInstance();
        String year=String.format("%d",cal.get(Calendar.YEAR));
        txtCompanyInfo.setText(mSharedPrefs.getString("company_name", "").toString()+" ©"+year);


        btnBatchOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String CLOSED = "1";
                Cursor count = db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                        + Database.Closed + " ='" + CLOSED + "'", null);
                if (count.getCount() > 10) {

                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Sorry! Batch Allocation Exhausted!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    //customtoast.show();
                    //return;
                }
                String selectQuery2 = "SELECT * FROM " + Database.Fmr_FactoryDeliveries + " WHERE FdStatus=0";
                Cursor cursor1 = db.rawQuery(selectQuery2, null);

                if (cursor1.moveToFirst()) {

                    //Toast.makeText(getActivity(), "Complete Pending Delivery !!", Toast.LENGTH_LONG).show();
                    //return;
                }
                DataDevice = mSharedPrefs.getString("terminalID", "").toString();
                if (DataDevice.equals("")) {
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Set Terminal ID in Settings To Open a Batch");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    return;
                }
                String fromProd = prefs.getString("FromProd", "");
                if(fromProd.equals("Agents")){
                   ShowAgentOpenBatch();
                }else
                {
                    ShowOpenBatch();
                }


            }
        });
        btnBatchOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectQuery = "SELECT * FROM " + Database.Fmr_FactoryDeliveries + " WHERE FdStatus=0";
                Cursor cursor = db.rawQuery(selectQuery, null);

                /*if (cursor.moveToFirst()) {
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
                }*/


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                // Setting Dialog Title
                dialogBuilder.setTitle("Close Batch?");
                // Setting Dialog Message
                dialogBuilder.setMessage("Are you sure you want to close a Batch?");

                // Setting Positive "Yes" Button
                dialogBuilder.setNegativeButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                                if (!mSharedPrefs.getBoolean("enableShifts", false) == true) {
                                    // go back to milkers activity
                                    //Toast.makeText(getActivity(), "Shifts not enabled on settings", Toast.LENGTH_LONG).show();
                                    String fromProd = prefs.getString("FromProd", "");
                                    if(fromProd.equals("Agents")){
                                        ShowCloseAgentBatch();
                                    }else
                                    {
                                        ShowCloseBatch();
                                    }

                                }
                                else
                                {
                                    dialog.cancel();
                                    AlertDialog.Builder dialogSession = new AlertDialog.Builder(getContext());
                                    LayoutInflater inflater1 = getActivity().getLayoutInflater();
                                    final View dialogView1 = inflater1.inflate(R.layout.dialog_close_batch, null);
                                    dialogSession.setView(dialogView1);
                                    dialogSession.setCancelable(true);
                                    dialogSession.setTitle("Batch Session");
                                    Spinnersession = (Spinner) dialogView1.findViewById(R.id.spinnerSession);
                                    Spinnersession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                        @Override
                                        public void onItemSelected(AdapterView<?> arg0, View adapter,
                                                                   int position, long arg3) {
                                            TextView tv = (TextView) adapter;
                                            if (position % 2 == 1) {
                                                // Set the item background color
                                                tv.setTextSize(26);
                                                tv.setTextColor(Color.BLUE);
                                                tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                                            } else {
                                                // Set the alternate item background color
                                                tv.setTextSize(26);
                                                tv.setTextColor(Color.BLUE);
                                                tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                                            }

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> arg0) {
                                            // TODO Auto-generated method stub

                                        }
                                    });

                                    dialogSession.setPositiveButton("Close Batch", new DialogInterface.OnClickListener() {
                                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //do something with edt.getText().toString();


                                        }
                                    });

                                    dialogSession.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        @Override
                                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                Toast.makeText(getActivity(), "Please Select Batch Session", Toast.LENGTH_LONG).show();
                                                return true;
                                            }
                                            return false;
                                        }
                                    });

                                    b = dialogSession.create();
                                    b.show();
                                    b.getButton(AlertDialog.BUTTON_POSITIVE).setPadding(10,10,10,10);
                                    b.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                                    b.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.BLUE);
                                    b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (Spinnersession.getSelectedItem().toString().equals("Select ...")) {
                                                Toast.makeText(getContext(), "Please Select Session", Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            if (Spinnersession.getSelectedItem().toString().equals("Morning")) {

                                                BSession = "1";

                                            } else if (Spinnersession.getSelectedItem().toString().equals("Mid-Morning")) {

                                                BSession = "2";
                                            } else if (Spinnersession.getSelectedItem().toString().equals("Afternoon")) {

                                                BSession = "3";
                                            } else if (Spinnersession.getSelectedItem().toString().equals("Evening")) {

                                                BSession = "3";
                                            } else {

                                                BSession = "1";
                                            }


                                            String dbtBatchOn = dtpBatchOn.getText().toString() + " 00:00:00";
                                            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                                            Date date = null;
                                            try {
                                                date = fmt.parse(dbtBatchOn);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                                            BatchDate = format1.format(date);
                                            BatchNumber = prefs.getString("BatchNumber", "");
                                            // Toast.makeText(getActivity(), BatchDate, Toast.LENGTH_LONG).show();
                                            Cursor count = db.rawQuery("select * from " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                                                    + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNumber + "'", null);
                                            if (count.getCount() > 0) {
                                                final DecimalFormat df = new DecimalFormat("#0.0#");
                                                final DecimalFormat df1 = new DecimalFormat("##");
                                                Cursor c = db.rawQuery("select " +
                                                        "" + Database.DataCaptureDevice +
                                                        ",COUNT(" + Database.ROW_ID + ")" +
                                                        ",SUM(" + Database.Tareweight + ")" +
                                                        ",SUM(" + Database.Quantity + ")" +
                                                        " from FarmersProduceCollection WHERE "
                                                        + Database.CollDate + " ='" + BatchDate + "'and " + Database.BatchNumber + " ='" + BatchNumber + "'", null);
                                                if (c != null) {

                                                    c.moveToFirst();

                                                    NoOfWeighments = df1.format(c.getDouble(1));
                                                    TotalWeights = df.format(c.getDouble(3));

                                                }
                                                c.close();
                                                DeliverNoteNumber = txtBatchNo.getText().toString();
                                                Calendar cal = Calendar.getInstance();
                                                SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                                                ClosingTime = format2.format(cal.getTime());
                                                // ClosingTime = textClock.getText().toString();


                                                ContentValues values = new ContentValues();
                                                values.put(Database.Closed, 1);
                                                values.put(Database.ClosingTime, ClosingTime);
                                                values.put(Database.NoOfWeighments, NoOfWeighments);
                                                values.put(Database.TotalWeights, TotalWeights);
                                                values.put(Database.BatchSession, BSession);


                                                long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                                                        "DeliveryNoteNumber = ?", new String[]{DeliverNoteNumber});
                                                if (rows > 0) {
                                                    SharedPreferences.Editor edit = prefs.edit();
                                                    edit.remove("DeliverNoteNumber");
                                                    edit.remove("BatchON");
                                                    edit.commit();
                                                    Context context = getActivity();
                                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                                    View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                                                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                                    text.setText("Closed Batch " + DeliverNoteNumber + "" +
                                                            "\nNo Of Weighments " + NoOfWeighments + "" +
                                                            "\nTotal Weights " + TotalWeights + " Kgs" +
                                                            "\nSuccessfully at " + ClosingTime);
                                                    Toast customtoast = new Toast(context);
                                                    customtoast.setView(customToastroot);
                                                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                                    customtoast.setDuration(Toast.LENGTH_LONG);
                                                    customtoast.show();
                                                    //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
                                                }
                                                btnBatchOn.setVisibility(View.VISIBLE);
                                                btnBatchOff.setVisibility(View.GONE);
                                                getActivity().finish();
                                                mIntent = new Intent(getActivity(), MainActivity.class);
                                                startActivity(mIntent);


                                            } else {
                                                Context context = getActivity();
                                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                                                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                                                text.setText("Sorry! Could Not Close Empty Batch!");
                                                Toast customtoast = new Toast(context);
                                                customtoast.setView(customToastroot);
                                                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                                                customtoast.setDuration(Toast.LENGTH_LONG);
                                                customtoast.show();


                                                deleteBatch();

                                            }

                                            Boolean wantToCloseDialog = false;
                                            //Do stuff, possibly set wantToCloseDialog to true then...
                                            if (wantToCloseDialog)
                                                b.dismiss();
                                        }
                                    });


                                }


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

    }
    public void ShowOpenBatch()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        // Setting Dialog Title
        dialogBuilder.setTitle("Open Batch?");
        // Setting Dialog Message
        dialogBuilder.setMessage("Are you sure you want to open a Batch?");

        // Setting Positive "Yes" Button
        dialogBuilder.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        BatchDate = prefs.getString("basedate", "");
                        Date date = new Date(getDate());
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                        String dateBatch = format.format(date);

                        Cursor count = db.rawQuery("select * from FarmersSuppliesConsignments WHERE BatchDate ='" + BatchDate + "'", null);
                        if (count.getCount() > 0) {
                            Cursor c = db.rawQuery("select MAX(BatchNumber) from FarmersSuppliesConsignments WHERE BatchDate ='" + BatchDate + "'", null);
                            if (c != null) {

                                c.moveToFirst();

                                BatchNo = Integer.parseInt(c.getString(0)) + 1;
                                BatchNumber = formatter.format(BatchNo);

                            }
                            c.close();
                        } else {
                            BatchNumber = formatter.format(BatchNo);

                        }
                        DeliverNoteNumber = DataDevice + dateBatch + BatchNumber;
                        UserID = prefs.getString("user", "");

                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                        OpeningTime = format2.format(cal.getTime());

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("DeliverNoteNumber", DeliverNoteNumber);
                        edit.commit();
                        edit.putString("BatchNumber", BatchNumber);
                        edit.commit();

                        dbhelper.AddBatch(BatchDate, DeliverNoteNumber, DataDevice, BatchNumber, UserID, OpeningTime);
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime);
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        // Toast.makeText(getActivity(), "Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime, Toast.LENGTH_LONG).show();
                        btnBatchOff.setVisibility(View.VISIBLE);
                        btnBatchOn.setVisibility(View.GONE);
                        getActivity().finish();
                        mIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(mIntent);

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
    public void ShowCloseBatch(){
        String dbtBatchOn = dtpBatchOn.getText().toString() + " 00:00:00";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fmt.parse(dbtBatchOn);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        BatchDate = format1.format(date);
        BatchNumber = prefs.getString("BatchNumber", "");
        // Toast.makeText(getActivity(), BatchDate, Toast.LENGTH_LONG).show();
        Cursor count = db.rawQuery("select * from " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                + Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNumber + "'", null);
        if (count.getCount() > 0) {
            final DecimalFormat df = new DecimalFormat("#0.0#");
            final DecimalFormat df1 = new DecimalFormat("##");
            Cursor c = db.rawQuery("select " +
                    "" + Database.DataCaptureDevice +
                    ",COUNT(" + Database.ROW_ID + ")" +
                    ",SUM(" + Database.Tareweight + ")" +
                    ",SUM(" + Database.Quantity + ")" +
                    " from FarmersProduceCollection WHERE "
                    + Database.CollDate + " ='" + BatchDate + "'and " + Database.BatchNumber + " ='" + BatchNumber + "'", null);
            if (c != null) {

                c.moveToFirst();

                NoOfWeighments = df1.format(c.getDouble(1));
                TotalWeights = df.format(c.getDouble(3));

            }
            c.close();
            DeliverNoteNumber = txtBatchNo.getText().toString();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
            ClosingTime = format2.format(cal.getTime());
            // ClosingTime = textClock.getText().toString();


            ContentValues values = new ContentValues();
            values.put(Database.Closed, 1);
            values.put(Database.ClosingTime, ClosingTime);
            values.put(Database.NoOfWeighments, NoOfWeighments);
            values.put(Database.TotalWeights, TotalWeights);


            long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                    "DeliveryNoteNumber = ?", new String[]{DeliverNoteNumber});
            if (rows > 0) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("DeliverNoteNumber");
                edit.remove("BatchON");
                edit.commit();
                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Closed Batch " + DeliverNoteNumber + "" +
                        "\nNo Of Weighments " + NoOfWeighments + "" +
                        "\nTotal Weights " + TotalWeights + " Kgs" +
                        "\nSuccessfully at " + ClosingTime);
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
            }
            btnBatchOn.setVisibility(View.VISIBLE);
            btnBatchOff.setVisibility(View.GONE);
            getActivity().finish();
            mIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(mIntent);


        } else {
            Context context = getActivity();
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Sorry! Could Not Close Empty Batch!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();


            deleteBatch();

        }
    }

    public void ShowAgentOpenBatch()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        // Setting Dialog Title
        dialogBuilder.setTitle("Open Batch?");
        // Setting Dialog Message
        dialogBuilder.setMessage("Are you sure you want to open a Batch?");

        // Setting Positive "Yes" Button
        dialogBuilder.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        BatchDate = prefs.getString("basedate", "");
                        Date date = new Date(getDate());
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                        String dateBatch = format.format(date);

                        Cursor count = db.rawQuery("select * from AgentsSuppliesConsignments WHERE BatchDate ='" + BatchDate + "'", null);
                        if (count.getCount() > 0) {
                            Cursor c = db.rawQuery("select MAX(BatchNumber) from AgentsSuppliesConsignments WHERE BatchDate ='" + BatchDate + "'", null);
                            if (c != null) {

                                c.moveToFirst();

                                BatchNo = Integer.parseInt(c.getString(0)) + 1;
                                BatchNumber = formatter.format(BatchNo);

                            }
                            c.close();
                        } else {
                            BatchNumber = formatter.format(BatchNo);

                        }
                        DeliverNoteNumber = DataDevice + dateBatch + BatchNumber;
                        UserID = prefs.getString("user", "");

                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
                        OpeningTime = format2.format(cal.getTime());

                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("DeliverNoteNumb", DeliverNoteNumber);
                        edit.commit();
                        edit.putString("BatchNumb", BatchNumber);
                        edit.commit();

                        dbhelper.AddAgtBatch(BatchDate, DeliverNoteNumber, DataDevice, BatchNumber, UserID, OpeningTime);
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime);
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        // Toast.makeText(getActivity(), "Opened Batch: " + DeliverNoteNumber + " Successfully at " + OpeningTime, Toast.LENGTH_LONG).show();
                        btnBatchOff.setVisibility(View.VISIBLE);
                        btnBatchOn.setVisibility(View.GONE);
                        getActivity().finish();
                        mIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(mIntent);

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
    public void ShowCloseAgentBatch(){
        String dbtBatchOn = dtpBatchOn.getText().toString() + " 00:00:00";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fmt.parse(dbtBatchOn);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        BatchDate = format1.format(date);
        BatchNumber = prefs.getString("BatchNumb", "");
        // Toast.makeText(getActivity(), BatchDate, Toast.LENGTH_LONG).show();
        Cursor count = db.rawQuery("select * from " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
                + Database.ACollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNumber + "'", null);
        if (count.getCount() > 0) {
            final DecimalFormat df = new DecimalFormat("#0.0#");
            final DecimalFormat df1 = new DecimalFormat("##");
            Cursor c = db.rawQuery("select " +
                    "" + Database.ADataCaptureDevice +
                    ",COUNT(" + Database.ROW_ID + ")" +
                    ",SUM(" + Database.ATareweight + ")" +
                    ",SUM(" + Database.AQuantity + ")" +
                    " from AgentsProduceCollection WHERE "
                    + Database.ACollDate + " ='" + BatchDate + "'and " + Database.BatchNumber + " ='" + BatchNumber + "'", null);
            if (c != null) {

                c.moveToFirst();

                NoOfWeighments = df1.format(c.getDouble(1));
                TotalWeights = df.format(c.getDouble(3));

            }
            c.close();
            DeliverNoteNumber = txtBatchNo.getText().toString();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
            ClosingTime = format2.format(cal.getTime());


            ContentValues values = new ContentValues();
            values.put(Database.Closed, 1);
            values.put(Database.ClosingTime, ClosingTime);
            values.put(Database.NoOfWeighments, NoOfWeighments);
            values.put(Database.TotalWeights, TotalWeights);


            long rows = db.update(Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
                    "DeliveryNoteNumber = ?", new String[]{DeliverNoteNumber});
            if (rows > 0) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("DeliverNoteNumb");
                edit.remove("BatchON");
                edit.commit();
                Context context = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.white_red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Closed Batch " + DeliverNoteNumber + "" +
                        "\nNo Of Weighments " + NoOfWeighments + "" +
                        "\nTotal Weights " + TotalWeights + " Kgs" +
                        "\nSuccessfully at " + ClosingTime);
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(getActivity(), "Closed Batch "+DeliverNoteNumber +" Successfully at "+ClosingTime, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Sorry! Could not Close Batch!", Toast.LENGTH_LONG).show();
            }
            btnBatchOn.setVisibility(View.VISIBLE);
            btnBatchOff.setVisibility(View.GONE);
            getActivity().finish();
            mIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(mIntent);


        } else {
            Context context = getActivity();
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View customToastroot = inflater.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Sorry! Could Not Close Empty Batch!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();


            deleteBatch();

        }
    }

    public void deleteBatch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml("<font color='#4285F4'>Do you want to delete this empty batch?</font>"))
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

                        String fromProd = prefs.getString("FromProd", "");
                        if(fromProd.equals("Agents")){
                            deleteAgentAccount();
                        }else
                        {
                            deleteCurrentAccount();
                        }
                        getActivity().finish();
                        mIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(mIntent);
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
            DBHelper dbhelper = new DBHelper(getActivity());
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, "DeliveryNoteNumber=?", new String[]{ txtBatchNo.getText().toString()});

            if ( rows == 1) {
                Toast.makeText(getActivity(), "Batch Deleted Successfully!", Toast.LENGTH_LONG).show();
                int rows1 = db.delete(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME,
                        Database.CollDate + "=? AND " + Database.BatchNumber + "=? ", new String[]{BatchDate, BatchNumber}
                );
                dbhelper.close();
                if ( rows1 == 1) {
                    Toast.makeText(getActivity(), "Transactions Deleted Successfully!", Toast.LENGTH_LONG).show();

                }
                else{
                    //Toast.makeText(getActivity(), "No Transactions!", Toast.LENGTH_LONG).show();
                    }
            }
            else
                Toast.makeText(getActivity(), "Could not delete Batch!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
    public void deleteAgentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(getActivity());
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, "DeliveryNoteNumber=?", new String[]{ txtBatchNo.getText().toString()});

            if ( rows == 1) {
                Toast.makeText(getActivity(), "Batch Deleted Successfully!", Toast.LENGTH_LONG).show();
                int rows1 = db.delete(Database.AGENTSPRODUCECOLLECTION_TABLE_NAME,
                        Database.ACollDate + "=? AND " + Database.BatchNumber + "=? ", new String[]{BatchDate, BatchNumber}
                );
                dbhelper.close();
                if ( rows1 == 1) {
                    Toast.makeText(getActivity(), "Transactions Deleted Successfully!", Toast.LENGTH_LONG).show();

                }
                else{
                    //Toast.makeText(getActivity(), "No Transactions!", Toast.LENGTH_LONG).show();
                }
            }
            else
                Toast.makeText(getActivity(), "Could not delete Batch!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    //This is method to call the date and not accessible outside this class
    private String getDate(){

        //A string to hold the current date
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

        //Return the current date
        return currentDateTimeString;
    }

}
