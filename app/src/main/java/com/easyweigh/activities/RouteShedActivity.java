package com.easyweigh.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.services.WeighingService;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;

/**
 * Created by Michael on 16/08/2016.
 */
public class RouteShedActivity extends AppCompatActivity {
    public Intent mIntent;
    Spinner mc_ctransporter,mc_cRoute,sp_fshed;
    String s_mcno,s_mcname;
    DBHelper dbhelper;
    String transporters,routes,shedcode;
    String routeid=null;
    String transporterid=null;
    ArrayList<String> transporterdata=new ArrayList<String>();
    ArrayList<String> iddata=new ArrayList<String>();
    ArrayList<String> routedata=new ArrayList<String>();
    ArrayAdapter<String> transporteradapter,routeadapter;
    String sheds,shedid,manageid;
    ArrayList<String> sheddata=new ArrayList<String>();
    ArrayAdapter<String> shedadapter;
    static SharedPreferences mSharedPrefs,prefs;
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String TRANCELL_TI500 = "TI-500";
    public static final String DR_150 = "DR-150";

    public static final String CARD = "Card";
    public static final String MANUAL = "Manual";
    public static final String BOTH = "Both";

    Button btn_next,btnBack;
    private Snackbar snackbar;
    public static String cachedDeviceAddress;
    WeighingService resetConn;
    public Toolbar toolbar;
    EditText edtCan;
    private LocalDate systemdate,currentdate,batchdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_browser);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupToolbar();
        initializer();
    }
    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_trs);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        return;
    }
    public void initializer(){
        mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(RouteShedActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(RouteShedActivity.this);
        dbhelper = new DBHelper(RouteShedActivity.this);
        resetConn=new WeighingService();
        systemdate = LocalDate.parse( prefs.getString("basedate",""));
        batchdate=LocalDate.parse( prefs.getString("BatchON",""));
        currentdate = new LocalDate();
        if(Days.daysBetween(currentdate,systemdate ).getDays()>=1){

            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Please Contact the Administrator to Solve Base Date Error!!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            new LogOut().execute();
            return;
        }
        else if(Days.daysBetween(batchdate, currentdate).getDays()>=1){

            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Please ensure the Batch Date is the same as Current Date!!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            finish();
            return;
        }
        else if(Days.daysBetween(systemdate, currentdate).getDays()>=7){
            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Please Contact the Administrator to Solve Base Date Error!!");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            new LogOut().execute();
            return;
        }
        mc_cRoute = (Spinner) findViewById(R.id.mc_cRoute);
        sp_fshed = (Spinner) findViewById(R.id.sp_shed);
        sp_fshed.setEnabled(false);
        edtCan = (EditText) findViewById(R.id.edtCan);
        if(!mSharedPrefs.getBoolean("enableCanSerial", false)==true) {
          edtCan.setEnabled(false);

        }else{
            edtCan.setEnabled(true);
        }

        RouteList();
        ShedList();
        enableBT();
        btnBack = (Button) findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });



        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
                    // snackbar.show();
                    Context context = RouteShedActivity.this;
                    LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Scale Model to Weigh");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(RouteShedActivity.this, "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }
                if (mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)||
                        mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_11)) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(RouteShedActivity.this);
                    cachedDeviceAddress = pref.getString("address", "");
                    if (cachedDeviceAddress.toString().equals("")) {
                        // snackbar.show();
                        Context context = RouteShedActivity.this;
                        LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please pair scale");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(RouteShedActivity.this, "Please pair scale", Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_15)||
                        mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_11)||
                        mSharedPrefs.getString("scaleVersion", "").toString().equals(DR_150)) {
                    if(mc_cRoute.getSelectedItem().equals("Select ..."))
                    {   Context context = RouteShedActivity.this;
                        LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Route");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Route", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(sp_fshed.getSelectedItem().equals("Select ..."))
                    {
                        Context context = RouteShedActivity.this;
                        LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Shed");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Shed", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("transporterCode", "");
                    edit.commit();

                    edit.putString("routeCode", routeid);
                    edit.commit();

                    edit.putString("canSerial", edtCan.getText().toString());
                    edit.commit();

                    /*Toast.makeText(getApplicationContext(),  prefs.getString("transporterCode", "") + " --- "
                            + prefs.getString("routeCode", "")+"----"+
                            prefs.getString("shedCode", "")+"---"
                            +prefs.getString("canSerial", ""), Toast.LENGTH_LONG).show();*/
                    if (mSharedPrefs.getString("vModes", "Card").toString().equals(CARD)){
                        mIntent = new Intent(RouteShedActivity.this, FarmerScaleWeighCardActivity.class);
                        startActivity(mIntent);

                    }else  if (mSharedPrefs.getString("vModes", "Manual").toString().equals(MANUAL)){
                        mIntent = new Intent(RouteShedActivity.this, FarmerScaleWeighActivity.class);
                        startActivity(mIntent);

                    }else  if (mSharedPrefs.getString("vModes", "Both").toString().equals(BOTH)){
                        mIntent = new Intent(RouteShedActivity.this, VerificationModeActivity.class);
                        startActivity(mIntent);
                    }
                    else{
                        mIntent = new Intent(RouteShedActivity.this, FarmerScaleWeighActivity.class);
                        startActivity(mIntent);
                    }

                }
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(TRANCELL_TI500)) {
                    if(mc_cRoute.getSelectedItem().equals("Select ..."))
                    {   Context context = RouteShedActivity.this;
                        LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Route");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Route", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(sp_fshed.getSelectedItem().equals("Select ..."))
                    {
                        Context context = RouteShedActivity.this;
                        LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Shed");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Shed", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("transporterCode", "");
                    edit.commit();

                    edit.putString("routeCode", routeid);
                    edit.commit();

                    edit.putString("canSerial", edtCan.getText().toString());
                    edit.commit();

                    /*Toast.makeText(getApplicationContext(),  prefs.getString("transporterCode", "") + " --- "
                            + prefs.getString("routeCode", "")+"----"+
                            prefs.getString("shedCode", "")+"---"
                            +prefs.getString("canSerial", ""), Toast.LENGTH_LONG).show();*/
                    mIntent = new Intent(RouteShedActivity.this, FarmerScaleSerialWeighActivity.class);
                    startActivity(mIntent);
                }

               /* if (mSharedPrefs.getString("scaleVersion", "").toString().equals(MANUAL)) {

                    if(mc_cRoute.getSelectedItem().equals("Select ..."))
                    {
                        Context context = RouteShedActivity.this;
                        LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Route");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Route", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(sp_fshed.getSelectedItem().equals("Select ..."))
                    {   Context context = RouteShedActivity.this;
                        LayoutInflater inflater = RouteShedActivity.this.getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Shed");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getApplicationContext(), "Please Select Shed", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("transporterCode", transporterid);
                    edit.commit();

                    edit.putString("routeCode", routeid);
                    edit.commit();

                    edit.putString("canSerial", edtCan.getText().toString());
                    edit.commit();

                    /*Toast.makeText(getApplicationContext(),  prefs.getString("transporterCode", "") + " --- "
                            + prefs.getString("routeCode", "")+"----"+
                            prefs.getString("shedCode", "")+"---"
                            +prefs.getString("canSerial", ""), Toast.LENGTH_LONG).show();
                    new GoingManual().execute();

                }*/
            }


        });

      

    }



    public void enableBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }
    private void ShedList() {
        sheddata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select MccNo,MccName from CollectionCenters WHERE MccRoute ='" + routeid + "'", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    sheds=c.getString(c.getColumnIndex("MccName"));
                    sheddata.add(sheds);

                }while(c.moveToNext());
            }
        }
        c.close();
        db.close();
        dbhelper.close();

        shedadapter=new ArrayAdapter<String>(RouteShedActivity.this,R.layout.spinner_item,sheddata);
        shedadapter.setDropDownViewResource(R.layout.spinner_item);
        shedadapter.notifyDataSetChanged();
        sp_fshed.setAdapter(shedadapter);
        sp_fshed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String shedName =parent.getItemAtPosition(position).toString();
                SQLiteDatabase db= dbhelper.getReadableDatabase();
                Cursor c=db.rawQuery("select MccNo from CollectionCenters where MccName= '"+shedName+"'", null);
                if(c!=null)
                {
                    c.moveToFirst();
                    shedid=c.getString(c.getColumnIndex("MccNo"));



                }
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("shedCode", shedid);
                edit.commit();
                c.close();
                db.close();
                dbhelper.close();

                TextView tv = (TextView) view;
                if(position%2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
                else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }

            } @Override
              public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void RouteList() {
        routedata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select McRCode,McRName from routes ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    routes=c.getString(c.getColumnIndex("McRName"));
                    routedata.add(routes);

                }while(c.moveToNext());
            }
        }


        routeadapter=new ArrayAdapter<String>(RouteShedActivity.this,R.layout.spinner_item,routedata);
        routeadapter.setDropDownViewResource(R.layout.spinner_item);
        mc_cRoute.setAdapter(routeadapter);
        mc_cRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String routeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select McRCode from routes where McRName= '" + routeName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    routeid = c.getString(c.getColumnIndex("McRCode"));


                }
                c.close();
                db.close();
                dbhelper.close();
                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
                if(position==0){
                    sp_fshed.setEnabled(false);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("shedCode");
                    edit.commit();
                }else{
                sp_fshed.setEnabled(true);}
                ShedList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private class GoingManual extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute()
        {
            dialog = ProgressDialog.show( RouteShedActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.going_manual),
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
          //  disableBT();
            mIntent = new Intent(RouteShedActivity.this,FarmerManualWeighActivity.class);
            startActivity(mIntent);
        }
    }
    public void disableBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
        }
    }

    private class LogOut extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute()
        {
            dialog = ProgressDialog.show( RouteShedActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);
        }

        @Override
        protected String doInBackground(Void... params)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RouteShedActivity.this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("pass");
            edit.commit();

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
            disableBT();
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addCategory(android.content.Intent.CATEGORY_HOME);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
