package com.easyweigh.fragments;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.activities.FarmerManualWeighActivity;
import com.easyweigh.activities.FarmerScaleWeighActivity;
import com.easyweigh.data.DBHelper;
import com.easyweigh.services.WeighingService;

import java.util.ArrayList;


public class RouteShedFragment extends Fragment {

    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;

    Spinner mc_ctransporter,mc_cRoute,sp_fshed;
    String s_mcno,s_mcname;
    DBHelper dbhelper;
    String transporters,routes,shedcode;
    String routeid=null;
    String zoneid=null;
    ArrayList<String> transporterdata=new ArrayList<String>();
    ArrayList<String> iddata=new ArrayList<String>();
    ArrayList<String> routedata=new ArrayList<String>();
    ArrayAdapter<String> transporteradapter,routeadapter;
    String sheds,shedid,manageid;
    ArrayList<String> sheddata=new ArrayList<String>();
    ArrayAdapter<String> shedadapter;
    static SharedPreferences mSharedPrefs;
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String MANUAL = "Manual";
    Button btn_next,btnBack;
    private Snackbar snackbar;
    public static String cachedDeviceAddress;
    WeighingService resetConn;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_route_browser, container, false);
        initializer();
        //setupProgressBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        //getUser();
       // setupSnackBar();
        return mView;
    }
    public void setupSnackBar() {
        snackbar = Snackbar.make(mView.findViewById(R.id.ParentLayout), getString(R.string.ScaleError), Snackbar.LENGTH_LONG);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#FF5252"));
    }

    public void initializer(){
        mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        dbhelper = new DBHelper(getActivity());
        resetConn=new WeighingService();
        mc_ctransporter = (Spinner) mView.findViewById(R.id.mc_ctransporter);
        mc_cRoute = (Spinner) mView.findViewById(R.id.mc_cRoute);
        sp_fshed = (Spinner) mView.findViewById(R.id.sp_shed);
                TransporterList();
                RouteList();
               ShedList();
        enableBT();
        btnBack = (Button) mView.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        btn_next = (Button) mView.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
                    // snackbar.show();
                    Context context=getActivity();
                    LayoutInflater inflater=getActivity().getLayoutInflater();
                    View customToastroot =inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Scale Model to Weigh");
                    Toast customtoast=new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15)) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                cachedDeviceAddress = pref.getString("address", "");
                if (cachedDeviceAddress.toString().equals("")) {
                    // snackbar.show();
                    Context context=getActivity();
                    LayoutInflater inflater=getActivity().getLayoutInflater();
                    View customToastroot =inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please pair scale");
                    Toast customtoast=new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please pair scale", Toast.LENGTH_LONG).show();
                    return;
                }

                }
                if(mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_15)){
                    mIntent = new Intent(getActivity(),FarmerScaleWeighActivity.class);
                    startActivity(mIntent);
                }
                if(mSharedPrefs.getString("scaleVersion", "").toString().equals(MANUAL)){
                  new GoingManual().execute();

                }
            }


        });

            mFragmentManager = getActivity().getSupportFragmentManager();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            //mFragmentTransaction.replace(R.id.container, mFragment).commit();

        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if(getFragmentManager().getBackStackEntryCount() == 0)
                    getActivity().finish();
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
        Cursor c=db.rawQuery("select MccNo,MccName from CollectionCenters ", null);
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

        shedadapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,sheddata);
        shedadapter.setDropDownViewResource(R.layout.spinner_item);
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
    private void TransporterList() {
        transporterdata.clear();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select tptID,tptName from transporter ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    transporters=c.getString(c.getColumnIndex("tptName"));
                    transporterdata.add(transporters);

                }while(c.moveToNext());
            }
        }


        transporteradapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,transporterdata);
        transporteradapter.setDropDownViewResource(R.layout.spinner_item);
        mc_ctransporter.setAdapter(transporteradapter);
        mc_ctransporter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String transporterName =parent.getItemAtPosition(position).toString();
                SQLiteDatabase db= dbhelper.getReadableDatabase();
                Cursor c=db.rawQuery("select tptID from transporter where tptName= '"+transporterName+"' ", null);
                if(c!=null)
                {
                    c.moveToFirst();
                    zoneid=c.getString(c.getColumnIndex("tptID"));

                }
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
            }

            @Override
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


        routeadapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,routedata);
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
                if(position%2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
                else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
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
            dialog = ProgressDialog.show( getActivity(),
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
            disableBT();
            mIntent = new Intent(getActivity(),FarmerManualWeighActivity.class);
            startActivity(mIntent);
        }
    }
    public void disableBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
        }
    }
}
