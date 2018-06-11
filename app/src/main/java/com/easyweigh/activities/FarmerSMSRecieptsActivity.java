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
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.printerutils.DataConstants;
import com.easyweigh.printerutils.DateUtil;
import com.easyweigh.printerutils.Util;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Michael on 30/06/2016.
 */
public class FarmerSMSRecieptsActivity extends AppCompatActivity {
    public static final String TAG = "SMS";
    public Toolbar toolbar;
    DBHelper dbhelper;
    ListView listReciepts;
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
    EditText etFrom,etTo,etFarmerNo;
    private Button mConnectBtn;
    private Button btnSearchReceipt,btnFilter;
    private Button pickFrom,pickTo;
     String  fromDate,toDate,farmerNo,FarmerName;
    String  condition = " _id > 0 ";
    String  cond = " _id > 0 ";
    AlertDialog b;
    AlertDialog alert;
    DecimalFormat formatter;
    String SessionNo,DataDevice;
    String ReceiptNo,produce;
    String Kgs,Time;
    ListView listbags;
    String MobileNo,FarmerNo,NetWeight,Bags,Date,TotalKg;
    TextView NoReceiptFound;
    String smsurl;
    String username,password,senderid,textmessage;
    String retval = "";
    String error_code="100";
    String error_desc="";
    String success_desc="";
    SimpleDateFormat dateTimeFormat;
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
        NoReceiptFound=(TextView) findViewById(R.id.tvNoreceipt);
        mConnectBtn			= (Button) findViewById(R.id.btnConnect);
        mConnectBtn.setVisibility(View.GONE);
        btnFilter			= (Button) findViewById(R.id.btnFilter);
        formatter = new DecimalFormat("0000");
        dbhelper = new DBHelper(getApplicationContext());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(FarmerSMSRecieptsActivity.this);
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


