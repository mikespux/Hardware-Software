package com.easyweigh.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.modificator.waterwave_progress.WaterWaveProgress;

@SuppressWarnings("ALL")
public class UploadAgentActivity extends AppCompatActivity {
	private String TAG = "Vik";
	public Toolbar toolbar;
	Button btnUpload,btnCancel;
	private String checkListReturnValue;
	private SharedPreferences mSharedPrefs;
	DBHelper dbhelper;
	SoapRequest request;
	String batchInfo;
	String batchNo, deviceID, stringOpenDate, deliveryNoteNo, userID, ClerkID, stringOpenTime, weighingSession,
			closed, stringCloseTime, factory, tractorNo, trailerNo,TransporterCode,DelivaryNo,Co_prefix,Current_User;

	private int progressStatus = 0, count = 0;

	String weighmentInfo,deliveryInfo;
	String totalWeight;
	String SessionNo,ColDate, Time, DataDevice, BatchNo, Agent, FarmerNo;
	String WarehouseID, FieldClerk, ProduceCode;
	String VarietyCode, GradeCode, RouteCode, ShedCode;
	String  GrossTotal, TareWeight, UnitCount,BatchDte;
	String UnitPrice, RecieptNo, CanSerial, NetWeight, UsedCard;

	String serverBatchID;
	String shedCode;
	private String soapResponse, serverBatchNo,serverDeliveryNo;
	String returnValue;
	SimpleDateFormat dateTimeFormat;
	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	SimpleDateFormat dateOnlyFormat;
	ArcProgress arcProgress;
	WaterWaveProgress waveProgress;

	private TextView textView, txtFNo;
	private Button pickFrom, pickTo;
	EditText etFrom, etTo, etFarmerNo;
	String fromDate, toDate, farmerNo;
	String condition = " _id > 0 ";
	String condition1 = " _id > 0 ";
	AlertDialog b;
	private Button btnSearchReceipt;
	public SimpleCursorAdapter ca;
	SQLiteDatabase db;
	Intent mIntent;
	static SharedPreferences prefs;

	private Button btnFilter;
	ListView listReciepts;
	String BatchDate;
	TextView textBatchNo, textBatchDate, textDelNo, textStatus;
	String cond;
	SearchView searchView;
	int closed1 = 1;
	int cloudid = 0;
	String DelNo;
	String error,errorNo;

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
		prefs = PreferenceManager.getDefaultSharedPreferences(UploadAgentActivity.this);
		dbhelper = new DBHelper(getApplicationContext());
		db = dbhelper.getReadableDatabase();
		request = new SoapRequest(getApplicationContext());
		btnFilter = (Button) findViewById(R.id.btnFilter);
		btnFilter.setVisibility(View.GONE);
		btnFilter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Restart().execute();
				showSearchReceipt();
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

					textBatchNo = (TextView) selectedView.findViewById(R.id.tv_reciept);
					textBatchDate = (TextView) selectedView.findViewById(R.id.tv_date);
					textDelNo = (TextView) selectedView.findViewById(R.id.tv_number);
					Log.d("Accounts", "Selected Account Id : " + textBatchNo.getText().toString());

