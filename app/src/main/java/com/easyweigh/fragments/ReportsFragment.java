package com.easyweigh.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.activities.DeliveryReportActivity;
import com.easyweigh.activities.FarmerDetailedRecieptsActivity;
import com.easyweigh.activities.FarmerReceiptInternalPrinter;
import com.easyweigh.activities.FarmerRecieptsActivity;
import com.easyweigh.activities.FarmerSMSRecieptsActivity;
import com.easyweigh.activities.ZReportActivity;
import com.easyweigh.helpers.CustomListReport;


public class ReportsFragment extends Fragment {

    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;
    SharedPreferences prefs;
    static SharedPreferences mSharedPrefs;

    ListView list;
    String[]  web     = {
            "View Receipts", "Print Receipts","SMS Receipts","Z Report","Delivery Report"
    };
    Integer[] imageId = {
            R.drawable.ic_viewreceipt,
            R.drawable.ic_receipt,
            R.drawable.ic_sms,
            R.drawable.ic_zreport,
            R.drawable.ic_delivary

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_report, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        initializer();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);

        return mView;
    }
    public void initializer() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CustomListReport adapter = new CustomListReport(getActivity(), web, imageId);
        list = (ListView) mView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                if (position == 1)
                {

                }
                switch (position) {
                    case 0:
                        mIntent = new Intent(getActivity(),FarmerDetailedRecieptsActivity.class);
                        startActivity(mIntent);
                        break;
                    case 1:
                        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                            // go back to milkers activity
                            Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                            if (!mSharedPrefs.getBoolean("enableInternalP", false) == true) {
                                // go back to milkers activity
                                //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();

                            } else {
                                mIntent = new Intent(getActivity(),FarmerReceiptInternalPrinter.class);
                                startActivity(mIntent);
                                return;
                            }
                            return;
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
                        mIntent = new Intent(getActivity(),FarmerRecieptsActivity.class);
                        startActivity(mIntent);
                        break;
                    case 2:

                        if(!mSharedPrefs.getBoolean("enableSMS", false)==true) {
                            Toast.makeText(getActivity(), "SMS not enabled on Settings", Toast.LENGTH_LONG).show();
                            return;
                        } else {

                            if (mSharedPrefs.getString("SMSModes", "BS").toString().equals("BS")) {

                                if (!isInternetOn()) {
                                    createNetErrorDialog();
                                    return;
                                }else{
                                    if (mSharedPrefs.getString("prefUser", "").equals("")) {
                                        Toast.makeText(getActivity(), "SMS service Username not found!", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    if (mSharedPrefs.getString("prefPass", "").equals("")) {
                                        Toast.makeText(getActivity(), "SMS service Password not found!", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    if (mSharedPrefs.getString("SenderID", "").equals("")) {
                                        Toast.makeText(getActivity(), "SMS service SenderID not found!", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }

                            }
                            mIntent = new Intent(getActivity(),FarmerSMSRecieptsActivity.class);
                            startActivity(mIntent);
                        }


                        break;
                    case 3:
                        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                            // go back to milkers activity
                            // Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
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
                            }}
                        mIntent = new Intent(getActivity(),ZReportActivity.class);
                        startActivity(mIntent);
                        break;
                    case 4:

                        if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                            // go back to milkers activity
                            // Toast.makeText(getActivity(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
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
                        mIntent = new Intent(getActivity(),DeliveryReportActivity.class);
                        startActivity(mIntent);
                        break;
                    default:
                        break;
                }


            }
        });

    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE)
                ;

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml("<font color='#FF7F27'>You need internet connection to upload data. Please turn on mobile network or Wi-Fi in Settings.</font>"))
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setNegativeButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (mSharedPrefs.getString("internetAccessModes", "WF").toString().equals("WF")) {

                                    Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    startActivity(i);
                                }else{
                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivity(i);

                                }


                            }
                        }
                )
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

}
