package com.example.myapplication;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NetworkTask extends AsyncTask<Void, Void, String> {
    private String url;
    private ContentValues values;
    //point index
     int i =0;
    ArrayList<String> list=new ArrayList<>();

    public NetworkTask(String url, ContentValues values) {
        this.url = url;
        this.values = values;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(Void... params) {
        String result;
        // 요청 결과를 저장할 변수.
        RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
        result = requestHttpURLConnection.request(url, values);
        // 해당 URL로 부터 결과물을 얻어온다.
        return result;
    }

    @Override
    protected void onPostExecute(String s) {

        super.onPostExecute(s);
        //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.

           try {


                //전체 데이터를 제이슨 객체로 변환
                JSONObject root = new JSONObject(s);
                System.out.println("제일 상위 " + root);

                //전체 데이터중에 features리스트의 첫번째 객체를 가지고 오기
                JSONObject features=(JSONObject) root.getJSONArray("features").get(i);
                System.out.println("상위에서 첫번째 리스트 " + features);

                //리스트의 첫번째 객체에 있는 geometry가져오기
                JSONObject geometry = features.getJSONObject("geometry");
                //String name=geometry.getString("coordinates");
                list.add(geometry.getString("coordinates"));







                //최종적으로 위도와 경도를 가져온다.
               /* String coordinates = geometry.getJSONArray("coordinates").get(0).toString();
                int init_idx_longitude=coordinates.indexOf("[")+1;
                int end_idx_longitude=coordinates.indexOf(",");
                int init_idx_latitude=coordinates.indexOf(",")+2;
                int end_idx_latitude=coordinates.indexOf("]");
              String logitude=coordinates.substring(init_idx_longitude,end_idx_longitude);
                String latitude=coordinates.substring(init_idx_latitude,end_idx_latitude);
*/


                //System.out.println(coordinates);
//              textView.setText((CharSequence) root);


            } catch (JSONException e) {
                e.printStackTrace();
            }



    }
}
