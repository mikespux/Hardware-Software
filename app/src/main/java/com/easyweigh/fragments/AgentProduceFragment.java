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
import com.easyweigh.activities.AgentScaleEW15WeighActivity;
import com.easyweigh.activities.AgentScaleSerialWeighActivity;
import com.easyweigh.activities.MainActivity;
import com.easyweigh.activities.RouteShedActivity;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.services.WeighingService;

import java.util.ArrayList;


public class AgentProduceFragment extends Fragment {

    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;

    Spinner spProduce, spWarehouse, spAgent;
    EditText edtPrice;
    DBHelper dbhelper;
    static SharedPreferences mSharedPrefs, prefs;
    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String MANUAL = "Manual";
    public static final String TRANCELL_TI500 = "TI-500";
    public static final String DR_150 = "DR-150";
    Button btn_next, btnHome;
    private Snackbar snackbar;
    public static String cachedDeviceAddress;
    WeighingService resetConn;

    String warehouse, warehouseid;
    ArrayList<String> warehousedata = new ArrayList<String>();
    ArrayAdapter<String> warehouseadapter;

    String agents;
    ArrayList<String> agentdata = new ArrayList<String>();
    ArrayAdapter<String> agentadapter;

    String produce, produceid,agentid;
    ArrayList<String> producedata = new ArrayList<String>();
    ArrayAdapter<String> produceadapter;
    TextView tv;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    String disabled;
    String BaseDate, BatchDate, DelDate;
    int CLOSED = 1;
    SQLiteDatabase db;
    int SIGNEDOFF = 0;
    String AgName;
    String WhName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_agent_produce_browser, container, false);
        initializer();
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

    public void initializer() {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dbhelper = new DBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        resetConn = new WeighingService();
        spWarehouse = (Spinner) mView.findViewById(R.id.spWarehouse);
        spAgent = (Spinner) mView.findViewById(R.id.spAgent);
        spProduce = (Spinner) mView.findViewById(R.id.spProduce);
        edtPrice = (EditText) mView.findViewById(R.id.edtPrice);
        if (!mSharedPrefs.getBoolean("enableBuyingPrice", false) == true) {
            edtPrice.setEnabled(false);

        } else {
            edtPrice.setEnabled(true);
        }


        enableBT();
        Produce();
        Agent();
        Warehouse();
        btnHome = (Button) mView.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                mIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mIntent);

            }
        });
        btn_next = (Button) mView.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (prefs.getString("DeliverNoteNumb", "").toString().equals("")
                        || prefs.getString("DeliverNoteNumb", "").toString().equals("No Batch Opened")) {
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

                BaseDate = prefs.getString("basedate", "");
                BatchDate = prefs.getString("AgentBatchON", "");
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
                Cursor batches = db.rawQuery("select BatchDate,DeliveryNoteNumber from " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
                        + Database.Closed + " ='" + CLOSED + "' and " + Database.SignedOff + " ='" + SIGNEDOFF + "'", null);

                if (batches.getCount() > 0) {
                    batches.moveToLast();
                    DelDate = batches.getString(0);

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
                if (!mSharedPrefs.getBoolean("enablePrinting", false) == true) {
                    // go back to milkers activity
                    //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                } else {
                    if (prefs.getString("mDevice", "").toString().equals("")) {
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
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_15) ||
                        mSharedPrefs.getString("scaleVersion", "").toString().equals(EASYWEIGH_VERSION_11)) {
                    if (spWarehouse.getSelectedItem().equals("Select ...")) {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Warehouse");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (spAgent.getSelectedItem().equals("Select ...")) {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Agent");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (spProduce.getSelectedItem().equals("Select ...")) {
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
                    if (!mSharedPrefs.getBoolean("enableBuyingPrice", false) == true) {
                        // go back to milkers activity
                        //Toast.makeText(getBaseContext(), "Bprice enabled on settings", Toast.LENGTH_LONG).show();
                    } else {
                        if (edtPrice.length()==0) {
                            Context context = getActivity();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                            text.setText("Please Enter Buying Price");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                            return;
                        }}
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("warehouseCode", warehouseid);
                    edit.commit();
                    edit.putString("agentCode", agentid);
                    edit.commit();
                    edit.putString("agentName", AgName);
                    edit.commit();
                    edit.putString("whName", WhName);
                    edit.commit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("unitPrice", edtPrice.getText().toString());
                    edit.commit();

                    mIntent = new Intent(getActivity(), AgentScaleEW15WeighActivity.class);
                    startActivity(mIntent);

                   /* Toast.makeText(getActivity(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("warehouseCode", "")+"---"
                            +prefs.getString("unitPrice", ""), Toast.LENGTH_LONG).show();*/
                }
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(TRANCELL_TI500)) {
                    if (spWarehouse.getSelectedItem().equals("Select ...")) {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Warehouse");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (spAgent.getSelectedItem().equals("Select ...")) {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Agent");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (spProduce.getSelectedItem().equals("Select ...")) {
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
                    if (!mSharedPrefs.getBoolean("enableBuyingPrice", false) == true) {
                        // go back to milkers activity
                        //Toast.makeText(getBaseContext(), "Bprice enabled on settings", Toast.LENGTH_LONG).show();
                    } else {
                        if (edtPrice.length()==0) {
                            Context context = getActivity();
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                            text.setText("Please Enter Buying Price");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                            return;
                        }}
                    SharedPreferences.Editor edit = prefs.edit();

                    edit.putString("warehouseCode", warehouseid);
                    edit.commit();
                    edit.putString("agentCode", agentid);
                    edit.commit();
                    edit.putString("agentName", AgName);
                    edit.commit();
                    edit.putString("whName", WhName);
                    edit.commit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("unitPrice", edtPrice.getText().toString());
                    edit.commit();


                    mIntent = new Intent(getActivity(), AgentScaleSerialWeighActivity.class);
                    startActivity(mIntent);

                   /* Toast.makeText(getActivity(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("warehouseCode", "")+"---"
                            +prefs.getString("unitPrice", ""), Toast.LENGTH_LONG).show();*/
                }
                if (mSharedPrefs.getString("scaleVersion", "").toString().equals(DR_150)) {
                    if (spWarehouse.getSelectedItem().equals("Select ...")) {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Warehouse");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (spAgent.getSelectedItem().equals("Select ...")) {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Select Agent");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (spProduce.getSelectedItem().equals("Select ...")) {
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
                    if (!mSharedPrefs.getBoolean("enableBuyingPrice", false) == true) {
                        // go back to milkers activity
                        //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                    } else {
                    if (edtPrice.length()==0) {
                        Context context = getActivity();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Enter Buying Price");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        //Toast.makeText(getActivity(), "Please Select Produce", Toast.LENGTH_LONG).show();
                        return;
                    }}
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("warehouseCode", warehouseid);
                    edit.commit();
                    edit.putString("agentCode", agentid);
                    edit.commit();
                    edit.putString("agentName", AgName);
                    edit.commit();
                    edit.putString("whName", WhName);
                    edit.commit();
                    edit.putString("produceCode", produceid);
                    edit.commit();
                    edit.putString("unitPrice", edtPrice.getText().toString());
                    edit.commit();

                    mIntent = new Intent(getActivity(), AgentScaleSerialWeighActivity.class);
                    startActivity(mIntent);

                   /* Toast.makeText(getActivity(),  prefs.getString("produceCode", "") + " --- "
                            + prefs.getString("varietyCode", "")+"----"+
                            prefs.getString("warehouseCode", "")+"---"
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
                            prefs.getString("warehouseCode", "")+"---"
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

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void Produce() {
        producedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select MpCode,MpDescription from Produce", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    produce = c.getString(c.getColumnIndex("MpDescription"));
                    producedata.add(produce);

                } while (c.moveToNext());
            }
        }


        produceadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, producedata);
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





                // db.close();
                //dbhelper.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //  tv.setHint("Select Country");
            }
        });


    }

    private void Warehouse() {
        warehousedata.clear();

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select whID,whName from warehouse", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    warehouse = c.getString(c.getColumnIndex("whName"));
                    warehousedata.add(warehouse);

                } while (c.moveToNext());
            }
        }


        warehouseadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, warehousedata);
        warehouseadapter.setDropDownViewResource(R.layout.spinner_item);
        warehouseadapter.notifyDataSetChanged();
        spWarehouse.setAdapter(warehouseadapter);
        spWarehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                WhName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select whID from warehouse where whName= '" + WhName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    warehouseid = c.getString(c.getColumnIndex("whID"));


                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("warehouseCode", warehouseid);
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

    private void Agent() {
        agentdata.clear();
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.rawQuery("select agtID,agtName from agent", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    agents = c.getString(c.getColumnIndex("agtName"));
                    agentdata.add(agents);

                } while (c.moveToNext());
            }
        }


        agentadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, agentdata);
        agentadapter.setDropDownViewResource(R.layout.spinner_item);
        spAgent.setAdapter(agentadapter);
        spAgent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AgName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select agtID from agent where agtName= '" + AgName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                   agentid = c.getString(c.getColumnIndex("agtID"));


                }
                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("agentCode", agentid);
                edit.commit();
                c.close();
                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }



}
