package com.easyweigh.soap;

import org.ksoap2.serialization.SoapObject;

public class SoapResponse {
    String _description;
    String _message;
    String _number;

    public SoapResponse(SoapObject object) {
        new Deserilization().SoapDeserilize(this, object);
    }
}
