package com.example.myapplication;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.RequestHttpURLConnection;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    String uu;
    String appKey = "l7xx47b4e0ab8cf14541ba920ca916d19d30";
    // T Map View
    TMapView tMapView = null;


    String startX = "126.968773";
    Double startx = 126.968773;
    Double starty = 37.4847422;
    Double endx = 126.9723668;
    Double endy = 37.4857433;
    String startY = "37.4847422";
    String endX = "126.9723668";
    String endY = "37.4857433";
    TMapPoint start = new TMapPoint(37.4847422, 126.968773);
    TMapPoint end = new TMapPoint(37.4857433, 126.9723668);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TextView textView = (TextView) findViewById(R.id.textView);


        // T Map View
        tMapView = new TMapView(this);

        // API Key
        tMapView.setSKTMapApiKey(appKey);

        // Initial Setting
        tMapView.setZoomLevel(13);
        tMapView.setHttpsMode(true);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        tMapView.setSightVisible(true);
        tMapView.setTrackingMode(true);//현재위치로 화면을 옮김



        // T Map View Using Linear Layout
        // 지도를 화면에 띄움
        LinearLayout linearLayoutTmap = (LinearLayout) findViewById(R.id.linearLayoutTmap);
        linearLayoutTmap.addView(tMapView);

        Button btntest = (Button) findViewById(R.id.btntest);


        //테스트버튼을 누르면 폴리라인이 그려짐
            btntest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tMapView.setLocationPoint(start.getLongitude(),start.getLatitude());
                    tMapView.setZoomLevel(15);
                    new Thread() {
                        //경로를 찾는 메소드를 두번호출해서 경로가 여러개로 나오는지 실험
                        @Override
                        public void run() {
                            TMapData tMapData = new TMapData();
                            try {
                                TMapPolyLine tMapPolyLine = tMapData.findPathData(start, end);
                                //TMapPolyLine tMapPolyLine1 = tMapData.findPathData(tMapPointStart, tMapPointEnd);
                                tMapPolyLine.setLineColor(Color.GREEN);
                                // tMapPolyLine1.setLineColor(Color.YELLOW);
                                //tMapPolyLine1.setLineWidth(2);
                                tMapPolyLine.setLineWidth(2);
                                tMapView.addTMapPolyLine("Line1", tMapPolyLine);
                                //tMapView.addTMapPolyLine("Line2",tMapPolyLine1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
