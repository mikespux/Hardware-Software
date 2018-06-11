package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;

/**
 * Created by Michael on 30/06/2016.
 */
public class ZoneDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddZone,btn_svZone;
    ListView listZones;
    EditText fzcode,fzname;
    String s_fzcode,s_fzname;
    String accountId,zonecode;
    TextView textAccountId;
    Boolean success = true;
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
        getSupportActionBar().setTitle(R.string.title_zone);

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
        btAddZone=(Button)findViewById(R.id.btAddUser);
        btAddZone.setVisibility(View.GONE);
        btAddZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
        listZones = (ListView) this.findViewById(R.id.lvUsers);
        listZones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
               // showUpdateUserDialog();
            }
        });


    }
    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_zone, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Zones");
        fzcode = (EditText) dialogView.findViewById(R.id.fz_code);
        fzname = (EditText) dialogView.findViewById(R.id.fz_name);



        btn_svZone = (Button) dialogView.findViewById(R.id.btn_svZone);
        btn_svZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_fzcode = fzcode.getText().toString();
                    s_fzname = fzname.getText().toString();


                    if (s_fzcode.equals("") || s_fzname.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkZone =dbhelper.CheckZone(s_fzcode);
                    //Check for duplicate id number
                    if (checkZone.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Zone already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddZones(s_fzcode, s_fzname);
                    if (success) {


                        Toast.makeText(ZoneDetailsActivity.this, "Zone Saved successfully!!", Toast.LENGTH_LONG).show();

                        fzcode.setText("");
                        fzname.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(ZoneDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_zone, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Zones");
        accountId = textAccountId.getText().toString();

        fzcode = (EditText) dialogView.findViewById(R.id.fz_code);
        fzcode.setEnabled(false);
        fzname = (EditText) dialogView.findViewById(R.id.fz_name);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.ZONES_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            fzcode.setText(account.getString(account
                    .getColumnIndex(Database.FZ_CODE)));
            fzname.setText(account.getString(account
                    .getColumnIndex(Database.FZ_NAME)));




        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svZone = (Button) dialogView.findViewById(R.id.btn_svZone);
        btn_svZone.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteZone();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateZone();
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
    public void updateZone() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.FZ_CODE, fzcode.getText().toString());
            values.put( Database.FZ_NAME, fzname.getText().toString());



            long rows = db.update(Database.ZONES_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated Zone Successfully!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Zone!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteZone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this zone?")
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
            Cursor c=db.rawQuery("select fzCode from zones where _id= '"+accountId+"' ", null);
            if(c!=null)
            {
                c.moveToFirst();
                zonecode= c.getString(c.getColumnIndex("fzCode"));
            }
            c.close();

            Cursor c1=db.rawQuery("select * from CollectionCenters where MccZone= '" + zonecode + "' ", null);
            if(c1.getCount() > 0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete zone! ,Because its related in sheds");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(this, "Could not delete zone! ,Because its related in sheds", Toast.LENGTH_LONG).show();
                c1.close();
            }
            else{

            int rows = db.delete(Database.ZONES_TABLE_NAME, "_id=?", new String[]{ accountId});
            dbhelper.close();
            if ( rows == 1) {
                Toast.makeText(this, "Zone Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            }
            else{
                Toast.makeText(this, "Could not delete zone!", Toast.LENGTH_LONG).show();}
            }

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata(){

        try {
            int ROWID=0;
            SQLiteDatabase db= dbhelper.getReadableDatabase();
            Cursor accounts = db.query( true, Database.ZONES_TABLE_NAME,null,Database.ROW_ID + ">'" + ROWID + "'",null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.FZ_CODE , Database.FZ_NAME};
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




}
