package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
public class GradeDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddGrades,btn_svGrade;
    ListView ListGrades;
    EditText gr_code,gr_name,gr_price;
    String s_gr_code,s_gr_name;
    String accountId,Gradecode;
    TextView textAccountId,txtProduce;
    Boolean success = true;
    Spinner spinnerProduce;
    String produce,produceid;
    ArrayList<String> producedata=new ArrayList<String>();
    ArrayAdapter<String> produceadapter;
    LinearLayout ltprice;
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
        getSupportActionBar().setTitle(R.string.title_grade);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    public void onBackPressed() {
        //Display alert message when back button has been pressed
        finish();

        return;

    }

    public void initializer(){

        dbhelper = new DBHelper(getApplicationContext());
        btAddGrades=(Button)findViewById(R.id.btAddUser);
        btAddGrades.setVisibility(View.GONE);
        btAddGrades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
                Produce();
            }
        });
        ListGrades = (ListView) this.findViewById(R.id.lvUsers);
        ListGrades.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                // Intent intent = new Intent(Activity_ListStock.this, UpdateStock.class);
                // intent.putExtra("accountid", textAccountId.getText().toString());
                // startActivity(intent);
               // showUpdateUserDialog();
               // Produce();
            }
        });


    }

    private void Produce() {
        producedata.clear();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select MpCode,MpDescription from Produce", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    produce=c.getString(c.getColumnIndex("MpDescription"));
                    producedata.add(produce);

                }while(c.moveToNext());
            }
        }


        produceadapter=new ArrayAdapter<String>(GradeDetailsActivity.this,R.layout.spinner_item,producedata);
        produceadapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerProduce.setAdapter(produceadapter);
        spinnerProduce.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String produceName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select MpCode from Produce where MpDescription= '" + produceName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    produceid = c.getString(c.getColumnIndex("MpCode"));

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
        final View dialogView = inflater.inflate(R.layout.dialog_add_grade, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Grades");
        gr_code = (EditText) dialogView.findViewById(R.id.gr_code);
        gr_name = (EditText) dialogView.findViewById(R.id.gr_name);
        spinnerProduce = (Spinner)dialogView.findViewById(R.id.spinnerProduce);



        btn_svGrade = (Button) dialogView.findViewById(R.id.btn_svGrade);
        btn_svGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_gr_code = gr_code.getText().toString();
                    s_gr_name = gr_name.getText().toString();


                    if (s_gr_code.equals("") || s_gr_name.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkGrade =dbhelper.CheckGrade(s_gr_code);
                    //Check for duplicate id number
                    if (checkGrade.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Grade already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddGrade(s_gr_code, s_gr_name,produceid);
                    if (success) {


                        Toast.makeText(GradeDetailsActivity.this, "Grade Saved successfully!!", Toast.LENGTH_LONG).show();

                        gr_code.setText("");
                        gr_name.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(GradeDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_grade, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Grades");
        accountId = textAccountId.getText().toString();

        gr_code = (EditText) dialogView.findViewById(R.id.gr_code);
        gr_code.setEnabled(false);
        gr_name = (EditText) dialogView.findViewById(R.id.gr_name);
        gr_price = (EditText) dialogView.findViewById(R.id.gr_price);
        ltprice = (LinearLayout) dialogView.findViewById(R.id.ltprice);
        ltprice.setVisibility(View.VISIBLE);
        spinnerProduce = (Spinner) dialogView.findViewById(R.id.spinnerProduce);
        txtProduce=(TextView) dialogView.findViewById(R.id.txtProduce);


        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.PRODUCEGRADES_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            gr_code.setText(account.getString(account
                    .getColumnIndex(Database.PG_DREF)));
            gr_name.setText(account.getString(account
                    .getColumnIndex(Database.PG_DNAME)));
            gr_price.setText(account.getString(account
                    .getColumnIndex(Database.PG_RETAILPRICE)));

            produceid =account.getString(account.getColumnIndex(Database.PG_DPRODUCE));
            if(produceid==null){
                txtProduce.setTextColor(Color.parseColor("#E91E63"));
                txtProduce.setText("Current Produce :- ");
            }
            else {
                Cursor c = db.rawQuery("select MpDescription from Produce where MpCode= '" + produceid + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    txtProduce.setTextColor(Color.parseColor("#E91E63"));
                    txtProduce.setText("Current Produce :- " + c.getString(c.getColumnIndex("MpDescription")));

                }
                c.close();
            }

        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svGrade = (Button) dialogView.findViewById(R.id.btn_svGrade);
        btn_svGrade.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteGrade();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateGrade();
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
    public void updateGrade() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.PG_DREF, gr_code.getText().toString());
            values.put( Database.PG_DNAME, gr_name.getText().toString());
            values.put( Database.PG_DPRODUCE, produceid);



            long rows = db.update(Database.PRODUCEGRADES_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated Grade Successfully!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Grade!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteGrade() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Grade?")
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
           /* Cursor c=db.rawQuery("select fzCode from Grades where _id= '"+accountId+"' ", null);
            if(c!=null)
            {
                c.moveToFirst();
                Gradecode= c.getString(c.getColumnIndex("fzCode"));
            }
            c.close();

            Cursor c1=db.rawQuery("select * from CollectionCenters where MccGrade= '" + Gradecode + "' ", null);
            if(c1.getCount() > 0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete Grade! ,Because its related in sheds");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(this, "Could not delete Grade! ,Because its related in sheds", Toast.LENGTH_LONG).show();
                c1.close();
            }
            else{*/

            int rows = db.delete(Database.PRODUCEGRADES_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if ( rows == 1) {
                Toast.makeText(this, "Grade Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            }
            else{
                Toast.makeText(this, "Could not delete Grade!", Toast.LENGTH_LONG).show();}
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
            Cursor accounts = db.query( true, Database.PRODUCEGRADES_TABLE_NAME,null,Database.ROW_ID + ">'" + ROWID + "'",null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.PG_DREF , Database.PG_DNAME};
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
