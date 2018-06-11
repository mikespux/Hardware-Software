package com.easyweigh.data;


public class Database {

    //Constants
	public static final String ROW_ID = "_id";
	public static final String CloudID   = "CloudID";

    // Company Table
	public static final String COMPANY_TABLE_NAME = "company";
	public static final String CO_PREFIX = "CoPrefix";
	public static final String CO_NAME = "CoName";
	public static final String CO_LETTERBOX = "CoLetterBox";
	public static final String CO_POSTCODE = "CoPostCode";
	public static final String CO_POSTNAME = "CoPostName";
	public static final String CO_POSTREGION = "coPostRegion";
	public static final String CO_TELEPHONE = "CoTelephone";
	public static final String CO_ClOUDID = "CoCloudID";


	//Warehouse Table
	public static final String WAREHOUSE_TABLE_NAME= "warehouse";
	public static final String WH_ID = "whID";
	public static final String WH_NAME = "whName";

	//Agent Table
	public static final String AGENT_TABLE_NAME= "agent";
	public static final String AGT_ID = "agtID";
	public static final String AGT_NAME = "agtName";

	//Transporter Table
	public static final String TRANSPORTER_TABLE_NAME= "transporter";
	public static final String TPT_ID = "tptID";
	public static final String TPT_NAME = "tptName";

    //Factory Table
	public static final String FACTORY_TABLE_NAME= "factory";
	public static final String FRY_PREFIX = "FryPrefix";
	public static final String FRY_TITLE = "FryTitle";
	public static final String FRY_ClOUDID= "FryCloudID";

	// Zones Table
	public static final String ZONES_TABLE_NAME = "zones";
	public static final String FZ_CODE = "fzCode";
    public static final String FZ_NAME = "fzName";
	public static final String FZ_ClOUDID = "FzCloudID";

	// Routes Table
	public static final String ROUTES_TABLE_NAME = "routes";
	public static final String MC_RCODE = "McRCode";
	public static final String MC_RNAME = "McRName";
	public static final String MC_RClOUDID = "McRCloudID";

	//CollectionCenters Table
	public static final String COLLECTIONCENTERS_TABLE_NAME = "CollectionCenters";
	public static final String MC_CNO = "MccNo";
    public static final String MC_CNAME = "MccName";
   	public static final String MC_CZONE = "MccZone";
	public static final String MC_CROUTE = "MccRoute";
	public static final String MC_CClOUDID = "MccCloudID";

    //Farmers Table
	public static final String FARMERS_TABLE_NAME = "farmers";
    public static final String F_FARMERNO = "FFarmerNo";
   	public static final String F_CARDNUMBER = "FCardNumber";
	public static final String F_FARMERNAME = "FFarmerName";
	public static final String F_NATIONALID = "FNationalID";
	public static final String F_MOBILENUMBER = "FMobileNumber";
	public static final String F_SHED = "FShed";
	public static final String F_MANAGEDFARM = "FManagedFarm";
	public static final String F_PRODUCE_KG_TODATE= "FProduceKg";
	public static final String F_CLOUDID = "FCloudID";


    //OperatorsMaster Table
	public static final String OPERATORSMASTER_TABLE_NAME = "OperatorsMaster";
	public static final String USERIDENTIFIER= "UserIdentifier";
	public static final String CLERKNAME = "ClerkName";
	public static final String ACCESSLEVEL = "AccessLevel";
	public static final String USERPWD = "UserPwd";
	public static final String USERCLOUDID= "UserCloudID";


    //Produce Table
	public static final String PRODUCE_TABLE_NAME = "Produce";
	public static final String MP_CODE = "MpCode";
	public static final String MP_DESCRIPTION = "MpDescription";
	public static final String MP_RETAILPRICE = "MpRetailPrice";
	public static final String MP_SALESTAX = "MpSalesTax";
	public static final String MP_CLOUDID  = "MpCloudID";

	//ProduceGrades Table
	public static final String PRODUCEGRADES_TABLE_NAME = "ProduceGrades";
	public static final String PG_DREF = "pgdRef";
	public static final String PG_DNAME = "pgdName";
	public static final String PG_DPRODUCE = "pgdProduce";
	public static final String PG_RETAILPRICE = "PgRetailPrice";
	public static final String PG_SALESTAX = "PgSalesTax";
	public static final String PG_DCLOUDID = "pgdCloudID";

	//ProduceVarieties Table
	public static final String PRODUCEVARIETIES_TABLE_NAME = "ProduceVarieties";
	public static final String VRT_REF = "vtrRef";
	public static final String VRT_NAME = "vrtName";
	public static final String VRT_PRODUCE = "vrtProduce";
	public static final String VRT_RETAILPRICE = "vrtRetailPrice";
	public static final String VRT_SALESTAX = "vrtSalesTax";
	public static final String VRT_CLOUDID = "vrtCloudID";

