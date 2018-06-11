package com.easyweigh.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.activities.MainActivity;
import com.easyweigh.activities.RouteShedActivity;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.services.WeighingService;

import java.util.ArrayList;


public class ProduceVGPFragment extends Fragment {

    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;

    Spinner spProduce,spVariety,spGrade;
   EditText edtPrice;
    DBHelper dbhelper;
    static SharedPreferences mSharedPrefs,prefs;
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String MANUAL = "Manual";
    public static final String TRANCELL_TI500 = "TI-500";
    public static final String DR_150 = "DR-150";
    Button btn_next,btnHome;
    private Snackbar snackbar;
    public static String cachedDeviceAddress;
    WeighingService resetConn;

    String grade,gradeid;
    ArrayList<String> gradedata=new ArrayList<String>();
    ArrayAdapter<String> gradeadapter;

    String variety,varietyid;
    ArrayList<String> varietydata=new ArrayList<String>();
    ArrayAdapter<String> varietyadapter;

    String produce,produceid;
    ArrayList<String> producedata=new ArrayList<String>();
    ArrayAdapter<String> produceadapter;
    TextView tv;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    String disabled;
    String BaseDate,BatchDate,DelDate;
    int CLOSED = 1;
    SQLiteDatabase db;
    int SIGNEDOFF=0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_produce_browser, container, false);
        initializer();
        //setupProgressBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);

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
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        resetConn=new WeighingService();
        spProduce = (Spinner) mView.findViewById(R.id.spProduce);
        spVariety = (Spinner) mView.findViewById(R.id.spVariety);
        spGrade = (Spinner) mView.findViewById(R.id.spGrade);
        edtPrice = (EditText) mView.findViewById(R.id.edtPrice);
        if(!mSharedPrefs.getBoolean("enableBuyingPrice", false)==true) {
            edtPrice.setEnabled(false);

        }else{
            edtPrice.setEnabled(true);
        }


        enableBT();
        Produce();
        Variety();
        Grade();
        btnHome = (Button) mView.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().finish();
                mIntent = new Intent(getActivity(),MainActivity.class);
                startActivity(mIntent);

            }
        });
        btn_next = (Button) mView.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (prefs.getString("DeliverNoteNumber", "").toString().equals("")
                        ||prefs.getString("DeliverNoteNumber", "").toString().equals("No Batch Opened")) {
                    // snackbar.show();
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Open A Batch To Proceed...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }

                BaseDate=prefs.getString("basedate", "");
                BatchDate=prefs.getString("BatchON", "");
                if (!BaseDate.equals(BatchDate)) {
                    // snackbar.show();
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Close Yesterday's Batch and\nOpen A New To Proceed...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }
                Cursor batches = db.rawQuery("select BatchDate,DeliveryNoteNumber from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                        + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                if (batches.getCount() > 0){
                    batches.moveToLast();
                    DelDate=batches.getString(0);

                    if (!BaseDate.equals(DelDate)) {
                        // snackbar.show();
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Deliver Yesterday's Batches To Proceed...");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                        return;
                    }

                    //db.close();
                    //dbhelper.close();
                }

                if (mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
                    // snackbar.show();
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Select Scale Model to Weigh");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }
                if (mSharedPrefs.getString("scaleVersion", "EW15").toString().equals(EASYWEIGH_VERSION_15) ||
                        mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_11)) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    cachedDeviceAddress = pref.getString("address", "");
                    if (cachedDeviceAddress.toString().equals("")) {
                        // snackbar.show();
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please pair scale");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please pair scale", Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                    // go back to milkers activity
                    //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                if ( prefs.getString("mDevice", "").toString().equals("")) {
                    // snackbar.show();
                    Context context = getActivity();
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View customToastroot = inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Pair Printer...");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                    return;
                }
                }
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_15)||
                        mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_11)) {
                    if(spProduce.getSelectedItem().equals("Select ..."))
                    {   Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Produce");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("unitPrice", edtPrice.getText().toString());
                    edit.commit();

                    mIntent = new Intent(getActivity(), RouteShedActivity.class);
                    startActivity(mIntent);

                   /* Toast.makeText(getActivity(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("gradeCode", "")+"---"
                            +prefs.getString("unitPrice", ""), Toast.LENGTH_LONG).show();*/
                }
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(TRANCELL_TI500)) {
                    if(spProduce.getSelectedItem().equals("Select ..."))
                    {   Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Produce");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("unitPrice", edtPrice.getText().toString());
                    edit.commit();

                    mIntent = new Intent(getActivity(), RouteShedActivity.class);
                    startActivity(mIntent);

                   /* Toast.makeText(getActivity(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("gradeCode", "")+"---"
                            +prefs.getString("unitPrice", ""), Toast.LENGTH_LONG).show();*/
                }
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(DR_150)) {
                    if(spProduce.getSelectedItem().equals("Select ..."))
                    {   Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Produce");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("unitPrice", edtPrice.getText().toString());
                    edit.commit();

                    mIntent = new Intent(getActivity(), RouteShedActivity.class);
                    startActivity(mIntent);

                   /* Toast.makeText(getActivity(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("gradeCode", "")+"---"
                            +prefs.getString("unitPrice", ""), Toast.LENGTH_LONG).show();*/
                }
               /* if (mSharedPrefs.getString("scaleVersion", "").toString().equals(MANUAL)) {
                    if(spProduce.getSelectedItem().equals("Select ..."))
                    {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Produce");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                        // go back to milkers activity
                        //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                    } else {
                        if ( prefs.getString("mDevice", "").toString().equals("")) {
                            // snackbar.show();
                            Context context = getActivity();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                            text.setText("Please Pair Printer...");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getActivity(), "Please Select Scale Model to Weigh", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("produceCode", produceid);
                    edit.commit();

                    edit.putString("unitPrice", edtPrice.getText().toString());
                    edit.commit();

                   /* Toast.makeText(getActivity(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("gradeCode", "")+"---"
                            +prefs.getString("unitPrice", ""), Toast.LENGTH_LONG).show();
                    mIntent = new Intent(getActivity(), RouteShedActivity.class);
                    startActivity(mIntent);
                }*/
            }


        });


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return getActivity().onKeyDown(keyCode, event);
    }
    public void enableBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }
    private void Produce() {
        producedata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select MpCode,MpDescription from Produce", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    produce=c.getString(c.getColumnIndex("MpDescription"));
                    producedata.add(produce);

                }while(c.moveToNext());
            }
        }


        produceadapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,producedata);
        produceadapter.setDropDownViewResource(R.layout.spinner_item);
        spProduce.setAdapter(produceadapter);
        spProduce.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String produceName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select MpCode from Produce where MpDescription= '" + produceName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    produceid = c.getString(c.getColumnIndex("MpCode"));

                }
                c.close();


                tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }

                if (position == 0) {
                    spVariety.setEnabled(false);
                    spGrade.setEnabled(false);
                    edtPrice.setText("");
                    disabled="true";
                    Variety();
                    Grade();
                    SharedPreferences.Editor edit = prefs.edit();
                    //edit.remove("produceCode");
                    //edit.commit();
                    edit.remove("varietyCode");
                    edit.commit();
                    edit.remove("gradeCode");
                    edit.commit();
                    edit.remove("unitPrice");
                    edit.commit();
                    //Toast.makeText(getActivity(), "Please sel", Toast.LENGTH_LONG).show();

             }else{

                    Variety();
                    Grade();
                    Cursor c1 = db.rawQuery("select * from ProduceGrades where pgdProduce= '" + produceid + "' ", null);
                    Cursor c2 = db.rawQuery("select * from ProduceVarieties where vrtProduce= '" + produceid + "' ", null);
                    if(c2.getCount() > 0) {
                        spVariety.setEnabled(true);
                        disabled="false";
                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c2.close();

                    }else{
                       spVariety.setEnabled(false);
                       varietydata.clear();
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.remove("varietyCode");
                        edit.commit();

                   }
                    if (c1.getCount() > 0) {
                        spGrade.setEnabled(true);
                        disabled="false";

                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c1.close();
                    }else{
                        spGrade.setEnabled(false);
                        gradedata.clear();
                        SharedPreferences.Editor edit = prefs.edit();

                        edit.remove("gradeCode");
                        edit.commit();
                    }



                }
               // db.close();
                //dbhelper.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
              //  tv.setHint("Select Country");
            }
        });


    }
    private void Grade() {
        gradedata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select pgdRef,pgdName from ProduceGrades where pgdProduce= '" + produceid + "' ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    grade=c.getString(c.getColumnIndex("pgdName"));
                    gradedata.add(grade);

                }while(c.moveToNext());
            }
        }


        gradeadapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,gradedata);
        gradeadapter.setDropDownViewResource(R.layout.spinner_item);
        gradeadapter.notifyDataSetChanged();
        spGrade.setAdapter(gradeadapter);
        spGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String GradeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select pgdRef from ProduceGrades where pgdName= '" + GradeName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    gradeid = c.getString(c.getColumnIndex("pgdRef"));


                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("gradeCode", gradeid);
                edit.commit();
                c.close();
                //db.close();
                //dbhelper.close();
                TextView tv = (TextView) view;


               if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                    tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void Variety() {
        varietydata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select vtrRef,vrtName from ProduceVarieties where vrtProduce= '" + produceid + "' ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    variety=c.getString(c.getColumnIndex("vrtName"));
                    varietydata.add(variety);

                }while(c.moveToNext());
            }
        }


        varietyadapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,varietydata);
        varietyadapter.setDropDownViewResource(R.layout.spinner_item);
        varietyadapter.notifyDataSetChanged();
        spVariety.setAdapter(varietyadapter);
        spVariety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String varietyName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select vtrRef from ProduceVarieties where vrtName= '" + varietyName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    varietyid = c.getString(c.getColumnIndex("vtrRef"));

                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("varietyCode", varietyid);
                edit.commit();
                c.close();
                //db.close();
                //dbhelper.close();
                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                  tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
}
