package com.example.myapplication;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import static android.speech.tts.TextToSpeech.ERROR;
import static java.lang.Thread.sleep;


public class MainActivity<implement> extends AppCompatActivity implements OnMapReadyCallback {
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
    PolylineOverlay polylineOverlay;
    Marker markerStart;
    Marker markerEnd;
    TextView totalDistanceText;
    TextView totalTimeText;
    EditText editTextStart;
    EditText editTextEnd;
    Button Button;
    Button routbutton;
    getAltitude getAltitudes;
    getCoordinate getCoordinates;
    Handler handler = new Handler();
    ValueHandler vHandler = new ValueHandler();
    arraylistHandler alHandler = new arraylistHandler();

    //tts생성
    TextToSpeech tts;

    //포인트랑 tts출력을 위한 description을 담은 hashmap
    Map<LatLng,String> ttsService=new HashMap<>();

    static String startLatG, endLatG, startLonG, endLonG;
    static ArrayList<LatLng> addressList;

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
        routbutton=findViewById(R.id.routbutton);
        polylineOverlay = new PolylineOverlay();

        //tts를 생성하고 OnInitListener로 초기화
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=ERROR){
                    //언어선택
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });


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
        //초기 카메라를 현재위치로
        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        //현재위치 버튼 사용가능
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        //LatLng initialPosition = new LatLng(37.506855, 127.066242);
        //CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        //naverMap.moveCamera(cameraUpdate);

        //tts목소리랑 속도 설정
        tts.setPitch(1.0f);
        tts.setSpeechRate(1.3f);


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

                ArrayList<String> coordinate = null;
                List<Address> addressListStart = null, addressListEnd = null;

                Log.i("백그라운드 스레드 시작", " ");
                BackgroundThread coordinateThread = new BackgroundThread(strStart, strEnd);
                coordinateThread.start();

                try {
                    coordinateThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("백그라운드 스레드 종료", " ");

                //vHandler.handleMessage(vHandler.obtainMessage());
                //Message message = vHandler.obtainMessage();

                System.out.println("메세지 열어보자");
                System.out.println(startLatG + " " + startLonG + " " + endLatG + " " + endLonG);

                LatLng Startpoint = new LatLng(Double.parseDouble(startLatG), Double.parseDouble(startLonG));
                LatLng Endpoint = new LatLng(Double.parseDouble(endLatG), Double.parseDouble(endLonG));

                // 마커 표시
                markerStart.setPosition(Startpoint);
                markerEnd.setPosition(Endpoint);
                // 마커 추가
                markerStart.setMap(naverMap);
                markerEnd.setMap(naverMap);

                //시작,도착지점을 Tmap서버에 보내기위해 url형식대로 만들어줌
                String url = TMapWalkerTrackerURL(Startpoint, Endpoint);

                latLngArrayList = new ArrayList<>();

                getLouteThread getLouteThread = new getLouteThread(url);
                getLouteThread.start();
                Log.i("getLoute 스레드 시작", " ");
                try {
                    getLouteThread.join();
                } catch (InterruptedException e) {

                }
                Log.i("getLoute 스레드 끝", " ");

                for (int i = 0; i < latLngArrayList.size(); i++) {
                    System.out.println(latLngArrayList.get(i));
                }


                //검색완료 후 키보드 내리기
                try {
                    InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (NullPointerException e) {
                    System.out.println("이미 키보드 없음^^");
                }

                polylineOverlay.setCoords(latLngArrayList);
                polylineOverlay.setWidth(10);
                polylineOverlay.setPattern(10, 5);
                polylineOverlay.setColor(Color.GREEN);
                polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
                polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);

                polylineOverlay.setMap(naverMap);

                // 영역이 온전히 보이는 좌표와 최대 줌 레벨로 카메라의 위치를 변경합니다.
                // 경로의 첫번째포인트,마지막포인트를 지도에 꽉차게 보여줌
                LatLng firstLatlng = latLngArrayList.get(0);
                LatLng lastLatlng = latLngArrayList.get(latLngArrayList.size() - 1);
                LatLngBounds latLngBounds = new LatLngBounds(firstLatlng, lastLatlng);
                CameraUpdate cameraUpdate = CameraUpdate.fitBounds(latLngBounds);
                naverMap.moveCamera(cameraUpdate);
            }



                // 해당 좌표로 화면 줌
