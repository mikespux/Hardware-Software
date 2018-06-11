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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;

/**
 * Created by Michael on 30/06/2016.
 */
public class ShedDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddShed,btn_svShed;
    ListView listSheds;
    EditText mc_cno,mc_cname;
    Spinner mc_czone,mc_cRoute;
    String s_mcno,s_mcname;
    String accountId;
    TextView textAccountId,tv_zone,tv_route;
    Boolean success = true;

    String zones,routes,shedcode;
    String routeid=null;
    String zoneid=null;
    ArrayList<String> zonedata=new ArrayList<String>();
    ArrayList<String> iddata=new ArrayList<String>();
    ArrayList<String> routedata=new ArrayList<String>();
    ArrayAdapter<String> zoneadapter,routeadapter;
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
        getSupportActionBar().setTitle(R.string.title_shed);

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
        btAddShed=(Button)findViewById(R.id.btAddUser);
        btAddShed.setVisibility(View.GONE);
        btAddShed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
                ZoneList();
                RouteList();

            }
        });
        listSheds = (ListView) this.findViewById(R.id.lvUsers);
        listSheds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
                //showUpdateUserDialog();
                //ZoneList();
                //RouteList();
            }
        });


    }

    private void ZoneList() {
        zonedata.clear();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select fzCode,fzName from zones ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    zones=c.getString(c.getColumnIndex("fzName"));
                    zonedata.add(zones);

                }while(c.moveToNext());
            }
        }


        zoneadapter=new ArrayAdapter<String>(ShedDetailsActivity.this,R.layout.spinner_item,zonedata);
        zoneadapter.setDropDownViewResource(R.layout.spinner_item);
        mc_czone.setAdapter(zoneadapter);
        mc_czone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String zoneName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select fzCode from zones where fzName= '" + zoneName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    zoneid = c.getString(c.getColumnIndex("fzCode"));

                }
                c.close();
                db.close();
                dbhelper.close();
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
    private void RouteList() {
        routedata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select McRCode,McRName from routes ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    routes=c.getString(c.getColumnIndex("McRName"));
                    routedata.add(routes);

                }while(c.moveToNext());
            }
        }


        routeadapter=new ArrayAdapter<String>(ShedDetailsActivity.this,R.layout.spinner_item,routedata);
        routeadapter.setDropDownViewResource(R.layout.spinner_item);
        mc_cRoute.setAdapter(routeadapter);
        mc_cRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String routeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select McRCode from routes where McRName= '" + routeName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    routeid = c.getString(c.getColumnIndex("McRCode"));


                }
                c.close();
                db.close();
                dbhelper.close();
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


    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_shed, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Sheds");
        mc_cno = (EditText) dialogView.findViewById(R.id.mc_cno);
        mc_cname = (EditText) dialogView.findViewById(R.id.mc_cname);
        mc_czone = (Spinner) dialogView.findViewById(R.id.mc_czone);
        mc_cRoute = (Spinner) dialogView.findViewById(R.id.mc_croute);



        btn_svShed = (Button) dialogView.findViewById(R.id.btn_svShed);
        btn_svShed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_mcno = mc_cno.getText().toString();
                    s_mcname = mc_cname.getText().toString();


                    if (s_mcno.equals("") || s_mcname.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(mc_czone.getSelectedItem().equals("SELECT"))
                    {
                        Toast.makeText(getApplicationContext(), "Please Select Zone", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(mc_cRoute.getSelectedItem().equals("SELECT"))
                    {
                        Toast.makeText(getApplicationContext(), "Please Select Route", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkShed =dbhelper.CheckShed(s_mcno,routeid);
                    //Check for duplicate shed
                    if (checkShed.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Shed already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddSheds(s_mcno, s_mcname,routeid,zoneid);
                    if (success) {


                        Toast.makeText(ShedDetailsActivity.this, "Shed Saved successfully!!", Toast.LENGTH_LONG).show();

                        mc_cno.setText("");
                        mc_cname.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(ShedDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_shed, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Sheds");
        accountId = textAccountId.getText().toString();
        tv_zone = (TextView) dialogView.findViewById(R.id.zoneid);
        tv_route = (TextView) dialogView.findViewById(R.id.routeid);

        mc_czone = (Spinner) dialogView.findViewById(R.id.mc_czone);
        mc_cRoute = (Spinner) dialogView.findViewById(R.id.mc_croute);


        mc_cno = (EditText) dialogView.findViewById(R.id.mc_cno);
        mc_cname = (EditText) dialogView.findViewById(R.id.mc_cname);
        mc_cno.setEnabled(false);

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.COLLECTIONCENTERS_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view

            mc_cno.setText(account.getString(account
                    .getColumnIndex(Database.MC_CNO)));
            mc_cname.setText(account.getString(account
                    .getColumnIndex(Database.MC_CNAME)));


            String zoneid =account.getString(account.getColumnIndex(Database.MC_CZONE));
            String routeid =account.getString(account.getColumnIndex(Database.MC_CROUTE));
            if(zoneid.equals("")){
                tv_zone.setTextColor(Color.parseColor("#E91E63"));
                tv_zone.setText("Current Zone :- ");
            }else {
                Cursor c = db.rawQuery("select fzName from zones where fzCode= '" + zoneid + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    tv_zone.setTextColor(Color.parseColor("#E91E63"));
                    tv_zone.setText("Current Zone :- " + c.getString(c.getColumnIndex("fzName")));

                }
                c.close();
            }
            if(routeid==null){
                tv_route.setTextColor(Color.parseColor("#E91E63"));
                tv_route.setText("Current Route :- ");
            }else {
                Cursor c1=db.rawQuery("select McRName from routes where McRCode= '"+routeid+"' ", null);


                if (c1 != null) {
                    c1.moveToFirst();
                    tv_route.setTextColor(Color.parseColor("#E91E63"));
                    tv_route.setText("Current Route :- " + c1.getString(c1.getColumnIndex("McRName")));

                }

                c1.close();
            }


        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svShed = (Button) dialogView.findViewById(R.id.btn_svShed);
        btn_svShed.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteShed();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateShed();
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
    public void updateShed() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.MC_CNO, mc_cno.getText().toString());
            values.put( Database.MC_CNAME, mc_cname.getText().toString());
            values.put( Database.MC_CZONE, zoneid);
            values.put(Database.MC_CROUTE, routeid);



            long rows = db.update(Database.COLLECTIONCENTERS_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated Shed Successfully!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Shed!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteShed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Shed?")
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
            Cursor c=db.rawQuery("select MccNo from CollectionCenters where _id= '"+accountId+"' ", null);
            if(c!=null)
            {
                c.moveToFirst();
                shedcode= c.getString(c.getColumnIndex("MccNo"));
            }

            c.close();
            Cursor c1=db.rawQuery("select * from farmers where FShed= '" + shedcode + "' ", null);
            if(c1.getCount() > 0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete shed! ,Because its related in farmers");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
               // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                c1.close();
            }
            else {
                int rows = db.delete(Database.COLLECTIONCENTERS_TABLE_NAME, "_id=?", new String[]{accountId});
                dbhelper.close();
                if (rows == 1) {
                    Toast.makeText(this, "Shed Deleted Successfully!", Toast.LENGTH_LONG).show();

                    //this.finish();
                    getdata();
                } else{
                    Toast.makeText(this, "Could not delete Shed!", Toast.LENGTH_LONG).show();}
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
            final Cursor accounts = db.query( true, Database.COLLECTIONCENTERS_TABLE_NAME,null,Database.ROW_ID + ">'" + ROWID + "'",null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.MC_CNO , Database.MC_CNAME};
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
