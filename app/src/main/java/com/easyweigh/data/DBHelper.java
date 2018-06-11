package com.easyweigh.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import java.util.HashMap;



public class DBHelper extends SQLiteOpenHelper {
	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "Easyweigh.db";

   
	public DBHelper(Context ctx) {
		super(ctx, DB_NAME, null, DB_VERSION);
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
          createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}

	public void createTables(SQLiteDatabase database) {

		// Company Table
		String company_table_sql = "create table " + Database.COMPANY_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.CO_PREFIX + " TEXT," +
				Database.CO_NAME + " TEXT," +
				Database.CO_LETTERBOX + " TEXT," +
				Database.CO_POSTCODE + " TEXT," +
				Database.CO_POSTNAME + " TEXT," +
				Database.CO_POSTREGION + " TEXT," +
				Database.CO_TELEPHONE + " TEXT," +
				Database.CO_ClOUDID + " TEXT)";

		//Factory Table
		String factory_table_sql = "create table " + Database.FACTORY_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.FRY_PREFIX + " TEXT," +
				Database.FRY_TITLE  + " TEXT," +
				Database.FRY_ClOUDID + " TEXT)";

		// Zones Table
		String zones_table_sql = "create table " + Database.ZONES_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.FZ_CODE + " TEXT," +
				Database.FZ_NAME + " TEXT," +
				Database.FZ_ClOUDID + " TEXT)";

		// Routes Table
		String routes_table_sql = "create table " + Database.ROUTES_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.MC_RCODE + " TEXT," +
				Database.MC_RNAME + " TEXT," +
				Database.MC_RClOUDID + " TEXT)";

		//CollectionCenters Table
		String collectioncenters_table_sql = "create table " + Database.COLLECTIONCENTERS_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.MC_CNO + " TEXT," +
				Database.MC_CNAME + " TEXT," +
				Database.MC_CZONE + " TEXT," +
				Database.MC_CROUTE + " TEXT," +
				Database.MC_CClOUDID + " TEXT)";

		//Farmers Table
		String farmers_table_sql = "create table " + Database.FARMERS_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.F_FARMERNO + " TEXT," +
				Database.F_CARDNUMBER + " TEXT," +
				Database.F_NATIONALID + " TEXT," +
				Database.F_MOBILENUMBER + " TEXT," +
				Database.F_FARMERNAME + " TEXT," +
				Database.F_SHED + " TEXT," +
				Database.F_MANAGEDFARM + " TEXT," +
				Database.F_PRODUCE_KG_TODATE + " TEXT," +
				Database.F_CLOUDID + " TEXT)";


		//OperatorsMaster Table
		String operators_master_table_sql = "create table " + Database.OPERATORSMASTER_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.USERIDENTIFIER + " TEXT," +
				Database.CLERKNAME + " TEXT," +
				Database.ACCESSLEVEL + " TEXT," +
				Database.USERPWD + " TEXT," +
				Database.USERCLOUDID + " TEXT)";

		//Produce Table
		String produce_table_sql = "create table " + Database.PRODUCE_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.MP_CODE + " TEXT," +
				Database.MP_DESCRIPTION + " TEXT," +
				Database.MP_RETAILPRICE + " FLOAT," +
				Database.MP_SALESTAX + " FLOAT," +
				Database.MP_CLOUDID + " TEXT)";

		//ProduceGrades Table
		String producegrades_table_sql = "create table " + Database.PRODUCEGRADES_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.PG_DREF + " TEXT," +
				Database.PG_DNAME + " TEXT," +
				Database.PG_DPRODUCE + " TEXT," +
				Database.PG_RETAILPRICE + " FLOAT," +
				Database.PG_SALESTAX + " FLOAT," +
				Database.PG_DCLOUDID + " TEXT)";

		//ProduceVarieties Table
		String producevarieties_table_sql = "create table " + Database.PRODUCEVARIETIES_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.VRT_REF + " TEXT," +
				Database.VRT_NAME + " TEXT," +
				Database.VRT_PRODUCE + " TEXT," +
				Database.VRT_RETAILPRICE + " FLOAT," +
				Database.VRT_SALESTAX + " FLOAT," +
				Database.VRT_CLOUDID + " TEXT)";

		//Farms Workers Table
		String farm_workers_table_sql = "create table " + Database.WORKERS_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.FW_PFNO + " TEXT," +
				Database.FW_CARDNUMBER + " TEXT," +
				Database.FW_NATIONALID + " TEXT," +
				Database.FW_MOBILENUMBER + " TEXT," +
				Database.FW_EMPLOYEENAME + " TEXT," +
				Database.FW_ATTACHEDFARM + " TEXT," +
				Database.FW_CLOUDID + " TEXT)";

		//FarmersSuppliesConsignments Table
		String farmerssuppliesconsignments_table_sql = "create table " + Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.DataDevice + " TEXT," +
				Database.Userid + " TEXT," +
				Database.BatchDate + " TEXT," +
				Database.OpeningTime + " TEXT," +
				Database.BatchCount + " TEXT," +
				Database.BatchNumber + " TEXT," +
				Database.BatchSession + " TEXT," +
				Database.NoOfWeighments + " TEXT," +
				Database.TotalWeights + " TEXT," +
				Database.Closed + " TEXT," +
				Database.ClosingTime + " TEXT," +
				Database.Dispatched + " TEXT," +
				Database.DeliveryNoteNumber + " TEXT," +
				Database.Factory + " TEXT," +
				Database.Transporter + " TEXT," +
				Database.Tractor + " TEXT," +
				Database.Trailer + " TEXT," +
				Database.DelivaryNO + " TEXT," +
				Database.SignedOff + " TEXT," +
				Database.SignedOffTime + " TEXT," +
				Database.BatCloudID + " TEXT)";


          //Session Table
		String session_table_sql = "create table " + Database.SESSION_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.SessionDate + " TEXT," +
				Database.SessionTime + " TEXT," +
				Database.SessionDevice + " TEXT," +
				Database.SessionFarmerNo + " TEXT," +
				Database.SessionBags + " TEXT," +
				Database.SessionNet + " TEXT," +
				Database.SessionTare + " TEXT," +
				Database.SessionRoute + " TEXT," +
				Database.SessionCounter + " TEXT," +
				Database.SessionReceipt + " TEXT)";

		// Delivery Table
		String Delivery_table_sql = "create table " + Database.Fmr_FactoryDeliveries + " ( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.FdWeighbridgeTicket + " TEXT," +
				Database.FdDNoteNum + " TEXT," +
				Database.FdDate + " TEXT," +
				Database.FdFactory + " TEXT," +
				Database.FdTransporter + " TEXT," +
				Database.FdVehicle + " TEXT," +
				Database.FdFieldWt + " TEXT," +
				Database.FdArrivalTime + " TEXT," +
				Database.FdGrossWt + " FLOAT," +
				Database.FdTareWt + " FLOAT," +
				Database.FdRejectWt + " FLOAT," +
				Database.FdQualityScore + " FLOAT," +
				Database.FdDepartureTime + " TEXT," +
				Database.FdStatus + " TEXT," +
				Database.CloudID + " TEXT)";



		//FarmersProduceCollection Table
		String farmersproducecollection_table_sql = "create table " + Database.FARMERSPRODUCECOLLECTION_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.CollDate + " TEXT," +
				Database.CaptureTime + " TEXT," +
				Database.DataCaptureDevice + " TEXT," +
				Database.BatchNumber + " TEXT," +
				Database.DataSource + " TEXT," +
				Database.FarmerNo + " TEXT," +
				Database.WorkerNo + " TEXT," +
				Database.FieldClerk + " TEXT," +
				Database.DeliveredProduce + " TEXT," +
				Database.ProduceVariety + " TEXT," +
				Database.ProduceGrade + " TEXT," +
				Database.SourceRoute + " TEXT," +
				Database.BuyingCenter + " TEXT," +
				Database.Quantity + " FLOAT," +
				Database.Tareweight + " FLOAT," +
				Database.LoadCount + " TEXT," +
				Database.UnitPrice + " FLOAT," +
				Database.ReceiptNo + " TEXT," +
				Database.ReferenceNo + " TEXT," +
				Database.BatchSerial + " TEXT," +
				Database.Quality + " TEXT," +
				Database.Container + " TEXT," +
				Database.UsedSmartCard + " TEXT," +
				Database.CloudID + " TEXT)";

		// Inventory Table
		String Inventory_table_sql = "create table " + Database.Inventory + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.Product_Code + " TEXT," +
				Database.Product_Name + " TEXT," +
				Database.Unit_Type + " TEXT," +
				Database.Unit_Price + " TEXT," +
				Database.SaleTaxRate + " TEXT," +
				Database.CloudID + " TEXT)";

		// Account Customers Table
		String AccountCustomers_table_sql = "create table " + Database.AccountCustomers + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.C_CUSTOMERNO + " TEXT," +
				Database.C_CUSTOMERNAME + " TEXT," +
				Database.C_MOBILENUMBER + " TEXT," +
				Database.C_EMAIL + " TEXT," +
				Database.C_ADDRESS + " TEXT)";

		// Farm Inputs Order Table
		String FarmInputOrders_table_sql = "create table " + Database.FarmInputOrders + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.Farmer_No + " TEXT," +
				Database.OrderDate + " TEXT," +
				Database.OrderTime + " TEXT," +
				Database.ClerkID + " TEXT," +
				Database.TerminalID + " TEXT," +
				Database.PaymentTerms + " TEXT," +
				Database.TotalSale + " TEXT," +
				Database.SalesTax + " TEXT," +
				Database.TotalAmount + " TEXT," +
				Database.CashPaid + " TEXT," +
				Database.CashChange + " TEXT," +
				Database.OrdCloudId + " TEXT)";

		// FarmInputSales Table
		String FarmInputSales_table_sql = "create table " + Database.FarmInputSales + " ( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.OrderNo + " TEXT," +
				Database.ItemCode + " TEXT," +
				Database.Units + " TEXT," +
				Database.UPrice + " TEXT," +
				Database.ItemCost + " TEXT," +
				Database.SaleTax + " TEXT," +
				Database.Amount + " TEXT," +
				Database.lnCloudID + " TEXT)";

		// ProduceSales Table
		String ProduceSales_table_sql = "create table " + Database.ProduceSales + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.CustomerNo + " TEXT," +
				Database.SaleDate + " TEXT," +
				Database.SaleTime + " TEXT," +
				Database.ClerkID + " TEXT," +
				Database.TerminalID + " TEXT," +
				Database.Produce + " TEXT," +
				Database.Pvariety + " TEXT," +
				Database.Pgrade + " TEXT," +
				Database.DataSource + " TEXT," +
				Database.Quantity + " TEXT," +
				Database.UPrice + " TEXT," +
				Database.SaleValue + " TEXT," +
				Database.SaleTax + " TEXT," +
				Database.Amount + " TEXT," +
				Database.PaymentTerms + " TEXT," +
				Database.CashPaid + " TEXT," +
				Database.CashChange + " TEXT," +
				Database.CloudID + " TEXT)";

		//Agent Table
		String agent_table_sql = "create table " + Database.AGENT_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.AGT_ID + " TEXT," +
				Database.AGT_NAME + " TEXT," +
				Database.CloudID + " TEXT)";

		//Warehouse Table
		String warehouse_table_sql = "create table " + Database.WAREHOUSE_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.WH_ID + " TEXT," +
				Database.WH_NAME + " TEXT," +
				Database.CloudID + " TEXT)";

		// Agent Session Table
		String agent_session_table_sql = "create table " + Database.ASESSION_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.ASessionDate + " TEXT," +
				Database.ASessionTime + " TEXT," +
				Database.ASessionDevice + " TEXT," +
				Database.ASessionAgentNo + " TEXT," +
				Database.ASessionBags + " TEXT," +
				Database.ASessionNet + " TEXT," +
				Database.ASessionTare + " TEXT," +
				Database.ASessionRecieptNo + " TEXT," +
				Database.ASessionBatchID + " TEXT," +
				Database.ASessionClerkID + " TEXT," +
				Database.ASessionWarehouseID + " TEXT," +
				Database.ASessionProduceCode + " TEXT," +
				Database.ASessionVarietyCode + " TEXT," +
				Database.ASessionGradeCode + " TEXT," +
				Database.CloudID + " TEXT)";

		// Agent Delivery Table
		String AgtDelivery_table_sql = "create table " + Database.Agt_FactoryDeliveries + " ( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.FdWeighbridgeTicket + " TEXT," +
				Database.FdDNoteNum + " TEXT," +
				Database.FdDate + " TEXT," +
				Database.FdFactory + " TEXT," +
				Database.FdTransporter + " TEXT," +
				Database.FdVehicle + " TEXT," +
				Database.FdFieldWt + " TEXT," +
				Database.FdArrivalTime + " TEXT," +
				Database.FdGrossWt + " FLOAT," +
				Database.FdTareWt + " FLOAT," +
				Database.FdRejectWt + " FLOAT," +
				Database.FdQualityScore + " FLOAT," +
				Database.FdDepartureTime + " TEXT," +
				Database.FdStatus + " TEXT," +
				Database.CloudID + " TEXT)";
		//AgentsSuppliesConsignments Table
		String agentsuppliesconsignments_table_sql = "create table " + Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.DataDevice + " TEXT," +
				Database.Userid + " TEXT," +
				Database.BatchDate + " TEXT," +
				Database.OpeningTime + " TEXT," +
				Database.BatchCount + " TEXT," +
				Database.BatchNumber + " TEXT," +
				Database.BatchSession + " TEXT," +
				Database.NoOfWeighments + " TEXT," +
				Database.TotalWeights + " TEXT," +
				Database.Closed + " TEXT," +
				Database.ClosingTime + " TEXT," +
				Database.Dispatched + " TEXT," +
				Database.DeliveryNoteNumber + " TEXT," +
				Database.Factory + " TEXT," +
				Database.Transporter + " TEXT," +
				Database.Tractor + " TEXT," +
				Database.Trailer + " TEXT," +
				Database.DelivaryNO + " TEXT," +
				Database.SignedOff + " TEXT," +
				Database.SignedOffTime + " TEXT," +
				Database.BatCloudID + " TEXT)";

		//AgentsProduceCollection Table
		String agentsproducecollection_table_sql = "create table " + Database.AGENTSPRODUCECOLLECTION_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.ACollDate + " TEXT," +
				Database.ACaptureTime + " TEXT," +
				Database.ADataCaptureDevice + " TEXT," +
				Database.BatchNumber + " TEXT," +
				Database.ADataSource + " TEXT," +
				Database.AFarmerNo + " TEXT," +
				Database.AgentNo + " TEXT," +
				Database.ADeliveredProduce + " TEXT," +
				Database.AQuantity + " FLOAT," +
				Database.ATareweight + " FLOAT," +
				Database.ALoadCount + " TEXT," +
				Database.AUnitPrice + " FLOAT," +
				Database.AReceiptNo + " TEXT," +
				Database.AContainer + " TEXT," +
				Database.AUsedSmartCard + " TEXT," +
				Database.CloudID + " TEXT)";

		//Transporter Table
		String transporter_table_sql = "create table " + Database.TRANSPORTER_TABLE_NAME + "( " +
				Database.ROW_ID + " integer  primary key autoincrement," +
				Database.TPT_ID + " TEXT," +
				Database.TPT_NAME + " TEXT," +
				Database.CloudID + " TEXT)";

		String DefaultUsers = "INSERT INTO " + Database.OPERATORSMASTER_TABLE_NAME + " ("
				+ Database.USERIDENTIFIER + ", "
				+ Database.CLERKNAME + ", "
				+ Database.USERPWD+ ", "
				+ Database.ACCESSLEVEL + ") Values ('OCTAGON', 'ODS', '1234', '1')";

		String DefaultWarehouse = "INSERT INTO " + Database.WAREHOUSE_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.WH_NAME + ") Values ('0', 'Select ...')";

		String DefaultAgent = "INSERT INTO " + Database.AGENT_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.AGT_NAME + ") Values ('0', 'Select ...')";
		String DefaultProduce = "INSERT INTO " + Database.PRODUCE_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.MP_DESCRIPTION + ") Values ('0', 'Select ...')";
		String DefaultVariety = "INSERT INTO " + Database.PRODUCEVARIETIES_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.VRT_NAME + ") Values ('0', 'Select ...')";
		String DefaultGrade = "INSERT INTO " + Database.PRODUCEGRADES_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.PG_DNAME + ") Values ('0', 'Select ...')";
		String DefaultTransporter = "INSERT INTO " + Database.TRANSPORTER_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.TPT_NAME + ") Values ('0', 'Select ...')";
		String DefaultZone = "INSERT INTO " + Database.ZONES_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.FZ_NAME + ") Values ('0', 'Select ...')";
		String DefaultRoute = "INSERT INTO " + Database.ROUTES_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.MC_RNAME + ") Values ('0', 'Select ...')";
		String DefaultShed = "INSERT INTO " +Database.COLLECTIONCENTERS_TABLE_NAME + " ("
				+ Database.ROW_ID + ", "
				+ Database.MC_CNAME + ") Values ('0', 'Select ...')";




		try {
			database.execSQL(company_table_sql);
			database.execSQL(factory_table_sql);
			database.execSQL(zones_table_sql);
			database.execSQL(routes_table_sql);
			database.execSQL(collectioncenters_table_sql);
			database.execSQL(farmers_table_sql);
			database.execSQL(operators_master_table_sql);
			database.execSQL(produce_table_sql);
			database.execSQL(producegrades_table_sql);
			database.execSQL(producevarieties_table_sql);
			database.execSQL(farm_workers_table_sql);
			database.execSQL(farmersproducecollection_table_sql);
			database.execSQL(farmerssuppliesconsignments_table_sql);
			database.execSQL(Delivery_table_sql);
			database.execSQL(Inventory_table_sql);
			database.execSQL(FarmInputSales_table_sql);
			database.execSQL(FarmInputOrders_table_sql);
			database.execSQL(AccountCustomers_table_sql);
			database.execSQL(ProduceSales_table_sql);
			database.execSQL(agent_table_sql);
			database.execSQL(transporter_table_sql);
			database.execSQL(session_table_sql);

			database.execSQL(warehouse_table_sql);
			database.execSQL(agent_session_table_sql);
			database.execSQL(agentsproducecollection_table_sql);
			database.execSQL(agentsuppliesconsignments_table_sql);
			database.execSQL(AgtDelivery_table_sql);

			Log.d("EasyweighDB", "Tables created!");
			//Defaults
			database.execSQL(DefaultUsers);
			database.execSQL(DefaultAgent);
			database.execSQL(DefaultProduce);
			database.execSQL(DefaultVariety);
			database.execSQL(DefaultGrade);
			database.execSQL(DefaultTransporter);
			database.execSQL(DefaultZone);
			database.execSQL(DefaultRoute);
			database.execSQL(DefaultShed);

			database.execSQL(DefaultWarehouse);



		}
		catch(Exception ex) {
			Log.d("EasyweighDB", "Error in DBHelper.onCreate() : " + ex.getMessage());
		}
	}


