package com.easyweigh.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.activities.AgentDeliveryReportActivity;
import com.easyweigh.activities.AgentRecieptsActivity;
import com.easyweigh.activities.DeliveryReportActivity;
import com.easyweigh.activities.FarmerDetailedRecieptsActivity;
import com.easyweigh.activities.FarmerRecieptsActivity;
import com.easyweigh.activities.ZAgtReportActivity;
import com.easyweigh.activities.ZReportActivity;
import com.easyweigh.helpers.CustomListReport;


public class AgentsReportsFragment extends Fragment {

    public View mView;
    public Intent mIntent;
    public LinearLayoutManager layoutManager;
    public Context mContext;
    SharedPreferences prefs;
    static SharedPreferences mSharedPrefs;

    ListView list;
    String[]  web     = {
            "Agent Receipts","Z Report","Delivery Report"
    };
    Integer[] imageId = {

            R.drawable.ic_receipt,
            R.drawable.ic_zreport,
            R.drawable.ic_delivary,
            R.drawable.ic_list,
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
        CustomListReport adapter = new
                CustomListReport(getActivity(), web, imageId);
        list = (ListView) mView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                if (position == 1)
                {
                    //Intent myIntent = new Intent(Ali10Activity.this, Hassan.class);
                    //startActivity(myIntent);
                }
                switch (position) {
                    case 0:
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
                        }
                        }
                        mIntent = new Intent(getActivity(),AgentRecieptsActivity.class);
                        startActivity(mIntent);
                        break;
                    case 1:
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
                        mIntent = new Intent(getActivity(),ZAgtReportActivity.class);
                        startActivity(mIntent);
                        break;
                    case 2:
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
                        mIntent = new Intent(getActivity(),AgentDeliveryReportActivity.class);
                        startActivity(mIntent);
                        //mIntent = new Intent(getActivity(),AgentDetailsActivity.class);//Agent Details
                        break;
                    default:
                        break;
                }


            }
        });

    }



}
