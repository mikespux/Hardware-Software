package com.easyweigh.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
import com.easyweigh.soap.SoapRequest;
import com.github.lzyzsd.circleprogress.ArcProgress;

import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.modificator.waterwave_progress.WaterWaveProgress;

public class UploadDeliveryActivity extends AppCompatActivity {
	private String TAG = "Vik";
	public Toolbar toolbar;
	Button btnUpload,btnCancel;
	private String checkListReturnValue;
	private SharedPreferences mSharedPrefs;
	DBHelper dbhelper;
	SoapRequest request;
	String DeliveryInfo;
	String  BatchNo,deliveryNoteNo;
	String  TicketNo,DNoteNo,DelDate,Factory,Transporter,Vehicle,ArrivalTime,FieldWt ,GrossWt,TareWt,
			RejectWt,QualityScore, DepartureTime,CoPrefix,InternalSerial,UserIdentifier,CloudID;
	private int progressStatus = 0, count = 0;
	private String soapResponse, DeliveryNo;
	String returnValue;
	SimpleDateFormat dateTimeFormat;
	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	SimpleDateFormat dateOnlyFormat;
	ArcProgress arcProgress;
	WaterWaveProgress waveProgress;

	String condition = " _id > 0 ";
	AlertDialog b;
	public SimpleCursorAdapter ca;
	SQLiteDatabase db;
	Intent mIntent;
	static SharedPreferences prefs;

	private Button btnFilter;
	ListView listReciepts;
	String BatchDate;
	TextView textBatchNo, textBatchDate, textDelNo, textStatus;

