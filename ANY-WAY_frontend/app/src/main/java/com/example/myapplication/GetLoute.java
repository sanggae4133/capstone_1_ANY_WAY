package com.example.myapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolylineOverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//출발,도착지를 요청하는 url을 요청해서 경로를 받아 네이버지도폴리라인을 그리는 클래스
public class GetLoute extends AsyncTask<Void, Void, String>  {

    private String url;
    private ContentValues values;
    private NaverMap naverMap;
    ArrayList<LatLng> latLngArrayList=new ArrayList<LatLng>();
    Marker marker = new Marker();
    PolylineOverlay polylineOverlay;
    TextView totalDistanceText;
    TextView totalTimeText;
    public GetLoute(String url, ContentValues values,NaverMap naverMap,TextView totalDistanceText,TextView totalTimeText,PolylineOverlay polylineOverlay) {
        this.url = url;
        this.values = values;
        this.naverMap=naverMap;
        this.totalTimeText=totalTimeText;
        this.totalDistanceText=totalDistanceText;
        this.polylineOverlay=polylineOverlay;
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
            System.out.println("제일 상위 "+root);


            //총 경로 횟수 featuresArray에 저장
            JSONArray featuresArray = root.getJSONArray("features");

            for (int i = 0; i < featuresArray.length(); i++){
                JSONObject featuresIndex = (JSONObject) featuresArray.get(i);
//                    System.out.println("뭐가 저장 됨?"+featuresIndex);
                JSONObject geometry =  featuresIndex.getJSONObject("geometry");

                String type =  geometry.getString("type");

                //type이 LineString일 경우 좌표값이 하나가 아니라 여러개로 책정이 된다.
                //전부 뽑아서 전체경로에 추가해준다.
                //type이 Point일 경우에는 출발점, 경유지, 도착지점 이 세경우 뿐인데
                //세가지는 구분하는 기준은 properties의 pointType으로 구분 가능하다.

                if(type.equals("LineString")){


                    JSONArray coordinatesArray = geometry.getJSONArray("coordinates");

//                        System.out.println("라인이 여러개다"+coordinatesArray);

                    for(int j=0; j<coordinatesArray.length(); j++){

//                            System.out.println(coordinatesArray.get(j).getClass().getName());

                        JSONArray pointArray = (JSONArray) coordinatesArray.get(j);
                        double longitude =Double.parseDouble(pointArray.get(0).toString());
                        double latitude =Double.parseDouble(pointArray.get(1).toString());

                        latLngArrayList.add(new LatLng(latitude, longitude));
                        System.out.println("LineString를 저장 ");
//                            System.out.println("만들어진 어레이는  "+latLngArrayList);
//                            System.out.println("총저장된 경로의 갯수는"+latLngArrayList.size());
                    }
                }

                if(type.equals("Point")){
                    JSONObject properties =  featuresIndex.getJSONObject("properties");
                    try{
                        double totalDistance = Integer.parseInt(properties.getString("totalDistance"));


                        totalDistanceText.setText("총 거리 :"+totalDistance/1000+" km");

                        int totalTime = Integer.parseInt(properties.getString("totalTime"));
                        totalTimeText.setText("총 거리 :"+ totalTime/60+"분");

                    }catch (Exception e){

                    }

                    String pointType = properties.getString("pointType");


                    double longitude =  Double.parseDouble(geometry.getJSONArray("coordinates").get(0).toString());
                    double latitude =  Double.parseDouble(geometry.getJSONArray("coordinates").get(1).toString());
//                        System.out.println("Point를 저장 ");
//                        latLngArrayList.add(new LatLng(latitude, longitude));

                    if(pointType.equals("SP")){
                        System.out.println("시작지점이다");

                    }
                    else if(pointType.equals("GP")){

                        System.out.println("중간지점이다");
                    }
                    else if(pointType.equals("EP")){

                        System.out.println("끝지점이다");

                    }
//                        marker.setPosition(new LatLng(latitude, longitude));
//                        System.out.println(latitude+","+longitude);
//                        marker.setMap(naverMap);
                }

                System.out.println("총저장된 경로의 갯수는"+latLngArrayList.size());


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        polylineOverlay.setCoords(latLngArrayList);
        polylineOverlay.setWidth(10);
        polylineOverlay.setPattern(10, 5);
        polylineOverlay.setColor(Color.GREEN);
        polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
        polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);

        polylineOverlay.setMap(naverMap);

        //출발,도착지점이 화면에 경로를 중심으로 보이도록 카메라 줌 설정
        LatLngBounds latLngBounds=new LatLngBounds( latLngArrayList.get(0), latLngArrayList.get(latLngArrayList.size()-1));
        CameraUpdate cameraUpdate=CameraUpdate.fitBounds(latLngBounds);
        naverMap.moveCamera(cameraUpdate);





    }
}