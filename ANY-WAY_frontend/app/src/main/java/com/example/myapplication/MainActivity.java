package com.example.myapplication;
import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    //지자기, 가속도 센서를 활용해 위치를 반환하는 구현체
    // 구글 플레이서비스의 FusedLocationProviderClient도 사용한다.
    //FusedLocationProviderClient란 통합 위치 제공자와 상호 작용하기 위한 기본 진입점.
    //요약하면 센서들을 활용해서 위치를 반환하는 클래스다
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Geocoder geocoder;
    ArrayList<LatLng> latLngArrayList;
    PolylineOverlay polylineOverlay=new PolylineOverlay();
    Marker markerStart;
    Marker markerEnd;
    TextView totalDistanceText;
    TextView totalTimeText;
    EditText editTextStart;
    EditText editTextEnd;
    Button Button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        markerStart = new Marker();
        markerEnd=new Marker();
        totalDistanceText=findViewById(R.id.totalDistance);
        totalTimeText=findViewById(R.id.totalTime);
        editTextStart = findViewById(R.id.editTextStart);
        editTextEnd=findViewById(R.id.editTextEnd);
        Button = findViewById(R.id.button);

        //지도 사용권한을 받아 온다.
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        //getMapAsync를 호출하여 비동기로 onMapReady콜백 메서드 호출
        //onMapReady에서 NaverMap객체를 받음
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("여기는 스타트");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨


            }
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        geocoder = new Geocoder(this, Locale.KOREAN);
        //네이버 맵에 locationSource를 셋하면 위치 추적 기능을 사용 할 수 있다
        naverMap.setLocationSource(locationSource);
        //위치 추적 모드 지정 가능 내 위치로 이동
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
//        //현재위치 버튼 사용가능
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        LatLng initialPosition = new LatLng(37.506855, 127.066242);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);


        // 카메라 이동 되면 호출 되는 이벤트
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int reason, boolean animated) {


            }
        });

        //검색을 하면 검색한 좌표에 마커를 찍어준다.
        // 버튼 이벤트
        Button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                //누를때마다 새로운 마커랑 폴리라인이 기존것과 중복안되게 null처리
                markerStart.setMap(null);
                markerEnd.setMap(null);
                polylineOverlay.setMap(null);

                //입력받은 주소 변환
                String strStart=editTextStart.getText().toString();
                String strEnd=editTextEnd.getText().toString();
                List<Address> addressListStart = null,addressListEnd = null;
                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressListStart = geocoder.getFromLocationName(
                            strStart, // 주소
                            10); // 최대 검색 결과 개수
                    addressListEnd = geocoder.getFromLocationName(
                            strEnd, // 주소
                            10); // 최대 검색 결과 개수
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                    //지오코딩으로 받은 주소를 위도,경도로 파싱하는 클래스
                    AddressPasing addressPasingStart=new AddressPasing(addressListStart);
                    AddressPasing addressPasingEnd=new AddressPasing(addressListEnd);

                    try {
                        // 좌표(위도, 경도) 생성
                        LatLng Startpoint = new LatLng(Double.parseDouble(addressPasingStart.getlatitude()), Double.parseDouble(addressPasingStart.getlongitude()));
                        LatLng Endpoint = new LatLng(Double.parseDouble(addressPasingEnd.getlatitude()), Double.parseDouble(addressPasingEnd.getlongitude()));
                        // 마커 표시
                        markerStart.setPosition(Startpoint);
                        markerEnd.setPosition(Endpoint);
                        // 마커 추가
                        markerStart.setMap(naverMap);
                        markerEnd.setMap(naverMap);

                        //시작,도착지점을 Tmap서버에 보내기위해 url형식대로 만들어줌
                        TMapWalkerTrackerURL(Startpoint, Endpoint);
                        //System.out.println(url);
                    }
                    catch (IndexOutOfBoundsException e){
                        //없는 주소를 입력시 다시 입력하라는 팝업이 뜬다.
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("없는 주소입니다.다시 입력해주세요.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {      // 버튼1 (직접 작성)
                                    public void onClick(DialogInterface dialog, int which){
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {     // 버튼2 (직접 작성)
                                    public void onClick(DialogInterface dialog, int which){
                                    }
                                })
                                .show();


                    }
                    //검색완료 후 키보드 내리기
                    InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                    // 해당 좌표로 화면 줌
//                    naverMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));

                /*
                    CameraUpdate cameraUpdate = CameraUpdate.scrollTo(Startpoint);
                    naverMap.moveCamera(cameraUpdate);

                 */

            }
        });

    }


    // 현재 카메라가 보고있는 위치
    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }


    //TMap보행자 경로를 검색해주는 메서드
    //출발좌표와 도팍 좌표를 입력하여 보행자길찾기가 가능하다
    public void TMapWalkerTrackerURL(LatLng startPoint, LatLng endPoint) {

        String url = null;


        try {
            String appKey = "l7xx47b4e0ab8cf14541ba920ca916d19d30";


            String startX = new Double(startPoint.longitude).toString();
            String startY = new Double(startPoint.latitude).toString();
            String endX = new Double(endPoint.longitude).toString();
            String endY = new Double(endPoint.latitude).toString();

            String startName = URLEncoder.encode("출발지", "UTF-8");

            String endName = URLEncoder.encode("도착지", "UTF-8");
            url = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=result&appKey=" + appKey
                    + "&startX=" + startX + "&startY=" + startY + "&endX=" + endX + "&endY=" + endY
                    + "&startName=" + startName + "&endName=" + endName;


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //url을 넘겨 tmap에서 받은 경로를 네이버지도로 표시
        GetLoute getLoute=new GetLoute(url,null,naverMap,totalDistanceText,totalTimeText,polylineOverlay);
        getLoute.execute();
    }
    //맵검색을 비동기 식으로 처리한다.
    /* 현재위치기반 길찾기는 구현안할수도있으니 일단 보류
    public class MapSearchTask extends AsyncTask<Void, Void, String>{
        String str=editText.getText().toString();
        List<Address> addressList = null;

        @Override
        protected String doInBackground(Void... voids) {

            try {
                // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                addressList = geocoder.getFromLocationName(
                        str, // 검색키워드
                        10); // 최대 검색 결과 개수
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            AddressPasing addressPasing=new AddressPasing(addressList);
            String result= addressPasing.result();

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            String[] latlong = result.split(",");
//            System.out.println("위도 텍스트로 바꾸면 어떻게 가지고 오냐 "+latlong[0]);
            double lat = Double.parseDouble(latlong[0]);
            double lon = Double.parseDouble(latlong[1]);
//            System.out.println("위도 텍스트로 바꾸면 어떻게 가지고 오냐 "+lat);
//            System.out.println("경도 텍스트로 바꾸면 어떻게 가지고 오냐 "+lon);


            //검색한 좌표를 만들어준다
            LatLng endPoint = new LatLng(lat, lon);


            // 마커 생성
            Marker marker = new Marker();
            marker.setPosition(endPoint);
            // 마커 추가
            marker.setMap(naverMap);



            //현재위치를 가지고 온다
            // 아직 현재위치 기반 길찾기는 todo로
            GpsTracker gpsTracker = new GpsTracker(MainActivity.this);
            double currentLatitude = gpsTracker.getLatitude();
            double currentLongitude = gpsTracker.getLongitude();


           // LatLng startPoint = new LatLng(currentLatitude,currentLongitude);

            //현재 보고있는 화면의 중심을 기준으로 좌표를 만들어주는 메서드
            LatLng startPoint=getCurrentPosition(naverMap);


            //검색한 좌표와 현재 위치를 넣어서 url을 가지고 온다.
             String url=TMapWalkerTrackerURL(startPoint, endPoint);

            //검색한 url을 가지고 데이터를 파싱한다
             GetLoute getLoute = new GetLoute(url, null,naverMap,totalDistanceText,totalTimeText);
             getLoute.execute();

//            //검색한 좌표로 카메라 이동
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(endPoint);
            naverMap.moveCamera(cameraUpdate);
        }
    }

     */
    // 마커 정보 저장시킬 변수들 선언
    private Vector<LatLng> markersPosition;
    private Vector<Marker> activeMarkers;


    // 선택한 마커의 위치가 가시거리(카메라가 보고있는 위치 반경 3km 내)에 있는지 확인
    public final static double REFERANCE_LAT = 1 / 109.958489129649955;
    public final static double REFERANCE_LNG = 1 / 88.74;
    public final static double REFERANCE_LAT_X3 = 3 / 109.958489129649955;
    public final static double REFERANCE_LNG_X3 = 3 / 88.74;
    public boolean withinSightMarker(LatLng currentPosition, LatLng markerPosition) {
        boolean withinSightMarkerLat = Math.abs(currentPosition.latitude - markerPosition.latitude) <= REFERANCE_LAT_X3;
        boolean withinSightMarkerLng = Math.abs(currentPosition.longitude - markerPosition.longitude) <= REFERANCE_LNG_X3;
        return withinSightMarkerLat && withinSightMarkerLng;
    }

    // 지도상에 표시되고있는 마커들 지도에서 삭제
    private void freeActiveMarkers() {
        if (activeMarkers == null) {
            activeMarkers = new Vector<Marker>();
            return;
        }
        for (Marker activeMarker: activeMarkers) {
            activeMarker.setMap(null);
        }
        activeMarkers = new Vector<Marker>();
    }
}





