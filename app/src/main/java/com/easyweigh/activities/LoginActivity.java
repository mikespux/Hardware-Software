package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Michael on 9/24/2015.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputLayout usernameWrapper, passwordWrapper;
    private ProgressBar mProgress;
    private Button signInBtn;
    private Snackbar snackbar;
    DBHelper dbhelper;
    Spinner spinnerAgent;
    String agents,systembasedate;
    ArrayList<String> agentdata=new ArrayList<String>();
    ArrayAdapter<String> agentadapter;
    int count;
    int usercount;
    static SharedPreferences mSharedPrefs,prefs;
    private DevicePolicyManager devicePolicyManager=null;
    private ComponentName demoDeviceAdmin=null;
    private int ACTIVATION_REQUEST;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* if (M.getToken(this) != null) {
            Intent mIntent = new Intent(this, MainActivity.class);
            startActivity(mIntent);
            finish();
        }*/
        setContentView(R.layout.activity_login);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        demoDeviceAdmin = new ComponentName(this, DeviceAdmin.class);
        Log.e("DeviceAdminActive==", "" + demoDeviceAdmin);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);// adds new device administrator
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, demoDeviceAdmin);//ComponentName of the administrator component.
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"Disable app");//dditional explanation
        startActivityForResult(intent, ACTIVATION_REQUEST);
         /*
        if (!devicePolicyManager.isAdminActive(demoDeviceAdmin)) {

            // Triggers password change screen in Settings.
            Intent pass = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
            startActivity(pass);
        }*/

        initView();
        initToolbar();
        setupProgressBar();
        setupSnackBar();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

    }
    @Override
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        backButtonHandler();
        return;
    }
    public void backButtonHandler() {

        Intent intent = new Intent(android.content.Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(android.content.Intent.CATEGORY_HOME);
        finish();
        startActivity(intent);
    }
    public void setupSnackBar() {
        snackbar = Snackbar.make(findViewById(R.id.ParentLayoutLogin), getString(R.string.LoginError), Snackbar.LENGTH_LONG);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#FF5252"));
    }

    public void setupProgressBar() {
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF5252"),
                PorterDuff.Mode.SRC_IN);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        spinnerAgent = (Spinner) findViewById(R.id.spinnerAgent);
        List<String> mProduce = new ArrayList<String>();
        mProduce.add("Outgrowers");
        mProduce.add("Agents");
        agentadapter=new ArrayAdapter<String>(LoginActivity.this,R.layout.spinner_item,mProduce);
        agentadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerAgent.setAdapter(agentadapter);
        spinnerAgent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("FromProd", selectedText);
                edit.commit();

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
        signInBtn = (Button) findViewById(R.id.signInBtn);
        signInBtn.setVisibility(View.VISIBLE);
        TextView registerBtn = (TextView) findViewById(R.id.register);
        usernameWrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
        findViewById(R.id.forgotBtn).setOnClickListener(this);
        signInBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        dbhelper = new DBHelper(LoginActivity.this);
       // Agent();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor accounts = db.query(true, Database.FARMERS_TABLE_NAME, null, null, null, null, null, null, null, null);
        count=accounts.getCount();

        Cursor users = db.query(true, Database.OPERATORSMASTER_TABLE_NAME, null, null, null, null, null, null, null, null);
        usercount=users.getCount();
        if(usercount==0){
            String DefaultUsers = "INSERT INTO " + Database.OPERATORSMASTER_TABLE_NAME + " ("
                    + Database.USERIDENTIFIER + ", "
                    + Database.CLERKNAME + ", "
                    + Database.USERPWD+ ", "
                    + Database.ACCESSLEVEL + ") Values ('OCTAGON', 'ODS', '1234', '1')";

            db.execSQL(DefaultUsers);

        }
       // Toast.makeText(LoginActivity.this, String.valueOf(count), Toast.LENGTH_LONG).show();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(LoginActivity.this);

        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("count", String.valueOf(count));
        edit.commit();


    }
    private void Agent() {
        agentdata.clear();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select agtID,agtName from agent", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    agents=c.getString(c.getColumnIndex("agtName"));
                    agentdata.add(agents);

                }while(c.moveToNext());
            }
        }


        agentadapter=new ArrayAdapter<String>(LoginActivity.this,R.layout.spinner_item,agentdata);
        agentadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerAgent.setAdapter(agentadapter);
        spinnerAgent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*String zoneName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select fzCode from zones where fzName= '" + zoneName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    zoneid = c.getString(c.getColumnIndex("fzCode"));

                }
                c.close();
                db.close();
                dbhelper.close();*/
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

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.signInBtn) {
            String userName = usernameWrapper.getEditText().getText().toString().trim();
            String userPassword = passwordWrapper.getEditText().getText().toString().trim();
            if (userName.length() <=0 ) {
                usernameWrapper.setError("Enter a valid Username");
            } else if (userPassword.length() < 3) {
                passwordWrapper.setError("Invalid Password");
            } else {
                usernameWrapper.setErrorEnabled(false);
                passwordWrapper.setErrorEnabled(false);





                if(dbhelper.UserLogin(userName,userPassword))
                {
                    // save user data
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(LoginActivity.this);

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("user", userName);
                    edit.commit();

                    edit.putString("pass", userPassword);
                    edit.commit();
                    mProgress.setVisibility(View.VISIBLE);
                    signInBtn.setVisibility(View.GONE);

                    Cursor d = dbhelper.getAccessLevel(userName);
                    String full_name = d.getString(1);
                    Context context=getApplicationContext();
                    LayoutInflater inflater=getLayoutInflater();
                    View customToastroot =inflater.inflate(R.layout.blue_toast, null);
                    TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                    text.setText("Successfully Logged In " + full_name);
                    Toast customtoast=new Toast(context);
                    customtoast.setView(customToastroot);
                    customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                    customtoast.setDuration(Toast.LENGTH_LONG);
                    customtoast.show();
                   // Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                    //systembasedate = prefs.getString("basedate","");
                   // if (systembasedate.toString().equals("") && mSharedPrefs.getString("scaleVersion", "").toString().equals("") && count==0) {
                     if(usercount<=1) {
                        finish();
                        Intent login = new Intent(getApplicationContext(), SplashActivity.class);
                        startActivity(login);

                      return;
                    }

                    finish();
                    Intent login = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(login);
                } else {
                    Toast.makeText(LoginActivity.this,"Invalid Username/Password", Toast.LENGTH_LONG).show();
                    snackbar.show();
                }
                dbhelper.close();


            }
        } else if (view.getId() == R.id.register) {


        }else if (view.getId() == R.id.forgotBtn) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.insertEmail));
            final EditText emailInput = new EditText(this);
            alert.setView(emailInput);
            alert.setPositiveButton(getString(R.string.rest), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String email = emailInput.getText().toString().trim();


                }
            });
            alert.setNegativeButton(getString(R.string.cancel), null);
            alert.show();
        }
    }

    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        String uname = prefs.getString("user", "");
        String pass = prefs.getString("pass", "");

        String userName = uname;
        String userPassword = pass;
        usernameWrapper.getEditText().setText(uname);

        try{
            if(userName.length() > 0 && userPassword.length() >0)
            {
                DBHelper dbUser = new DBHelper(LoginActivity.this);


                if(dbUser.UserLogin(userName, userPassword))
                {



                    finish();
                    Intent login =new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(login);
                }else{

                }
                dbUser.close();
            }

        }catch(Exception e)
        {
            Toast.makeText(LoginActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}
