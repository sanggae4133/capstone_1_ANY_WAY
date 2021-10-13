package com.example.myapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class getAltitude {

    int ret;
    double Lat,Lon;
    double elevation;
    String ApiKey = "XXXXXXXXXXXXXXXX";   //Your Api Key
    String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=" + Lat + "," + Lon + "&key="+ApiKey;
    StringBuffer response = new StringBuffer();

    ret = com.example.package.getHttp.GetHttpToServer(url, response);


    if (ret == 0)
    {
        try {
            JSONObject jsonObj = new JSONObject(response.toString());
            JSONArray resultEl = jsonObj.getJSONArray("results");
            JSONObject current = resultEl.getJSONObject(0);
            elevation = Double.parseDouble(current.getString("elevation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