                        PrintDetailedReceipt();


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
        final View dialogView = inflater.inflate(R.layout.dialog_search_receipt, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Search Receipt");
        etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
        etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
        etFarmerNo = (EditText) dialogView.findViewById(R.id.edtFarmerNo);
        etFarmerNo.setVisibility(View.GONE);
        TextView txtFNo = (TextView) dialogView.findViewById(R.id.txtFNo);
        txtFNo.setVisibility(View.GONE);

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
               /* if (farmerNo.equals("")) {

                    Context context = FarmerSMSRecieptsActivity.this;
                    LayoutInflater inflater = FarmerSMSRecieptsActivity.this.getLayoutInflater();
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
                }*/
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

                    return true;
                }
                return false;
            }
        });
        dialogBuilder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

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
     public void PrintDetailedReceipt() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_transaction_details_more, null);
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
         SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
         SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
         Date ReceiptDate = null;
         Date ReceiptTime = null;

        Cursor session = db.query(Database.SESSION_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);

         if (session.moveToFirst()) {
            SharedPreferences.Editor edit = prefs.edit();
            SessionNo=session.getString(session.getColumnIndex(Database.SessionCounter));
            String[] allColumns = new String[] {Database.F_FARMERNAME,Database.F_PRODUCE_KG_TODATE,Database.F_MOBILENUMBER};
            Cursor c = db.query(Database.FARMERS_TABLE_NAME, allColumns,Database.F_FARMERNO + "='" + session.getString(session
                            .getColumnIndex(Database.SessionFarmerNo)) + "'", null, null, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();

                textName.setText(c.getString(c.getColumnIndex(Database.F_FARMERNAME)));
                textTotalKgs.setText(c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE)));

                FarmerName=c.getString(c.getColumnIndex(Database.F_FARMERNAME));
                MobileNo=c.getString(c.getColumnIndex(Database.F_MOBILENUMBER));
                TotalKg=c.getString(c.getColumnIndex(Database.F_PRODUCE_KG_TODATE));
            }

            textRoute.setText(session.getString(session.getColumnIndex(Database.SessionRoute)));
            textShed.setText(session.getString(session.getColumnIndex(Database.SessionCounter)));

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
                     + Database.FarmerNo + " ='" + textFarmerNo.getText().toString() + "'"+
                     " and "+ Database.CollDate +" ='" + textTransDate.getText().toString()+ "'"+
                     " and "+ Database.CaptureTime +" <='" + session.getString(session.getColumnIndex(Database.SessionTime)) + "'" +
                     " and "+ Database.DataCaptureDevice +"='" + DataDevice +"'" +
                     " and "+ Database.ReceiptNo +"='" + SessionNo +"' ORDER BY CaptureTime ASC LIMIT '"+ textBags.getText().toString() +"'",null);
            if(s!=null)
            {
                String from[] = {Database.ROW_ID,Database.LoadCount, Database.Quantity, Database.CaptureTime};
                int to[] = {R.id.txtAccountId, R.id.txtBagNo, R.id.txtBagW, R.id.txtTime};


                cb = new SimpleCursorAdapter(this, R.layout.baglist, s, from, to);
                //cb.notifyDataSetChanged();
                listbags.setAdapter(cb);

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


            ReceiptNo=session.getString(session.getColumnIndex(Database.SessionDevice))+SessionNo;
            FarmerNo= session.getString(session.getColumnIndex(Database.SessionFarmerNo));
            Bags=session.getString(session.getColumnIndex(Database.SessionBags));
            NetWeight=session.getString(session.getColumnIndex(Database.SessionNet));
            Date=session.getString(session.getColumnIndex(Database.SessionDate));

        }
        session.close();
       // db.close();
        //dbhelper.close();

         dialogBuilder.setNegativeButton("Send SMS", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {


                 if(!mSharedPrefs.getBoolean("enableSMS", false)==true) {

                     Toast.makeText(getBaseContext(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();

                 } else {

                     if (mSharedPrefs.getString("SMSModes", "BS").toString().equals("BS")) {
                         if (MobileNo.equals("")) {
                             Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
                             return;
                         }
                         if (FarmerNo.equals("")) {
                             Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
                             return;
                         }

                         new WEBSMS().execute();
                     }
                     else{
                             // MobileNo="+254715881439";
                         if (MobileNo.equals("")) {
                             Toast.makeText(getApplicationContext(), "Please Update Mobile Number", Toast.LENGTH_LONG).show();
                             return;
                         }
                         if (FarmerNo.equals("")) {
                             Toast.makeText(getApplicationContext(), "Please Update FarmerNo", Toast.LENGTH_LONG).show();
                             return;
                         }
                         textmessage=ReceiptNo +" for "+farmerNo+"," + FarmerName +
                                 " weighed " + Bags +" bags of Net weight: " +NetWeight+ " Kg on " +Date+
                                 ". Month to Date weight: " + TotalKg + " Kg.";

                         Sendsms(MobileNo, textmessage);

                         Toast.makeText(getBaseContext(), "Message Sent Successfully!!", Toast.LENGTH_LONG).show();
                     }

                 }
             }
         });


        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


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
    private class WEBSMS extends AsyncTask<Void, Void, String>
    {    @Override
        protected void onPreExecute()
        {
        }
        @Override
        protected String doInBackground(Void... params)
        {  makePostRequest();
            return "";
        }
        @Override
        protected void onPostExecute(String result)
        {
            if(Integer.parseInt(error_code)<0){
            Toast.makeText(getApplicationContext(), error_desc, Toast.LENGTH_LONG).show();
                return;
            }else{
            Toast.makeText(getApplicationContext(), success_desc,Toast.LENGTH_LONG).show();
            }

        }
    }
    private void makePostRequest() {
        smsurl="http://messaging.advantasms.com/sendsms.jsp?";
        username=mSharedPrefs.getString("prefUser", "octagon");
        password=mSharedPrefs.getString("prefPass", "octagon");
        senderid=mSharedPrefs.getString("SenderID", "OCTAGON");
        //MobileNo="+254715881439";
        textmessage=ReceiptNo +" for "+farmerNo+"," + FarmerName +
                " weighed " + Bags +" bags of Net weight: " +NetWeight+ " Kg on " +Date+
                ". Month to Date weight: " + TotalKg + " Kg.";

        String requestUrl="user="+username+"&password="+password+
                "&senderid="+senderid+
                "&mobiles="+MobileNo+
                "&sms="+textmessage;


        try{

            URL url = new URL(smsurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", "" +Integer.toString(requestUrl.getBytes().length));
            connection.setUseCaches (false);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

            wr.writeBytes(requestUrl);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                retval += decodedString;
            }
            in.close();
            System.out.println(retval);
            String sampleXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+retval;
            JSONObject jsonObj = null;

            try {
                jsonObj = XML.toJSONObject(sampleXml);
                success_desc= "Message Sent Successfully To "+jsonObj.getJSONObject("smslist").getJSONObject("sms").getString("mobile-no");
            }
            catch (JSONException e) {
                Log.e("JSON exception", e.getMessage());
                error_code = jsonObj.getJSONObject("smslist").getJSONObject("error").getString("error-code");
                error_desc = jsonObj.getJSONObject("smslist").getJSONObject("error").getString("error-description");
                e.printStackTrace();
            }

            Log.d("XML", sampleXml);

            Log.d("JSON", jsonObj.toString());

            Log.d("VALUE", error_code+ ":"+error_desc );

            connection.disconnect();


        }
        catch(Exception ex)
        {
            System.out.print(ex);
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
            Cursor accounts = db.query(true, Database.SESSION_TABLE_NAME, null, null, null, null, null, null, null, null);
            if (accounts.getCount() > 0) {
            String from [] = {  Database.ROW_ID,Database.SessionFarmerNo, Database.SessionDevice , Database.SessionBags, Database.SessionDate};
            int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date};


            ca  = new SimpleCursorAdapter(this,R.layout.receipt_list, accounts,from,to);

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

            mIntent = new Intent(getApplicationContext(),FarmerSMSRecieptsActivity.class);
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
