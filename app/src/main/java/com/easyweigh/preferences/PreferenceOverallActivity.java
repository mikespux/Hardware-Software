package com.easyweigh.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.easyweigh.R;

import com.easyweigh.helpers.InputFilterMinMax;

/**
 * Created by Michael on 8/19/2015.
 */
public class PreferenceOverallActivity extends PreferenceActivity {
    static SharedPreferences mSharedPrefs,prefs;
    Intent mIntent;
    EditText sampleReadings;
    EditTextPreference timeInterval;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_settings);
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

        sampleReadings = ((EditTextPreference) findPreference("stabilityReadingCounter")).getEditText();
        sampleReadings.setFilters(new InputFilter[]{ new InputFilterMinMax("2", "9")});

        timeInterval=(EditTextPreference)findPreference("milliSeconds");
        final int minTime = 100;
        final int maxTime = 999;

        timeInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("")){
                    newValue="30";
                }

                int val = Integer.parseInt(newValue.toString());
                if ((val >= minTime) && (val <= maxTime)) {

                    //Log.d(LOGTAG, "Value saved: " + val);
                    return true;
                }
                else {
                    // invalid you can show invalid message
                    Context context=getApplicationContext();
                    LayoutInflater inflater=getLayoutInflater();
                    View customToastroot =inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please enter values between "+minTime +" and "+maxTime);
                    Toast customtoast=new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    //Toast.makeText(getApplicationContext(), "Please enter values between "+minTime +" and "+maxTime, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
      }

    public void onBackPressed() {
        //Display alert message when back button has been pressed

        if (mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
            finish();
            mIntent = new Intent(PreferenceOverallActivity.this,PreferenceGeneralActivity.class);
            startActivity(mIntent);
            return;
        }
        finish();
        return;

    }
}