	public long AddCompanyDetails(String co_prefix, String co_name, String co_letterbox,String co_postcode, String co_postname, String co_postregion,String co_telephone) {
		SQLiteDatabase db = this.getReadableDatabase();
		HashMap<String, String> queryValues = new HashMap<String, String>();
		queryValues.put("co_prefix", co_prefix);
		queryValues.put("co_name", co_name);
		queryValues.put("co_letterbox",co_letterbox);
		queryValues.put("co_postcode", co_postcode);
		queryValues.put("co_postname", co_postname);
		queryValues.put("co_posregion", co_postregion);
		queryValues.put("co_telephone", co_telephone);

		
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.CO_PREFIX, queryValues.get("co_prefix"));
		initialValues.put(Database.CO_NAME, queryValues.get("co_name"));
		initialValues.put(Database.CO_LETTERBOX, queryValues.get("co_letterbox"));
		initialValues.put(Database.CO_POSTCODE, queryValues.get("co_postcode"));
		initialValues.put(Database.CO_POSTNAME, queryValues.get("co_postname"));
		initialValues.put(Database.CO_POSTREGION, queryValues.get("co_postregion"));
		initialValues.put(Database.CO_TELEPHONE, queryValues.get("co_telephone"));
		
		return db.insert(Database.COMPANY_TABLE_NAME, null, initialValues);

	}

	/////////////////////////////////////////////////////////////////////
	//USERS FUNCTIONS///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddUsers(String s_etFullName,String s_etNewUserId,String s_etPassword,String s_spUserLevel) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.USERIDENTIFIER, s_etFullName);
		initialValues.put(Database.CLERKNAME, s_etNewUserId);
		initialValues.put(Database.USERPWD, s_etPassword);
		initialValues.put(Database.ACCESSLEVEL, s_spUserLevel);

		return db.insert(Database.OPERATORSMASTER_TABLE_NAME, null, initialValues);

	}

	public Cursor fetchUsername(String username) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.OPERATORSMASTER_TABLE_NAME,
				new String[]{"_id", "ClerkName"},
				"ClerkName" + "='" + username + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	public boolean UserLogin(String username, String password)  {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor mCursor = db.rawQuery("SELECT * FROM " + Database.OPERATORSMASTER_TABLE_NAME
				+ " WHERE ClerkName=? COLLATE NOCASE AND UserPwd=?", new String[]{username, password});
		if (mCursor != null) {
			if (mCursor.getCount() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * cursor for viewing access level
	 */
	public Cursor getAccessLevel(String username) {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] allColumns = new String[] {Database.ACCESSLEVEL,Database.USERIDENTIFIER};
		Cursor c = db.query(Database.OPERATORSMASTER_TABLE_NAME, allColumns, "ClerkName COLLATE NOCASE" + "='" + username + "'", null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	/**
	 * cursor for viewing password
	 */
	public Cursor getPassword(String username) {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] allColumns = new String[] {Database.USERPWD,Database.USERIDENTIFIER};
		Cursor c = db.query(Database.OPERATORSMASTER_TABLE_NAME, allColumns, "ClerkName COLLATE NOCASE" + "='" + username + "'", null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	/////////////////////////////////////////////////////////////////////
	//FACTORY FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddFactories(String s_fryprefix,String s_fryname) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.FRY_PREFIX, s_fryprefix);
		initialValues.put(Database.FRY_TITLE, s_fryname);
		return db.insert(Database.FACTORY_TABLE_NAME, null, initialValues);

	}
	public Cursor CheckFactory(String s_fryprefix) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.FACTORY_TABLE_NAME,
				new String[]{"_id", Database.FRY_PREFIX},
				Database.FRY_PREFIX + "='" + s_fryprefix + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	/////////////////////////////////////////////////////////////////////
	//PRODUCE FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddProduce(String s_etDgProduceCode,String s_etDgProduceTitle) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.MP_CODE, s_etDgProduceCode);
		initialValues.put(Database.MP_DESCRIPTION, s_etDgProduceTitle);
		return db.insert(Database.PRODUCE_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckProduce(String mp_code) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.PRODUCE_TABLE_NAME,
				new String[] { "_id", Database.MP_CODE },
				Database.MP_CODE + "='" + mp_code + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	/////////////////////////////////////////////////////////////////////
	//VARIETY FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddVariety(String s_etDgProduceCode,String s_etDgProduceTitle,String produceid) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.VRT_REF, s_etDgProduceCode);
		initialValues.put(Database.VRT_NAME, s_etDgProduceTitle);
		initialValues.put(Database.VRT_PRODUCE, produceid);
		return db.insert(Database.PRODUCEVARIETIES_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckVariety(String mp_code) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.PRODUCEVARIETIES_TABLE_NAME,
				new String[] { "_id", Database.VRT_REF },
				Database.VRT_REF + "='" + mp_code + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	/////////////////////////////////////////////////////////////////////
	//GRADE FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddGrade(String s_etDgProduceCode,String s_etDgProduceTitle,String produceid) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.PG_DREF, s_etDgProduceCode);
		initialValues.put(Database.PG_DNAME, s_etDgProduceTitle);
		initialValues.put(Database.PG_DPRODUCE, produceid);
		return db.insert(Database.PRODUCEGRADES_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckGrade(String mp_code) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.PRODUCEGRADES_TABLE_NAME,
				new String[] { "_id", Database.PG_DREF },
				Database.PG_DREF + "='" + mp_code + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	/////////////////////////////////////////////////////////////////////
	//ZONE FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddZones(String s_fzcode,String s_fzname) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.FZ_CODE, s_fzcode);
		initialValues.put(Database.FZ_NAME, s_fzname);
		return db.insert(Database.ZONES_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckZone(String fz_code) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.ZONES_TABLE_NAME,
				new String[] { "_id", Database.FZ_CODE },
				Database.FZ_CODE + "='" + fz_code + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}

	/////////////////////////////////////////////////////////////////////
	//AGENT FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddAgent(String s_agtID,String s_agtName) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.AGT_ID, s_agtID);
		initialValues.put(Database.AGT_NAME, s_agtName);
		return db.insert(Database.AGENT_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckAgent(String agtID) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.AGENT_TABLE_NAME,
				new String[] { "_id", Database.AGT_ID },
				Database.AGT_ID + "='" + agtID + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}

	/////////////////////////////////////////////////////////////////////
	//WAREHOUSE FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddWarehouse(String s_whID,String s_whName) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.WH_ID, s_whID);
		initialValues.put(Database.WH_NAME, s_whName);
		return db.insert(Database.WAREHOUSE_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckWarehouse(String whID) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.WAREHOUSE_TABLE_NAME,
				new String[] { "_id", Database.WH_ID },
				Database.WH_ID + "='" + whID + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}

	/////////////////////////////////////////////////////////////////////
	//TRANSPORTER FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddTransporter(String s_tptID,String s_tptName) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.TPT_ID, s_tptID);
		initialValues.put(Database.TPT_NAME, s_tptName);
		return db.insert(Database.TRANSPORTER_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckTransporter(String tptID) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.TRANSPORTER_TABLE_NAME,
				new String[] { "_id", Database.TPT_ID },
				Database.TPT_ID + "='" + tptID + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	/////////////////////////////////////////////////////////////////////
	//ROUTES FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddRoutes(String s_mcrcode,String s_mcrname) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.MC_RCODE, s_mcrcode);
		initialValues.put(Database.MC_RNAME, s_mcrname);
		return db.insert(Database.ROUTES_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckRoute(String mc_rcode) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.ROUTES_TABLE_NAME,
				new String[] { "_id", Database.MC_RCODE },
				Database.MC_RCODE + "='" + mc_rcode + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}

	/////////////////////////////////////////////////////////////////////
	//SHEDS FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddSheds(String s_mcno,String s_mcname,String s_mcroute,String s_mczone) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.MC_CNO, s_mcno);
		initialValues.put(Database.MC_CNAME, s_mcname);
		initialValues.put(Database.MC_CROUTE, s_mcroute);
		initialValues.put(Database.MC_CZONE, s_mczone);

		return db.insert(Database.COLLECTIONCENTERS_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckShed(String mc_cno,String mc_croute) {
		SQLiteDatabase db = this.getReadableDatabase();

  Cursor myCursor=db.rawQuery("SELECT * FROM " + Database.COLLECTIONCENTERS_TABLE_NAME + " WHERE  MccNo=? AND MccRoute=?",
		  new String[]{mc_cno,mc_croute});
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}

	/////////////////////////////////////////////////////////////////////
	//FARMERS FUNCTIONS/////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddFarmers(String s_farmerno,String s_cardno,String s_idno,String s_mobileno,String s_farmername,String s_fshed,String s_managedfarm,String s_producekg) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.F_FARMERNO, s_farmerno);
		initialValues.put(Database.F_CARDNUMBER, s_cardno);
		initialValues.put(Database.F_NATIONALID, s_idno);
		initialValues.put(Database.F_MOBILENUMBER, s_mobileno);
		initialValues.put(Database.F_FARMERNAME, s_farmername);
		initialValues.put(Database.F_SHED, s_fshed);
		initialValues.put(Database.F_MANAGEDFARM, s_managedfarm);
		initialValues.put(Database.F_PRODUCE_KG_TODATE, s_producekg);

		return db.insert(Database.FARMERS_TABLE_NAME, null, initialValues);

	}

	public Cursor CheckFarmer(String farmerno) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.FARMERS_TABLE_NAME,
				new String[] { "_id", Database.F_FARMERNO },
				Database.F_FARMERNO + "='" + farmerno + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}

	public Cursor SearchFarmer(String farmerNo) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(Database.FARMERS_TABLE_NAME,
				new String[] {Database.ROW_ID,Database.F_FARMERNO,Database.F_FARMERNAME, Database.F_SHED, Database.F_MOBILENUMBER },Database.F_FARMERNO + " LIKE ?",
				new String[] {"%"+  farmerNo+ "%" },null, null,Database.F_FARMERNO +" ASC");

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}

	public Cursor fetchAllEntries(CharSequence constraint) {
		SQLiteDatabase db = this.getReadableDatabase();

			String value = "%" + constraint.toString() + "%";

			String[] columns = new String[] { Database.ROW_ID,Database.F_FARMERNO,Database.F_FARMERNAME, Database.F_SHED, Database.F_MOBILENUMBER };

			return db.query(Database.FARMERS_TABLE_NAME, columns,
					Database.F_FARMERNO + " like ? OR " + Database.F_MOBILENUMBER + " like ? ", new String[]{value, value}, null, null,
					Database.F_FARMERNO + " ASC ");


	}



	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchSpecific(String farmerNo) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(true, Database.FARMERS_TABLE_NAME, null, Database.F_FARMERNO + "='" + farmerNo + "'", null, null, null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchOneFarmer(String farmerNo) {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] allColumns = new String[] {Database.F_FARMERNO,Database.F_FARMERNAME};

		Cursor myCursor = db.query(true, Database.FARMERS_TABLE_NAME, allColumns, Database.F_FARMERNO + "='" + farmerNo + "'", null, null, null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}


	/////////////////////////////////////////////////////////////////////
	//BATCH FUNCTIONS///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddBatch(String BatchDate,String DeliverNoteNumber,String DataDevice,String BatchNumber,String UserID,String OpeningTime) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.BatchDate, BatchDate);
		initialValues.put(Database.DeliveryNoteNumber, DeliverNoteNumber);
		initialValues.put(Database.DataDevice, DataDevice);
		initialValues.put(Database.BatchNumber, BatchNumber);
		initialValues.put(Database.Userid, UserID);
		initialValues.put(Database.OpeningTime, OpeningTime);
		initialValues.put(Database.Closed, 0);
		initialValues.put(Database.SignedOff, 0);
		initialValues.put(Database.BatCloudID, 0);

		return db.insert(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, initialValues);

	}

	/////////////////////////////////////////////////////////////////////
	//AGENT BATCH FUNCTIONS///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddAgtBatch(String BatchDate,String DeliverNoteNumber,String DataDevice,String BatchNumber,String UserID,String OpeningTime) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.BatchDate, BatchDate);
		initialValues.put(Database.DeliveryNoteNumber, DeliverNoteNumber);
		initialValues.put(Database.DataDevice, DataDevice);
		initialValues.put(Database.BatchNumber, BatchNumber);
		initialValues.put(Database.Userid, UserID);
		initialValues.put(Database.OpeningTime, OpeningTime);
		initialValues.put(Database.Closed, 0);
		initialValues.put(Database.SignedOff, 0);
		initialValues.put(Database.BatCloudID, 0);

		return db.insert(Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, initialValues);

	}


	/////////////////////////////////////////////////////////////////////
	//FARMER PRODUCE COLLECTION TRANSACTIONS FUNCTIONS///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddFarmerTrans(String ColDate,String Time,String DataDevice,String BatchNumber,String Agent,String FarmerNo,
							   String WorkerNo,String FieldClerk,String ProduceCode,
							   String VarietyCode,String GradeCode,String RouteCode,String ShedCode,
							   String NetWeight,String TareWeight,String UnitCount,
							   String UnitPrice,String RecieptNo,String ReferenceNo,String BatchSerial,String Quality,String CanSerial) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.CollDate, ColDate);
		initialValues.put(Database.CaptureTime, Time);
		initialValues.put(Database.DataCaptureDevice, DataDevice);
		initialValues.put(Database.BatchNumber, BatchNumber);
		initialValues.put(Database.DataSource, Agent);
		initialValues.put(Database.FarmerNo, FarmerNo);
		initialValues.put(Database.WorkerNo, WorkerNo);
		initialValues.put(Database.FieldClerk, FieldClerk);
		initialValues.put(Database.DeliveredProduce, ProduceCode);
		initialValues.put(Database.ProduceVariety, VarietyCode);
		initialValues.put(Database.ProduceGrade, GradeCode);
		initialValues.put(Database.SourceRoute, RouteCode);
		initialValues.put(Database.BuyingCenter, ShedCode);
		initialValues.put(Database.Quantity, NetWeight);
		initialValues.put(Database.Tareweight, TareWeight);
		initialValues.put(Database.LoadCount, UnitCount);
		initialValues.put(Database.UnitPrice, UnitPrice);
		initialValues.put(Database.ReceiptNo, RecieptNo);
		initialValues.put(Database.ReferenceNo, ReferenceNo);
		initialValues.put(Database.BatchSerial, BatchSerial);
		initialValues.put(Database.Quality, Quality);
		initialValues.put(Database.Container, CanSerial);
		initialValues.put(Database.UsedSmartCard, 0);
		initialValues.put(Database.CloudID, 0);

		return db.insert(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null, initialValues);

	}
	public Cursor SearchReciept(String RecieptNo) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME,
				new String[] {Database.ROW_ID,Database.FarmerNo, Database.DataCaptureDevice , Database.ReceiptNo, Database.CollDate },Database.ReceiptNo + " LIKE ?",
				new String[] {"%"+  RecieptNo+ "%" },null, null,Database.ReceiptNo +" ASC");

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;

	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchSpecificReciept(String RecieptNo) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(true, Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null, Database.ReceiptNo + "='" + RecieptNo + "'", null, null, null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchRecieptByDate(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.SESSION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchGenReciept(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
	public Cursor SearchBatch(String RecieptNo) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME,
				new String[] {Database.ROW_ID,Database.DeliveryNoteNumber, Database.DataDevice , Database.BatchNumber, Database.BatchDate },Database.DeliveryNoteNumber + " LIKE ?",
				new String[] {"%"+  RecieptNo+ "%" },null, null,Database.DeliveryNoteNumber +" ASC");

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;

	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchSpecificBatch(String RecieptNo) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, Database.DeliveryNoteNumber + "='" + RecieptNo + "'", null, null, null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchBatchByDate(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
	public Cursor SearchOnR(String farmerNo ) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(Database.FARMERSPRODUCECOLLECTION_TABLE_NAME,
				new String[] {Database.ROW_ID,Database.FarmerNo,Database.Quantity },Database.FarmerNo + " LIKE ?",
				new String[] {"%"+  farmerNo+ "%" },null,null,Database.FarmerNo +" ASC");

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}

 @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchSpecificOnR(String farmerNo,String condition) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor myCursor = db.query(true, Database.FARMERSPRODUCECOLLECTION_TABLE_NAME, null, Database.FarmerNo + "='" + farmerNo + "'", null, "" + condition + "", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}

	public long AddSession(String SessionID,String SessionDate,String SessionTime,String SessionDevice,
						   String SessionFarmerNo,String SessionBags,String SessionNet,String SessionTare
			,String SessionRoute,String SessionNo,String SessionReceipt) {

		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.ROW_ID, SessionID);
		initialValues.put(Database.SessionDate, SessionDate);
		initialValues.put(Database.SessionTime, SessionTime);
		initialValues.put(Database.SessionDevice, SessionDevice);
		initialValues.put(Database.SessionFarmerNo, SessionFarmerNo);
		initialValues.put(Database.SessionBags, SessionBags);
		initialValues.put(Database.SessionNet, SessionNet);
		initialValues.put(Database.SessionTare, SessionTare);
		initialValues.put(Database.SessionRoute, SessionRoute);
		initialValues.put(Database.SessionCounter, SessionNo);
		initialValues.put(Database.SessionReceipt, SessionReceipt);
		return db.insert(Database.SESSION_TABLE_NAME, null, initialValues);

	}

	/////////////////////////////////////////////////////////////////////
	//AGENT PRODUCE COLLECTION TRANSACTIONS FUNCTIONS///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddAgentTrans(String ColDate,String Time,String DataDevice,String BatchNumber,String Agent,String FarmerNo,
							   String ProduceCode,String NetWeight,String TareWeight,String UnitCount,
							   String UnitPrice,String RecieptNo,String Warehouse) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.ACollDate, ColDate);
		initialValues.put(Database.ACaptureTime, Time);
		initialValues.put(Database.ADataCaptureDevice, DataDevice);
		initialValues.put(Database.BatchNumber, BatchNumber);
		initialValues.put(Database.AgentNo, Agent);
		initialValues.put(Database.AFarmerNo, FarmerNo);
		initialValues.put(Database.ADeliveredProduce, ProduceCode);
		initialValues.put(Database.AQuantity, NetWeight);
		initialValues.put(Database.ATareweight, TareWeight);
		initialValues.put(Database.ALoadCount, UnitCount);
		initialValues.put(Database.AUnitPrice, UnitPrice);
		initialValues.put(Database.AReceiptNo, RecieptNo);
		initialValues.put(Database.AContainer, Warehouse);
		initialValues.put(Database.AUsedSmartCard, 0);
		initialValues.put(Database.CloudID, 0);


		return db.insert(Database.AGENTSPRODUCECOLLECTION_TABLE_NAME, null, initialValues);

	}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchARecieptByDate(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.ASESSION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchAGenReciept(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.AGENTSPRODUCECOLLECTION_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchAgtBatchByDate(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
	public long AddASession(String SessionID,String SessionDate,String SessionTime,String SessionDevice,
						   String SessionAgentNo,String SessionBags,String SessionNet,String SessionTare
			                ,String SessionNo,String BatchID,String ClerkID,String WarehouseID,String ProduceCode,String VarietyCode,String GradeCode) {

		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.ROW_ID, SessionID);
		initialValues.put(Database.ASessionDate, SessionDate);
		initialValues.put(Database.ASessionTime, SessionTime);
		initialValues.put(Database.ASessionDevice, SessionDevice);
		initialValues.put(Database.ASessionAgentNo, SessionAgentNo);
		initialValues.put(Database.ASessionBags, SessionBags);
		initialValues.put(Database.ASessionNet, SessionNet);
		initialValues.put(Database.ASessionTare, SessionTare);
		initialValues.put(Database.AReceiptNo, SessionNo);
		initialValues.put(Database.ASessionBatchID, BatchID);
		initialValues.put(Database.ASessionClerkID, ClerkID);
		initialValues.put(Database.ASessionWarehouseID, WarehouseID);
		initialValues.put(Database.ASessionProduceCode, ProduceCode);
		initialValues.put(Database.ASessionVarietyCode, VarietyCode);
		initialValues.put(Database.ASessionGradeCode, GradeCode);
		initialValues.put(Database.CloudID, 0);
		return db.insert(Database.ASESSION_TABLE_NAME, null, initialValues);

	}
	/////////////////////////////////////////////////////////////////////
	//DELIVERY FUNCTIONS///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddDelivery(String DNoteNo,String Date,String Factory,String Transporter,String Vehicle,String ArrivalTime,String FieldWt) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.FdDNoteNum, DNoteNo);
		initialValues.put(Database.FdDate, Date);
		initialValues.put(Database.FdFactory, Factory);
		initialValues.put(Database.FdTransporter, Transporter);
		initialValues.put(Database.FdVehicle, Vehicle);
		initialValues.put(Database.FdArrivalTime, ArrivalTime);
		initialValues.put(Database.FdFieldWt, FieldWt);
		initialValues.put(Database.FdStatus, 0);
		initialValues.put(Database.CloudID, 0);

		return db.insert(Database.Fmr_FactoryDeliveries, null, initialValues);

	}
	public Cursor CheckDelNo(String delNo) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.Fmr_FactoryDeliveries,
				new String[] { "_id", Database.FdDNoteNum },
				Database.FdDNoteNum + "='" + delNo + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	public Cursor CheckDelNoAgt(String delNo) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.Agt_FactoryDeliveries,
				new String[] { "_id", Database.FdDNoteNum },
				Database.FdDNoteNum + "='" + delNo + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	public Cursor CheckDelivery(String ticketNo) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.Fmr_FactoryDeliveries,
				new String[] { "_id", Database.FdWeighbridgeTicket },
				Database.FdWeighbridgeTicket + "='" + ticketNo + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	public Cursor CheckDeliveryAgt(String ticketNo) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(Database.Agt_FactoryDeliveries,
				new String[] { "_id", Database.FdWeighbridgeTicket },
				Database.FdWeighbridgeTicket + "='" + ticketNo + "'", null, null, null, null);

		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		return myCursor;
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchDeliveryByDate(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.Fmr_FactoryDeliveries, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
	/////////////////////////////////////////////////////////////////////
	//AGENT DELIVERY FUNCTIONS///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public long AddAGTDelivery(String DNoteNo,String Date,String Factory,String Transporter,String Vehicle,String ArrivalTime,String FieldWt) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(Database.FdDNoteNum, DNoteNo);
		initialValues.put(Database.FdDate, Date);
		initialValues.put(Database.FdFactory, Factory);
		initialValues.put(Database.FdTransporter, Transporter);
		initialValues.put(Database.FdVehicle, Vehicle);
		initialValues.put(Database.FdArrivalTime, ArrivalTime);
		initialValues.put(Database.FdFieldWt, FieldWt);
		initialValues.put(Database.FdStatus, 0);
		initialValues.put(Database.CloudID, 0);

		return db.insert(Database.Agt_FactoryDeliveries, null, initialValues);

	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public Cursor SearchAgtDeliveryByDate(String condition) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor myCursor = db.query(true, Database.Agt_FactoryDeliveries, null, "" + condition + "", null, null, null, null, null, null);

		//Cursor myCursor=db.rawQuery("select * from FarmersProduceCollection where " + condition + "", null);
		if (myCursor != null) {
			myCursor.moveToFirst();
		}
		db.close();
		return myCursor;
	}
}