					//Toast.makeText(UploadActivity.this,textBatchDate.getText().toString()+ textBatchNo.getText().toString(), Toast.LENGTH_LONG).show();
				}else{

					//Toast.makeText(UploadActivity.this,"empty", Toast.LENGTH_LONG).show();
				}



			}


		});


		if (!checkList()) {
			finish();
			return;
		}

		String selectQuery = "SELECT * FROM " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE SignedOff=1";
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.getCount() <= 0) {
			Toast.makeText(UploadAgentActivity.this, "No Batch Dispatched to Upload!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		cursor.close();
		//showSearchReceipt();

	}


	private boolean checkList() {
		this.checkListReturnValue = XmlPullParser.NO_NAMESPACE;
		try {
			if (this.mSharedPrefs.getBoolean("cloudServices", false)) {
				try {
					if (this.mSharedPrefs.getString("internetAccessModes", null).toString().equals(null)) {
						Toast.makeText(UploadAgentActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
						return false;

					}
						try {
							if (this.mSharedPrefs.getString("licenseKey", null).equals(null) || this.mSharedPrefs.getString("licenseKey", null).equals(XmlPullParser.NO_NAMESPACE)) {
								//this.checkListReturnValue = "License key not found!";
								Toast.makeText(UploadAgentActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
								return false;
							}
							try {
								if (!this.mSharedPrefs.getString("portalURL", null).equals(null) && !this.mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
									return true;
								}
								//this.checkListReturnValue = "Portal URL not configured!";
								Toast.makeText(UploadAgentActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
								return false;
							} catch (Exception e) {
								//this.checkListReturnValue = "Portal URL not configured!";
								Toast.makeText(UploadAgentActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
								return false;
							}
						} catch (Exception e2) {
							//this.checkListReturnValue = "License key not found!";
							Toast.makeText(UploadAgentActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
							return false;
						}

				} catch (Exception e3) {
					e3.printStackTrace();
					//this.checkListReturnValue = "Cloud Services not enabled!";
					Toast.makeText(UploadAgentActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
					return false;
				}
			}
			Toast.makeText(UploadAgentActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;


			//this.checkListReturnValue = "Cloud Services not enabled!";

		} catch (Exception e4) {
			e4.printStackTrace();
			//this.checkListReturnValue = "Cloud Services not enabled!";
			Toast.makeText(UploadAgentActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;
		}

	}
	public void showSearchReceipt() {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_search_batches, null);
		dialogBuilder.setView(dialogView);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setTitle("Search Batches");
		etFrom = (EditText) dialogView.findViewById(R.id.edtFromDate);
		etTo = (EditText) dialogView.findViewById(R.id.edtToDate);
		etFarmerNo = (EditText) dialogView.findViewById(R.id.edtFarmerNo);

		Date date = new Date(getDate());
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		etFrom.setText(format1.format(date));
		etTo.setText(format1.format(date));

		pickFrom =(Button)dialogView.findViewById(R.id.btnFrom);
		pickFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(getSupportFragmentManager(), "datePicker");

			}
		});

		pickTo =(Button)dialogView.findViewById(R.id.btnTo);
		pickTo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment2();
				newFragment.show(getSupportFragmentManager(), "datePicker");

			}
		});


		btnSearchReceipt = (Button) dialogView.findViewById(R.id.btn_SearchReceipt);
		btnSearchReceipt.setVisibility(View.VISIBLE);
		btnSearchReceipt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fromDate = etFrom.getText().toString();
				toDate = etTo.getText().toString();
				farmerNo = etFarmerNo.getText().toString();

				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("fromDate", fromDate);
				edit.commit();
				edit.putString("toDate", toDate);
				edit.commit();
				edit.putString("farmerNo", farmerNo);
				edit.commit();

				if (fromDate.length() > 0)
					condition += " and  " + Database.BatchDate + " >= '" + fromDate + "'";
				else
				new Restart().execute();

				if (toDate.length() > 0)
					condition += " and  " + Database.BatchDate + " <= '" + toDate + "'";

				if (closed1 > 0)
					condition += " and  " + Database.Closed + " = '" + closed1 + "'";

				//getSearch();
				ca.getFilter().filter(condition.toString());
				ca.setFilterQueryProvider(new FilterQueryProvider() {

					@Override
					public Cursor runQuery(CharSequence constraint) {
						String reciept = constraint.toString();
						return dbhelper.SearchAgtBatchByDate(reciept);
					}
				});

				b.dismiss();
			}
		});

		dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {

					return true;
				}
				return false;
			}
		});
		dialogBuilder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//pass
				//getdata();
				finish();
			}
		});
		b = dialogBuilder.create();
		b.show();

	}
	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			int hour = c.get( Calendar.HOUR );
			int minute = c.get( Calendar.MINUTE );

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			// edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());

			// Create a Date variable/object with user chosen date
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(year, month, day, 0, 0, 0);
			Date chosenDate = cal.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
			etFrom.setText(format1.format(chosenDate));
		}
	}
	@SuppressLint("ValidFragment")
	public class DatePickerFragment2 extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			// edtBaseDate.setText(view.getYear() + "/" + view.getMonth() + "/" + view.getDayOfMonth());
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(year, month, day, 0, 0, 0);
			Date chosenDate = cal.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
			etTo.setText(format1.format(chosenDate));
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
				String dbtBatchOn =textBatchDate.getText().toString()+" 00:00:00";
				String dbtBatchOn1 =textBatchDate.getText().toString();
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				try {
					date = fmt.parse(dbtBatchOn);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
				BatchDate=format1.format(date);
				SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMdd");
				BatchDte=format2.format(date);

				if (dbtBatchOn1.length() > 0)
					condition += " and  " + Database.BatchDate + " = '" + dbtBatchOn1 + "'";

				if (BatchNo.length() > 0)
					condition += " and  " + Database.BatchNumber + " = '" + BatchNo + "'";

				if (closed1 > 0)
					condition += " and  " + Database.Closed + " = '" + closed1 + "'";
				    condition += " and  " + Database.BatCloudID + " = '" + cloudid + "'";


				SQLiteDatabase db= dbhelper.getReadableDatabase();
				Cursor accounts = db.rawQuery("SELECT * FROM " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where " + condition + " and SignedOff=1", null);
				count=accounts.getCount();
				if (accounts.getCount() > 0) {
					accounts.moveToFirst();
					while (!accounts.isAfterLast()) {
						totalWeight=accounts.getString(accounts.getColumnIndex(Database.TotalWeights));

						Date openTime = dateTimeFormat.parse(accounts.getString(accounts.getColumnIndex(Database.BatchDate)).toString() +
								" " +
								accounts.getString(accounts.getColumnIndex(Database.OpeningTime)).toString());
						Date closeTime = dateTimeFormat.parse(accounts.getString(accounts.getColumnIndex(Database.BatchDate)).toString() +
								" " +
								accounts.getString(accounts.getColumnIndex(Database.ClosingTime)).toString());
						batchNo = accounts.getString(accounts.getColumnIndex(Database.BatchNumber));
						deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
						stringOpenDate = dateFormat.format(openTime);
						deliveryNoteNo = accounts.getString(accounts.getColumnIndex(Database.DeliveryNoteNumber));
						userID = accounts.getString(accounts.getColumnIndex(Database.Userid));
						stringOpenTime = timeFormat.format(openTime);
						if (accounts.getString(accounts.getColumnIndex(Database.BatchSession))==null){
							weighingSession = "1";
						}else{
							weighingSession=accounts.getString(accounts.getColumnIndex(Database.BatchSession));
						}
						closed = accounts.getString(accounts.getColumnIndex(Database.Closed));
						stringCloseTime = timeFormat.format(closeTime);
						factory = accounts.getString(accounts.getColumnIndex(Database.Factory));
						if (accounts.getString(accounts.getColumnIndex(Database.Transporter))==null){
							TransporterCode="";
						}else{
						TransporterCode=accounts.getString(accounts.getColumnIndex(Database.Transporter));
						}
						tractorNo = accounts.getString(accounts.getColumnIndex(Database.Tractor));
						trailerNo = accounts.getString(accounts.getColumnIndex(Database.Trailer));

						if (accounts.getString(accounts.getColumnIndex(Database.DelivaryNO))==null){
							DelivaryNo="";
						}else{
							DelivaryNo=accounts.getString(accounts.getColumnIndex(Database.DelivaryNO));
						}
						Co_prefix=mSharedPrefs.getString("company_prefix", "").toString();
						Current_User=prefs.getString("user", "");
						//factory = "1";
						//tractorNo = "KCG 575B";
						//trailerNo = "KCG 575B";
						StringBuilder sb = new StringBuilder();
						sb.append(batchNo + ",");
						sb.append(deviceID + ",");
						sb.append(stringOpenDate + ",");
						sb.append(deliveryNoteNo + ",");
						sb.append(userID + ",");
						sb.append(stringOpenTime + ",");
						sb.append(weighingSession + ",");
						sb.append(closed + ",");
						sb.append(stringCloseTime + ",");
						sb.append(factory + ",");
						sb.append(tractorNo + ",");
						sb.append(trailerNo + ",");
						sb.append(DelivaryNo + ",");
						sb.append(TransporterCode + ",");
						sb.append(Co_prefix + ",");
						sb.append(Current_User);
						batchInfo = sb.toString();

						accounts.moveToNext();

						progressStatus++;
						publishProgress("" + progressStatus);
					}
					accounts.close();
					//request.createBatch(batchInfo);
					UploadAgentActivity.this.soapResponse = new SoapRequest(UploadAgentActivity.this).CreateAgentsBatch(batchInfo);
					error=soapResponse;
					/*errorNo=prefs.getString("errorNo", "");
					if (Integer.valueOf(errorNo).intValue()<0) {
							return null;
					}*/
					try {
						if (Integer.valueOf(error).intValue()<0) {
							return null;
						}
						//System.out.println(value);}
					} catch (NumberFormatException e) {
						//value = 0; // your default value

							return null;

					}
					serverBatchNo=soapResponse;


				}
				else{

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}
				Cursor delivery =  db.rawQuery("select * from " + Database.ASESSION_TABLE_NAME + " WHERE "
						+ Database.ASessionDate + " ='" + BatchDate + "' and " + Database.ASessionBatchID + " ='" + batchNo + "' and " + Database.CloudID + " ='" + cloudid + "'", null);
				if (delivery.getCount() > 0) {
					count=count+delivery.getCount();
					while (delivery.moveToNext()) {

						SessionNo=delivery.getString(delivery.getColumnIndex(Database.ASessionRecieptNo));
						RecieptNo =delivery.getString(delivery.getColumnIndex(Database.ASessionDevice))+delivery.getString(delivery.getColumnIndex(Database.ASessionRecieptNo));
						ColDate = delivery.getString(delivery.getColumnIndex(Database.ASessionDate));
						Time= delivery.getString(delivery.getColumnIndex(Database.ASessionDate));
						BatchNo=delivery.getString(delivery.getColumnIndex(Database.ASessionBatchID));
						DataDevice =  mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
						if (delivery.getString(delivery.getColumnIndex(Database.ASessionAgentNo))==null){
							Agent="";
						}else{
							Agent=delivery.getString(delivery.getColumnIndex(Database.ASessionAgentNo));
						}
						WarehouseID=delivery.getString(delivery.getColumnIndex(Database.ASessionWarehouseID));
						ClerkID=delivery.getString(delivery.getColumnIndex(Database.ASessionClerkID));
						ProduceCode=delivery.getString(delivery.getColumnIndex(Database.ASessionProduceCode));
						VarietyCode=delivery.getString(delivery.getColumnIndex(Database.ASessionVarietyCode));
						GradeCode=delivery.getString(delivery.getColumnIndex(Database.ASessionGradeCode));
						Co_prefix=mSharedPrefs.getString("company_prefix", "").toString();
						Current_User=prefs.getString("user", "");

                       //ReceiptNo,CollDate,TerminalID,ClerkID,WarehouseID,AgentID,ProduceCode,VarietyCode,GradeCode,CompanyID,CurrectUserID
						StringBuilder sb = new StringBuilder();
						sb.append(RecieptNo + ",");
						sb.append(ColDate + ",");
						sb.append(DataDevice + ",");
						sb.append(ClerkID + ",");
						sb.append(WarehouseID + ",");
						sb.append(Agent + ",");
						sb.append(ProduceCode + ",");
						sb.append(VarietyCode + ",");
						sb.append(GradeCode + ",");
						sb.append(Co_prefix + ",");
						sb.append(Current_User);

						deliveryInfo = sb.toString();

						try {
							soapResponse = new SoapRequest(UploadAgentActivity.this).AddAgentDelivery(serverBatchNo, deliveryInfo);
							error=soapResponse;
							errorNo=prefs.getString("errorNo", "");
							/*if (Integer.valueOf(errorNo).intValue()<0) {
								return null;
							}*/

							try {
								if (Integer.valueOf(UploadAgentActivity.this.soapResponse).intValue()<0) {
									return null;
								}
								//System.out.println(value);}
							} catch (NumberFormatException e) {
								//value = 0; // your default value
								return null;
							}

							serverDeliveryNo = soapResponse;
						} catch (Exception e) {
							e.printStackTrace();
							returnValue = e.toString();
						}

						progressStatus++;
						publishProgress("" + progressStatus);


						Cursor weightrans =  db.rawQuery("select * from " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
								+ Database.ACollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "' and " + Database.AReceiptNo + " ='" + SessionNo + "' and " + Database.CloudID + " ='" + cloudid + "' and " + Database.AQuantity + " >'0'", null);
						if (weightrans.getCount() > 0) {

							count=count+weightrans.getCount();
							while (weightrans.moveToNext()) {
								Time= ColDate+" "+weightrans.getString(weightrans.getColumnIndex(Database.ACaptureTime));
								FarmerNo=weightrans.getString(weightrans.getColumnIndex(Database.AFarmerNo));
								//ProduceCode=weightrans.getString(weightrans.getColumnIndex(Database.ADeliveredProduce));
								NetWeight=weightrans.getString(weightrans.getColumnIndex(Database.AQuantity));
								TareWeight=weightrans.getString(weightrans.getColumnIndex(Database.ATareweight));
								UnitCount=weightrans.getString(weightrans.getColumnIndex(Database.ALoadCount));
								UnitPrice=weightrans.getString(weightrans.getColumnIndex(Database.AUnitPrice));
								Co_prefix=mSharedPrefs.getString("company_prefix", "").toString();
								Current_User=prefs.getString("user", "");


								//PieceCount,CaptureTime,FFarmerNo,Quantity,SetTare,UnitPrice,UserID
								StringBuilder wt = new StringBuilder();
								wt.append(UnitCount + ",");
								wt.append(Time + ",");
								wt.append(FarmerNo + ",");
								wt.append(NetWeight + ",");
								wt.append(TareWeight + ",");
								wt.append(UnitPrice + ",");
								wt.append(Current_User);
								weighmentInfo = wt.toString();

								try {
									soapResponse = new SoapRequest(UploadAgentActivity.this).AddAgentWeightRecord(serverDeliveryNo, weighmentInfo);
									error=soapResponse;
									try {
										if (Integer.valueOf(UploadAgentActivity.this.soapResponse).intValue()<0) {
											return null;
										}
										//System.out.println(value);}
									} catch (NumberFormatException e) {
										//value = 0; // your default value
										return null;
									}
									returnValue = soapResponse;
								} catch (Exception e) {
									e.printStackTrace();
									returnValue = e.toString();
								}

								progressStatus++;
								publishProgress("" + progressStatus);

							}


							weightrans.close();

						}
						else{

							//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

						}
					//delivery.close();

				}
				}
				else{

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}





			} catch (Exception e) {
				e.printStackTrace();
				returnValue = e.toString();
			}




				soapResponse = new SoapRequest(UploadAgentActivity.this).SignoffAgentsBatch(serverBatchNo, totalWeight);
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

			if(error.equals("-8080")){
				Toast.makeText(UploadAgentActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			try {

				if (Integer.valueOf(soapResponse).intValue() > 0) {
					returnValue = soapResponse;
					ContentValues values = new ContentValues();
					values.put(Database.BatCloudID, serverBatchNo);
					long rows = db.update(Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
							Database.DeliveryNoteNumber + " = ?", new String[]{DelNo});

					if (rows > 0) {

					}
					Toast.makeText(UploadAgentActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
					new Restart().execute();
					return;
				}
			} catch (NumberFormatException e) {
				errorNo=prefs.getString("errorNo", "");

				if(errorNo.equals("-1711"))
				{
					try {

						soapResponse = new SoapRequest(UploadAgentActivity.this).VerifyBatch(serverBatchNo, totalWeight);
						returnValue = soapResponse;

						ContentValues values = new ContentValues();
						values.put(Database.BatCloudID, 1);
						long rows = db.update(Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
								Database.DeliveryNoteNumber + " = ?", new String[]{DelNo});

						if (rows > 0) {

						}
						Toast.makeText(UploadAgentActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
						new Restart().execute();
					    }
						catch (NumberFormatException err) {
							if (Integer.valueOf(returnValue).intValue() < 0) {
								Toast.makeText(UploadAgentActivity.this, error, Toast.LENGTH_LONG).show();
								finish();
							}
						}



				}
				else
				{

				Toast.makeText(UploadAgentActivity.this, error, Toast.LENGTH_LONG).show();
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

			Cursor accounts = db.rawQuery("select * from " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
					+ Database.Closed + " ='" + closed1 + "' and " + Database.BatCloudID + " ='" + cloudid + "' and SignedOff=1", null);
			if (accounts.getCount() > 0) {

				String from [] = {  Database.ROW_ID,Database.DeliveryNoteNumber, Database.DataDevice ,
						Database.BatchNumber, Database.BatchDate,Database.NoOfWeighments, Database.TotalWeights};
				int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_reciept,R.id.tv_date,R.id.txtWeigments,R.id.txtTotalKgs};


				ca  = new SimpleCursorAdapter(this,R.layout.upload_list, accounts,from,to);

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



			mIntent = new Intent(getApplicationContext(),UploadAgentActivity.class);
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
			//mIntent = new Intent(UploadAgentActivity.this, UploadAgentDeliveryActivity.class);
			//startActivity(mIntent);
			Context context=getApplicationContext();
			LayoutInflater inflater=getLayoutInflater();
			View customToastroot =inflater.inflate(R.layout.white_red_toast, null);
			TextView text = (TextView) customToastroot.findViewById(R.id.toast);
			text.setText("No Batches Found To Upload");
			Toast customtoast = new Toast(context);
			customtoast.setView(customToastroot);
			customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
			customtoast.setDuration(Toast.LENGTH_LONG);
			customtoast.show();
		}
	}
}
