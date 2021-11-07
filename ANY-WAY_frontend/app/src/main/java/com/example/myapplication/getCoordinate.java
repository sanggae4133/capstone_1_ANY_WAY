package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.example.myapplication.AddressPasing;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class getCoordinate extends AsyncTask<String, Void, ArrayList<String>> {
    private Geocoder geocoder;
    private Context mContext = null ;
    ProgressDialog asyncDialog;

    public getCoordinate(Context context) {
        mContext = context;
        geocoder= new Geocoder(mContext, Locale.KOREAN);;
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
    protected ArrayList<String> doInBackground(String... strings) {
        String strStart = strings[0];
        String strEnd = strings[1];
        System.out.println("strStart = " + strStart+" strEnd = " + strEnd);

        List<Address> addressListStart = null,addressListEnd = null;
        try {
            // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
            addressListStart = geocoder.getFromLocationName(strStart, // 주소
                    10); // 최대 검색 결과 개수
            addressListEnd = geocoder.getFromLocationName(strEnd, // 주소
                    10); // 최대 검색 결과 개수
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        AddressPasing startAddress = new AddressPasing(addressListStart);
        AddressPasing endAddress = new AddressPasing(addressListEnd);
        ArrayList<String> result = new ArrayList<>();

        return result;
    }

    //고도를 get으로 땡겨오기 위한 메소드 처리
    @Override
    protected void onPostExecute(ArrayList<String> result) {
        super.onPostExecute(result);
        if (asyncDialog != null && asyncDialog.isShowing()) {
            asyncDialog.dismiss();
        }

        asyncDialog = null;
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



}
