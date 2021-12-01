package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.MultipartPathOverlay;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    ArrayList<LatLng> latLngArrayList, impossibleRoad;
    PolylineOverlay polylineOverlay;
    Marker markerStart;
    Marker markerEnd;
    TextView totalDistanceText;
    TextView totalTimeText;
    EditText editTextStart;
    EditText editTextEnd;
    Button Button, getCurPosition, routbutton, changeBtn;
    getAltitude getAltitudes;
    getCoordinate getCoordinates;
    Handler handler = new Handler();
    arraylistHandler alHandler = new arraylistHandler();
    Context context;


    static String startLatG, endLatG, startLonG, endLonG;
    static ArrayList<LatLng> addressList;
    static Double Elevation, curLatG, curLonG, troubleLat, troubleLon;
    static boolean getInTrouble, isCurP;
    static PriorityQueue<Loute> possibleRoad;
    static double totalDistanceG, curLat,curLon,lonGap, latGap;
    static int totalTimeG, latOp, lonOp;

    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        markerStart = new Marker();
        markerEnd = new Marker();
        totalDistanceText = findViewById(R.id.totalDistance);
        totalTimeText = findViewById(R.id.totalTime);
        editTextStart = findViewById(R.id.editTextStart);
        editTextEnd = findViewById(R.id.editTextEnd);
        Button = findViewById(R.id.button);
        changeBtn = findViewById(R.id.changeBTN);
        routbutton = findViewById(R.id.routbutton);
        getCurPosition = findViewById(R.id.getCurPosition);
        polylineOverlay = new PolylineOverlay();
        context = getApplicationContext();

        isCurP = false;
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
        Button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //누를때마다 새로운 마커랑 폴리라인이 기존것과 중복안되게 null처리
                markerStart.setMap(null);
                markerEnd.setMap(null);
                polylineOverlay.setMap(null);
                startLatG = null;
                startLonG = null;
                endLatG = null;
                endLonG = null;
                //getAltitudes = new getAltitude(MainActivity.this); // 고도 정보 관련
                //getCoordinates = new getCoordinate(MainActivity.this);
                //입력받은 주소 변환
                String strStart = editTextStart.getText().toString();
                String strEnd = editTextEnd.getText().toString();
                getInTrouble = false;
                isCurP = false;


                ArrayList<String> coordinate = null;
                List<Address> addressListStart = null, addressListEnd = null;
                possibleRoad = new PriorityQueue<Loute>();

                Log.i("백그라운드 스레드 시작", " ");

                //백그라운스레드 == 주소 ->좌표변환  좌표는 전역변수
                BackgroundThread coordinateThread = new BackgroundThread(strStart, strEnd);
                coordinateThread.start();

                try {
                    coordinateThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("백그라운드 스레드 종료", " ");

                //바뀐 전역변수(좌표) 출력
                System.out.println(startLatG + " " + startLonG + " " + endLatG + " " + endLonG);

                //좌표들의 유효성 체크
                if (startLatG == null || startLonG == null || endLatG == null || endLonG == null||
                        startLatG.equals("true") || startLonG.equals("true") || endLatG.equals("true") || endLonG.equals("true")) {
                    System.out.println("검색 실패 예외처리");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("에러").setMessage("주소 검색 실패");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    curLatG = Double.parseDouble(startLatG);
                    curLonG = Double.parseDouble(startLonG);
                    double nextLat = Double.parseDouble(endLatG);
                    double nextLon = Double.parseDouble(endLonG);
                    LatLng Startpoint = new LatLng(curLatG, curLonG);
                    LatLng Endpoint = new LatLng(Double.parseDouble(endLatG), Double.parseDouble(endLonG));

                    //출발 끝 중간 좌표
                    troubleLat = (curLatG + nextLat) / 2;
                    troubleLon = (curLonG + nextLon) / 2;
                    //위도 경도 차 절대값
                    latGap = Math.abs(curLatG - nextLat);
                    lonGap = Math.abs(curLonG - nextLon);

                    //수직방향 정하기위한 ㄱㅈㄹ
                    if ((curLatG - nextLat) * (curLonG - nextLon) < 0) {
                        System.out.println("음수" + (curLonG - nextLon) + " " + (curLatG - nextLat));
                        lonOp = 1;
                        latOp = 1;
                    } else {
                        System.out.println("양수"+(curLonG - nextLon)+" "+(curLatG - nextLat));
                        latOp = 1;
                        lonOp = -1;
                    }

                    // 마커 표시
                    markerStart.setPosition(Startpoint);
                    markerEnd.setPosition(Endpoint);
                    markerStart.setIconTintColor(Color.BLUE);
                    markerEnd.setIconTintColor(Color.CYAN);
                    // 마커 추가
                    markerStart.setMap(naverMap);
                    markerEnd.setMap(naverMap);

                    //url 따고
                    String url = TMapWalkerTrackerURL(Startpoint, Endpoint);

                    //총거리 총시간 변수
                    totalDistanceG = 0;
                    totalTimeG = 0;
                    // 길 리스트, 못가는 길 리스트
                    latLngArrayList = new ArrayList<>();
                    latLngArrayList.add(Startpoint);
                    impossibleRoad = new ArrayList<>();

                    //길찾는 스레드
                    getLouteThread getLouteThread = new getLouteThread(url);
                    getLouteThread.start();
                    Log.i("getLoute 스레드 시작", " ");
                    try {
                        getLouteThread.join();
                    } catch (InterruptedException e) {

                    }
                    Log.i("getLoute 스레드 끝", " ");

                    if(getInTrouble){
                        loop:
                        for (int i = 1; i <= 3; i++) {
                            for (int j = -1; j < 2; j+=2) {
                                totalDistanceG = 0;
                                totalTimeG = 0;
                                latLngArrayList = new ArrayList<>();
                                latLngArrayList.add(Startpoint);
                                impossibleRoad = new ArrayList<>();

                                //숫자 a, b 로 gap의 사이즈 조절
                                double moveLat = (latOp * j * (1 * i * lonGap)) / 5;
                                double moveLon = (lonOp * j * (1 * i * latGap)) / 5;

                                String testLat = String.valueOf(troubleLat - moveLat);
                                String testLon = String.valueOf(troubleLon - moveLon);
                                PolylineOverlay testPolyLine = new PolylineOverlay();

                                System.out.println("latGap = " + latGap + " lonGap = " + lonGap);
                                System.out.println("moveLat = " + moveLat + " moveLon = " + moveLon);
                                System.out.println("testLat = " + testLat + " testLon = " + testLon);

                                //중간 지점 마커 찍는거
                                try {
                                    Marker marker1 = new Marker(new LatLng(troubleLat, troubleLon));
                                    marker1.setIconTintColor(Color.RED);
                                    marker1.setMap(naverMap);

                                    Marker marker = new Marker(new LatLng(Double.parseDouble(testLat), Double.parseDouble(testLon)));
                                    marker.setIconTintColor(Color.YELLOW);
                                    marker.setMap(naverMap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                // 경유지 추가해서 돌림
                                MyRunnable r = new MyRunnable(url + "&&passList=" + testLon + "," + testLat + "&searchOption=10");
                                Thread thread = new Thread(r);
                                thread.start();
                                try {
                                    thread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                System.out.println("갈수있는 경로 좌표 사이즈 : " + latLngArrayList.size());
                                System.out.println("경사도 초과 좌표 사이즈 : " + impossibleRoad.size());

                                //갈수없는 길이 없는 경우 == 올바른 길
                                if (impossibleRoad.size() < 2) {
                                    latLngArrayList.add(Endpoint);
                                    System.out.println("올바른 길");

                                    break loop;
                                } else {
                                    //갈 수 없는 길이 포함된 경우
                                    //갈 수 없는 길은 빨간색 갈 수 있는 길은 노란색으로 칠함
                                    testPolyLine.setCoords(latLngArrayList);
                                    testPolyLine.setWidth(10);
                                    testPolyLine.setPattern(10, 5);
                                    testPolyLine.setColor(Color.MAGENTA);
                                    testPolyLine.setCapType(PolylineOverlay.LineCap.Round);
                                    testPolyLine.setJoinType(PolylineOverlay.LineJoin.Round);
                                    testPolyLine.setMap(naverMap);

                                    MultipartPathOverlay multipartPathOverlay = new MultipartPathOverlay();
                                    multipartPathOverlay.setCoordParts(Arrays.asList(latLngArrayList, impossibleRoad));
                                    multipartPathOverlay.setColorParts(Arrays.asList(new MultipartPathOverlay.ColorPart(Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW),
                                            new MultipartPathOverlay.ColorPart(Color.RED, Color.RED, Color.RED, Color.RED)));
                                    multipartPathOverlay.setMap(naverMap);
                                }
                            }
                        }
                    }

                    //길의 유효성 체크 통과되면
                    if (latLngArrayList.size() > 1) {
                        polylineOverlay.setCoords(latLngArrayList);
                        polylineOverlay.setWidth(10);
                        polylineOverlay.setPattern(10, 5);
                        polylineOverlay.setColor(Color.GREEN);
                        polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
                        polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
                        polylineOverlay.setMap(naverMap);

                        totalDistanceText.setText("총 거리 :" + totalDistanceG / 1000 + " km");
                        totalTimeText.setText("총 거리 :" + (totalTimeG * 1.5) / 60 + "분");
                        // 시간에 1.5 배 곱하는 이유는 장애인의 평균적인 보행속도가 일반적인 경우에 비해 65% 정도라고 하기에

                        // 영역이 온전히 보이는 좌표와 최대 줌 레벨로 카메라의 위치를 변경합니다.
                        // 경로의 첫번째포인트,마지막포인트를 지도에 꽉차게 보여줌
                        LatLng firstLatlng = latLngArrayList.get(0);
                        LatLng lastLatlng = latLngArrayList.get(latLngArrayList.size() - 1);
                        LatLngBounds latLngBounds = new LatLngBounds(firstLatlng, lastLatlng);
                        CameraUpdate cameraUpdate = CameraUpdate.fitBounds(latLngBounds);
                        naverMap.moveCamera(cameraUpdate);
                    }else{
                        Log.i("에러", "경로 X");
                    }


                }
            }
        });

        changeBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp;

                temp = startLatG;
                startLatG = endLatG;
                endLatG = temp;

                temp = startLonG;
                startLonG = endLonG;
                endLonG = temp;

                temp = String.valueOf(editTextStart.getText());
                editTextStart.setText(editTextEnd.getText());
                editTextEnd.setText(temp);

            }
        });

        getCurPosition.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //사용자의 위치 수신을 위한 세팅
                locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                //사용자의 현재 위치
                Location userLocation = getMyLocation();
                if( userLocation != null ) {
                    double latitude = userLocation.getLatitude();
                    double longitude = userLocation.getLongitude();

                    System.out.println("////////////현재 내 위치값 : "+latitude+","+longitude);

                    editTextStart.setText("현위치");
                    curLat = latitude;
                    curLon = longitude;
                }else{
                    System.out.println("에러 : getMyLocation 에서 받아온 정보가 null");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("에러").setMessage("현위치 정보 못 가져옴");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }
        });

        //버튼을 누르면 gps작동, 이제 위치이동이벤트가 발생할때 좌표를 받고 tts를 구현하자
        routbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //길찾기버튼을 누르면 확대되면서 tts서비스
                System.out.print("해치웠나?");
                LatLng firstLatlng = latLngArrayList.get(0);
                double latitute=firstLatlng.latitude;
                double longtitute=firstLatlng.longitude;
                System.out.println(firstLatlng.latitude);
                CameraPosition cameraPosition = new CameraPosition(
                        new LatLng(latitute,longtitute),   // 위치 지정
                        18,                           // 줌 레벨
                        45,                          // 기울임 각도
                        45                           // 방향
                );
                naverMap.setCameraPosition(cameraPosition);
                naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
            }
        });

    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation(); //이건 써도되고 안써도 되지만, 전 권한 승인하면 즉시 위치값 받아오려고 썼습니다!
        }
        else {
            System.out.println("////////////권한요청 안해도됨");

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            } else {
                System.out.println("에러 : 위치정보 못받아옴");
            }
        }
        return currentLocation;
    }


    //검색완료 후 키보드 내리기