	int cloudid = 0;
	String DelNo;
	String error,errorNo;
	private EditText edtTicketNo;
	Button pickDate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		/*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);*/
		setupToolbar();
		initializer();
	}

	public void setupToolbar() {
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.title_upload);

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

	public void initializer() {
		dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(UploadDeliveryActivity.this);
		dbhelper = new DBHelper(getApplicationContext());
		db = dbhelper.getReadableDatabase();
		request = new SoapRequest(getApplicationContext());
		btnFilter = (Button) findViewById(R.id.btnFilter);
		btnFilter.setVisibility(View.GONE);
		btnFilter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Restart().execute();
				//showSearchReceipt();
			}
		});
		btnUpload = (Button) findViewById(R.id.btnUpload);
		btnUpload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!checkList()) {
					return;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setMessage(Html.fromHtml("<font color='#FF7F27'>Do you want to Upload Data?</font>"))
						.setCancelable(false)
						.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								new AsyncCallWS().execute();

							}
						})
						.setPositiveButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();

							}
						});
				AlertDialog alert2 = builder.create();
				alert2.show();

			}
		});
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();

			}
		});
		listReciepts = (ListView) this.findViewById(R.id.lvReciepts);

		listReciepts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View selectedView, int position, long arg3) {


				if(position==0){

					textBatchNo = (TextView) selectedView.findViewById(R.id.tv_device);
					textBatchDate = (TextView) selectedView.findViewById(R.id.tv_date);
					textDelNo = (TextView) selectedView.findViewById(R.id.tv_number);
					Log.d("Accounts", "Selected Account Id : " + textBatchNo.getText().toString());

					//Toast.makeText(UploadActivity.this,textBatchDate.getText().toString()+ textBatchNo.getText().toString(), Toast.LENGTH_LONG).show();
				}else{

					//Toast.makeText(UploadActivity.this,"empty", Toast.LENGTH_LONG).show();
				}
						//showRecieptDetails();


			}


		});


		if (!checkList()) {
			finish();
			return;
		}



	}


	private boolean checkList() {
		this.checkListReturnValue = XmlPullParser.NO_NAMESPACE;
		try {
			if (this.mSharedPrefs.getBoolean("cloudServices", false)) {
				try {
					if (this.mSharedPrefs.getString("internetAccessModes", null).toString().equals(null)) {
						Toast.makeText(UploadDeliveryActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
						return false;

					}
						try {
							if (this.mSharedPrefs.getString("licenseKey", null).equals(null) || this.mSharedPrefs.getString("licenseKey", null).equals(XmlPullParser.NO_NAMESPACE)) {
								//this.checkListReturnValue = "License key not found!";
								Toast.makeText(UploadDeliveryActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
								return false;
							}
							try {
								if (!this.mSharedPrefs.getString("portalURL", null).equals(null) && !this.mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
									return true;
								}
								//this.checkListReturnValue = "Portal URL not configured!";
								Toast.makeText(UploadDeliveryActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
								return false;
							} catch (Exception e) {
								//this.checkListReturnValue = "Portal URL not configured!";
								Toast.makeText(UploadDeliveryActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
								return false;
							}
						} catch (Exception e2) {
							//this.checkListReturnValue = "License key not found!";
							Toast.makeText(UploadDeliveryActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
							return false;
						}

				} catch (Exception e3) {
					e3.printStackTrace();
					//this.checkListReturnValue = "Cloud Services not enabled!";
					Toast.makeText(UploadDeliveryActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
					return false;
				}
			}
			Toast.makeText(UploadDeliveryActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;


			//this.checkListReturnValue = "Cloud Services not enabled!";

		} catch (Exception e4) {
			e4.printStackTrace();
			//this.checkListReturnValue = "Cloud Services not enabled!";
			Toast.makeText(UploadDeliveryActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;
		}

	}



	private class AsyncCallWS extends AsyncTask<String, String, String> {

			@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute");

			waveProgress = (WaterWaveProgress) findViewById(R.id.waterWaveProgress1);
			waveProgress.setShowProgress(true);
			waveProgress.setProgress(0);
			waveProgress.animateWave();

			arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
			arcProgress.setVisibility(View.VISIBLE);
			arcProgress.setProgress(0);

			textStatus = (TextView) findViewById(R.id.textStatus);
			textStatus.setVisibility(View.VISIBLE);

			listReciepts.setVisibility(View.GONE);
			btnFilter.setVisibility(View.GONE);
			listReciepts.performItemClick(listReciepts.getAdapter().getView(0, null, null), 0, listReciepts.getAdapter().getItemId(0));

		}

		@Override
		protected String doInBackground(String... aurl) {
			Log.i(TAG, "doInBackground");
			try {
			try {
				BatchNo = textBatchNo.getText().toString();
				DelNo = textDelNo.getText().toString();

				if (DelNo.length() > 0)
					condition += " and  " + Database.FdDNoteNum + " = '" + DelNo + "'";
				if (cloudid > 0)
				    condition += " and  " + Database.CloudID + " = '" + cloudid + "'";


				SQLiteDatabase db= dbhelper.getReadableDatabase();
				Cursor accounts = db.rawQuery("SELECT * FROM " + Database.Fmr_FactoryDeliveries + " where " + condition + "", null);
				count=accounts.getCount();
				if (accounts.getCount() > 0) {
					accounts.moveToFirst();
					while (!accounts.isAfterLast()) {

						TicketNo= accounts.getString(accounts.getColumnIndex(Database.FdWeighbridgeTicket));
						DNoteNo= accounts.getString(accounts.getColumnIndex(Database.FdDNoteNum));
						DelDate= accounts.getString(accounts.getColumnIndex(Database.FdDate));
						Factory= accounts.getString(accounts.getColumnIndex(Database.FdFactory));

						if (accounts.getString(accounts.getColumnIndex(Database.FdTransporter))==null){
							Transporter="";
						}else{
							Transporter=accounts.getString(accounts.getColumnIndex(Database.FdTransporter));
						}
						Vehicle= accounts.getString(accounts.getColumnIndex(Database.FdVehicle));
						ArrivalTime= accounts.getString(accounts.getColumnIndex(Database.FdArrivalTime));
						FieldWt= accounts.getString(accounts.getColumnIndex(Database.FdFieldWt));
						GrossWt= accounts.getString(accounts.getColumnIndex(Database.FdGrossWt));
						TareWt= accounts.getString(accounts.getColumnIndex(Database.FdTareWt));

						if (accounts.getString(accounts.getColumnIndex(Database.FdRejectWt)).equals("")){
							RejectWt="0";
						}
						else{
							RejectWt= accounts.getString(accounts.getColumnIndex(Database.FdRejectWt));
						}
						if (accounts.getString(accounts.getColumnIndex(Database.FdQualityScore)).equals("")){
							QualityScore="0";
						}else{
							QualityScore= accounts.getString(accounts.getColumnIndex(Database.FdQualityScore));
						}

						DepartureTime= accounts.getString(accounts.getColumnIndex(Database.FdDepartureTime));
						CloudID= accounts.getString(accounts.getColumnIndex(Database.CloudID));

						CoPrefix=mSharedPrefs.getString("company_prefix", "").toString();
						InternalSerial=mSharedPrefs.getString("terminalID", "").toString();
						UserIdentifier=prefs.getString("user", "");


						StringBuilder sb = new StringBuilder();
						sb.append(TicketNo + ",");
						sb.append(DNoteNo + ",");
						sb.append(DelDate + ",");
						sb.append(Factory + ",");
						sb.append(Transporter + ",");
						sb.append(Vehicle + ",");
						sb.append(ArrivalTime + ",");
						sb.append(FieldWt + ",");
						sb.append(GrossWt + ",");
						sb.append(TareWt + ",");
						sb.append(RejectWt + ",");
						sb.append(QualityScore + ",");
						sb.append(DepartureTime + ",");
						sb.append(CoPrefix + ",");
						sb.append(InternalSerial + ",");
						sb.append(UserIdentifier);
						DeliveryInfo = sb.toString();

						accounts.moveToNext();

						progressStatus++;
						publishProgress("" + progressStatus);
					}
					accounts.close();
					//request.createBatch(DeliveryInfo);
					UploadDeliveryActivity.this.soapResponse = new SoapRequest(UploadDeliveryActivity.this).createDelivery(DeliveryInfo);
					error=soapResponse;
					errorNo=prefs.getString("DelerrorNo", "");
					DeliveryNo=soapResponse;
					try{
					if (Integer.valueOf(UploadDeliveryActivity.this.errorNo).intValue()<0) {

						DeliveryNo=CloudID;

					}
					} catch (NumberFormatException e) {

						DeliveryNo=soapResponse;
					}


				}
				else{

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}

				Cursor accounts1 =  db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
						+ Database.DelivaryNO + " ='" + DelNo + "'", null);
				if (accounts1.getCount() > 0) {
					//accounts1.moveToFirst();
					count=count+accounts1.getCount();
					while (accounts1.moveToNext()) {
						deliveryNoteNo = accounts1.getString(accounts1.getColumnIndex(Database.DeliveryNoteNumber));

						StringBuilder sb = new StringBuilder();

						sb.append(deliveryNoteNo);

						BatchNo = sb.toString();

						try {
							soapResponse = new SoapRequest(UploadDeliveryActivity.this).DeliverBatch(DeliveryNo, BatchNo);
							error=soapResponse;
							errorNo=prefs.getString("DelerrorNo", "");

							try{
								if (Integer.valueOf(UploadDeliveryActivity.this.errorNo).intValue()<0) {
									return null;
								}
							} catch (NumberFormatException e) {

								returnValue = soapResponse;
							}

						} catch (Exception e) {
							e.printStackTrace();
							returnValue = e.toString();
						}

						progressStatus++;
						publishProgress("" + progressStatus);

					}


					accounts1.close();

				}
				else{

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}








			} catch (Exception e) {
				e.printStackTrace();
				returnValue = e.toString();
			}




				soapResponse = new SoapRequest(UploadDeliveryActivity.this).SignoffDelivery(DeliveryNo);
				error=soapResponse;


			} catch (Exception e) {
				e.printStackTrace();
				returnValue = e.toString();
			}


			return null;
		}
		@Override
		protected void onProgressUpdate(String... progress) {
			Log.i(TAG, "onProgressUpdate");
			waveProgress.setProgress(Integer.parseInt(progress[0]));
			waveProgress.setMaxProgress(count);
			arcProgress.setProgress(Integer.parseInt(progress[0]));
			arcProgress.setMax(count);
			arcProgress.setBottomText("Uploading ...");

			textStatus.setText("Uploading... " + Integer.parseInt(progress[0]) + "/" + count + " Records");
		}



		@Override
		protected void onPostExecute(String unused) {
				Log.i(TAG, "onPostExecute");

			//finish();
			//Toast.makeText(UploadActivity.this, returnValue, Toast.LENGTH_LONG).show();






			try {

				if (Integer.valueOf(soapResponse).intValue() > 0) {
					returnValue = soapResponse;
					ContentValues values = new ContentValues();
					values.put(Database.CloudID, DeliveryNo);
					long rows = db.update(Database.Fmr_FactoryDeliveries, values,
							Database.FdDNoteNum + " = ?", new String[]{DelNo});

					if (rows > 0) {

					}
					Toast.makeText(UploadDeliveryActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
					new Restart().execute();
					return;
				}
			} catch (NumberFormatException e) {
				errorNo=prefs.getString("DelerrorNo", "");

				if(error.equals("-8080")){
					Toast.makeText(UploadDeliveryActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				else if (errorNo.equals("-1317")) {
					//Toast.makeText(UploadDeliveryActivity.this, "Similar delivery(Ticket No) record exist", Toast.LENGTH_LONG).show();


				/*	AlertDialog.Builder dialogBasedate = new AlertDialog.Builder(UploadDeliveryActivity.this);
					LayoutInflater inflater1 = getLayoutInflater();
					final View dialogView1 = inflater1.inflate(R.layout.dialog_basedate, null);
					dialogBasedate.setView(dialogView1);
					dialogBasedate.setCancelable(false);
					dialogBasedate.setTitle("Edit TicketNo");
					edtTicketNo	= (EditText) dialogView1.findViewById(R.id.editText);

					edtTicketNo.setText(TicketNo);

					pickDate =(Button)dialogView1.findViewById(R.id.btnDate);
					pickDate.setVisibility(View.GONE);
					pickDate.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {


						}
					});

					dialogBasedate.setPositiveButton("Save", new DialogInterface.OnClickListener() {
						@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
						public void onClick(DialogInterface dialog, int whichButton) {
							//do something with edt.getText().toString();




						}
					});

					dialogBasedate.setOnKeyListener(new DialogInterface.OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK) {
								Toast.makeText(UploadDeliveryActivity.this, "Please Edit Ticket No", Toast.LENGTH_LONG).show();
								return true;
							}
							return false;
						}
					});

					b = dialogBasedate.create();
					b.show();
					b.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {

							if (edtTicketNo.getText().length() == 0) {
								Toast.makeText(UploadDeliveryActivity.this, "Please Edit Ticket No", Toast.LENGTH_LONG).show();
								return;
							}

							ContentValues values = new ContentValues();
							values.put(Database.FdWeighbridgeTicket, edtTicketNo.getText().toString());
							long rows = db.update(Database.Fmr_FactoryDeliveries, values,
									Database.FdDNoteNum + " = ?", new String[]{DelNo});


							if (rows > 0) {

								Toast.makeText(UploadDeliveryActivity.this, "TicketNo Updated Successfully !!", Toast.LENGTH_LONG).show();
								b.dismiss();
								new Restart().execute();
							}



							Boolean wantToCloseDialog = false;
							//Do stuff, possibly set wantToCloseDialog to true then...
							if (wantToCloseDialog)
								b.dismiss();
						}
					});*/
					return;
				}else
				{
				Toast.makeText(UploadDeliveryActivity.this, error, Toast.LENGTH_LONG).show();
				finish();
				}
			}

			//b.dismiss();
		}

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

			Cursor accounts = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
					 + Database.CloudID + " ='" + cloudid + "'", null);
			if (accounts.getCount() > 0) {

				String from [] = {  Database.ROW_ID,Database.FdDNoteNum, Database.FdWeighbridgeTicket , Database.FdDate, Database.FdFieldWt};
				int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_date,R.id.txtTotalKgs};


				ca  = new SimpleCursorAdapter(this,R.layout.upload_delivery_list, accounts,from,to);

				listReciepts= (ListView) this.findViewById( R.id.lvReciepts);

				listReciepts.setAdapter(ca);
				ca.notifyDataSetChanged();
				listReciepts.setTextFilterEnabled(true);
				listReciepts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				listReciepts.performItemClick(listReciepts.getAdapter().getView(0, null, null), 0, listReciepts.getAdapter().getItemId(0));

				//db.close();
				//dbhelper.close();


			}

			else {

				new NoReceipt().execute();
			}
		} catch (Exception ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private String getDate(){

		//A string to hold the current date
		String currentDateTimeString = DateFormat.getDateInstance().format(new Date());

		//Return the current date
		return currentDateTimeString;
	}


	private class Restart extends AsyncTask<Void, Void, String>
	{
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute()
		{

		}

		@Override
		protected String doInBackground(Void... params)
		{
			finish();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			return "";
		}

		@Override
		protected void onPostExecute(String result)
		{



			mIntent = new Intent(getApplicationContext(),UploadDeliveryActivity.class);
			startActivity(mIntent);
		}
	}
	private class NoReceipt extends AsyncTask<Void, Void, String>
	{


		@Override
		protected void onPreExecute()
		{

		}

		@Override
		protected String doInBackground(Void... params)
		{


			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			return "";
		}

		@Override
		protected void onPostExecute(String result)
		{


			finish();
			Context context=getApplicationContext();
			LayoutInflater inflater=getLayoutInflater();
			View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
			TextView text = (TextView) customToastroot.findViewById(R.id.toast);
			text.setText("Nothing Found To Upload");
			Toast customtoast=new Toast(context);
			customtoast.setView(customToastroot);
			customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
			customtoast.setDuration(Toast.LENGTH_LONG);
			customtoast.show();
		}
	}
}
