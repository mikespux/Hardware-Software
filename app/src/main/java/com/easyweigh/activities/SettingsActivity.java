package com.easyweigh.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.easyweigh.R;
import com.easyweigh.adapters.SettingsAdapter;
import com.easyweigh.fragments.TabsFragment;
import com.easyweigh.helpers.DividerItemDecoration;
import com.easyweigh.helpers.RecyclerTouchListener;
import com.easyweigh.data.SettingsItem;
import com.easyweigh.preferences.PreferenceGeneralActivity;
import com.easyweigh.preferences.PreferenceOverallActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 8/19/2015.
 */
public class SettingsActivity extends AppCompatActivity {
    public RecyclerView settingsList;
    public SettingsAdapter settingsAdapter;
    public Intent mIntent = null;
    public Toolbar toolbar;
    static SharedPreferences mSharedPrefs,prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializer();


    }

    /**
     * method initializer
     */
    public void initializer() {
        mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        settingsList = (RecyclerView) findViewById(R.id.settingsList);
        settingsAdapter = new SettingsAdapter(this, getData());
        settingsList.setHasFixedSize(true);
        settingsList.setAdapter(settingsAdapter);
        settingsList.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, null);
        settingsList.addItemDecoration(itemDecoration);

        // this is the default; this call is actually only necessary with custom ItemAnimators
        settingsList.setItemAnimator(new DefaultItemAnimator());

        settingsList.addOnItemTouchListener(new RecyclerTouchListener(this, settingsList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                switch (position - 1) {
                    case 0:
                        mIntent = new Intent(SettingsActivity.this, PreferenceOverallActivity.class);//Overall Settings
                        break;
                    case 1:
                        mIntent = new Intent(SettingsActivity.this,UserDetailsActivity.class);//user Details
                        break;
                    case 2:
                        mIntent = new Intent(SettingsActivity.this,AgentDetailsActivity.class);//Agent Details
                        break;
                    case 3:
                        mIntent = new Intent(SettingsActivity.this, FactoryDetailsActivity.class);//Factory Details
                        break;
                    case 4:
                        mIntent = new Intent(SettingsActivity.this, WareHouseDetailsActivity.class);//Factory Details
                        break;
                    case 5:
                        mIntent = new Intent(SettingsActivity.this, ProduceDetailsActivity.class);//Produce details
                        break;
                    case 6:
                        mIntent = new Intent(SettingsActivity.this, ZoneDetailsActivity.class);//Zone details
                        break;
                    case 7:
                        mIntent = new Intent(SettingsActivity.this, RouteDetailsActivity.class);//Routes Details
                        break;
                    case 8:
                        mIntent = new Intent(SettingsActivity.this, ShedDetailsActivity.class);//Shed Details
                        break;
                    case 9:
                        mIntent = new Intent(SettingsActivity.this,FarmerDetailsActivity.class);//FarmerDetails
                        break;
                    case 10:
                        mIntent = new Intent(SettingsActivity.this,TransporterDetailsActivity.class);//Transporter Details
                        break;


                    default:
                        break;
                }

                if (mIntent != null) {
                    startActivity(mIntent);
                }

            }

            @Override
            public void onLongClick(View view, int position) {
                //add whatever you want like StartActivity.
            }
        }));
    }
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
        mIntent = new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(mIntent);
        return;

    }
    public static List<SettingsItem> getData() {
        List<SettingsItem> data = new ArrayList<>();
        int[] icons = { R.mipmap.ic_settings_black_24dp,R.mipmap.ic_useradd ,R.mipmap.ic_agent, R.mipmap.ic_factory,R.mipmap.ic_shed,R.mipmap.ic_produce, R.mipmap.ic_zone, R.mipmap.ic_route, R.mipmap.ic_shed, R.mipmap.ic_farmers, R.mipmap.ic_transporter};
        String[] titles = {"General Settings","Users","Agents","Factories","Warehouses","Produce", "Zones", "Routes", "Sheds","Farmers","Transporters" };

        for (int i = 0; i < titles.length && i < icons.length; i++) {

            SettingsItem current = new SettingsItem();
            current.iconId = icons[i];
            current.title = titles[i];
            data.add(current);
        }

        return data;
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);

    }


}