	//Workers Table
	public static final String WORKERS_TABLE_NAME = "workers";
	public static final String FW_PFNO = "FwPFNo";
	public static final String FW_CARDNUMBER = "FwCardNumber";
	public static final String FW_NATIONALID = "FwNationalID";
	public static final String FW_MOBILENUMBER = "FwMobileNumber";
	public static final String FW_EMPLOYEENAME = "FwEmployeeName";
	public static final String FW_ATTACHEDFARM = "FwAttachedFarm";
	public static final String FW_CLOUDID = "FwCloudID";

	//FarmersProduceCollection Table
	public static final String FARMERSPRODUCECOLLECTION_TABLE_NAME = "FarmersProduceCollection";
	public static final String CollDate   = "CollDate";
	public static final String DataCaptureDevice   = "DataCaptureDevice";
	public static final String DataSource  = "DataSource";
	public static final String FarmerNo   = "FarmerNo";
	public static final String WorkerNo   = "WorkerNo";
	public static final String FieldClerk   = "FieldClerk";
	public static final String DeliveredProduce   = "DeliveredProduce";
	public static final String ProduceVariety   = "ProduceVariety";
	public static final String ProduceGrade   = "ProduceGrade";
	public static final String SourceRoute   = "SourceRoute";
	public static final String BuyingCenter   = "BuyingCenter";
	public static final String Quantity   = "Quantity";
	public static final String Tareweight   = "Tareweight";
	public static final String LoadCount  = "LoadCount";
	public static final String UnitPrice   = "UnitPrice";
	public static final String ReceiptNo  = "ReceiptNo";
	public static final String BatchSerial  = "BatchSerial";
	public static final String Quality  = "Quality";
	public static final String Container  = "Container";
	public static final String CaptureTime  = "CaptureTime";
	public static final String UsedSmartCard   = "UsedSmartCard";
	public static final String ReferenceNo  = "ReferenceNo";


	//Session Table
	public static final String SESSION_TABLE_NAME = "SessionTbl";
	public static final String SessionDate    = "sdate";
	public static final String SessionTime    = "stime";
	public static final String SessionDevice  = "sdev";
	public static final String SessionFarmerNo= "sfarmerno";
	public static final String SessionBags    = "sbags";
	public static final String SessionNet     = "snet";
	public static final String SessionTare    = "stare";
	public static final String SessionRoute   = "sroute";
	public static final String SessionCounter = "scounter";
	public static final String SessionReceipt = "sreceipt";


	//AgentsProduceCollection Table
	public static final String AGENTSPRODUCECOLLECTION_TABLE_NAME = "AgentsProduceCollection";
	public static final String ACollDate   = "ACollDate";
	public static final String ADataCaptureDevice   = "ADataCaptureDevice";
	public static final String ADataSource  = "ADataSource";
	public static final String AFarmerNo   = "AFarmerNo";
	public static final String AgentNo   = "AgentNo";
	public static final String ADeliveredProduce   = "ADeliveredProduce";
	public static final String AQuantity   = "AQuantity";
	public static final String ATareweight   = "ATareweight";
	public static final String ALoadCount  = "ALoadCount";
	public static final String AUnitPrice   = "AUnitPrice";
	public static final String AReceiptNo  = "AReceiptNo";
	public static final String AContainer  = "AContainer";
	public static final String ACaptureTime  = "ACaptureTime";
	public static final String AUsedSmartCard   = "AUsedSmartCard";

	//Agent Session Table
	public static final String ASESSION_TABLE_NAME = "ASessionTbl";
	public static final String ASessionDate   = "asdate";
	public static final String ASessionTime   = "astime";
	public static final String ASessionDevice   = "asdev";
	public static final String ASessionAgentNo  = "asagentno";
	public static final String ASessionBags  = "asbags";
	public static final String ASessionNet   = "asnet";
	public static final String ASessionTare   = "astare";
	public static final String ASessionRecieptNo   = "areceiptno";
	public static final String ASessionBatchID   = "abatchid";
	public static final String ASessionClerkID   = "aclerkid";
	public static final String ASessionWarehouseID   = "awarehouseid";
	public static final String ASessionProduceCode   = "aproducecode";
	public static final String ASessionVarietyCode   = "avarietycode";
	public static final String ASessionGradeCode   = "agradecode";



	//AgentsSuppliesConsignments Table
	public static final String AGENTSSUPPLIESCONSIGNMENTS_TABLE_NAME = "AgentsSuppliesConsignments";


