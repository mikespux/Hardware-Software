package com.easyweigh.soap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class SoapRequest {
    private static final String MY_DB = "com.octagon.easyweigh_preferences";
    private final String CREATE_BATCH_METHOD;
    private final String METHOD_NAMESPACE;
    private final String NAMESPACE;
    private final String POST_WEIGHMENT_METHOD;
    private final String SIGNOFF_BATCH_METHOD;
    private final String TAG;
    private String _URL;
    private Context _context;
    private String _licenseKey;
    HttpTransportSE httpTransport;
    HttpTransportSE httpTransport2;
    SharedPreferences mSharedPrefs;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    public SoapRequest(Context ctx) {
        _URL = null;
        _licenseKey = null;
        NAMESPACE = "http://tempuri.org/IEasyweighService/";
        METHOD_NAMESPACE = "http://tempuri.org/";
        TAG = "SoapRequest";
        CREATE_BATCH_METHOD = "CreateBatch";
        POST_WEIGHMENT_METHOD = "PostWeighment";
        SIGNOFF_BATCH_METHOD = "SignoffBatch";
        _context = ctx;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);;
         _URL = mSharedPrefs.getString("portalURL", null);
        _licenseKey = mSharedPrefs.getString("licenseKey", null);
        //_URL = "http://192.168.0.24/Easyweigh/EasywayCloudService.svc/EasyweighService";
        //_licenseKey = "QMZ1Y46KD3";
        httpTransport = new HttpTransportSE(_URL);
        httpTransport2 = new HttpTransportSE(_URL);
    }

    public String createBatch(String batchInfo) {
        SoapObject request = new SoapObject("http://tempuri.org/", "CreateBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("BatchInfo", batchInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/CreateBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());

            if (response.getProperty(1).toString().equals("Ok")) {
               // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                return response.getProperty(0).toString();
            }
            String errorNO=response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("errorNo", errorNO);
            edit.commit();

            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            String Server="-8080";
            Log.e("SoapRequest", ex.toString());
            Log.e("Server Response", Server);

            return Server;
        }
    }

    public String CreateAgentsBatch(String batchInfo) {
        SoapObject request = new SoapObject("http://tempuri.org/", "CreateAgentsBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("BatchInfo", batchInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/CreateAgentsBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());

            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                return response.getProperty(0).toString();
            }
            String errorNO=response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("errorNo", errorNO);
            edit.commit();

            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server="-8080";
            return Server;
        }
    }

    public String AddAgentDelivery(String var1, String var2) {
        Log.i("SoapOperations", "Post delivery info " + var2);
        Log.i("SoapOperations", "Post batch id " + var1);
        Object var3 = null;
        SoapObject var4 = new SoapObject("http://tempuri.org/", "AddAgentDelivery");
        var4.addProperty("Lickey", this._licenseKey);
        var4.addProperty("Batch", var1);
        var4.addProperty("DeliveryInfo", var2);
        SoapSerializationEnvelope var6 = new SoapSerializationEnvelope(110);
        var6.dotNet = true;
        var6.setOutputSoapObject(var4);

        try {
            this.httpTransport.debug = true;
            this.httpTransport.call("http://tempuri.org/IEasyweighService/AddAgentDelivery", var6);
            SoapObject var7 = (SoapObject)var6.getResponse();
            Log.i("Response 0 ", var7.getProperty(0).toString());
            Log.i("Response 1 ", var7.getProperty(1).toString());
            Log.i("Response 2 ", var7.getProperty(2).toString());
            if(var7.getProperty(1).toString().equals("Ok")) {
                var1 = var7.getProperty(0).toString();
            } else {
                String errorNO=var7.getProperty(0).toString();
                // save user data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("errorNo", errorNO);
                edit.commit();
                var1 = var7.getProperty(2).toString();

            }
        } catch (Exception var5) {
            Log.e("SoapOperations", "Error posting delivery");
            var5.printStackTrace();
            var1 = (String)var3;
        }

        return var1;
    }
    public String AddAgentWeightRecord(String var1, String var2) {
        Log.i("SoapOperations", "Post recordInfo " + var2);
        Log.i("SoapOperations", "Post delivery id " + var1);
        Object var3 = null;
        SoapObject var4 = new SoapObject("http://tempuri.org/", "AddAgentWeightRecord");
        var4.addProperty("Lickey", this._licenseKey);
        var4.addProperty("Delivery", var1);
        var4.addProperty("RecordInfo", var2);
        SoapSerializationEnvelope var6 = new SoapSerializationEnvelope(110);
        var6.dotNet = true;
        var6.setOutputSoapObject(var4);

        try {
            this.httpTransport.debug = true;
            this.httpTransport.call("http://tempuri.org/IEasyweighService/AddAgentWeightRecord", var6);
            SoapObject var7 = (SoapObject)var6.getResponse();
            Log.i("Response 0 ", var7.getProperty(0).toString());
            Log.i("Response 1 ", var7.getProperty(1).toString());
            Log.i("Response 2 ", var7.getProperty(2).toString());
            if(var7.getProperty(1).toString().equals("Ok")) {
                var1 = var7.getProperty(0).toString();
            } else {
                String errorNO=var7.getProperty(0).toString();
                // save user data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("errorNo", errorNO);
                edit.commit();
                var1 = var7.getProperty(2).toString();

            }
        } catch (Exception var5) {
            Log.e("SoapOperations", "Error posting AgentWeightRecord");
            var5.printStackTrace();
            var1 = (String)var3;
        }

        return var1;
    }
   /* public String postWeighment(String batch, String weighmentInfo) {
        SoapObject request = new SoapObject("http://tempuri.org/", "PostWeighment");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("Batch", batch);
        request.addProperty("WeightInfo", weighmentInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport2.debug = true;
            httpTransport2.call("http://tempuri.org/IEasyweighService/PostWeighment", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            return response.getProperty(2).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }*/
    public String postWeighment(String var1, String var2) {
        Log.i("SoapOperations", "Post weighmeninfo " + var2);
        Log.i("SoapOperations", "Post batch id " + var1);
        Object var3 = null;
        SoapObject var4 = new SoapObject("http://tempuri.org/", "PostWeighment");
        var4.addProperty("Lickey", this._licenseKey);
        var4.addProperty("Batch", var1);
        var4.addProperty("WeightInfo", var2);
        SoapSerializationEnvelope var6 = new SoapSerializationEnvelope(110);
        var6.dotNet = true;
        var6.setOutputSoapObject(var4);

        try {
            this.httpTransport.debug = true;
            this.httpTransport.call("http://tempuri.org/IEasyweighService/PostWeighment", var6);
            SoapObject var7 = (SoapObject)var6.getResponse();
            Log.i("Response 0 ", var7.getProperty(0).toString());
            Log.i("Response 1 ", var7.getProperty(1).toString());
            Log.i("Response 2 ", var7.getProperty(2).toString());
            if(var7.getProperty(1).toString().equals("Ok")) {
                var1 = var7.getProperty(0).toString();
            } else {
                String errorNO=var7.getProperty(0).toString();
                // save user data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("errorNo", errorNO);
                edit.commit();
                var1 = var7.getProperty(2).toString();

            }
        } catch (Exception var5) {
            Log.e("SoapOperations", "Error posting weighment");
            var5.printStackTrace();
            var1 = (String)var3;
        }

        return var1;
    }


    public String signOffBatch(String batchID, String totalWeight) {
        SoapObject request = new SoapObject("http://tempuri.org/", "SignoffBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("Batch", batchID);
        request.addProperty("TotalWeights", totalWeight);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/SignoffBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO=response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("errorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();


        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }

    public String SignoffAgentsBatch(String batchID, String totalWeight) {
        SoapObject request = new SoapObject("http://tempuri.org/", "SignoffAgentsBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("Batch", batchID);
        request.addProperty("TotalWeights", totalWeight);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/SignoffAgentsBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO=response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("errorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();


        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }

    public String VerifyBatch(String BatchNo, String TotalWeights) {
        SoapObject request = new SoapObject("http://tempuri.org/", "VerifyBatch");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("BatchNo", BatchNo);
        request.addProperty("TotalWeights", TotalWeights);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/VerifyBatch", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO=response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("errorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();


        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }

    public String createDelivery(String DeliveryInfo ) {
        SoapObject request = new SoapObject("http://tempuri.org/", "CreateDelivery");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("DeliveryInfo", DeliveryInfo);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/CreateDelivery", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            edit = prefs.edit();
            if (response.getProperty(1).toString().equals("Ok")) {
                // Toast.makeText(_context, response.getProperty(0).toString(), Toast.LENGTH_LONG).show();
                edit.remove("DelerrorNo");
                edit.commit();
                return response.getProperty(0).toString();


            }
            String errorNO=response.getProperty(0).toString();
            // save user data

            edit.putString("DelerrorNo", errorNO);
            edit.commit();
            return new StringBuilder(response.getProperty(2).toString()).toString();
            //return new StringBuilder(String.valueOf(response.getProperty(0).toString())).append("\r\n").append(response.getProperty(1).toString()).append("\r\n").append(response.getProperty(2).toString()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            String Server="-8080";
            return Server;
        }
    }
    public String DeliverBatch(String var1, String var2) {
        Log.i("SoapOperations", "Post BatchNo" + var2);
        Log.i("SoapOperations", "Post Delivery " + var1);
        Object var3 = null;
        SoapObject var4 = new SoapObject("http://tempuri.org/", "DeliverBatch");
        var4.addProperty("Lickey", this._licenseKey);
        var4.addProperty("Delivery", var1);
        var4.addProperty("BatchNo", var2);
        SoapSerializationEnvelope var6 = new SoapSerializationEnvelope(110);
        var6.dotNet = true;
        var6.setOutputSoapObject(var4);

        try {
            this.httpTransport.debug = true;
            this.httpTransport.call("http://tempuri.org/IEasyweighService/DeliverBatch", var6);
            SoapObject var7 = (SoapObject)var6.getResponse();
            Log.i("Response 0 ", var7.getProperty(0).toString());
            Log.i("Response 1 ", var7.getProperty(1).toString());
            Log.i("Response 2 ", var7.getProperty(2).toString());
            if(var7.getProperty(1).toString().equals("Ok")) {
                var1 = var7.getProperty(0).toString();
            } else {
                String errorNO=var7.getProperty(0).toString();
                // save user data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("DelerrorNo", errorNO);
                edit.commit();
                var1 = var7.getProperty(2).toString();

            }
        } catch (Exception var5) {
            Log.e("SoapOperations", "Error posting BatchNo");
            var5.printStackTrace();
            var1 = (String)var3;
        }

        return var1;
    }

    public String SignoffDelivery(String Delivery) {
        SoapObject request = new SoapObject("http://tempuri.org/", "SignoffDelivery");
        request.addProperty("Lickey", _licenseKey);
        request.addProperty("Delivery", Delivery);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        try {
            httpTransport.debug = true;
            httpTransport.call("http://tempuri.org/IEasyweighService/SignoffDelivery", envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Log.i("Response 0 ", response.getProperty(0).toString());
            Log.i("Response 1 ", response.getProperty(1).toString());
            Log.i("Response 2 ", response.getProperty(2).toString());
            if (response.getProperty(1).toString().equals("Ok")) {
                return response.getProperty(0).toString();
            }
            String errorNO=response.getProperty(0).toString();
            // save user data
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("DelerrorNo", errorNO);
            edit.commit();
            return response.getProperty(2).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SoapRequest", ex.toString());
            return ex.getMessage();
        }
    }
}
