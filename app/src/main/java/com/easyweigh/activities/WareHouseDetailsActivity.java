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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
public class WareHouseDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddAgt,btn_svWH;
    ListView listWarehouses;
    EditText whID,whName;
    String s_whID,s_whName;
    String accountId,agentcode;
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
        getSupportActionBar().setTitle("Warehouses");

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
        btAddAgt=(Button)findViewById(R.id.btAddUser);
        btAddAgt.setVisibility(View.GONE);
        btAddAgt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
        listWarehouses = (ListView) this.findViewById(R.id.lvUsers);
        listWarehouses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
                //showUpdateUserDialog();
            }
        });


    }
    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_warehouse, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Warehouses");
        whID = (EditText) dialogView.findViewById(R.id.whID);
        whName = (EditText) dialogView.findViewById(R.id.whName);



        btn_svWH = (Button) dialogView.findViewById(R.id.btn_svWH);
        btn_svWH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_whID = whID.getText().toString();
                    s_whName = whName.getText().toString();


                    if (s_whID.equals("") || s_whName.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkWarehouse =dbhelper.CheckWarehouse(s_whID);
                    //Check for duplicate id number
                    if (checkWarehouse.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Warehouse already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddWarehouse(s_whID, s_whName);
                    if (success) {


                        Toast.makeText(WareHouseDetailsActivity.this, "Warehouse Saved successfully!!", Toast.LENGTH_LONG).show();

                        whID.setText("");
                        whName.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(WareHouseDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_warehouse, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Warehouses");
        accountId = textAccountId.getText().toString();

        whID = (EditText) dialogView.findViewById(R.id.whID);
        whID.setEnabled(false);
        whName = (EditText) dialogView.findViewById(R.id.whName);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.WAREHOUSE_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            whID.setText(account.getString(account
                    .getColumnIndex(Database.WH_ID)));
            whName.setText(account.getString(account
                    .getColumnIndex(Database.WH_NAME)));




        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svWH = (Button) dialogView.findViewById(R.id.btn_svWH);
        btn_svWH.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteWarehouse();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateWarehouse();
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
    public void updateWarehouse() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.WH_ID, whID.getText().toString());
            values.put( Database.WH_NAME, whName.getText().toString());



            long rows = db.update(Database.WAREHOUSE_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated Warehouse Successfully!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Warehouse!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteWarehouse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Warehouse?")
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

            int rows = db.delete(Database.WAREHOUSE_TABLE_NAME, "_id=?", new String[]{ accountId});
            dbhelper.close();
            if ( rows == 1) {
                Toast.makeText(this, "Warehouse Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            }
            else{
                Toast.makeText(this, "Could not delete agent!", Toast.LENGTH_LONG).show();}
            //}

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata(){

        try {
            int ROWID=0;
            SQLiteDatabase db= dbhelper.getReadableDatabase();
            Cursor accounts = db.query( true, Database.WAREHOUSE_TABLE_NAME,null,Database.ROW_ID + ">'" + ROWID + "'",null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.WH_ID , Database.WH_NAME};
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
