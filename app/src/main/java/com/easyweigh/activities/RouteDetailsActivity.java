package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
public class RouteDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddRoute,btn_svRoute;
    ListView listRoutes;
    EditText mc_rcode,mc_rname;
    String s_mcrcode,s_mcrname;
    String accountId,routecode;
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
        getSupportActionBar().setTitle(R.string.title_route);

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
        btAddRoute=(Button)findViewById(R.id.btAddUser);
        btAddRoute.setVisibility(View.GONE);
        btAddRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
        listRoutes = (ListView) this.findViewById(R.id.lvUsers);
        listRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_route, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Routes");
        mc_rcode = (EditText) dialogView.findViewById(R.id.mc_rcode);
        mc_rname = (EditText) dialogView.findViewById(R.id.mc_rname);



        btn_svRoute = (Button) dialogView.findViewById(R.id.btn_svRoute);
        btn_svRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_mcrcode = mc_rcode.getText().toString();
                    s_mcrname = mc_rname.getText().toString();


                    if (s_mcrcode.equals("") || s_mcrname.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkRoute =dbhelper.CheckRoute(s_mcrcode);
                    //Check for duplicate shed
                    if (checkRoute.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Route already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddRoutes(s_mcrcode, s_mcrname);
                    if (success) {


                        Toast.makeText(RouteDetailsActivity.this, "Route Saved successfully!!", Toast.LENGTH_LONG).show();

                        mc_rcode.setText("");
                        mc_rname.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(RouteDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_route, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Routes");
        accountId = textAccountId.getText().toString();

        mc_rcode = (EditText) dialogView.findViewById(R.id.mc_rcode);
        mc_rcode.setEnabled(false);
        mc_rname = (EditText) dialogView.findViewById(R.id.mc_rname);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.ROUTES_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            mc_rcode.setText(account.getString(account
                    .getColumnIndex(Database.MC_RCODE)));
            mc_rname.setText(account.getString(account
                    .getColumnIndex(Database.MC_RNAME)));




        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svRoute = (Button) dialogView.findViewById(R.id.btn_svRoute);
        btn_svRoute.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteRoute();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateRoute();
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
    public void updateRoute() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.MC_RCODE, mc_rcode.getText().toString());
            values.put( Database.MC_RNAME, mc_rname.getText().toString());



            long rows = db.update(Database.ROUTES_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated Route Successfully!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Route!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteRoute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this route?")
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
            Cursor c=db.rawQuery("select McRCode from routes where _id= '"+accountId+"' ", null);
            if(c!=null)
            {
                c.moveToFirst();
                 routecode= c.getString(c.getColumnIndex("McRCode"));

            }
            c.close();
            Cursor c1=db.rawQuery("select * from CollectionCenters where MccRoute= '"+routecode+"' ", null);
            if(c1.getCount() > 0){
                c1.moveToFirst();
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete route! ,Because its related in sheds");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
               // Toast.makeText(this,"Could not delete route! ,Because its related in sheds", Toast.LENGTH_LONG).show();
                c1.close();
            }else
            {
            int rows = db.delete(Database.ROUTES_TABLE_NAME, "_id=?", new String[]{ accountId});
            dbhelper.close();
            if ( rows == 1) {
                Toast.makeText(this, "Route Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            }
            else{
                Toast.makeText(this, "Could not delete route!", Toast.LENGTH_LONG).show();}
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
            Cursor accounts = db.query( true, Database.ROUTES_TABLE_NAME,null,Database.ROW_ID + ">'" + ROWID + "'",null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.MC_RCODE , Database.MC_RNAME};
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