//                    naverMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));

                /*
                    CameraUpdate cameraUpdate = CameraUpdate.scrollTo(Startpoint);
                    naverMap.moveCamera(cameraUpdate);

                 */

        });

        //버튼을 누르면 gps작동
        routbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                tts.speak("길찾기를 시작합니다.", TextToSpeech.QUEUE_FLUSH,null);

                //"길찾기를 시작합니다" 출력 후 1초 쉬고 찐tts서비스 시작
                try
                {
                    sleep(1000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }


                //길찾기버튼을 누르면 확대되면서 tts서비스
                System.out.print("해치웠나?");
                LatLng firstLatlng = latLngArrayList.get(0);
                double latitute=firstLatlng.latitude;
                double longtitute=firstLatlng.longitude;
                System.out.println(firstLatlng.latitude);
                CameraPosition cameraPosition = new CameraPosition(
                        new LatLng(latitute,longtitute),   // 위치 지정
                        18,                          // 줌 레벨
                        45,                          // 기울임 각도
                        45                           // 방향
                );
                naverMap.setCameraPosition(cameraPosition);
                naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
                startLocationService();
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    //현재gps위치를 가져오기위한 LocationManager객체생성
    public void startLocationService() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

        }

        //3초가 지나거나 or 2m 움직일때마다 이벤트호출
        GPSListener gpsListener=new GPSListener(ttsService);
        long minTime=3000;
        float minDistance=2;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime,minDistance,gpsListener);
        Toast.makeText(getApplicationContext(),"내 위치확인 요청함",Toast.LENGTH_SHORT).show();
    }

    //gps이동이벤트가 발생하면 로직수행
    class GPSListener implements LocationListener {
        Map<LatLng,String> ttsService;
        List<String> discription=new ArrayList<String>();
        LatLng ttsLatlng;
        double distance;

        public GPSListener(Map<LatLng, String> ttsService) {
            this.ttsService=ttsService;
        }

        @Override
        public void onLocationChanged(Location location) {
            Double latitude=location.getLatitude();
            Double longitude=location.getLongitude();


            for(Map.Entry<LatLng,String> entry:ttsService.entrySet()){
                ttsLatlng=entry.getKey();
                distance=getDistance(latitude,longitude,ttsLatlng.latitude,ttsLatlng.longitude);

                //현재위치와 포인트좌표가 20m밖에 있고 한번음성출력이된적이있으면 제외
                if(distance<20&&!(discription.contains(entry.getValue()))){
                    tts.speak(entry.getValue(),TextToSpeech.QUEUE_FLUSH,null);
                    discription.add(entry.getValue());
                }
            }

        }

        //현재gps위치와 point위치사이의 거리 리턴, 단위m
        public double getDistance(double lat1,double lng1,double lat2,double lng2){
            double distance;

            Location locationA=new Location("point A");
            locationA.setLatitude(lat1);
            locationA.setLongitude(lng1);

            Location locationB=new Location("point B");
            locationB.setLatitude(lat2);
            locationB.setLongitude(lng2);

            distance=locationA.distanceTo(locationB);
            return distance;
        }
    }

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

    class getAltitude extends Thread {
        Double lat;
        Double lon;

        public getAltitude(Double lat, Double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public void run() {
//            String lon = cutStr(strings[0]); //경도 124~134
//            String lat = cutStr(strings[1]); //위도 33~43
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
        }
        // 위도 경도 소수점 7자리까지 자르기
        public String cutStr(String point) {
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
            System.out.println("url = " + url);
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
            // 해당 URL로 부터 결과물을 얻어온다.
            try {
                //전체 데이터를 제이슨 객체로 변환
                JSONObject root = new JSONObject(result);
                System.out.println("제일 상위 " + root);


                //총 경로 횟수 featuresArray에 저장
                JSONArray featuresArray = root.getJSONArray("features");

                for (int i = 0; i < featuresArray.length(); i++) {
                    JSONObject featuresIndex = (JSONObject) featuresArray.get(i);
//                    System.out.println("뭐가 저장 됨?"+featuresIndex);
                    JSONObject geometry = featuresIndex.getJSONObject("geometry");

                    String type = geometry.getString("type");

                    //type이 LineString일 경우 좌표값이 하나가 아니라 여러개로 책정이 된다.
                    //전부 뽑아서 전체경로에 추가해준다.
                    //type이 Point일 경우에는 출발점, 경유지, 도착지점 이 세경우 뿐인데
                    //세가지는 구분하는 기준은 properties의 pointType으로 구분 가능하다.

                    if (type.equals("LineString")) {


                        JSONArray coordinatesArray = geometry.getJSONArray("coordinates");

//                        System.out.println("라인이 여러개다"+coordinatesArray);

                        for (int j = 0; j < coordinatesArray.length(); j++) {

//                            System.out.println(coordinatesArray.get(j).getClass().getName());

                            JSONArray pointArray = (JSONArray) coordinatesArray.get(j);
                            double longitude = Double.parseDouble(pointArray.get(0).toString());
                            double latitude = Double.parseDouble(pointArray.get(1).toString());


                            System.out.println("latitude = " + latitude+" longitude = " + longitude);
                            //System.out.println("경로 중 지점 elevation = " + elevation);

                            latLngArrayList.add(new LatLng(latitude, longitude));
                            System.out.println("LineString를 저장 ");
//                            System.out.println("만들어진 어레이는  "+latLngArrayList);
//                            System.out.println("총저장된 경로의 갯수는"+latLngArrayList.size());
                        }
                    }

                    if (type.equals("Point")) {
                        JSONObject properties = featuresIndex.getJSONObject("properties");
                        try {
                            double totalDistance = Integer.parseInt(properties.getString("totalDistance"));


                            totalDistanceText.setText("총 거리 :" + totalDistance / 1000 + " km");

                            int totalTime = Integer.parseInt(properties.getString("totalTime"));
                            totalTimeText.setText("총 거리 :" + totalTime / 60 + "분");

                        } catch (Exception e) {

                        }

                        String pointType = properties.getString("pointType");


                        double longitude = Double.parseDouble(geometry.getJSONArray("coordinates").get(0).toString());
                        double latitude = Double.parseDouble(geometry.getJSONArray("coordinates").get(1).toString());

                        //포인트마다 description을 넣어 tts서비스
                        LatLng latLng=new LatLng(latitude,longitude);
                        String decription=properties.getString("description");
                        ttsService.put(latLng,decription);

//                        System.out.println("Point를 저장 ");
//                        latLngArrayList.add(new LatLng(latitude, longitude));

//                        if (pointType.equals("SP")) {
//                            System.out.println("시작지점이다");

//                        } else if (pointType.equals("GP")) {

//                            System.out.println("중간지점이다");
//                        } else if (pointType.equals("EP")) {

//                            System.out.println("끝지점이다");

//                        }
//                        marker.setPosition(new LatLng(latitude, longitude));
//                        System.out.println(latitude+","+longitude);
//                        marker.setMap(naverMap);
                    }

                    System.out.println("총저장된 경로의 갯수는" + latLngArrayList.size());

                    //alHandler.sendMessage(message);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
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
            Message message = vHandler.obtainMessage();

            System.out.println("쓰레드 출격");
            System.out.println("strStart = " + strStart + " strEnd = " + strEnd);

            List<Address> addressListStart = null, addressListEnd = null;
            try {
                // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                addressListStart = geocoder.getFromLocationName(strStart, // 주소
                        10); // 최대 검색 결과 개수
                addressListEnd = geocoder.getFromLocationName(strEnd, // 주소
                        10); // 최대 검색 결과 개수
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 출발이나 도착 지점 주소 검색 결과가 0인 경우
            if (addressListStart.size() == 0 | addressListEnd.size() == 0) {
                System.out.println("오류 : 검색결과 없음");
                message.what = 0;
            } else {
                message.what = 1;
                startLat = getlatitude(String.valueOf(addressListStart.get(0)));
                startLon = getlongitude(String.valueOf(addressListStart.get(0)));
                endLat = getlatitude(String.valueOf(addressListEnd.get(0)));
                endLon = getlongitude(String.valueOf(addressListEnd.get(0)));
                startLatG = startLat;
                startLonG = startLon;
                endLatG = endLat;
                endLonG = endLon;
                message.obj = startLat + "," + startLon + "," + endLat + "," + endLon;
            }

            vHandler.sendMessage(message);
        }
        public String getlatitude(String input) {

            // 콤마를 기준으로 split
            String[] splitStr = input.toString().split(",");
            String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소

            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
            return latitude;
        }

        public String getlongitude(String input) {
            // 콤마를 기준으로 split
            String[] splitStr = input.toString().split(",");
            String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소

            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
            return longitude;
        }
    }

    static class ValueHandler extends Handler {

        String address;

        @Override
        public void handleMessage(@NonNull Message msg) {
            System.out.println("핸들러의 메세지핸들 출격");

            String address = (String) msg.obj;
            System.out.println(address);

        }

        public String getAddress() {
            return address;
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




