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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.modificator.waterwave_progress.WaterWaveProgress;

@SuppressWarnings("ALL")
public class UploadAllActivity extends AppCompatActivity {
	private String TAG = "Vik";
	public Toolbar toolbar;
	Button btnUpload,btnCancel;
	private String checkListReturnValue;
	private SharedPreferences mSharedPrefs;
	DBHelper dbhelper;
	SoapRequest request;
	String DeliveryInfo;
	String TicketNo,DNoteNo,DelDate,Factory,Transporter,Vehicle,ArrivalTime,FieldWt ,GrossWt,TareWt, 
			RejectWt,QualityScore, DepartureTime,CoPrefix,InternalSerial,UserIdentifier,CloudID;
	private String DeliveryNo;
	
	
	String batchInfo;
	String batchNo, deviceID, stringOpenDate, deliveryNoteNo, userID, userID2, stringOpenTime, weighingSession,
			closed, stringCloseTime, factory, tractorNo, trailerNo,TransporterCode,DelivaryNo,Co_prefix,Current_User,Quality;

	private int progressStatus = 0, count = 0;

	String weighmentInfo;
	String totalWeight;
	String ColDate, Time, DataDevice, BatchNo, Agent, FarmerNo;
	String WorkerNo, FieldClerk, ProduceCode;
	String VarietyCode, GradeCode, RouteCode, ShedCode;
	String  GrossTotal, TareWeight, UnitCount,BatchDte;
	String UnitPrice, RecieptNo, CanSerial, NetWeight, UsedCard;

	String serverBatchID;
	String shedCode;
	private String soapResponse, serverBatchNo;
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
	String BatchDate,BatchSerial;
	TextView textBatchNo, textBatchDate, textDelNo, textStatus;
	String cond;
	SearchView searchView;
	int closed1 = 1;
	int cloudid = 0;
	String DelNo;
	String error,errorNo,BatchDel;

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
		prefs = PreferenceManager.getDefaultSharedPreferences(UploadAllActivity.this);
		dbhelper = new DBHelper(getApplicationContext());
		db = dbhelper.getReadableDatabase();
		request = new SoapRequest(getApplicationContext());
		btnFilter = (Button) findViewById(R.id.btnFilter);
		btnFilter.setVisibility(View.GONE);
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
					Log.d("delivery", "Selected Account Id : " + textBatchNo.getText().toString());

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