	//FarmersSuppliesConsignments Table
	public static final String FARMERSSUPPLIESCONSIGNMENTS_TABLE_NAME = "FarmersSuppliesConsignments";
	public static final String BatchDate   = "BatchDate";
	public static final String DeliveryNoteNumber   = "DeliveryNoteNumber";
	public static final String DataDevice    = "DataDevice";
	public static final String BatchNumber    = "BatchNumber";
	public static final String Userid    = "Userid";
	public static final String OpeningTime    = "OpeningTime";
	public static final String Closed   = "Closed";
	public static final String ClosingTime    = "ClosingTime";
	public static final String NoOfWeighments    = "NoOfWeighments";
	public static final String TotalWeights    = "TotalWeights";
	public static final String Factory    = "Factory";
	public static final String Transporter   = "Transporter";
	public static final String Tractor   = "Tractor";
	public static final String Trailer   = "Trailer";
	public static final String DelivaryNO   = "DelivaryNO";
	public static final String SignedOff   = "SignedOff";
	public static final String SignedOffTime    = "SignedOffTime";
	public static final String BatchSession    = "BatchSession";
	public static final String BatchCount    = "BatchCount";
	public static final String Dispatched  = "Dispatched";
	public static final String BatCloudID    = "BatCloudID";

	// Agent Deliveries Table
	public static final String Agt_FactoryDeliveries = "Agt_FactoryDeliveries";

	// Deliveries Table
	public static final String Fmr_FactoryDeliveries = "Fmr_FactoryDeliveries";
	public static final String FdWeighbridgeTicket = "FdWeighbridgeTicket";
	public static final String FdDNoteNum = "FdDNoteNum";
	public static final String FdDate = "FdDate";
	public static final String FdFactory = "FdFactory";
	public static final String FdTransporter = "FdTransporter";
	public static final String FdVehicle = "FdVehicle";
	public static final String FdFieldWt = "FdFieldWt";
	public static final String FdArrivalTime = "FdArrivalTime";
	public static final String FdGrossWt = "FdGrossWt";
	public static final String FdTareWt = "FdTareWt";
	public static final String FdRejectWt = "FdRejectWt";
	public static final String FdQualityScore = "FdQualityScore";
	public static final String FdDepartureTime = "FdDepartureTime";
	public static final String FdStatus = "FdStatus";

	// Inventory Table
	public static final String Inventory = "Inventory";
	public static final String Product_Code   = "ProductCode";
	public static final String Product_Name   = "ProductName";
	public static final String Unit_Price   = "UnitPrice";
	public static final String Unit_Type   = "UnitType";
	public static final String SaleTaxRate  = "SaleTaxRate";



   // FarmInputSales Table
	public static final String FarmInputSales = "FarmInputSales";
	public static final String Farmer_No   = "FarmerNo";
	public static final String OrderNo   = "OrderNo";
	public static final String ItemCode   = "ItemCode";
	public static final String Units   = "Units";
	public static final String ItemCost   = "ItemCost";
	public static final String lnCloudID  = "lnCloudID";

	// FarmInputOrders Table

	public static final String FarmInputOrders = "FarmInputOrders";
	public static final String OrderDate   = "OrderDate";
	public static final String OrderTime   = "OrderTime";
	public static final String TotalSale   = "TotalSale";
	public static final String SalesTax   = "SalesTax";
	public static final String TotalAmount   = "TotalAmount";
	public static final String OrdCloudId   = "OrdCloudId";


	// AccountCustomers Table

	public static final String AccountCustomers = "AccountCustomers";
	public static final String C_CUSTOMERNO = "CCustomerNO";
	public static final String C_CUSTOMERNAME = "CCustomerName";
	public static final String C_MOBILENUMBER = "CMobileNumber";
	public static final String C_EMAIL = "CEmail";
	public static final String C_ADDRESS = "CAddress";

	// ProduceSales Table

	public static final String ProduceSales = "ProduceSales";
	public static final String CustomerNo   = "CustomerNo";
	public static final String SaleDate   = "SaleDate";
	public static final String SaleTime   = "SaleTime";
	public static final String ClerkID   = "ClerkID";
	public static final String TerminalID   = "TerminalID";
	public static final String Produce   = "Produce";
	public static final String Pvariety   = "Pvariety";
	public static final String Pgrade   = "Pgrade";


	public static final String UPrice   = "UPrice";
	public static final String SaleValue   = "SaleValue";
	public static final String SaleTax   = "SaleTax";
	public static final String Amount   = "Amount";
	public static final String PaymentTerms   = "PaymentTerms";
	public static final String CashPaid   = "CashPaid";
	public static final String CashChange   = "CashChange";

}
