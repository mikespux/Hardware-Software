package com.easyweigh.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;

/**
 * Created by Michael on 30/06/2016.
 */
public class CompanyDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    EditText co_prefix,co_name,co_letterbox,co_postcode,co_postname,co_postregion,co_telephone;
    String Sco_prefix,Sco_name,Sco_letterbox,Sco_postcode,Sco_postname,Sco_postregion,Sco_telephone;
    DBHelper db;
    Button btn_svCompany;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companydetails);
        setupToolbar();
        initializer();

    }

    public void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_company);

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

    co_prefix = (EditText) findViewById(R.id.co_prefix);
    co_name = (EditText) findViewById(R.id.co_name);
    co_letterbox = (EditText) findViewById(R.id.co_letterbox);
    co_postcode = (EditText) findViewById(R.id.co_postcode);
    co_postname = (EditText) findViewById(R.id.co_postname);
    co_postregion = (EditText) findViewById(R.id.co_postregion);
    co_telephone = (EditText) findViewById(R.id.co_telephone);

    db = new DBHelper(getApplicationContext());
    Sco_prefix = co_prefix.getText().toString();
    Sco_name = co_name.getText().toString();
    Sco_letterbox = co_letterbox.getText().toString();
    Sco_postcode = co_postcode.getText().toString();
    Sco_postname = co_postname.getText().toString();
    Sco_postregion = co_postregion.getText().toString();
    Sco_telephone = co_telephone.getText().toString();

    btn_svCompany=(Button) findViewById(R.id.btn_svCompany);
    btn_svCompany.setOnClickListener(new View.OnClickListener() {


        @Override
        public void onClick(View v) {

            if(Sco_prefix.equals("")||Sco_name.equals("")||Sco_telephone.equals(""))
            {
                Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                return;
            }





        }
    });


}




}