		String selectQuery = "SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE SignedOff=1";
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.getCount() <= 0) {
			Toast.makeText(UploadAllActivity.this, "No Batch Dispatched to Upload!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		cursor.close();

	}


	private boolean checkList() {
		this.checkListReturnValue = XmlPullParser.NO_NAMESPACE;
		try {
			if (this.mSharedPrefs.getBoolean("cloudServices", false)) {
				try {
					if (this.mSharedPrefs.getString("internetAccessModes", null).toString().equals(null)) {
						Toast.makeText(UploadAllActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
						return false;

					}
						try {
							if (this.mSharedPrefs.getString("licenseKey", null).equals(null) || this.mSharedPrefs.getString("licenseKey", null).equals(XmlPullParser.NO_NAMESPACE)) {
								//this.checkListReturnValue = "License key not found!";
								Toast.makeText(UploadAllActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
								return false;
							}
							try {
								if (!this.mSharedPrefs.getString("portalURL", null).equals(null) && !this.mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
									return true;
								}
								//this.checkListReturnValue = "Portal URL not configured!";
								Toast.makeText(UploadAllActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
								return false;
							} catch (Exception e) {
								//this.checkListReturnValue = "Portal URL not configured!";
								Toast.makeText(UploadAllActivity.this, "Portal URL not configured!", Toast.LENGTH_LONG).show();
								return false;
							}
						} catch (Exception e2) {
							//this.checkListReturnValue = "License key not found!";
							Toast.makeText(UploadAllActivity.this, "License key not found!", Toast.LENGTH_LONG).show();
							return false;
						}

				} catch (Exception e3) {
					e3.printStackTrace();
					//this.checkListReturnValue = "Cloud Services not enabled!";
					Toast.makeText(UploadAllActivity.this, "Please Select Prefered Data Access Mode!", Toast.LENGTH_LONG).show();
					return false;
				}
			}
			Toast.makeText(UploadAllActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
			return false;


			//this.checkListReturnValue = "Cloud Services not enabled!";

		} catch (Exception e4) {
			e4.printStackTrace();
			//this.checkListReturnValue = "Cloud Services not enabled!";
			Toast.makeText(UploadAllActivity.this, "Cloud Services not enabled!", Toast.LENGTH_LONG).show();
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
				Cursor delivery = db.rawQuery("SELECT * FROM " + Database.Fmr_FactoryDeliveries + " where " + condition + "", null);
				count=delivery.getCount();
				if (delivery.getCount() > 0) {
					delivery.moveToFirst();
					while (!delivery.isAfterLast()) {

						TicketNo= delivery.getString(delivery.getColumnIndex(Database.FdWeighbridgeTicket));
						DNoteNo= delivery.getString(delivery.getColumnIndex(Database.FdDNoteNum));
						DelDate= delivery.getString(delivery.getColumnIndex(Database.FdDate));
						Factory= delivery.getString(delivery.getColumnIndex(Database.FdFactory));

						if (delivery.getString(delivery.getColumnIndex(Database.FdTransporter))==null){
							Transporter="";
						}else{
							Transporter=delivery.getString(delivery.getColumnIndex(Database.FdTransporter));
						}
						Vehicle= delivery.getString(delivery.getColumnIndex(Database.FdVehicle));
						ArrivalTime= delivery.getString(delivery.getColumnIndex(Database.FdArrivalTime));
						FieldWt= delivery.getString(delivery.getColumnIndex(Database.FdFieldWt));
						GrossWt= delivery.getString(delivery.getColumnIndex(Database.FdGrossWt));
						TareWt= delivery.getString(delivery.getColumnIndex(Database.FdTareWt));

						if (delivery.getString(delivery.getColumnIndex(Database.FdRejectWt)).equals("")){
							RejectWt="0";
						}
						else{
							RejectWt= delivery.getString(delivery.getColumnIndex(Database.FdRejectWt));
						}
						if (delivery.getString(delivery.getColumnIndex(Database.FdQualityScore)).equals("")){
							QualityScore="0";
						}else{
							QualityScore= delivery.getString(delivery.getColumnIndex(Database.FdQualityScore));
						}

						DepartureTime= delivery.getString(delivery.getColumnIndex(Database.FdDepartureTime));
						CloudID= delivery.getString(delivery.getColumnIndex(Database.CloudID));

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

						delivery.moveToNext();

						progressStatus++;
						publishProgress("" + progressStatus);
					}
					delivery.close();
					//request.createBatch(DeliveryInfo);
					UploadAllActivity.this.soapResponse = new SoapRequest(UploadAllActivity.this).createDelivery(DeliveryInfo);
					error=soapResponse;
					errorNo=prefs.getString("DelerrorNo", "");
					DeliveryNo=soapResponse;
					try{
						if (Integer.valueOf(UploadAllActivity.this.errorNo).intValue()<0) {

							DeliveryNo=CloudID;

						}
					} catch (NumberFormatException e) {

						DeliveryNo=soapResponse;
					}


				Cursor batches = db.rawQuery("SELECT * FROM " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " where "
						+ Database.DelivaryNO + " ='" + DelNo + "' and  " + Database.Closed + " = '" + closed1 + "'" +
						" and " + Database.BatCloudID + " = '" + cloudid + "'", null);
				count=batches.getCount();
				if (batches.getCount() > 0) {
					batches.moveToFirst();
					while (!batches.isAfterLast()) {
						totalWeight = batches.getString(batches.getColumnIndex(Database.TotalWeights));

						Date openTime = dateTimeFormat.parse(batches.getString(batches.getColumnIndex(Database.BatchDate)).toString() +
								" " +
								batches.getString(batches.getColumnIndex(Database.OpeningTime)).toString());
						Date closeTime = dateTimeFormat.parse(batches.getString(batches.getColumnIndex(Database.BatchDate)).toString() +
								" " +
								batches.getString(batches.getColumnIndex(Database.ClosingTime)).toString());
						batchNo = batches.getString(batches.getColumnIndex(Database.BatchNumber));
						deviceID = mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
						stringOpenDate = dateFormat.format(openTime);
						deliveryNoteNo = batches.getString(batches.getColumnIndex(Database.DeliveryNoteNumber));
						userID = batches.getString(batches.getColumnIndex(Database.Userid));
						stringOpenTime = timeFormat.format(openTime);
						if (batches.getString(batches.getColumnIndex(Database.BatchSession)) == null) {
							weighingSession = "1";
						} else {
							weighingSession = batches.getString(batches.getColumnIndex(Database.BatchSession));
						}
						closed = batches.getString(batches.getColumnIndex(Database.Closed));
						stringCloseTime = timeFormat.format(closeTime);
						factory = batches.getString(batches.getColumnIndex(Database.Factory));
						if (batches.getString(batches.getColumnIndex(Database.Transporter)) == null) {
							TransporterCode = "";
						} else {
							TransporterCode = batches.getString(batches.getColumnIndex(Database.Transporter));
						}
						tractorNo = batches.getString(batches.getColumnIndex(Database.Tractor));
						trailerNo = batches.getString(batches.getColumnIndex(Database.Trailer));

						if (batches.getString(batches.getColumnIndex(Database.DelivaryNO)) == null) {
							DelivaryNo = "";
						} else {
							DelivaryNo = batches.getString(batches.getColumnIndex(Database.DelivaryNO));
						}
						Co_prefix = mSharedPrefs.getString("company_prefix", "").toString();
						Current_User = prefs.getString("user", "");

						BatchSerial = batches.getString(batches.getColumnIndex(Database.DeliveryNoteNumber));

						StringBuilder batch = new StringBuilder();
						batch.append(batchNo + ",");
						batch.append(deviceID + ",");
						batch.append(stringOpenDate + ",");
						batch.append(deliveryNoteNo + ",");
						batch.append(userID + ",");
						batch.append(stringOpenTime + ",");
						batch.append(weighingSession + ",");
						batch.append(closed + ",");
						batch.append(stringCloseTime + ",");
						batch.append(factory + ",");
						batch.append(tractorNo + ",");
						batch.append(trailerNo + ",");
						batch.append(DelivaryNo + ",");
						batch.append(TransporterCode + ",");
						batch.append(Co_prefix + ",");
						batch.append(Current_User);
						batchInfo = batch.toString();

						batches.moveToNext();

						progressStatus++;
						publishProgress("" + progressStatus);
					
					//request.createBatch(batchInfo);
					UploadAllActivity.this.soapResponse = new SoapRequest(UploadAllActivity.this).createBatch(batchInfo);
					error=soapResponse;

					try {
						if (Integer.valueOf(error).intValue()<0) {
							error=soapResponse;
							return null;
						}
						//System.out.println(value);}
					} catch (NumberFormatException e) {
						//value = 0; // your default value

						return null;

					}

					serverBatchNo=soapResponse;

				//+ Database.CollDate + " ='" + BatchDate + "' and " + Database.BatchNumber + " ='" + BatchNo + "'

				Cursor weighments =  db.rawQuery("select * from " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + " WHERE "
						+ Database.BatchSerial + " ='" + BatchSerial + "'", null);
				if (weighments.getCount() > 0) {
					//weighments.moveToFirst();
					count=count+weighments.getCount();
					while (weighments.moveToNext()) {
						ColDate = weighments.getString(weighments.getColumnIndex(Database.CollDate));
						//ColDate = BatchDate;
						//Time= weighments.getString(weighments.getColumnIndex(Database.CollDate))+
						Time= weighments.getString(weighments.getColumnIndex(Database.CaptureTime));
						BatchNo=weighments.getString(weighments.getColumnIndex(Database.BatchNumber));
						DataDevice =  mSharedPrefs.getString("terminalID", XmlPullParser.NO_NAMESPACE);
						if (weighments.getString(weighments.getColumnIndex(Database.DataSource))==null){
							Agent="";
						}else{
							Agent=weighments.getString(weighments.getColumnIndex(Database.DataSource));
						}
						FarmerNo=weighments.getString(weighments.getColumnIndex(Database.FarmerNo));
						WorkerNo=weighments.getString(weighments.getColumnIndex(Database.WorkerNo));
						userID2=weighments.getString(weighments.getColumnIndex(Database.FieldClerk));
						ProduceCode=weighments.getString(weighments.getColumnIndex(Database.DeliveredProduce));
						VarietyCode=weighments.getString(weighments.getColumnIndex(Database.ProduceVariety));
						GradeCode=weighments.getString(weighments.getColumnIndex(Database.ProduceGrade));
						RouteCode=weighments.getString(weighments.getColumnIndex(Database.SourceRoute));
						ShedCode=weighments.getString(weighments.getColumnIndex(Database.BuyingCenter));
						NetWeight=weighments.getString(weighments.getColumnIndex(Database.Quantity));
						TareWeight=weighments.getString(weighments.getColumnIndex(Database.Tareweight));
						UnitCount=weighments.getString(weighments.getColumnIndex(Database.LoadCount));
						UnitPrice=weighments.getString(weighments.getColumnIndex(Database.UnitPrice));
						CanSerial = weighments.getString(weighments.getColumnIndex(Database.Container));
						RecieptNo =weighments.getString(weighments.getColumnIndex(Database.DataCaptureDevice))+weighments.getString(weighments.getColumnIndex(Database.ReceiptNo));
						UsedCard=weighments.getString(weighments.getColumnIndex(Database.UsedSmartCard));
						Co_prefix=mSharedPrefs.getString("company_prefix", "").toString();
						Current_User=prefs.getString("user", "");

						if (weighments.getString(weighments.getColumnIndex(Database.Quality)).equals("0")){
							Quality=QualityScore;
						}else{
							Quality=weighments.getString(weighments.getColumnIndex(Database.Quality));
						}

						//TransporterCode=weighments.getString(weighments.getColumnIndex(Database.Transporter));
						//Accummulated=weighments.getString(weighments.getColumnIndex(Database.DataCaptureDevice));
						//UnitCount=weighments.getString(weighments.getColumnIndex(Database.LoadCount));
						//GrossTotal=weighments.getString(weighments.getColumnIndex(Database.DataCaptureDevice));
						//,2016-01-27,123,11:05,ODS,1,Kk1,1,13,2,10004,0.8,1.5,,1230000004,3,,,0

						StringBuilder data = new StringBuilder();
						data.append(ColDate + ",");
						data.append(DataDevice + ",");
						data.append(Time + ",");
						data.append(userID2 + ",");
						data.append(ProduceCode + ",");
						data.append(RouteCode + ",");
						data.append(ShedCode + ",");
						data.append(FarmerNo + ",");
						data.append(NetWeight + ",");
						data.append(TareWeight + ",");
						data.append(UnitCount + ",");
						data.append(CanSerial + ",");
						data.append(RecieptNo + ",");
						data.append(UsedCard + ",");
						data.append(VarietyCode + ",");
						data.append(GradeCode + ",");
						data.append(UnitPrice + ",");
						data.append(Co_prefix + ",");
						data.append(Agent + ",");
						data.append(Current_User + ",");
						data.append(Quality);

						weighmentInfo = data.toString();

						try {
							soapResponse = new SoapRequest(UploadAllActivity.this).postWeighment(serverBatchNo, weighmentInfo);
							error=soapResponse;
							if (Integer.valueOf(UploadAllActivity.this.soapResponse).intValue()<0) {
								return null;
							}
							returnValue = soapResponse;
						} catch (NumberFormatException e) {
							e.printStackTrace();
							returnValue = e.toString();
						}

						progressStatus++;
						publishProgress("" + progressStatus);

					}
					weighments.close();
				}
				else{

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}
					}
					batches.close();

					soapResponse = new SoapRequest(UploadAllActivity.this).signOffBatch(serverBatchNo, totalWeight);
					error=soapResponse;
				}
				else{
					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();
				}

				}
				else{

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}

				Cursor batchdel =  db.rawQuery("select * from " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + " WHERE "
						+ Database.DelivaryNO + " ='" + DelNo + "'", null);
				if (batchdel.getCount() > 0) {
					//batchdel.moveToFirst();
					count=count+batchdel.getCount();
					while (batchdel.moveToNext()) {
						deliveryNoteNo = batchdel.getString(batchdel.getColumnIndex(Database.DeliveryNoteNumber));

						StringBuilder sb = new StringBuilder();

						sb.append(deliveryNoteNo);

						BatchDel = sb.toString();

						try {
							soapResponse = new SoapRequest(UploadAllActivity.this).DeliverBatch(DeliveryNo, BatchDel);
							error=soapResponse;
							errorNo=prefs.getString("DelerrorNo", "");

							try{
								if (Integer.valueOf(UploadAllActivity.this.errorNo).intValue()<0) {
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
					batchdel.close();

				}
				else{

					//Toast.makeText(this, "No Records", Toast.LENGTH_LONG).show();

				}



			} catch (Exception e) {
				e.printStackTrace();
				returnValue = e.toString();
			}


				soapResponse = new SoapRequest(UploadAllActivity.this).SignoffDelivery(DeliveryNo);
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

		try {

				if (Integer.valueOf(soapResponse).intValue() > 0) {
					returnValue = soapResponse;
					ContentValues values = new ContentValues();
					values.put(Database.CloudID, DeliveryNo);
					long rows = db.update(Database.Fmr_FactoryDeliveries, values,
							Database.FdDNoteNum + " = ?", new String[]{DelNo});

					if (rows > 0) {

					}
					Toast.makeText(UploadAllActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
					new Restart().execute();
					return;
				}
			} catch (NumberFormatException e) {
				errorNo=prefs.getString("DelerrorNo", "");

				if(error.equals("-8080")){
					Toast.makeText(UploadAllActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				else
				{
					Toast.makeText(UploadAllActivity.this, error, Toast.LENGTH_LONG).show();
					finish();
				}
			}

			/*if(error.equals("-8080")){
				Toast.makeText(UploadAllActivity.this, "Server Not Available !!", Toast.LENGTH_LONG).show();
				finish();
				return;
			}

			try {

				if (Integer.valueOf(soapResponse).intValue() > 0) {
					returnValue = soapResponse;
					ContentValues values = new ContentValues();
					values.put(Database.BatCloudID, serverBatchNo);
					long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
							Database.DeliveryNoteNumber + " = ?", new String[]{DelNo});

					if (rows > 0) {

					}
					Toast.makeText(UploadAllActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
					new Restart().execute();
					return;
				}
			} catch (NumberFormatException e) {
				errorNo=prefs.getString("errorNo", "");
				if(errorNo.equals("-1011"))
				{
					try {

						soapResponse = new SoapRequest(UploadAllActivity.this).VerifyBatch(serverBatchNo, totalWeight);
						returnValue = soapResponse;

						ContentValues values = new ContentValues();
						values.put(Database.BatCloudID, 1);
						long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
								Database.DeliveryNoteNumber + " = ?", new String[]{DelNo});

						if (rows > 0) {

						}
						Toast.makeText(UploadAllActivity.this, "Data Uploaded Successfully !!!", Toast.LENGTH_LONG).show();
						new Restart().execute();
					    }
						catch (NumberFormatException err) {
							if (Integer.valueOf(returnValue).intValue() < 0) {
								Toast.makeText(UploadAllActivity.this, error, Toast.LENGTH_LONG).show();
								finish();
							}
						}



				}
				else
				{

				Toast.makeText(UploadAllActivity.this, error, Toast.LENGTH_LONG).show();
					finish();
				}
			}*/


			//b.dismiss();
		}

	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onStart() {
		super.onStart();
		getdata();
		/*ContentValues values = new ContentValues();
		values.put(Database.CloudID, 0);
		long rows = db.update(Database.Fmr_FactoryDeliveries, values,
				Database.FdDNoteNum + " = ?", new String[]{"27022018D0002"});*/

		/*ContentValues values = new ContentValues();
		values.put(Database.BatCloudID, 0);
		long rows = db.update(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, values,
				Database.DeliveryNoteNumber + " = ?", new String[]{"WK052018022702"});*/
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void getdata(){

		try {

			SQLiteDatabase db= dbhelper.getReadableDatabase();

			Cursor delivery = db.rawQuery("select * from " + Database.Fmr_FactoryDeliveries + " WHERE "
					+ Database.CloudID + " ='" + cloudid + "'", null);
			if (delivery.getCount() > 0) {

				String from [] = {  Database.ROW_ID,Database.FdDNoteNum, Database.FdWeighbridgeTicket , Database.FdDate, Database.FdFieldWt};
				int to [] = { R.id.txtAccountId,R.id.tv_number,R.id.tv_device,R.id.tv_date,R.id.txtTotalKgs};


				ca  = new SimpleCursorAdapter(this,R.layout.upload_delivery_list, delivery,from,to);

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



			mIntent = new Intent(getApplicationContext(),UploadAllActivity.class);
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
