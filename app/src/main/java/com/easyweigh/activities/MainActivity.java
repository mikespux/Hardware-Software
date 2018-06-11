package com.easyweigh.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.aidlprinter.service.utils.PrinterTestDemoAct;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;
import com.easyweigh.fragments.TabsFragment;
import com.easyweigh.preferences.PreferenceGeneralActivity;
import com.easyweigh.services.AgentWeighingService;
import com.easyweigh.services.WeighingService;
import com.easyweigh.vsp.serialdevice.SerialActivity;
import com.fgtit.fpreader.FDeviceListActivity;
import com.fgtit.fpreader.FingerPrintReader;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private Toolbar toolbar;
    private Fragment mFragment;
    DBHelper dbhelper;
    int accesslevel = 0;
    int useridentifier = 1;
    MenuItem item;
    Menu nav_Menu;
    Intent mIntent;
    TextView userid;
    Button btn_pairscale,btn_pairprinter,btn_pairreader;
    private LocalDate systemdate,currentdate;
    WeighingService resetConn;
    AgentWeighingService resetCon2;
    SharedPreferences prefs;
    String systembasedate;
    private EditText edtBaseDate;
    Button pickDate;
    public int count;
    private final int DATE_DIALOG_ID=1;
    AlertDialog b;
    String user_level;
    public static final String TRANCELL_TI500 = "TI-500";
    static SharedPreferences mSharedPrefs;
   SearchView searchView;
    CheckBox checkID,checkPhoneNo,checkVisiblePass;
    TextView et_farmerno, et_cardno, et_idno,et_mobileno,et_farmername;
    TextView   et_fshed, et_managedfarm;
    String accountId;
    NfcAdapter nfcAdapter;
    private AlertDialog mEnableNfc;

    EditText edtOldPass,edtNewPass,edtConfirmPass;
    Button btnChangePass;
    String username,userpass;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resetConn=new WeighingService();
        resetCon2=new AgentWeighingService();
        prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        dbhelper = new DBHelper(getApplicationContext());

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View header=navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                displayView(menuItem.getItemId());
                //Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        userid=(TextView) header.findViewById(R.id.userid);
        username= prefs.getString("user", "");
        Cursor d = dbhelper.getAccessLevel(username);
        user_level = d.getString(accesslevel);
        String user_fullname = d.getString(useridentifier);
        SharedPreferences.Editor edit1 = prefs.edit();
        edit1.putString("fullname", user_fullname);
        edit1.commit();

        // Toast.makeText(MainActivity.this, prefs.getString("count", ""), Toast.LENGTH_LONG).show();
        if(user_level.equals("2")){
            userid.setText("Welcome " + user_fullname+ "\n" + "(Clerk)");
        }else{
            userid.setText("Welcome "+user_fullname+ "\n" + "(Manager)");
        }


        //TABS VIEW
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.container, new TabsFragment()).commit();
        systembasedate = prefs.getString("basedate","");
        if (systembasedate.toString().equals("")) {


            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_basedate, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setTitle("App Base Date");
            edtBaseDate	= (EditText) dialogView.findViewById(R.id.editText);
            edtBaseDate.setEnabled(false);
           // edtBaseDate.setText(prefs.getString("basedate",""));

            pickDate =(Button)dialogView.findViewById(R.id.btnDate);
            pickDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(DATE_DIALOG_ID);

                }
            });

            dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {


                }
            });

            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    return false;
                }
            });


            b = dialogBuilder.create();

            if(user_level.equals("1")){
                b.show();
            }
            if(user_level.equals("2")){
                Context context=getApplicationContext();
                LayoutInflater inflater1=getLayoutInflater();
                View customToastroot =inflater1.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Please Contact the Administrator to Solve Base Date Error!!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                new LogOut().execute();
                return;
            }
            b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (edtBaseDate.getText().length() == 0) {
                        Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("basedate", edtBaseDate.getText().toString());
                    edit.commit();
                    b.dismiss();
                    count = Integer.parseInt(prefs.getString("count", ""));

                    if (count == 0) {
                        finish();
                        mIntent = new Intent(MainActivity.this, ImportMainMasterActivity.class);
                        startActivity(mIntent);
                        Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();


                        return;
                    }
                    if (count > 0) {

                        finish();
                        mIntent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(mIntent);
                        Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog)
                        b.dismiss();
                }
            });
            return;

        }else{
            systemdate = LocalDate.parse( prefs.getString("basedate",""));
            currentdate = new LocalDate();


            if(Days.daysBetween(currentdate,systemdate ).getDays()>=1){
                if(user_level.equals("1")){
                    Toast.makeText(MainActivity.this,"Current Base Date is:"+systemdate+" and should not be greater than Phone Date, Please Reset", Toast.LENGTH_LONG).show();
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.blue_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Please Reset PhoneDate and Login to Reset Base Date");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("basedate");
                    edit.commit();
                new LogOut().execute();
               //
                startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                 return;
                }
                if(user_level.equals("2")){
                    Context context=getApplicationContext();
                    LayoutInflater inflater=getLayoutInflater();
                    View customToastroot =inflater.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Contact the Administrator to Solve Base Date Error!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    new LogOut().execute();
                  return;
                }

            }
            else if(Days.daysBetween(systemdate, currentdate).getDays()>=7){


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater=getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_basedate, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setCancelable(false);
                dialogBuilder.setTitle("App Base Date");
                edtBaseDate	= (EditText) dialogView.findViewById(R.id.editText);
                edtBaseDate.setEnabled(false);
                edtBaseDate.setText(prefs.getString("basedate",""));

                pickDate =(Button)dialogView.findViewById(R.id.btnDate);
                pickDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DATE_DIALOG_ID);

                    }
                });

                dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString();




                    }
                });

                dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        return false;
                    }
                });
           /* dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    getdata();
                }
            });*/
                b = dialogBuilder.create();
                if(user_level.equals("1")){
                    Context context=getApplicationContext();
                    LayoutInflater inflater2=getLayoutInflater();
                    View customToastroot =inflater2.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Current Base Date is: '"+systemdate+"' and should not be less than 7 days the Phone Date, Please Reset");
                    Toast customtoast=new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    b.show();
                }
                if(user_level.equals("2")){
                    Context context=getApplicationContext();
                    LayoutInflater inflater1=getLayoutInflater();
                    View customToastroot =inflater1.inflate(R.layout.red_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Please Contact the Administrator to Solve Base Date Error!!");
                    Toast customtoast = new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                    new LogOut().execute();
                    return;
                }
                b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(edtBaseDate.getText().length()==0){
                            Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                            return;
                        }
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("basedate", edtBaseDate.getText().toString());
                        edit.commit();
                        b.dismiss();

                        count=Integer.parseInt(prefs.getString("count", ""));

                        if(count==0){
                            finish();
                            mIntent = new Intent(MainActivity.this,ImportMainMasterActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString()+" saved successfully", Toast.LENGTH_LONG).show();



                            return;
                        }
                        if(count>0){

                            finish();
                            mIntent = new Intent(MainActivity.this,MainActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString()+" saved successfully", Toast.LENGTH_LONG).show();
                            return;
                        }


                        Boolean wantToCloseDialog = false;
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            b.dismiss();
                    }
                });
                //Toast.makeText(MainActivity.this,"Current date is more than 5 days set date or reset Base date", Toast.LENGTH_LONG).show();

            }else{

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("basedate",currentdate.toString());
                edit.commit();

                if (prefs.getString("DeliverNoteNumber", "").toString().equals("")
                        ||prefs.getString("DeliverNoteNumber", "").toString().equals("No Batch Opened")) {
                    Context context1 = getApplicationContext();
                    LayoutInflater inflater1 = getLayoutInflater();
                    View customToastroot1 = inflater1.inflate(R.layout.white_red_toast, null);
                    TextView text1 = (TextView) customToastroot1.findViewById(R.id.toast);
                    text1.setText("Please Open Batch To Proceed ...");
                    Toast customtoast1 = new Toast(context1);
                    customtoast1.setView(customToastroot1);
                    customtoast1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    customtoast1.setDuration(Toast.LENGTH_LONG);
                   // customtoast1.show();
                }
                //Toast.makeText(MainActivity.this, currentdate.toString()+" updated successfully", Toast.LENGTH_LONG).show();

            }
        }
    }
    public boolean validatePastDate(Context mContext,int day,int month,int year){
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH)+1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        if (day > currentDay && year == currentYear && month == currentMonth) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select today's date not future date ", Toast.LENGTH_LONG).show();
            return false;
        } else if (month > currentMonth && year == currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid month", Toast.LENGTH_LONG).show();
            return false;
        } else if (year > currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid year", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    public boolean validateFutureDate(Context mContext,int day,int month,int year){
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH)+1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        if (day < currentDay && year == currentYear && month == currentMonth) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select today's date not past date ", Toast.LENGTH_LONG).show();
            return false;
        } else if (month < currentMonth && year == currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid month", Toast.LENGTH_LONG).show();
            return false;
        } else if (year < currentYear) {
            edtBaseDate.setText("");
            Toast.makeText(mContext, "Please select valid year", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    private DatePickerDialog.OnDateSetListener mDatelistener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,int month, int day) {
                    if (!validatePastDate(view.getContext(), day, month+1,  year)) {
                        return;
                    }
                    if (!validateFutureDate(view.getContext(), day, month+1, year)) {
                        return;
                    }
                    edtBaseDate.setText( String.format("%d-%d-%d",year,month + 1,day));

                }
            };


    @Override
    protected Dialog onCreateDialog(int id) {

            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1);



        switch (id) {
                case DATE_DIALOG_ID:
                    //start changes...
                    DatePickerDialog dialog = new DatePickerDialog(this, mDatelistener, year, month, day);
                    dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    dialog.getDatePicker().setMaxDate(System.currentTimeMillis());


                    return  dialog;
                //end changes...
            }

            return null;
            // Create a new instance of DatePickerDialog and return it
            //return new DatePickerDialog(getActivity(), this, year, month, day);
        }




    /**
     * method to display items of drawer
     *
     * @param id
     */
    private void displayView(int id) {
        switch (id) {
            case R.id.navigation_item_home:
                mFragment = new TabsFragment();
                break;
            case R.id.navigation_item_search:
                //Toast.makeText(getBaseContext(), "Search Farmer!!!", Toast.LENGTH_LONG).show();
                searchFarmer();
                break;
            case R.id.navigation_item_import:
                finish();
                mIntent = new Intent(MainActivity.this,ImportMainMasterActivity.class);
                startActivity(mIntent);
                break;
            case R.id.navigation_item_import_user:
                finish();
                mIntent = new Intent(MainActivity.this,ImportUsersActivity.class);
                startActivity(mIntent);
                break;
            case R.id.navigation_item_farmers:

                if ( prefs.getString("mDevice", "").toString().equals("")) {
                    // snackbar.show();
                    Context context = getApplicationContext();
                    LayoutInflater inflater = getLayoutInflater();
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
                mIntent = new Intent(MainActivity.this,FarmerRecieptsActivity.class);
                startActivity(mIntent);
                break;
            case R.id.navigation_item_paired:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.pair_devices, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("Pair Devices");

                btn_pairscale = (Button) dialogView.findViewById(R.id.btn_pairscale);
                btn_pairscale.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mIntent = new Intent(MainActivity.this,PairedDeviceListActivity.class);
                        startActivity(mIntent);
                    }
                });
                btn_pairreader = (Button) dialogView.findViewById(R.id.btn_pairreader);
                if (mSharedPrefs.getString("vModes", "Card").toString().equals("Card")){
                    btn_pairreader.setVisibility(View.VISIBLE);
                }else{
                    btn_pairreader.setVisibility(View.GONE);
                }
                btn_pairreader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mIntent = new Intent(MainActivity.this,FDeviceListActivity.class);
                        startActivity(mIntent);
                    }
                });
                btn_pairprinter = (Button) dialogView.findViewById(R.id.btn_pairprinter);
                btn_pairprinter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!mSharedPrefs.getBoolean("enableInternalP", false) == true) {
                            // go back to milkers activity
                            //Toast.makeText(getBaseContext(), "Printing not enabled on settings", Toast.LENGTH_LONG).show();
                            mIntent = new Intent(MainActivity.this,PrintTestActivity.class);
                            startActivity(mIntent);
                        } else {
                            mIntent = new Intent(MainActivity.this,PrinterTestDemoAct.class);
                            startActivity(mIntent);
                        }




                    }
                });

                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString();


                    }
                });

                b = dialogBuilder.create();
                b.show();
                break;
            case R.id.navigation_item_settings:
                 mIntent = new Intent(MainActivity.this,PreferenceGeneralActivity.class);
                startActivity(mIntent);
                break;
            case R.id.nav_basedate:

                AlertDialog.Builder dialogBasedate = new AlertDialog.Builder(this);
                LayoutInflater inflater1 = this.getLayoutInflater();
                final View dialogView1 = inflater1.inflate(R.layout.dialog_basedate, null);
                dialogBasedate.setView(dialogView1);
                dialogBasedate.setCancelable(true);
                dialogBasedate.setTitle("App Base Date");
                edtBaseDate	= (EditText) dialogView1.findViewById(R.id.editText);
                edtBaseDate.setEnabled(false);
                edtBaseDate.setText(prefs.getString("basedate",""));

                pickDate =(Button)dialogView1.findViewById(R.id.btnDate);
                pickDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DATE_DIALOG_ID);

                    }
                });

                dialogBasedate.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do something with edt.getText().toString();




                    }
                });

                dialogBasedate.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            Toast.makeText(MainActivity.this, "Please Set Base Date", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        return false;
                    }
                });
           /* dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    getdata();
                }
            });*/
                b = dialogBasedate.create();
                b.show();
                b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (edtBaseDate.getText().length() == 0) {
                            Toast.makeText(MainActivity.this, "Please set Base Date", Toast.LENGTH_LONG).show();
                            return;
                        }
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString("basedate", edtBaseDate.getText().toString());
                        edit.commit();
                        b.dismiss();

                        count = Integer.parseInt(prefs.getString("count", ""));

                        if (count == 0) {
                            finish();
                            mIntent = new Intent(MainActivity.this, ImportMainMasterActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();


                            return;
                        }
                        if (count > 0) {

                            finish();
                            mIntent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(mIntent);
                            Toast.makeText(MainActivity.this, edtBaseDate.getText().toString() + " saved successfully", Toast.LENGTH_LONG).show();
                            return;
                        }


                        Boolean wantToCloseDialog = false;
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            b.dismiss();
                    }
                });
                break;
            case R.id.ChangePass:
                changePassword();
                break;
            case R.id.fp_reader:
                //finish();
                mIntent = new Intent(MainActivity.this, FingerPrintReader.class);
                startActivity(mIntent);
                break;
            case R.id.SerialDev:
                //finish();
                mIntent = new Intent(MainActivity.this,SerialActivity.class);
                startActivity(mIntent);
                break;
            case R.id.SerialDevTest:
                //finish();
                mIntent = new Intent(MainActivity.this,FarmerScaleSerialWeighActivity.class);
                startActivity(mIntent);
                break;
            case R.id.SignOutItem:
                new LogOut().execute();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("user");
                edit.remove("pass");
                edit.commit();
                break;
        }
        if (mFragment != null && mIntent == null) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.container, mFragment).commit();
        } else {
            //startActivity(mIntent);
            mIntent = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        dbhelper = new DBHelper(getApplicationContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String username = prefs.getString("user", "");

        Cursor d = dbhelper.getAccessLevel(username);
        String user_level = d.getString(accesslevel);
        //Toast.makeText(MainActivity.this, user_level, Toast.LENGTH_LONG).show();(
        if(user_level.equals("2")){
            item = menu.findItem(R.id.action_settings);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;


            case R.id.action_settings:
                finish();
                Intent mIntent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(mIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onBackPressed() {
        //Display alert message when back button has been pressed
        backButtonHandler();
        return;
    }


    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Close Easyway?");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to close the application?");

        // Setting Positive "Yes" Button
        alertDialog.setNegativeButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new LogOut().execute();
                       // stopService(new Intent(MainActivity.this, WeighingService.class));


                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setPositiveButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }
    public void onStart() {
        super.onStart();
        dbhelper = new DBHelper(getApplicationContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String username = prefs.getString("user", "");
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        nav_Menu = navigationView.getMenu();
        if (mSharedPrefs.getString("scaleVersion", "").toString().equals(TRANCELL_TI500)) {
            //nav_Menu.findItem(R.id.SerialDev).setVisible(false);
            nav_Menu.findItem(R.id.SerialDevTest).setVisible(false);
        }
        else{
            nav_Menu.findItem(R.id.SerialDev).setVisible(false);
            nav_Menu.findItem(R.id.SerialDevTest).setVisible(false);
        }
        Cursor d = dbhelper.getAccessLevel(username);
        user_level = d.getString(accesslevel);
        //Toast.makeText(MainActivity.this, user_level, Toast.LENGTH_LONG).show();(
        if(user_level.equals("2")){

           //nav_Menu.findItem(R.id.navigation_item_search).setVisible(false);
            nav_Menu.findItem(R.id.navigation_item_settings).setVisible(false);
            nav_Menu.findItem(R.id.navigation_item_import).setVisible(false);
            nav_Menu.findItem(R.id.navigation_item_import_user).setVisible(false);
            nav_Menu.findItem(R.id.SerialDev).setVisible(false);
            nav_Menu.findItem(R.id.SerialDevTest).setVisible(false);
            nav_Menu.findItem(R.id.nav_basedate).setVisible(false);

        }

    }

  /*  @Override
    protected void onResume() {
        super.onResume();
        if(!mSharedPrefs.getBoolean("enableNFC", false)==true) {
            // Toast.makeText(getApplicationContext(), "NFC not enabled on Settings", Toast.LENGTH_LONG).show();
            return;
        } else {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if(nfcAdapter == null){
                Toast.makeText(this,
                        "NFC NOT supported on this devices!",
                        Toast.LENGTH_LONG).show();
                finish();
            }else if(!nfcAdapter.isEnabled()){
                Toast.makeText(this,
                        "NFC NOT Enabled!",
                        Toast.LENGTH_LONG).show();
                createNfcEnableDialog();
            }

            Intent intent = getIntent();
            String action = intent.getAction();

            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
                //Toast.makeText(this,"NFC is ON",Toast.LENGTH_SHORT).show();

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if(tag == null){
                    Toast.makeText(this,"tag == null",
                            Toast.LENGTH_SHORT).show();
                }else{
                    String tagInfo = "";
                    byte[] tagId = tag.getId();
                    for(int i=0; i<tagId.length; i++){
                        tagInfo += String.format("%02X", tagId[i] & 0xff) + " ";
                    }
                    tagInfo += "\n";
                    Toast.makeText(this,"Tag Serial No:"+tagInfo,Toast.LENGTH_SHORT).show();



                }



            }else{
                Toast.makeText(this,
                        "NFC IS" + action,
                        Toast.LENGTH_SHORT).show();
            }

        }

    }*/

    public void changePassword(){


        AlertDialog.Builder dialogFarmerSearch = new AlertDialog.Builder(this);
        LayoutInflater inflater1 = this.getLayoutInflater();
        final View dialogView = inflater1.inflate(R.layout.dialog_change_password, null);
        dialogFarmerSearch.setView(dialogView);
        dialogFarmerSearch.setCancelable(true);
        dialogFarmerSearch.setTitle("Change Password");

        edtOldPass=(EditText) dialogView.findViewById(R.id.edtOldPass);
        edtNewPass=(EditText) dialogView.findViewById(R.id.edtNewPass);
        edtConfirmPass=(EditText) dialogView.findViewById(R.id.edtConfirmPass);
        checkVisiblePass=(CheckBox) dialogView.findViewById(R.id.checkVisiblePass);
        checkVisiblePass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //do stuff
                if(checkVisiblePass.isChecked()){
                    edtOldPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    edtNewPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    edtConfirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{

                    edtOldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edtNewPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edtConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btnChangePass=(Button) dialogView.findViewById(R.id.btnChangePass);
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor d = dbhelper.getPassword(username);
                userpass = d.getString(0);
                SQLiteDatabase db = dbhelper.getWritableDatabase();
                //Toast.makeText(v.getContext(),"UserName: "+username+" Pass: "+userpass,Toast.LENGTH_SHORT).show();

                String opassword = edtOldPass.getText().toString();
                String password = edtNewPass.getText().toString();
                String cpassword = edtConfirmPass.getText().toString();
                if (opassword.length() <4) {
                    edtOldPass.setError("Invalid Password Length");
                    return;
                }
                if (password.length() <4) {
                    edtNewPass.setError("Invalid Password Length");
                    return;
                }
                if (cpassword.length() <4) {
                    edtConfirmPass.setError("Invalid Password Length");
                    return;
                }

                if(!opassword.equals(userpass))
                {
                    Toast.makeText(getApplicationContext(), "Invalid Old Password", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!cpassword.equals(password))
                {
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                    return;
                }

                ContentValues values = new ContentValues();
                values.put( Database.USERPWD, password);
                long rows = db.update(Database.OPERATORSMASTER_TABLE_NAME, values,
                        "ClerkName COLLATE NOCASE = ?", new String[] { username });

                db.close();
                if (rows > 0){
                    Toast.makeText(getApplicationContext(), "Updated Password Successfully!",Toast.LENGTH_LONG).show();
                    edtOldPass.setText("");
                    edtNewPass.setText("");
                    edtConfirmPass.setText("");
                    new LogOut().execute();

                }
                else{
                    Toast.makeText(getApplicationContext(), "Sorry! Could not update Password!",
                            Toast.LENGTH_LONG).show();}

            }
        });

        dialogFarmerSearch.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int whichButton) {




            }
        });


           /* dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    getdata();
                }
            });*/
        AlertDialog changepass = dialogFarmerSearch.create();
        changepass.show();
    }

    private void createNfcEnableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable NFC")
                .setMessage("NFC not Enabled")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNegativeButton("ENABLE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Goto NFC Settings.
                        if (Build.VERSION.SDK_INT >= 16) {
                            startActivity(new Intent(
                                    Settings.ACTION_NFC_SETTINGS));
                        } else {
                            startActivity(new Intent(
                                    Settings.ACTION_WIRELESS_SETTINGS));
                        }

                    }
                })
                .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        mEnableNfc = builder.create();
        mEnableNfc.show();
    }
    public void showFarmerById() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_view_farmer, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Farmer Details");


        et_farmerno = (TextView) dialogView.findViewById(R.id.et_farmerno);
        et_cardno = (TextView) dialogView.findViewById(R.id.et_cardno);
        et_idno = (TextView) dialogView.findViewById(R.id.et_Idno);
        et_mobileno = (TextView) dialogView.findViewById(R.id.et_mobileno);
        et_farmername = (TextView) dialogView.findViewById(R.id.et_FarmerName);
        et_fshed = (TextView) dialogView.findViewById(R.id.et_FShed);
        et_managedfarm = (TextView) dialogView.findViewById(R.id.et_FManaged);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.FARMERS_TABLE_NAME, null,
                " FNationalID = ?", new String[] { accountId }, null, null, null);
        if(account.getCount()==0){
            Context context = getApplicationContext();
            LayoutInflater inflater1 = getLayoutInflater();
            View customToastroot = inflater1.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Farmer Not Found!.");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            et_farmerno.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNO)));
            et_cardno.setText(account.getString(account
                    .getColumnIndex(Database.F_CARDNUMBER)));
            et_idno.setText(account.getString(account
                    .getColumnIndex(Database.F_NATIONALID)));
            et_mobileno.setText(account.getString(account
                    .getColumnIndex(Database.F_MOBILENUMBER)));
            et_farmername.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNAME)));
            et_fshed.setText(account.getString(account
                    .getColumnIndex(Database.F_SHED)));
            et_managedfarm.setText(account.getString(account
                    .getColumnIndex(Database.F_MANAGEDFARM)));
            String ShedCode=account.getString(account
                    .getColumnIndex(Database.F_SHED));
            Cursor shedname=db.rawQuery("select MccName from CollectionCenters where MccNo= '"+ShedCode+"' ", null);
            if(shedname!=null)
            {
                shedname.moveToFirst();
                et_fshed.setText(shedname.getString(shedname.getColumnIndex("MccName")));
            }

            if (account.getString(account.getColumnIndex(Database.F_MANAGEDFARM))=="1")
            {
                et_managedfarm.setText("yes");
            }else
            {
                et_managedfarm.setText("No");
            }

        }
        account.close();
        db.close();
        dbhelper.close();






        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });
       /* dialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {




            }
        });*/
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public void showFarmerByPhone() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_view_farmer, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Farmer Details");


        et_farmerno = (TextView) dialogView.findViewById(R.id.et_farmerno);
        et_cardno = (TextView) dialogView.findViewById(R.id.et_cardno);
        et_idno = (TextView) dialogView.findViewById(R.id.et_Idno);
        et_mobileno = (TextView) dialogView.findViewById(R.id.et_mobileno);
        et_farmername = (TextView) dialogView.findViewById(R.id.et_FarmerName);
        et_fshed = (TextView) dialogView.findViewById(R.id.et_FShed);
        et_managedfarm = (TextView) dialogView.findViewById(R.id.et_FManaged);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.FARMERS_TABLE_NAME, null,
                " FMobileNumber = ?", new String[] { accountId }, null, null, null);
        if(account.getCount()==0){
            Context context = getApplicationContext();
            LayoutInflater inflater1 = getLayoutInflater();
            View customToastroot = inflater1.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Farmer Not Found!.");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            et_farmerno.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNO)));
            et_cardno.setText(account.getString(account
                    .getColumnIndex(Database.F_CARDNUMBER)));
            et_idno.setText(account.getString(account
                    .getColumnIndex(Database.F_NATIONALID)));
            et_mobileno.setText(account.getString(account
                    .getColumnIndex(Database.F_MOBILENUMBER)));
            et_farmername.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNAME)));
            String ShedCode=account.getString(account
                    .getColumnIndex(Database.F_SHED));
            Cursor shedname=db.rawQuery("select MccName from CollectionCenters where MccNo= '"+ShedCode+"' ", null);
            if(shedname!=null)
            {
                shedname.moveToFirst();
                et_fshed.setText(shedname.getString(shedname.getColumnIndex("MccName")));
            }
            if (account.getString(account.getColumnIndex(Database.F_MANAGEDFARM))=="1")
            {
            et_managedfarm.setText("yes");
            }else
                {
                    et_managedfarm.setText("No");
                }





        }
        account.close();
        db.close();
        dbhelper.close();






        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });
        /*dialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {




            }
        });*/
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public void showFarmerByFarmerNo() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_view_farmer, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Farmer Details");


        et_farmerno = (TextView) dialogView.findViewById(R.id.et_farmerno);
        et_cardno = (TextView) dialogView.findViewById(R.id.et_cardno);
        et_idno = (TextView) dialogView.findViewById(R.id.et_Idno);
        et_mobileno = (TextView) dialogView.findViewById(R.id.et_mobileno);
        et_farmername = (TextView) dialogView.findViewById(R.id.et_FarmerName);
        et_fshed = (TextView) dialogView.findViewById(R.id.et_FShed);
        et_managedfarm = (TextView) dialogView.findViewById(R.id.et_FManaged);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.FARMERS_TABLE_NAME, null,
                " FFarmerNo = ?", new String[] { accountId }, null, null, null);
        if(account.getCount()==0){
            Context context = getApplicationContext();
            LayoutInflater inflater1 = getLayoutInflater();
            View customToastroot = inflater1.inflate(R.layout.red_toast, null);
            TextView text = (TextView) customToastroot.findViewById(R.id.toast);
            text.setText("Farmer Not Found!.");
            Toast customtoast = new Toast(context);
            customtoast.setView(customToastroot);
            customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            customtoast.setDuration(Toast.LENGTH_LONG);
            customtoast.show();
            return;
        }
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            et_farmerno.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNO)));
            et_cardno.setText(account.getString(account
                    .getColumnIndex(Database.F_CARDNUMBER)));
            et_idno.setText(account.getString(account
                    .getColumnIndex(Database.F_NATIONALID)));
            et_mobileno.setText(account.getString(account
                    .getColumnIndex(Database.F_MOBILENUMBER)));
            et_farmername.setText(account.getString(account
                    .getColumnIndex(Database.F_FARMERNAME)));
            String ShedCode=account.getString(account
                    .getColumnIndex(Database.F_SHED));
            Cursor shedname=db.rawQuery("select MccName from CollectionCenters where MccNo= '"+ShedCode+"' ", null);
            if(shedname!=null)
            {
                shedname.moveToFirst();
                et_fshed.setText(shedname.getString(shedname.getColumnIndex("MccName")));
            }
            if (account.getString(account.getColumnIndex(Database.F_MANAGEDFARM))=="1")
            {
                et_managedfarm.setText("yes");
            }else
            {
                et_managedfarm.setText("No");
            }





        }
        account.close();
        db.close();
        dbhelper.close();






        dialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();


            }
        });
        /*dialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {




            }
        });*/
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public void searchFarmer(){


        AlertDialog.Builder dialogFarmerSearch = new AlertDialog.Builder(this);
        LayoutInflater inflater1 = this.getLayoutInflater();
        final View dialogView = inflater1.inflate(R.layout.dialog_search_farmer, null);
        dialogFarmerSearch.setView(dialogView);
        dialogFarmerSearch.setCancelable(true);
        dialogFarmerSearch.setTitle("Search Farmer");

        checkID=(CheckBox) dialogView.findViewById(R.id.checkID);
        checkPhoneNo=(CheckBox) dialogView.findViewById(R.id.checkPhone);
        searchView= (SearchView) dialogView.findViewById(R.id.searchView);
        searchView.setQueryHint("Search By Farmer No...");
        checkID.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //do stuff
                if(checkID.isChecked()){
                    searchView.setQueryHint("Search By ID No...");
                    checkPhoneNo.setChecked(false);

                }else{
                    if(checkPhoneNo.isChecked()){
                        searchView.setQueryHint("Search By Phone No...");
                        checkID.setChecked(false);

                    }else{
                        searchView.setQueryHint("Search By Farmer No...");
                    }
                }

            }
        });
        checkPhoneNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //do stuff
                if(checkPhoneNo.isChecked()){
                    searchView.setQueryHint("Search By Phone No...");
                    checkID.setChecked(false);

                }else{
                    if(checkID.isChecked()){
                        searchView.setQueryHint("Search By ID No...");
                        checkPhoneNo.setChecked(false);

                    }else{

                        searchView.setQueryHint("Search By Farmer No...");
                    }
                }
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(checkID.isChecked()){
                    accountId = query;
                    showFarmerById();

                }else if(checkPhoneNo.isChecked()){
                    accountId = query;
                    showFarmerByPhone();

                }else{
                    accountId = query;
                    showFarmerByFarmerNo();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                return false;
            }
        });




        dialogFarmerSearch.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();




            }
        });


           /* dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                    getdata();
                }
            });*/
        AlertDialog search = dialogFarmerSearch.create();
        search.show();
    }
    public void disableBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
        }
    }

    private class LogOut extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute()
        {
            dialog = ProgressDialog.show( MainActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_out),
                    true);
        }

        @Override
        protected String doInBackground(Void... params)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove("pass");
            edit.commit();

            try {
                Thread.sleep(1000);
                resetConn.stop();
                resetCon2.stop();
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
            finish();
            /*Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addCategory(android.content.Intent.CATEGORY_HOME);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
        }
    }


}
