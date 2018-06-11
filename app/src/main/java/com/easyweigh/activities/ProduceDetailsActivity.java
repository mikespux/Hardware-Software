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
public class ProduceDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    DBHelper dbhelper;
    Button btAddUser,btn_svProduce,btnVariety,btnGrade,btnPrice,btn_svPrice;
    LinearLayout layoutVR;
    ListView listProduce;
    EditText etDgProduceCode,etDgProduceTitle,pr_price1;
    String s_etDgProduceCode,s_etDgProduceTitle;
    String accountId;
    TextView textAccountId;
    Boolean success = true;
    public Intent mIntent = null;
    String ProduceCode;
    Spinner spinnerProduce,spinnerVariety,spinnerGrade;
    EditText pr_price,vr_price,gr_price;

    String grade,gradeid;
    ArrayList<String> gradedata=new ArrayList<String>();
    ArrayAdapter<String> gradeadapter;

    String variety,varietyid;
    ArrayList<String> varietydata=new ArrayList<String>();
    ArrayAdapter<String> varietyadapter;

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
        getSupportActionBar().setTitle(R.string.title_produce);

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
        layoutVR=(LinearLayout)findViewById(R.id.layoutVR);
        layoutVR.setVisibility(View.VISIBLE);
        dbhelper = new DBHelper(getApplicationContext());
        btAddUser=(Button)findViewById(R.id.btAddUser);
        btAddUser.setVisibility(View.GONE);
        btnGrade=(Button)findViewById(R.id.btnGrades);
        btnVariety=(Button)findViewById(R.id.btnVarieties);
        btnPrice=(Button)findViewById(R.id.btnPrices);

        btnGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(ProduceDetailsActivity.this,GradeDetailsActivity.class);
                startActivity(mIntent);
            }
        });
        btnVariety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(ProduceDetailsActivity.this, VarietyDetailsActivity.class);
                startActivity(mIntent);
            }
        });
        btnPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPrices();
                Produce();
                Grade();
                Variety();
            }
        });
        btAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
        listProduce = (ListView) this.findViewById(R.id.lvUsers);
        listProduce.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = (TextView) selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
               // showUpdateUserDialog();
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


        produceadapter=new ArrayAdapter<String>(ProduceDetailsActivity.this,R.layout.spinner_item,producedata);
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

                TextView tv = (TextView) view;
                if (position % 2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                } else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(Color.parseColor("#B3E5FC"));
                }
                if (position == 0) {
                    spinnerVariety.setEnabled(false);
                    spinnerGrade.setEnabled(false);
                    pr_price.setEnabled(false);
                    vr_price.setEnabled(false);
                    gr_price.setEnabled(false);
                    Variety();
                    Grade();
                    //Toast.makeText(getActivity(), "Please sel", Toast.LENGTH_LONG).show();

                }else{
                    pr_price.setEnabled(true);
                    Variety();
                    Grade();
                    Cursor c1 = db.rawQuery("select * from ProduceGrades where pgdProduce= '" + produceid + "' ", null);
                    Cursor c2 = db.rawQuery("select * from ProduceVarieties where vrtProduce= '" + produceid + "' ", null);
                    if(c2.getCount() > 0) {
                        spinnerVariety.setEnabled(true);
                        vr_price.setEnabled(true);


                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c2.close();

                    }else{
                        spinnerVariety.setEnabled(false);
                        vr_price.setEnabled(false);
                        varietydata.clear();
                    }
                    if (c1.getCount() > 0) {
                        spinnerGrade.setEnabled(true);
                        gr_price.setEnabled(true);
                        // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                        c1.close();
                    }else{
                        spinnerGrade.setEnabled(false);
                        gr_price.setEnabled(false);
                        gradedata.clear();
                    }



                }
                c.close();
                db.close();

                dbhelper.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    private void Grade() {
        gradedata.clear();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select pgdRef,pgdName from ProduceGrades where pgdProduce= '" + produceid + "' ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    grade=c.getString(c.getColumnIndex("pgdName"));
                    gradedata.add(grade);

                }while(c.moveToNext());
            }
        }


        gradeadapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_item,gradedata);
        gradeadapter.setDropDownViewResource(R.layout.spinner_item);
        gradeadapter.notifyDataSetChanged();
        spinnerGrade.setAdapter(gradeadapter);
        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String GradeName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select pgdRef from ProduceGrades where pgdName= '" + GradeName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    gradeid = c.getString(c.getColumnIndex("pgdRef"));

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
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                    tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void Variety() {
        varietydata.clear();
        SQLiteDatabase db= dbhelper.getReadableDatabase();
        Cursor c=db.rawQuery("select vtrRef,vrtName from ProduceVarieties where vrtProduce= '" + produceid + "' ", null);
        if(c!=null)
        {
            if(c.moveToFirst())
            {
                do{
                    variety=c.getString(c.getColumnIndex("vrtName"));
                    varietydata.add(variety);

                }while(c.moveToNext());
            }
        }


        varietyadapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_item,varietydata);
        varietyadapter.setDropDownViewResource(R.layout.spinner_item);
        varietyadapter.notifyDataSetChanged();
        spinnerVariety.setAdapter(varietyadapter);
        spinnerVariety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String varietyName = parent.getItemAtPosition(position).toString();
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("select vtrRef from ProduceVarieties where vrtName= '" + varietyName + "' ", null);
                if (c != null) {
                    c.moveToFirst();
                    varietyid = c.getString(c.getColumnIndex("vtrRef"));

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
               /* if(disabled.equals("true")) {
                    // Set the disable item text color
                  tv.setBackgroundColor(Color.parseColor("#E3E4ED"));

                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_produce, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Produce");
        etDgProduceCode = (EditText) dialogView.findViewById(R.id.etDgProduceCode);
        etDgProduceTitle = (EditText) dialogView.findViewById(R.id.etDgProduceTitle);



        btn_svProduce = (Button) dialogView.findViewById(R.id.btn_svProduce);
        btn_svProduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_etDgProduceCode = etDgProduceCode.getText().toString();
                    s_etDgProduceTitle = etDgProduceTitle.getText().toString();


                    if (s_etDgProduceTitle.equals("") || s_etDgProduceCode.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Cursor checkProduce =dbhelper.CheckProduce(s_etDgProduceCode);
                    //Check for duplicate id number
                    if (checkProduce.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Produce already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbhelper.AddProduce(s_etDgProduceCode, s_etDgProduceTitle);
                    if (success) {


                        Toast.makeText(ProduceDetailsActivity.this, "Produce Saved successfully!!", Toast.LENGTH_LONG).show();

                        etDgProduceCode.setText("");
                        etDgProduceTitle.setText("");

                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(ProduceDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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

    public void showAddPrices() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_prices, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Prices");
        pr_price = (EditText) dialogView.findViewById(R.id.pr_price);
        vr_price = (EditText) dialogView.findViewById(R.id.vr_price);
        gr_price = (EditText) dialogView.findViewById(R.id.gr_price);

        spinnerProduce = (Spinner) dialogView.findViewById(R.id.spinnerProduce);
        spinnerVariety = (Spinner) dialogView.findViewById(R.id.spinnerVariety);
        spinnerGrade = (Spinner) dialogView.findViewById(R.id.spinnerGrade);



        btn_svPrice = (Button) dialogView.findViewById(R.id.btn_svPrice);
        btn_svPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    if (success) {

                        if(pr_price.getText().toString().length()>0){

                            updateProducePrice();


                        }

                        if(vr_price.getText().toString().length()>0){

                            updateVarietyPrice();


                        }
                        if(gr_price.getText().toString().length()>0){

                            updateGradePrice();


                        }
                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(ProduceDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
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
        final View dialogView = inflater.inflate(R.layout.dialog_add_produce, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Produce");
        accountId = textAccountId.getText().toString();

        etDgProduceCode = (EditText) dialogView.findViewById(R.id.etDgProduceCode);
        etDgProduceCode .setEnabled(false);
        etDgProduceTitle = (EditText) dialogView.findViewById(R.id.etDgProduceTitle);
        pr_price1 = (EditText) dialogView.findViewById(R.id.pr_price);
        ltprice = (LinearLayout) dialogView.findViewById(R.id.ltprice);
        ltprice.setVisibility(View.VISIBLE);

        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.PRODUCE_TABLE_NAME, null,
                " _id = ?", new String[] { accountId }, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            etDgProduceCode.setText(account.getString(account
                    .getColumnIndex(Database.MP_CODE)));
            etDgProduceTitle.setText(account.getString(account
                    .getColumnIndex(Database.MP_DESCRIPTION)));
pr_price1.setText(account.getString(account
        .getColumnIndex(Database.MP_RETAILPRICE)));


        }
        account.close();
        db.close();
        dbhelper.close();



        btn_svProduce = (Button) dialogView.findViewById(R.id.btn_svProduce);
        btn_svProduce.setVisibility(View.GONE);


        dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteProduce();

            }
        });
        dialogBuilder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateProduce();
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
    public void updateProduce() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.MP_CODE, etDgProduceCode.getText().toString());
            values.put( Database.MP_DESCRIPTION, etDgProduceTitle.getText().toString());



            long rows = db.update(Database.PRODUCE_TABLE_NAME, values,
                    "_id = ?", new String[] { accountId });

            db.close();
            if (rows > 0){
                Toast.makeText(this, "Updated Produce Successfully!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Produce!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateProducePrice() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.MP_RETAILPRICE, pr_price.getText().toString());


            long rows = db.update(Database.PRODUCE_TABLE_NAME, values,
                    "MpCode = ?", new String[] { produceid });

            db.close();
            if (rows > 0){

                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Updated Produce Price Successfully!");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.TOP | Gravity.TOP, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
               // Toast.makeText(this, "Updated Produce Price Successfully!",Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Produce!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void updateGradePrice() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.PG_RETAILPRICE, gr_price.getText().toString());


            long rows = db.update(Database.PRODUCEGRADES_TABLE_NAME, values,
                    "pgdRef = ?", new String[] { gradeid });

            db.close();
            if (rows > 0){
                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Updated Grade Price Successfully!");
                Toast customtoast=new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                ///Toast.makeText(this, "Updated Grade Price Successfully!",Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Grade Price!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateVarietyPrice() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command

            ContentValues values = new ContentValues();
            values.put( Database.VRT_RETAILPRICE, vr_price.getText().toString());


            long rows = db.update(Database.PRODUCEVARIETIES_TABLE_NAME, values,
                    "vtrRef = ?", new String[]{ varietyid });

            db.close();
            if (rows > 0){

                Context context=getApplicationContext();
                LayoutInflater inflater=getLayoutInflater();
                View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Updated Variety Price Successfully!");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                //Toast.makeText(this, "Updated Variety Price Successfully!",Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Sorry! Could not update Variety!",
                        Toast.LENGTH_LONG).show();}
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void deleteProduce() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this produce?")
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
            Cursor c = db.rawQuery("select MpCode from Produce where _id= '" + accountId + "' ", null);
            if (c != null) {
                c.moveToFirst();
                ProduceCode = c.getString(c.getColumnIndex("MpCode"));
            }
            c.close();
            Cursor c1 = db.rawQuery("select * from ProduceGrades where pgdProduce= '" + ProduceCode + "' ", null);
            Cursor c2 = db.rawQuery("select * from ProduceVarieties where vrtProduce= '" + ProduceCode + "' ", null);
            if (c1.getCount() > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete Produce! ,Because its related in Grades");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                c1.close();
            }else if(c2.getCount() > 0) {
                Context context = getApplicationContext();
                LayoutInflater inflater = getLayoutInflater();
                View customToastroot = inflater.inflate(R.layout.red_toast, null);
                TextView text = (TextView) customToastroot.findViewById(R.id.toast);
                text.setText("Could not delete Produce! ,Because its related in Varieties");
                Toast customtoast = new Toast(context);
                customtoast.setView(customToastroot);
                customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                customtoast.setDuration(Toast.LENGTH_LONG);
                customtoast.show();
                // Toast.makeText(this, "Could not delete shed! ,Because its related in farmers", Toast.LENGTH_LONG).show();
                c2.close();

            } else {
                int rows = db.delete(Database.PRODUCE_TABLE_NAME, "_id=?", new String[]{accountId});
                dbhelper.close();
                if (rows == 1) {
                    Toast.makeText(this, "Produce Deleted Successfully!", Toast.LENGTH_LONG).show();

                    //this.finish();
                    getdata();
                } else
                    Toast.makeText(this, "Could not delete produce!", Toast.LENGTH_LONG).show();
            }
            }catch(Exception ex){
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata(){

        try {
            int ROWID=0;
            SQLiteDatabase db= dbhelper.getReadableDatabase();
            Cursor accounts = db.query( true, Database.PRODUCE_TABLE_NAME,null,Database.ROW_ID + ">'" + ROWID + "'",null,null,null,null,null,null);

            String from [] = {  Database.ROW_ID,Database.MP_CODE , Database.MP_DESCRIPTION};
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
