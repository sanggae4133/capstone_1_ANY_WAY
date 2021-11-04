package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class getAltitude extends AsyncTask<String, Void, String> {
    private Context mContext = null ;
    ProgressDialog asyncDialog;


    public getAltitude(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        asyncDialog = new ProgressDialog(mContext, android.R.style.Theme_Holo_Light);

        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //asyncDialog.setMessage(mContext.getResources().getString());
        asyncDialog.setCancelable(false);
        asyncDialog.setCanceledOnTouchOutside(false);

        asyncDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String lon = cutStr(strings[0]); //경도 124~134
        String lat = cutStr(strings[1]); //위도 33~43
        System.out.println("lat = " + lat+" lon = " + lon);

        // google elevation api 로 response 받아옴 response 는 json 형태가 아니라 json 을 받을 수 있는 url을 품고 있는 형태
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://maps.googleapis.com/maps/api/elevation/json?locations=" + lat + "," + lon + "&key=AIzaSyBCMR4hCvoxoPclUVQt7I4DcZZjE_cQu3M")
                .method("GET", null)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("고도내놔");
        System.out.println(response.toString());

        double elevation = 0;

        //url 로 json 받아와 원하는 고도 정보만 빼먹는 로직
        try {
            JSONObject jsonObj = readJsonFromUrl(parsingResponse(response.toString()));
            JSONArray resultEl = jsonObj.getJSONArray("results");
            JSONObject current = resultEl.getJSONObject(0);
            elevation = Double.parseDouble(current.getString("elevation"));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("elevation = "+elevation);
        return String.valueOf(elevation);
    }

    //고도를 get으로 땡겨오기 위한 메소드 처리
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (asyncDialog != null && asyncDialog.isShowing()) {
            asyncDialog.dismiss();
        }

        asyncDialog = null;
    }

    // response 에서 json 받아오기 위한 url 만 추출
    public static String parsingResponse(String Response) {

        String[] temp = Response.split("url=");

        String url = temp[1].substring(0, temp[1].length() - 1);
        System.out.println("url = " + url);
        return url;
    }

    // 위도 경도 소수점 7자리까지 자르기
    public static String cutStr(String point) {
        String result = "";
        int j = 0;
        boolean flag = false;
        for (int i = 0; i < point.length(); i++) {
            result += point.charAt(i);
            if (flag) {
                j++;
            }
            if (j == 7) {
                return result;
            }
            if (point.charAt(i) == '.') {
                flag = true;
            }
        }
        return "";
    }

    //모두 읽어 하나의 문자열 만들기
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
    // url 으로 json 받아오는 메소드
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }
}

