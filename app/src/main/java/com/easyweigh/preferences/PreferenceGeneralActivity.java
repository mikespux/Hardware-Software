package com.easyweigh.preferences;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.activities.LoginActivity;
import com.easyweigh.activities.MainActivity;
import com.easyweigh.activities.PairedDeviceListActivity;
import com.easyweigh.activities.PrintTestActivity;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;


/**
 * Created by Michael on 8/19/2015.
 */
public class PreferenceGeneralActivity extends PreferenceActivity {
    DBHelper dbhelper;
    Button btnImport;
    Intent mIntent;
    static SharedPreferences mSharedPrefs,prefs;
    String cachedDeviceAddress;
    Button btn_pairscale,btn_pairprinter;
    AlertDialog b;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_general);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(toolbar, 0);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(R.string.General_settings);
        toolbar.setTitleTextColor(Color.WHITE);
        mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        cachedDeviceAddress = pref.getString("address", "");

        if (mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Please Select Scale Model to Weigh!!");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
       else if (mSharedPrefs.getString("scaleVersion", "").toString().equals("EW15")) {
            if (!cachedDeviceAddress.toString().equals("")) {
               finish();
                return;
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.pair_devices, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle("Pair Devices");
            dialogBuilder.setCancelable(false);

            btn_pairscale = (Button) dialogView.findViewById(R.id.btn_pairscale);
            btn_pairscale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mIntent = new Intent(PreferenceGeneralActivity.this,PairedDeviceListActivity.class);
                    startActivity(mIntent);


                }
            });
            btn_pairprinter = (Button) dialogView.findViewById(R.id.btn_pairprinter);
            if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                // go back to milkers activity
                Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                btn_pairprinter.setVisibility(View.GONE);

                //return;
            }else{
                btn_pairprinter.setVisibility(View.VISIBLE);
            }

            btn_pairprinter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    mIntent = new Intent(PreferenceGeneralActivity.this, PrintTestActivity.class);
                    startActivity(mIntent);



                }
            });

            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();


                }
            });
            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //Toast.makeText(PreferenceGeneralActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    return false;
                }
            });
            b = dialogBuilder.create();
            b.show();

            b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    b.dismiss();
                    finish();
                    mIntent = new Intent(PreferenceGeneralActivity.this,MainActivity.class);
                    startActivity(mIntent);
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog)
                        b.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        } else if (mSharedPrefs.getString("scaleVersion", "").toString().equals("EW11")) {
            if (!cachedDeviceAddress.toString().equals("")) {
                finish();
                return;
            }
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.pair_devices, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle("Pair Devices");
            dialogBuilder.setCancelable(false);

            btn_pairscale = (Button) dialogView.findViewById(R.id.btn_pairscale);
            btn_pairscale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mIntent = new Intent(PreferenceGeneralActivity.this,PairedDeviceListActivity.class);
                    startActivity(mIntent);
                }
            });
            btn_pairprinter = (Button) dialogView.findViewById(R.id.btn_pairprinter);
            if(!mSharedPrefs.getBoolean("enablePrinting", false)==true) {
                // go back to milkers activity
                Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                btn_pairprinter.setVisibility(View.GONE);

                //return;
            }else{
                btn_pairprinter.setVisibility(View.VISIBLE);
            }

            btn_pairprinter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mIntent = new Intent(PreferenceGeneralActivity.this, PrintTestActivity.class);
                    startActivity(mIntent);


                }
            });

            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();


                }
            });
            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //Toast.makeText(PreferenceGeneralActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    return false;
                }
            });
            b = dialogBuilder.create();
            b.show();
            b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  /*  if (cachedDeviceAddress.toString().equals("")) {
                        Context context = dialogView.getContext();
                        LayoutInflater inflater = getLayoutInflater();
                        View customToastroot = inflater.inflate(R.layout.red_toast, null);
                        TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                        text.setText("Please Pair Scale ...");
                        Toast customtoast = new Toast(context);
                        customtoast.setView(customToastroot);
                        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                        customtoast.setDuration(Toast.LENGTH_LONG);
                        customtoast.show();
                        return;
                    }

                    if (mSharedPrefs.getBoolean("enablePrinting", false) == true) {
                        // go back to milkers activity

                        if (prefs.getString("mDevice", "").equals("")) {
                            Context context = dialogView.getContext();
                            LayoutInflater inflater = getLayoutInflater();
                            View customToastroot = inflater.inflate(R.layout.red_toast, null);
                            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                            text.setText("Please Pair Printer ...");
                            Toast customtoast = new Toast(context);
                            customtoast.setView(customToastroot);
                            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            customtoast.setDuration(Toast.LENGTH_LONG);
                            customtoast.show();
                            return;
                        }
                        return;
                    }*/
                   /* SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PreferenceGeneralActivity.this);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("user");
                    edit.remove("pass");
                    edit.commit();*/
                    b.dismiss();
                    finish();
                    mIntent = new Intent(PreferenceGeneralActivity.this, MainActivity.class);
                    startActivity(mIntent);
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog)
                        b.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }  else{
            if (!cachedDeviceAddress.toString().equals("")) {
                finish();
                return;
            }
           /* SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PreferenceGeneralActivity.this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("user");
            edit.remove("pass");
            edit.commit();*/
            finish();
            mIntent = new Intent(PreferenceGeneralActivity.this,MainActivity.class);
            startActivity(mIntent);
        }

        return;
    }
}