//                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


    // 해당 좌표로 화면 줌
//                    naverMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));

                /*
                    CameraUpdate cameraUpdate = CameraUpdate.scrollTo(Startpoint);
                    naverMap.moveCamera(cameraUpdate);

                 */


    // 현재 카메라가 보고있는 위치
    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }


    //TMap보행자 경로를 검색해주는 메서드
    //출발좌표와 도팍 좌표를 입력하여 보행자길찾기가 가능하다
    public String TMapWalkerTrackerURL(LatLng startPoint, LatLng endPoint) {

        String url = null;

        try {
            String appKey = "l7xx47b4e0ab8cf14541ba920ca916d19d30";

            String startX = new Double(startPoint.longitude).toString();
            String startY = new Double(startPoint.latitude).toString();
            String endX = new Double(endPoint.longitude).toString();
            String endY = new Double(endPoint.latitude).toString();

            System.out.println("출발 좌표: " + startX + " " + startY + " 도착 좌표: " + endX + " " + endY);
            String startName = URLEncoder.encode("출발지", "UTF-8");

            //System.out.println(getAltitudes.execute(startX, startY).get());
            //getAltitudes.execute(startX, startY) 으로 실행 하고 get() 하면 고도(문자열)로 리턴함

            String endName = URLEncoder.encode("도착지", "UTF-8");
            url = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=result&appKey=" + appKey
                    + "&startX=" + startX + "&startY=" + startY + "&endX=" + endX + "&endY=" + endY
                    + "&startName=" + startName + "&endName=" + endName;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }


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
        for (Marker activeMarker : activeMarkers) {
            activeMarker.setMap(null);
        }
        activeMarkers = new Vector<Marker>();
    }

    class getAltitudeThread extends Thread {
        String lat;
        String lon;

        public getAltitudeThread(Double lat, Double lon) {
            this.lat = String.valueOf(lat);
            this.lon = String.valueOf(lon);
        }

        @Override
        public void run() {

            //System.out.println("lat = " + lat+" lon = " + lon);

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
            response.close();
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

            Elevation = elevation;

            System.out.println("lat = " + lat + " lon = " + lon + " elevation = " + elevation);
        }

        //모두 읽어 하나의 문자열 만들기
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
        // url 으로 json 받아오는 메소드
        public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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
        // response 에서 json 받아오기 위한 url 만 추출
        public String parsingResponse(String Response) {

            String[] temp = Response.split("url=");

            String url = temp[1].substring(0, temp[1].length() - 1);
            //System.out.println("url = " + url);
            return url;
        }
    }

    class MyRunnable implements Runnable {
        String url;
        double curElevation;
        double nextElevation;

        public MyRunnable(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

            String result;
            // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, new ContentValues());
            double totalDistance = 0;
            int totalTime = 0;
            curElevation = 0;
            nextElevation = 0;

            ArrayList<LatLng> loute = new ArrayList<>();
            // 해당 URL로 부터 결과물을 얻어온다.
            try {
                //전체 데이터를 제이슨 객체로 변환
                JSONObject root = new JSONObject(result);
                //System.out.println("제일 상위 ");
                // System.out.println("result = \n" + result);
                //총 경로 횟수 featuresArray에 저장
                JSONArray featuresArray = root.getJSONArray("features");
                double longitude = 0, nextLongitude = 0;
                double latitude = 0, nextLatitude = 0;
                double startLat = 0, startLon = 0;
                double endLat = 0, endLon = 0;
                //double elevation = 0, nextElevation = 0;

                Loop:
                for (int i = 0; i < featuresArray.length(); i++) {
                    JSONObject featuresIndex = (JSONObject) featuresArray.get(i);
                    JSONObject geometry = featuresIndex.getJSONObject("geometry");

                    String type = geometry.getString("type");
                    //type이 LineString일 경우 좌표값이 하나가 아니라 여러개로 책정이 된다.
                    //전부 뽑아서 전체경로에 추가해준다.
                    //type이 Point일 경우에는 출발점, 경유지, 도착지점 이 세경우 뿐인데
                    //세가지는 구분하는 기준은 properties의 pointType으로 구분 가능하다.

                    if (type.equals("LineString")) {
                        JSONArray coordinatesArray = geometry.getJSONArray("coordinates");
                        System.out.println("coordinatesArray.length() = " + coordinatesArray.length());

                        for (int k = 0; k < coordinatesArray.length(); k++) {
                            JSONArray pointArray = (JSONArray) coordinatesArray.get(k);

                            longitude = Double.parseDouble(pointArray.get(0).toString());
                            latitude = Double.parseDouble(pointArray.get(1).toString());

                            //System.out.println("latitude = " + latitude + " longitude = " + longitude);

                            latLngArrayList.add(new LatLng(latitude, longitude));
                        }


                        JSONArray pointArray = (JSONArray) coordinatesArray.get(0);
                        startLon = Double.parseDouble(pointArray.get(0).toString());
                        startLat = Double.parseDouble(pointArray.get(1).toString());

                        pointArray = (JSONArray) coordinatesArray.get(coordinatesArray.length() - 1);
                        endLon = Double.parseDouble(pointArray.get(0).toString());
                        endLat = Double.parseDouble(pointArray.get(1).toString());

                        JSONObject properties = featuresIndex.getJSONObject("properties");
                        double distance = Double.parseDouble(properties.getString("distance"));
                        int time = Integer.parseInt(properties.getString("time"));

                        System.out.println("distance = " + distance);
                        totalDistance += distance;
                        totalTime += time;

                        //System.out.println("경로 중 지점 elevation = " + elevation);

                        thread2(startLat, startLon);

                        thread3(endLat, endLon);

                        double zDistance = Math.abs(nextElevation - curElevation);
                        double gradient = (zDistance / distance);
                        System.out.println("거리 " + distance + " 높이 " + zDistance + " " + zDistance / distance);
                        System.out.println("경사도 : " + gradient);

                        curLatG = endLat;
                        curLonG = endLon;

                        if (gradient > 0.083 && gradient < 0.3) {
                            System.out.println("경사도 초과");
                            impossibleRoad.add(new LatLng(startLat, startLon));
                            impossibleRoad.add(new LatLng(endLat, endLon));
//                            troubleLat = (startLat + endLat) / 2;
//                            troubleLon = (startLon + endLon) / 2;
//                            latGap = Math.abs(endLat - startLat);
//                            lonGap = Math.abs(endLon - startLon);
                            getInTrouble = true;
                            //break Loop;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                impossibleRoad.add(new LatLng(0, 0));
                impossibleRoad.add(new LatLng(1, 1));
            }
            System.out.println("totalDistance = " + totalDistance);
            System.out.println("totalTime = " + totalTime);

            totalDistanceG = totalDistance;
            totalTimeG = totalTime;
        }


        public void thread2(double lat, double lon) {
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
            response.close();
            curElevation = elevation;

            System.out.println("lat = " + lat + " lon = " + lon + " elevation = " + elevation);
        }
        public void thread3(double lat, double lon) {
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
            response.close();
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

            nextElevation = elevation;

            System.out.println("lat = " + lat + " lon = " + lon + " elevation = " + elevation);
        }

        //모두 읽어 하나의 문자열 만들기
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
        // url 으로 json 받아오는 메소드
        public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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
        // response 에서 json 받아오기 위한 url 만 추출
        public String parsingResponse(String Response) {

            String[] temp = Response.split("url=");

            String url = temp[1].substring(0, temp[1].length() - 1);
            //System.out.println("url = " + url);
            return url;
        }
    }


    class getLouteThread extends Thread {
        String url;

        public getLouteThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            //Message message = alHandler.obtainMessage();

            String result;
            // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, new ContentValues());
            double totalDistance = 0;
            int totalTime = 0;
            // 해당 URL로 부터 결과물을 얻어온다.
            try {
                //전체 데이터를 제이슨 객체로 변환
                JSONObject root = new JSONObject(result);
                //System.out.println("제일 상위 ");
                System.out.println("result = \n" + result);
                //총 경로 횟수 featuresArray에 저장
                JSONArray featuresArray = root.getJSONArray("features");
                double longitude = 0, nextLongitude = 0;
                double latitude = 0, nextLatitude = 0;
                double startLat = 0, startLon = 0;
                double endLat = 0, endLon = 0;
                double elevation = 0, nextElevation;

                Loop:
                for (int i = 0; i < featuresArray.length(); i++) {
                    JSONObject featuresIndex = (JSONObject) featuresArray.get(i);
                    JSONObject geometry = featuresIndex.getJSONObject("geometry");

                    String type = geometry.getString("type");
                    //type이 LineString일 경우 좌표값이 하나가 아니라 여러개로 책정이 된다.
                    //전부 뽑아서 전체경로에 추가해준다.
                    //type이 Point일 경우에는 출발점, 경유지, 도착지점 이 세경우 뿐인데
                    //세가지는 구분하는 기준은 properties의 pointType으로 구분 가능하다.

                    if (type.equals("LineString")) {
                        JSONArray coordinatesArray = geometry.getJSONArray("coordinates");
                        System.out.println("coordinatesArray.length() = " + coordinatesArray.length());

                        for (int k = 0; k < coordinatesArray.length(); k++) {
                            JSONArray pointArray = (JSONArray) coordinatesArray.get(k);

                            longitude = Double.parseDouble(pointArray.get(0).toString());
                            latitude = Double.parseDouble(pointArray.get(1).toString());

                            //System.out.println("latitude = " + latitude + " longitude = " + longitude);

                            latLngArrayList.add(new LatLng(latitude, longitude));
                        }


                        JSONArray pointArray = (JSONArray) coordinatesArray.get(0);
                        startLon = Double.parseDouble(pointArray.get(0).toString());
                        startLat = Double.parseDouble(pointArray.get(1).toString());

                        pointArray = (JSONArray) coordinatesArray.get(coordinatesArray.length() - 1);
                        endLon = Double.parseDouble(pointArray.get(0).toString());
                        endLat = Double.parseDouble(pointArray.get(1).toString());

                        JSONObject properties = featuresIndex.getJSONObject("properties");
                        double distance = Double.parseDouble(properties.getString("distance"));
                        int time = Integer.parseInt(properties.getString("time"));

                        System.out.println("distance = " + distance);
                        totalDistance += distance;
                        totalTime += time;

                        //System.out.println("경로 중 지점 elevation = " + elevation);

                        getAltitudeThread thread = new getAltitudeThread(startLat, startLon);
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        elevation = Elevation;

                        getAltitudeThread thread2 = new getAltitudeThread(endLat, endLon);
                        thread2.start();
                        try {
                            thread2.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nextElevation = Elevation;

                        double zDistance = Math.abs(nextElevation - elevation);
                        double gradient = (zDistance / distance);
                        System.out.println("거리 " + distance + " 높이 " + zDistance + " " + zDistance / distance);
                        System.out.println("경사도 : " + gradient);

                        curLatG = endLat;
                        curLonG = endLon;

                        //경사도
                        if (gradient > 0.083 && gradient < 0.3) {
                            System.out.println("경사도 초과");
                            impossibleRoad.add(new LatLng(startLat, startLon));
                            impossibleRoad.add(new LatLng(endLat, endLon));
                            getInTrouble = true;
                            break Loop;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                impossibleRoad.add(new LatLng(0, 0));
                impossibleRoad.add(new LatLng(1, 1));
            }
            System.out.println("totalDistance = " + totalDistance);
            System.out.println("totalTime = " + totalTime);

            totalDistanceG = totalDistance;
            totalTimeG = totalTime;
        }

    }

    static class Loute implements Comparator<Loute> {
        ArrayList<LatLng> Nodes;
        double totalDistance;

        public Loute(ArrayList<LatLng> nodes, double totalDistance) {
            Nodes = nodes;
            this.totalDistance = totalDistance;
        }

        @Override
        public int compare(Loute o1, Loute o2) {
            return (int) (o1.totalDistance - o2.totalDistance);
        }
    }

    class BackgroundThread extends Thread {
        String strStart;
        String strEnd;
        String startLat, endLat;
        String startLon, endLon;

        public BackgroundThread(String start, String end) {
            strStart = start;
            strEnd = end;
        }

        @Override
        public void run() {

            System.out.println("쓰레드 출격");
            System.out.println("strStart = " + strStart + " strEnd = " + strEnd);

            List<Address> addressListStart = null, addressListEnd = null;
            if (strStart.equals("현위치")) {
                try {
                    addressListEnd = geocoder.getFromLocationName(strEnd, // 주소
                            10); // 최대 검색 결과 개수
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressListStart = geocoder.getFromLocationName(strStart, // 주소
                            10); // 최대 검색 결과 개수
                    addressListEnd = geocoder.getFromLocationName(strEnd, // 주소
                            10); // 최대 검색 결과 개수
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // 출발이나 도착 지점 주소 검색 결과가 0이 아닌 경우
            if ((strStart.equals("현위치") && addressListEnd != null) | (addressListStart != null && addressListEnd != null && addressListStart.size() > 0 && addressListEnd.size() > 0)) {

                if (strStart.equals("현위치")) {
                    startLat = String.valueOf(curLat);
                    startLon = String.valueOf(curLon);
                } else {
                    startLat = getlatitude(String.valueOf(addressListStart.get(0)));
                    startLon = getlongitude(String.valueOf(addressListStart.get(0)));
                }

                endLat = getlatitude(String.valueOf(addressListEnd.get(0)));
                endLon = getlongitude(String.valueOf(addressListEnd.get(0)));
                startLatG = startLat;
                startLonG = startLon;
                endLatG = endLat;
                endLonG = endLon;
            } else {
                System.out.println("오류 : 검색결과 없음");
            }
        }
        public String getlatitude(String input) {
            // 콤마를 기준으로 split
            String[] splitStr = input.toString().split(",");
            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
            return latitude;
        }

        public String getlongitude(String input) {
            // 콤마를 기준으로 split
            String[] splitStr = input.toString().split(",");
            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
            return longitude;
        }
    }

    static class AltitudeRunnable implements Runnable {
        String lat, lon;

        public AltitudeRunnable(String lat, String lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public void run() {
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
            response.close();
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

            Elevation = elevation;

            System.out.println("lat = " + lat + " lon = " + lon + " elevation = " + elevation);
        }
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
        // url 으로 json 받아오는 메소드
        public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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
        // response 에서 json 받아오기 위한 url 만 추출
        public String parsingResponse(String Response) {

            String[] temp = Response.split("url=");

            String url = temp[1].substring(0, temp[1].length() - 1);
            //System.out.println("url = " + url);
            return url;
        }
    }

    static class arraylistHandler extends Handler {

        ArrayList<LatLng> address;

        @Override
        public void handleMessage(@NonNull Message msg) {
            System.out.println("arraylist 핸들러의 메세지핸들 출격");

            address = (ArrayList<LatLng>) msg.obj;
            //System.out.println(address);

        }

        public ArrayList<LatLng> getAddress() {
            return address;
        }
    }

}



