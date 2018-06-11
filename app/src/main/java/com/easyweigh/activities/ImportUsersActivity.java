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
import android.support.annotation.RequiresApi;
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
import com.easyweigh.preferences.PreferenceOverallActivity;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by Michael on 30/06/2016.
 */
public class ImportUsersActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btnImport;
    Intent mIntent;
    static SharedPreferences mSharedPrefs,prefs;
    String USER_MASTER = "1";


    String s_etFullName,s_etNewUserId,s_etPassword,s_spUserLevel;


    String path;
    Handler _handler;
    private int progressStatus = 0;
    int usercount;
    int count = 0;
    int lineCount = 0;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    ArcProgress arcProgress;
    private ProgressBar progressBar;
    private TextView textView;
    String systembasedate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_importuser);
        setupToolbar();
        initializer();
        _handler = new Handler();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_item_import_user);

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
                        ActivityCompat.requestPermissions(ImportUsersActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
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
        DirectoryChooserDialog FileSaveDialog =  new DirectoryChooserDialog(ImportUsersActivity.this, "FileSave",new DirectoryChooserDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {  path=chosenDir;

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


                                lineCount ++;
                                if(lineCount == 1) {
                                    if(s_spUserLevel.equals("1")||s_spUserLevel!=null){
                                        SQLiteDatabase db = dbhelper.getWritableDatabase();
                                        db.delete(Database.OPERATORSMASTER_TABLE_NAME,null,null);
                                        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + Database.OPERATORSMASTER_TABLE_NAME + "'");

                                    }else{
                                        return null;
                                    }
                                }


                            dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel);


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
            arcProgress.setProgress(Integer.parseInt(progress[0]));
            arcProgress.setMax(count);
            arcProgress.setBottomText("IMPORTING ...");
            textView.setText(Integer.parseInt(progress[0]) + "/" + count + " Records");
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String unused) {
           // dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            SQLiteDatabase db= dbhelper.getReadableDatabase();
            Cursor users = db.query(true, Database.OPERATORSMASTER_TABLE_NAME, null, null, null, null, null, null, null, null);
            usercount=users.getCount();

            if(count==0 || usercount<=1){

                finish();
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Error!! This file does not have an administrator.");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                systembasedate = prefs.getString("basedate","");
                if (systembasedate.toString().equals("") && mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ImportUsersActivity.this);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("user");
                    edit.remove("pass");
                    edit.commit();
                    finish();
                   Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(login);

                    return;
                }
                return;
            }
            finish();
            Context context=getApplicationContext();
            LayoutInflater inflater=getLayoutInflater();
            View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText(count+" users imported successfully");
            Toast customtoast=new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();

            systembasedate = prefs.getString("basedate","");
            if (systembasedate.toString().equals("") && mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ImportUsersActivity.this);
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("user");
                edit.remove("pass");
                edit.commit();
                finish();
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);

                return;
            }
            mIntent = new Intent(ImportUsersActivity.this,MainActivity.class);
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
       text.setText("You cannot close window while importing users !!");
       Toast customtoast=new Toast(context);
       customtoast.setView(customToastroot);
       customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
       customtoast.setDuration(Toast.LENGTH_LONG);
       customtoast.show();
    return;
  }
              systembasedate = prefs.getString("basedate","");
        if (systembasedate.toString().equals("") && mSharedPrefs.getString("scaleVersion", "").toString().equals("")) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ImportUsersActivity.this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("user");
            edit.remove("pass");
            edit.commit();
            finish();
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);

            return;
        }

            finish();
            mIntent = new Intent(ImportUsersActivity.this,MainActivity.class);
            startActivity(mIntent);


    }

}