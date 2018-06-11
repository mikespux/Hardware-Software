package com.easyweigh.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.helpers.DirectoryChooserDialog;
import com.easyweigh.preferences.PreferenceGeneralActivity;
import com.easyweigh.preferences.PreferenceOverallActivity;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by Michael on 30/06/2016.
 */
public class ImportMasterActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btnImport;
    Intent mIntent;
    static SharedPreferences mSharedPrefs,prefs;
    String USER_MASTER = "1";
    String AGENT_MASTER = "2";
    String FACTORY_MASTER = "3";
    String PRODUCE_MASTER = "4";
    String GRADE_MASTER = "5";
    String VARIETY_MASTER = "6";
    String ZONE_MASTER = "7";
    String ROUTE_MASTER = "8";
    String SHED_MASTER = "9";
    String FARMER_MASTER = "10";
    String TRANSPORTER_MASTER = "12";
    String WAREHOUSE_MASTER = "13";

    String s_etFullName,s_etNewUserId,s_etPassword,s_spUserLevel;
    String s_agtID,s_agtName;
    String s_whID,s_whName;
    String Sco_prefix,Sco_name,Sco_letterbox,Sco_postcode,Sco_postname,Sco_postregion,Sco_telephone;
    String s_fryprefix,s_fryname;
    String s_etDgProduceCode,s_etDgProduceTitle;
    String s_gr_code,s_gr_name;
    String s_vrt_code,s_vrt_name,produceid;
    String s_fzcode,s_fzname;
    String s_mcrcode,s_mcrname;
    String s_mcno,s_mcname,s_mcroute,s_mczone;
    String s_tptID,s_tptName;

    String s_farmerno, s_cardno, s_idno,s_mobileno,s_farmername, s_managedfarm,s_fshed,s_producetokg;

    String path;
    Handler _handler;
    private int progressStatus = 0;
    int count = 0;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    ArcProgress arcProgress;
    private ProgressBar progressBar;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_importmaster);
        setupToolbar();
        initializer();
        _handler = new Handler();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_item_import);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    public void initializer() {
        mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dbhelper = new DBHelper(getApplicationContext());
        btnImport = (Button) findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnImport.setEnabled(false);
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImportMasterActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                        btnImport.setEnabled(true);
                    }else{
                        btnImport.setEnabled(true);
                        openfolder();

                    }
                }else{
                openfolder();
                btnImport.setEnabled(true);
                }

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
       // btnImport.setEnabled(true);
    }

    public void openfolder(){



        /////////////////////////////////////////////////////////////////////////////////////////////////
        //Create FileSaveDialog and register a callback
        /////////////////////////////////////////////////////////////////////////////////////////////////
        DirectoryChooserDialog FileSaveDialog =  new DirectoryChooserDialog(ImportMasterActivity.this, "FileSave",new DirectoryChooserDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {  path=chosenDir;



                        SQLiteDatabase db = dbhelper.getWritableDatabase();
                        db.delete(Database.OPERATORSMASTER_TABLE_NAME,null,null);
                        db.delete(Database.AGENT_TABLE_NAME,null,null);
                        db.delete(Database.FACTORY_TABLE_NAME,null,null);
                        db.delete(Database.PRODUCE_TABLE_NAME,null,null);
                        db.delete(Database.PRODUCEVARIETIES_TABLE_NAME,null,null);
                        db.delete(Database.PRODUCEGRADES_TABLE_NAME,null,null);
                        db.delete(Database.ZONES_TABLE_NAME,null,null);
                        db.delete(Database.ROUTES_TABLE_NAME,null,null);
                        db.delete(Database.COLLECTIONCENTERS_TABLE_NAME,null,null);
                        db.delete(Database.FARMERS_TABLE_NAME, null, null);
                        db.delete(Database.TRANSPORTER_TABLE_NAME,null,null);
                        db.delete(Database.WAREHOUSE_TABLE_NAME,null,null);
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.OPERATORSMASTER_TABLE_NAME + "'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.AGENT_TABLE_NAME + "'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.FACTORY_TABLE_NAME + "'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCE_TABLE_NAME + "'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCEVARIETIES_TABLE_NAME + "'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.PRODUCEGRADES_TABLE_NAME + "'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+Database.ZONES_TABLE_NAME+"'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+Database.ROUTES_TABLE_NAME+"'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+Database.COLLECTIONCENTERS_TABLE_NAME+"'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+Database.FARMERS_TABLE_NAME+"'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+Database.TRANSPORTER_TABLE_NAME+"'");
                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.WAREHOUSE_TABLE_NAME + "'");
                        String DefaultAgent = "INSERT INTO " + Database.AGENT_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.AGT_NAME + ") Values ('0', 'Select ...')";
                        String DefaultProduce = "INSERT INTO " + Database.PRODUCE_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.MP_DESCRIPTION + ") Values ('0', 'Select ...')";
                        String DefaultVariety = "INSERT INTO " + Database.PRODUCEVARIETIES_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.VRT_NAME + ") Values ('0', 'Select ...')";
                        String DefaultGrade = "INSERT INTO " + Database.PRODUCEGRADES_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.PG_DNAME + ") Values ('0', 'Select ...')";
                        String DefaultTransporter = "INSERT INTO " + Database.TRANSPORTER_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.TPT_NAME + ") Values ('0', 'Select ...')";
                        String DefaultZone = "INSERT INTO " + Database.ZONES_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.FZ_NAME + ") Values ('0', 'Select ...')";
                        String DefaultRoute = "INSERT INTO " + Database.ROUTES_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.MC_RNAME + ") Values ('0', 'Select ...')";
                        String DefaultShed = "INSERT INTO " + Database.COLLECTIONCENTERS_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.MC_CNAME + ") Values ('0', 'Select ...')";
                        String DefaultWareHouse = "INSERT INTO " + Database.WAREHOUSE_TABLE_NAME + " ("
                                + Database.ROW_ID + ", "
                                + Database.WH_NAME + ") Values ('0', 'Select ...')";
                        db.execSQL(DefaultAgent);
                        db.execSQL(DefaultProduce);
                        db.execSQL(DefaultVariety);
                        db.execSQL(DefaultGrade);
                        db.execSQL(DefaultTransporter);
                        db.execSQL(DefaultZone);
                        db.execSQL(DefaultRoute);
                        db.execSQL(DefaultShed);
                        db.execSQL(DefaultWareHouse);
                        new ImportFileAsync().execute(path);

                    }
                });

        //You can change the default filename using the public variable "Default_File_Name"
        FileSaveDialog.Default_File_Name = "";

        FileSaveDialog.chooseFile_or_Dir();

        /////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Data Import ...");
                mProgressDialog.setMessage("Importing data from file ...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    class ImportFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
            arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
            arcProgress.setProgress(0);

            textView = (TextView) findViewById(R.id.textView1);
            btnImport.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... aurl) {


            String next[] = {};
            File file = new File(path);
            try {

                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader csvStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String input;

                while((input = bufferedReader.readLine()) != null)
                {
                    count++;
                }

               // System.out.println("Count : "+count);

                CSVReader reader = new CSVReader(csvStreamReader);

                for (;;) {
                    next = reader.readNext();



                    if (next != null) {



                        if (next[0].equals(USER_MASTER)) {

                            s_etNewUserId = next[1];
                            s_etFullName =  next[2];
                            s_etPassword = next[3];
                            s_spUserLevel = next[4];
                            dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel);

                        }
                        else if(next[0].equals(AGENT_MASTER)){

                            s_agtID = next[1];
                            s_agtName =  next[2];
                            dbhelper.AddAgent(s_agtID, s_agtName);

                        }
                        else if(next[0].equals(FACTORY_MASTER)){

                            s_fryprefix = next[1];
                            s_fryname = next[2];
                            dbhelper.AddFactories(s_fryprefix, s_fryname);

                        }
                        else if(next[0].equals(PRODUCE_MASTER)){


                            s_etDgProduceCode = next[1];
                            s_etDgProduceTitle = next[2];
                            dbhelper.AddProduce(s_etDgProduceCode, s_etDgProduceTitle);
                        }
                        else if(next[0].equals(GRADE_MASTER)){

                            s_gr_code = next[1];
                            s_gr_name = next[2];
                            produceid = next[3];
                            dbhelper.AddGrade(s_gr_code, s_gr_name,produceid);

                        }
                        else if(next[0].equals(VARIETY_MASTER)){

                            s_vrt_code = next[1];
                            s_vrt_name = next[2];
                            produceid = next[3];
                            dbhelper.AddVariety(s_vrt_code, s_vrt_name,produceid);

                        }
                        else if(next[0].equals(ZONE_MASTER)){



                            s_fzcode = next[1];
                            s_fzname = next[2];
                            dbhelper.AddZones(s_fzcode, s_fzname);
                        }
                        else if(next[0].equals(ROUTE_MASTER)){

                            s_mcrcode = next[1];
                            s_mcrname = next[2];
                            dbhelper.AddRoutes(s_mcrcode, s_mcrname);
                        }
                        else if(next[0].equals(SHED_MASTER)){

                            s_mcno = next[1];
                            s_mcname = next[2];
                            s_mczone = next[3];
                            s_mcroute = next[4];
                            dbhelper.AddSheds(s_mcno, s_mcname, s_mcroute, s_mczone);
                        }

                        else if(next[0].equals(FARMER_MASTER)){


                            s_farmerno = next[1];
                            s_cardno = next[2];
                            s_idno = next[3];
                            s_mobileno = next[4];
                            s_farmername = next[5];
                            s_fshed = next[6];
                            s_managedfarm=next[7];
                            s_producetokg=next[8];
                            dbhelper.AddFarmers(s_farmerno, s_cardno, s_idno,s_mobileno,s_farmername, s_fshed, s_managedfarm,s_producetokg);

                        }
                        else if(next[0].equals(TRANSPORTER_MASTER)){

                            s_tptID = next[1];
                            s_tptName = next[2];
                            dbhelper.AddTransporter(s_tptID, s_tptName);
                        }
                        else if(next[0].equals(WAREHOUSE_MASTER)){

                            s_whID = next[1];
                            s_whName =  next[2];
                            dbhelper.AddWarehouse(s_whID, s_whName);

                        }


                    } else {
                        break;
                    }
                    progressStatus++;
                   publishProgress("" + progressStatus);


                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
          //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setMax(count);
            arcProgress.setBottomText("IMPORTING ...");
            textView.setText(Integer.parseInt(progress[0]) + "/" + count + " Records");
        }

        @Override
        protected void onPostExecute(String unused) {
           // dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            finish();
            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText(count+" Records Imported successfully");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();


            if (mSharedPrefs.getString("terminalID", "").toString().equals("")) {
                mIntent = new Intent(ImportMasterActivity.this,PreferenceOverallActivity.class);
                startActivity(mIntent);


                View customToastroot1 =inflater.inflate(R.layout.blue_toast, null);
                TextView text1 = (TextView) customToastroot1.findViewById(R.id.toast);
                text1.setText("Prepare Settings ...");
                Toast customtoast1=new Toast(context);
                customtoast1.setView(customToastroot1);
                customtoast1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast1.setDuration(Toast.LENGTH_LONG);
                customtoast1.show();
                return;
            }
            mIntent = new Intent(ImportMasterActivity.this,MainActivity.class);
            startActivity(mIntent);

            //Toast.makeText(ImportMasterActivity.this, "Data Imported successfully!!", Toast.LENGTH_LONG).show();
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        //Display alert message when back button has been pressed

   if(count>0){
       Context context=getApplicationContext();
       LayoutInflater inflater=getLayoutInflater();
       View customToastroot =inflater.inflate(R.layout.red_toast, null);
       TextView text = (TextView) customToastroot.findViewById(R.id.toast);
       text.setText("You cannot close window while importing master !!");
       Toast customtoast=new Toast(context);
       customtoast.setView(customToastroot);
       customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
       customtoast.setDuration(Toast.LENGTH_LONG);
       customtoast.show();
    return;
  }

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor accounts = db.query(true, Database.FARMERS_TABLE_NAME, null, null, null, null, null, null, null, null);
        if(accounts.getCount()==0){
            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Please Import Master !!");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
        else{
            finish();
            btnImport.setVisibility(View.VISIBLE);
            mIntent = new Intent(ImportMasterActivity.this,MainActivity.class);
            startActivity(mIntent);
        }

        return;
    }

}