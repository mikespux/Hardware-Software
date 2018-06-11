package com.easyweigh.soap;

import android.content.Context;
import android.content.SharedPreferences;
import org.xmlpull.v1.XmlPullParser;

public class SoapOperations2 {
    private static final String MY_DB = "com.octagon.easyweigh_preferences";
    private static final String TAG = "SoapOperations";
    private Context _ctx;
    String batchInfo;
    String batchNo;
    String canSerial;
    String cardFlag;
    private String checkListReturnValue;
    String closed;
    String deliveryNoteNo;
    String deviceID;
    String factory;
    String farmerNo;
   // private SQLDataHelper mDbHelper;
    private SharedPreferences mSharedPrefs;
    String netWeight;
    String produceCode;
    String receiptNo;
    String returnValue;
    String routeCode;
    String serverBatchID;
    String shedCode;
    private String soapResponse;
    String[] soapValues;
    String stringCloseTime;
    String stringOpenDate;
    String stringOpenTime;
    String tareWeight;
    String tractorNo;
    String trailerNo;
    String userID;
    String weighingSession;
    String weighmentCount;
    String weighmentDate;
    String weighmentInfo;
    String weighmentTime;

    /* renamed from: com.octagon.easyweigh.soap.SoapOperations2.1 */
    class C01411 extends Thread {
        private final /* synthetic */ String val$batchInfo;
        private final /* synthetic */ String val$rowID;

        C01411(String str, String str2) {
            this.val$batchInfo = str;
            this.val$rowID = str2;
        }

        public void run() {
            try {
                SoapOperations2.this.soapResponse = new SoapRequest(SoapOperations2.this._ctx).createBatch(this.val$batchInfo);
                if (Integer.valueOf(SoapOperations2.this.soapResponse).intValue() > 0) {
                    SoapOperations2.this.serverBatchID = SoapOperations2.this.soapResponse;
                    //SoapOperations2.this.mDbHelper.ammendServerBatchID(this.val$rowID, SoapOperations2.this.soapResponse);
                    SoapOperations2.this.returnValue = SoapOperations2.this.soapResponse;
                }
            } catch (Exception e) {
                e.printStackTrace();
                SoapOperations2.this.returnValue = e.toString();
            }
        }
    }

    public SoapOperations2(Context ctx) {
        this.checkListReturnValue = XmlPullParser.NO_NAMESPACE;
        this.returnValue = null;
        this._ctx = ctx;
        this.mSharedPrefs = ctx.getSharedPreferences(MY_DB, 0);
    }

    public String createBatch(String batchInfo, String rowID) {
        C01411 c01411 = new C01411(batchInfo, rowID);
        return this.returnValue;
    }

    public String postWeighment(String serverBatchNo, String weighmentInfo) {
        String returnValue = null;
        if (!checkList()) {
            return this.checkListReturnValue;
        }
        try {
            this.soapResponse = new SoapRequest(this._ctx).postWeighment(serverBatchNo, weighmentInfo);
            returnValue = this.soapResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public String signOffBatch(String serverBatchID, String totalWeighment) {
        if (checkList()) {
            return null;
        }
        return this.checkListReturnValue;
    }

    private boolean checkList() {
        this.checkListReturnValue = XmlPullParser.NO_NAMESPACE;
        try {
            if (this.mSharedPrefs.getBoolean("cloudServices", false)) {
                try {
                    if (this.mSharedPrefs.getString("licenseKey", null).equals(null) || this.mSharedPrefs.getString("licenseKey", null).equals(XmlPullParser.NO_NAMESPACE)) {
                        this.checkListReturnValue = "License key not found!";
                        return false;
                    }
                    try {
                        if (!this.mSharedPrefs.getString("portalURL", null).equals(null) && !this.mSharedPrefs.getString("portalURL", null).equals(XmlPullParser.NO_NAMESPACE)) {
                            return true;
                        }
                        this.checkListReturnValue = "Portal URL not configured!";
                        return false;
                    } catch (Exception e) {
                        this.checkListReturnValue = "Portal URL not configured!";
                        return false;
                    }
                } catch (Exception e2) {
                    this.checkListReturnValue = "License key not found!";
                    return false;
                }
            }
            this.checkListReturnValue = "Cloud Services not enabled!";
            return false;
        } catch (Exception e3) {
            e3.printStackTrace();
            this.checkListReturnValue = "Cloud Services not enabled!";
            return false;
        }
    }
}
