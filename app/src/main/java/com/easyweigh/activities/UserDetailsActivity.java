package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;

/**
 * Created by Michael on 30/06/2016.
 */
public class UserDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    Button btAddUser;
    EditText etFullName,etNewUserId,etPassword;
    Spinner spUserLevel;
    Button btn_svUser;
    DBHelper dbhelper;
    Boolean success = true;
    String s_etFullName,s_etNewUserId,s_etPassword,s_spUserLevel;
    ListView listUsers;
    String accountId;
    TextView textAccountId;
    String user_level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_users);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
 public void initializer(){

     dbhelper = new DBHelper(getApplicationContext());

     btAddUser=(Button)findViewById(R.id.btAddUser);
     btAddUser.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             showAddUserDialog();
         }
     });
     btAddUser.setVisibility(View.GONE);
     listUsers = (ListView) this.findViewById(R.id.lvUsers);
     listUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
             textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
             Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
             //showUpdateUserDialog();
         }
     });


  }
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {

            case R.id.action_add:

                showAddUserDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Users");
        etFullName = (EditText) dialogView.findViewById(R.id.etFullName);
        etNewUserId = (EditText) dialogView.findViewById(R.id.etNewUserId);
        etPassword = (EditText) dialogView.findViewById(R.id.etPassword);
        spUserLevel = (Spinner) dialogView.findViewById(R.id.spUserLevel);




        btn_svUser = (Button) dialogView.findViewById(R.id.btn_svUser);
        btn_svUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_etFullName = etFullName.getText().toString();
                    s_etNewUserId = etNewUserId.getText().toString();
                    s_etPassword = etPassword.getText().toString();
                    s_spUserLevel = spUserLevel.getSelectedItem().toString();

                    if (s_spUserLevel.equals("Manager")){

                        user_level="1";
                    }else {
                        user_level="2";
                    }

                    if (s_etFullName.equals("") || s_etNewUserId.equals("") || s_etPassword.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkusername =dbhelper.fetchUsername(s_etNewUserId);
                    //Check for duplicate id number
                    if (checkusername.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Username already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, user_level);
                    if (success) {


                        Toast.makeText(UserDetailsActivity.this, "User Saved successfully!!", Toast.LENGTH_LONG).show();

                        etFullName.setText("");
                        etNewUserId.setText("");
                        etPassword.setText("");
                        spUserLevel.setTag(-1);
                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(UserDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });


        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                getdata();

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                getdata();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showUpdateUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Users");
        accountId = textAccountId.getText().toString();

        etFullName = (EditText) dialogView.findViewById(R.id.etFullName);
        etNewUserId = (EditText) dialogView.findViewById(R.id.etNewUserId);
        etPassword = (EditText) dialogView.findViewById(R.id.etPassword);
        spUserLevel = (Spinner) dialogView.findViewById(R.id.spUserLevel);

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.OPERATORSMASTER_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            etFullName.setText(account.getString(account
                    .getColumnIndex(Database.USERIDENTIFIER)));
            etNewUserId.setText(account.getString(account
                    .getColumnIndex(Database.CLERKNAME)));
            etPassword.setText(account.getString(account
                    .getColumnIndex(Database.USERPWD)));



        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svUser = (Button) dialogView.findViewById(R.id.btn_svUser);
        btn_svUser.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteUser();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateUsers();
                getdata();



            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata(){

        try {

            SQLiteDatabase db= dbhelper.getReadableDatabase();
            Cursor accounts = db.query( true, Database.OPERATORSMASTER_TABLE_NAME,null,null,null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.CLERKNAME , Database.USERIDENTIFIER};
            int to [] = { R.id.txtAccountId,R.id.txtUserName,R.id.txtUserType};

            @SuppressWarnings("deprecation")
            SimpleCursorAdapter ca  = new SimpleCursorAdapter(this,R.layout.userlist, accounts,from,to);

            ListView listusers = (ListView) this.findViewById( R.id.lvUsers);
            listusers.setAdapter(ca);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void updateUsers() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.USERIDENTIFIER, etFullName.getText().toString());
            values.put( Database.CLERKNAME, etNewUserId.getText().toString());
            values.put( Database.USERPWD,etPassword.getText().toString());
            s_spUserLevel=spUserLevel.getSelectedItem().toString();
            if (s_spUserLevel.equals("Manager")){

                user_level="1";
            }else {
                user_level="2";
            }

            values.put( Database.ACCESSLEVEL,user_level);


            long rows = db.update(Database.OPERATORSMASTER_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated User Successfully!",
                        Toast.LENGTH_LONG).show();
                            }
            else{
                Toast.makeText(this, "Sorry! Could not update User!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this user?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCurrentAccount();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }



    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.OPERATORSMASTER_TABLE_NAME, "_id=?", new String[]{ accountId});
            dbhelper.close();
            if ( rows == 1) {
                Toast.makeText(this, "User Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            }
            else
                Toast.makeText(this, "Could not delete user!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


}
