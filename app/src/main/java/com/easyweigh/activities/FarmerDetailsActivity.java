package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;
import com.easyweigh.data.Database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Michael on 30/06/2016.
 */
public class FarmerDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btn_svFarmer;
    ListView listFarmers;
    EditText et_farmerno, et_cardno, et_idno,et_mobileno,et_farmername;
    String s_farmerno, s_cardno, s_idno,s_mobileno,s_farmername, s_managedfarm,s_producetokg;
    Spinner   sp_fshed, sp_managedfarm;
    String accountId;
    TextView textAccountId;
    Boolean success = true;

    String sheds,shedid,manageid;
    ArrayList<String> sheddata=new ArrayList<String>();
    ArrayAdapter<String> shedadapter;
    SearchView searchView;
    public SimpleCursorAdapter ca;
    Intent mIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listfarmers);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_farmer);

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
        listFarmers = (ListView) this.findViewById(R.id.lvUsers);
        listFarmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                showUpdateUserDialog();
                ShedList();
            }
        });

        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setQueryHint("Search Farmer No ...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ca.getFilter().filter(query.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String farmerNo = constraint.toString();
                        return dbhelper.SearchSpecific(farmerNo);

                    }
                });
                // Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText.toString());
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String farmerNo = constraint.toString();
                        return dbhelper.SearchFarmer(farmerNo);

                    }
                });
                //Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });
       searchView.requestFocus();



    }
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_farmer, menu);

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

                showAddFarmerDialog();
                ShedList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
    private void showSoftInputUnchecked() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            Method showSoftInputUnchecked = null;
            try {
                showSoftInputUnchecked = imm.getClass()
                        .getMethod("showSoftInputUnchecked", int.class, ResultReceiver.class);
            } catch (NoSuchMethodException e) {
                // Log something
            }

            if (showSoftInputUnchecked != null) {
                try {
                    showSoftInputUnchecked.invoke(imm, 0, null);
                } catch (IllegalAccessException e) {
                    // Log something
                } catch (InvocationTargetException e) {
                    // Log something
                }
            }
        }
    }
    private void ShedList() {
        sheddata.clear();

        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select MccNo,MccName from CollectionCenters ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    sheds=c.getString(c.getColumnIndex("MccName"));
                    sheddata.add(sheds);

                }while(c.moveToNext());
            }
        }
        c.close();
        db.close();
        dbhelper.close();

        shedadapter=new ArrayAdapter<String>(FarmerDetailsActivity.this,R.layout.spinner_item,sheddata);
        shedadapter.setDropDownViewResource(R.layout.spinner_item);
        sp_fshed.setAdapter(shedadapter);
        sp_fshed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String shedName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select MccNo from CollectionCenters where MccName= '" + shedName + "'", null);
                if (c != null) {
                    c.moveToFirst();
                    shedid = c.getString(c.getColumnIndex("MccNo"));


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
    public void showAddFarmerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_farmer, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Farmers");
        et_farmerno = (EditText) dialogView.findViewById(R.id.et_farmerno);
        et_cardno = (EditText) dialogView.findViewById(R.id.et_cardno);
        et_idno = (EditText) dialogView.findViewById(R.id.et_Idno);
        et_mobileno = (EditText) dialogView.findViewById(R.id.et_mobileno);
        et_farmername = (EditText) dialogView.findViewById(R.id.et_FarmerName);
        sp_fshed = (Spinner) dialogView.findViewById(R.id.sp_shed);
        sp_managedfarm = (Spinner) dialogView.findViewById(R.id.sp_managedfarms);
        



        btn_svFarmer = (Button) dialogView.findViewById(R.id.btn_svFarmer);
        btn_svFarmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                try {
                    s_farmerno = et_farmerno.getText().toString();
                    s_cardno = et_cardno.getText().toString();
                    s_idno = et_idno.getText().toString();
                    s_mobileno = et_mobileno.getText().toString();
                    s_farmername = et_farmername.getText().toString();
                   // s_fzone = sp_fzone.getSelectedItem().toString();
                    //s_fshed = sp_fshed.getSelectedItem().toString();
                    s_managedfarm = sp_managedfarm.getSelectedItem().toString();

                    s_producetokg="0";
                    if (s_managedfarm.equals("Yes")){

                        manageid="1";
                    }else {
                        manageid="0";
                    }

                    if (s_farmerno.equals("") || s_cardno.equals("")||s_idno.equals("") || s_mobileno.equals("")|| s_farmername.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(sp_fshed.getSelectedItem().equals("SELECT"))
                    {
                        Toast.makeText(getApplicationContext(), "Please Select Shed", Toast.LENGTH_LONG).show();
                        return;
                    }

                Cursor checkFarmer =dbhelper.CheckFarmer(s_farmerno);
                //Check for duplicate farmer
                if (checkFarmer.getCount() > 0) {
                    Toast.makeText(getApplicationContext(), "Farmer already exists",Toast.LENGTH_SHORT).show();
                    return;
                }
                    dbhelper.AddFarmers(s_farmerno, s_cardno, s_idno,s_mobileno,s_farmername, shedid, manageid,s_producetokg);
                    if (success) {


                        Toast.makeText(FarmerDetailsActivity.this, "Farmer Saved successfully!!", Toast.LENGTH_LONG).show();

                        et_farmerno.setText("");
                        et_cardno.setText("");
                        et_idno.setText("");
                        et_mobileno.setText("");
                        et_farmername.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(FarmerDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_farmer, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Farmers");
        accountId = textAccountId.getText().toString();

        et_farmerno = (EditText) dialogView.findViewById(R.id.et_farmerno);
        et_cardno = (EditText) dialogView.findViewById(R.id.et_cardno);
        et_idno = (EditText) dialogView.findViewById(R.id.et_Idno);
        et_mobileno = (EditText) dialogView.findViewById(R.id.et_mobileno);
        et_farmername = (EditText) dialogView.findViewById(R.id.et_FarmerName);
        sp_fshed = (Spinner) dialogView.findViewById(R.id.sp_shed);
        sp_managedfarm = (Spinner) dialogView.findViewById(R.id.sp_managedfarms);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.FARMERS_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
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





        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svFarmer = (Button) dialogView.findViewById(R.id.btn_svFarmer);
        btn_svFarmer.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteFarmer();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateFarmer();
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
    public void updateFarmer() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.F_FARMERNO, et_farmerno.getText().toString());
            values.put( Database.F_CARDNUMBER, et_cardno.getText().toString());
            values.put( Database.F_NATIONALID, et_idno.getText().toString());
            values.put( Database.F_MOBILENUMBER, et_mobileno.getText().toString());
            values.put( Database.F_FARMERNAME, et_farmername.getText().toString());
            values.put( Database.F_SHED, shedid);
            s_managedfarm = sp_managedfarm.getSelectedItem().toString();
            if (s_managedfarm.equals("Yes")){

                manageid="1";
            }else {
                manageid="2";
            }
           values.put( Database.F_MANAGEDFARM, manageid);



            long rows = db.update(Database.FARMERS_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated Farmer Successfully!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Farmer!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteFarmer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Farmer?")
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
            int rows = db.delete(Database.FARMERS_TABLE_NAME, "_id=?", new String[]{ accountId});
            dbhelper.close();
            if ( rows == 1) {
                Toast.makeText(this, "Farmer Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            }
            else
                Toast.makeText(this, "Could not delete Farmer!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata(){

        try {

            SQLiteDatabase db= dbhelper.getReadableDatabase();
           Cursor accounts = db.query(true, Database.FARMERS_TABLE_NAME, null, null, null, null, null, null, null, null);
/*if(accounts.getCount()==0)
    Toast.makeText(this, "no records", Toast.LENGTH_LONG).show();*/
            String from [] = {  Database.ROW_ID,Database.F_FARMERNO , Database.F_FARMERNAME, Database.F_SHED, Database.F_MOBILENUMBER};
            int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_name,R.id.tv_shed,R.id.tv_phone};


            ca  = new SimpleCursorAdapter(this,R.layout.user_list, accounts,from,to);

            ListView listfarmers= (ListView) this.findViewById( R.id.lvUsers);
            listfarmers.setAdapter(ca);
            listfarmers.setTextFilterEnabled(true);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();
       // mIntent = new Intent(FarmerDetailsActivity.this,MainActivity.class);
        //startActivity(mIntent);
        return;
    }


}
