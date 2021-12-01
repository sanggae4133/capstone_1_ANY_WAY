package com.example.myapplication;

import static android.speech.tts.TextToSpeech.ERROR;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    ArrayList<LatLng> latLngArrayList;
    List<List<List<LatLng>>> impossibleRoads, possibleRoads;
    PolylineOverlay polylineOverlay, bannedOverlay;
    MultipartPathOverlay bannedOverlays;
    Marker markerStart;
    Marker markerEnd;
    TextView totalDistanceText;
    TextView totalTimeText;
    EditText editTextStart;
    EditText editTextEnd;
    Button Button, getCurPosition, bestWayBtn, sortByTimeBtn, sortByDistBtn, searchBarBtn, slideBtn;
    ImageButton searchBtn, routbutton, changeBtn;
    getAltitude getAltitudes;
    getCoordinate getCoordinates;
    Handler handler = new Handler();

    //tts생성
    TextToSpeech tts;

    //포인트랑 tts출력을 위한 description을 담은 hashmap
    Map<LatLng,String> ttsService=new HashMap<>();

    Context context;
    ConstraintLayout resultBar;
    Animation translate_up, translate_down, translate_up2, translate_down2;

    static String startLatG, endLatG, startLonG, endLonG;
    static ArrayList<LatLng> addressList;
    static Double Elevation, curLatG, curLonG, troubleLat, troubleLon;
    static boolean getInTrouble, isCurP;
    static ArrayList<Loute> possibleLoute;
    static double totalDistanceG, curLat, curLon, lonGap, latGap;
    static int totalTimeG, latOp, lonOp;
    static boolean isPageOpen;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    public class SlidingAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isPageOpen) {
                resultBar.setVisibility(View.INVISIBLE);
                slideBtn.setText("open");
                isPageOpen = false;
            } else {
                slideBtn.setText("close");
                isPageOpen = true;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        markerStart = new Marker();
        markerEnd = new Marker();
        totalDistanceText = findViewById(R.id.totalDistance);
        totalTimeText = findViewById(R.id.totalTime);
        editTextStart = findViewById(R.id.editTextStart);
        editTextEnd = findViewById(R.id.editTextEnd);
        searchBtn = findViewById(R.id.searchBtn);
        changeBtn = findViewById(R.id.changeBtn);
        routbutton = findViewById(R.id.routbutton);
        bestWayBtn = findViewById(R.id.bestWay);
        sortByDistBtn = findViewById(R.id.sortByDist);
        //searchBarBtn = findViewById(R.id.searchBar);
        searchBtn = findViewById(R.id.searchBtn);
        resultBar = findViewById(R.id.resultBar);
        translate_up = AnimationUtils.loadAnimation(this, R.anim.translate_up);
        translate_down = AnimationUtils.loadAnimation(this, R.anim.translate_down);
        translate_up2 = AnimationUtils.loadAnimation(this, R.anim.translate_up2);
        translate_down2 = AnimationUtils.loadAnimation(this, R.anim.translate_down2);
        slideBtn = findViewById(R.id.slideBtn);

        getCurPosition = findViewById(R.id.getCurPosition);
        polylineOverlay = new PolylineOverlay();
        bannedOverlay = new PolylineOverlay();
        bannedOverlays = new MultipartPathOverlay();
        context = getApplicationContext();
        isPageOpen = true;
        isCurP = false;
        //지도 사용권한을 받아 온다.
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

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
        //tts목소리랑 속도 설정
        tts.setPitch(1.0f);
        tts.setSpeechRate(1.3f);


        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        //getMapAsync를 호출하여 비동기로 onMapReady콜백 메서드 호출
        //onMapReady에서 NaverMap객체를 받음
        mapFragment.getMapAsync(this);

        editTextStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextStart.setText("");
            }
        });
        editTextEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextEnd.setText("");
            }
        });

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

    public void createSelection(double lat, double lon) {
        String target = "";
        List<Address> list = null;
        try {

            list = geocoder.getFromLocation(
                    lat, // 위도
                    lon, // 경도
                    10); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
        }
        if (list != null) {
            if (list.size() == 0) {
                System.out.println("해당되는 주소 정보는 없습니다");
            } else {
                target = list.get(0).getAddressLine(0);
            }
        }

        String finalTarget = target;
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("출발지 or 목적지 설정")
                .setMessage(target)
                .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("목적지", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, lat + " " + lon, Toast.LENGTH_SHORT).show();
                        editTextEnd.setText(finalTarget);
                    }
                })
                .setNeutralButton("출발지", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, lat + " " + lon, Toast.LENGTH_SHORT).show();
                        editTextStart.setText(finalTarget);
                    }
                });
        msgBuilder.create().show();
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

        //슬라이딩 관련
        SlidingAnimationListener listener = new SlidingAnimationListener();
        translate_up.setAnimationListener(listener);
        translate_down.setAnimationListener(listener);


        // 카메라 이동 되면 호출 되는 이벤트
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int reason, boolean animated) {


            }
        });

        //지도에서 클릭이벤트 발생시 클릭한 곳 좌표 정보얻고 createSelection 메소드 호출
        //createSelection 메소드는 해당 좌표 => 주소 변환 후 출발지 or 목적지로 설정할 수 있는 기능
        naverMap.setOnMapClickListener(
                (point, coord) -> createSelection(coord.latitude, coord.longitude)
        );

        slideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPageOpen) {
                    resultBar.startAnimation(translate_up);
                    slideBtn.startAnimation(translate_up);
                    slideBtn.setY(1700);
                } else {
                    resultBar.setVisibility(View.VISIBLE);
                    resultBar.startAnimation(translate_down);
                    slideBtn.startAnimation(translate_down);
                    slideBtn.setY(1300);
                }
            }
        });

        //검색을 하면 검색한 좌표에 마커를 찍어준다.
        // 버튼 이벤트
        searchBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //누를때마다 새로운 마커랑 폴리라인이 기존것과 중복안되게 null처리
                markerStart.setMap(null);
                markerEnd.setMap(null);
                polylineOverlay.setMap(null);
                bannedOverlay.setMap(null);
                bannedOverlays.setMap(null);

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
                if (startLatG == null || startLonG == null || endLatG == null || endLonG == null ||
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
                        System.out.println("양수" + (curLonG - nextLon) + " " + (curLatG - nextLat));
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
                    possibleLoute = new ArrayList<>();
                    impossibleRoads = new ArrayList<>();
                    //길찾는 스레드
                    MyRunnable first = new MyRunnable(url + "&searchOption=10");
                    Thread firstTry = new Thread(first);
                    firstTry.start();
                    try {
                        firstTry.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (getInTrouble) {
                        loop:
                        for (int i = 1; i <= 3; i++) {
                            for (int j = -1; j < 2; j += 2) {
                                totalDistanceG = 0;
                                totalTimeG = 0;
                                latLngArrayList = new ArrayList<>();
                                //숫자 a, b 로 gap의 사이즈 조절
                                double moveLat = (latOp * j * (1 * i * lonGap)) / 5;
                                double moveLon = (lonOp * j * (1 * i * latGap)) / 5;

                                String testLat = String.valueOf(troubleLat - moveLat);
                                String testLon = String.valueOf(troubleLon - moveLon);

                                System.out.println("latGap = " + latGap + " lonGap = " + lonGap);
                                System.out.println("moveLat = " + moveLat + " moveLon = " + moveLon);
                                System.out.println("testLat = " + testLat + " testLon = " + testLon);

                                /*//중간 지점 마커 찍는거
                                try {
                                    Marker marker1 = new Marker(new LatLng(troubleLat, troubleLon));
                                    marker1.setIconTintColor(Color.RED);
                                    marker1.setMap(naverMap);

                                    Marker marker = new Marker(new LatLng(Double.parseDouble(testLat), Double.parseDouble(testLon)));
                                    marker.setIconTintColor(Color.YELLOW);
                                    marker.setMap(naverMap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/
                                // 경유지 추가해서 돌림
                                MyRunnable r = new MyRunnable(url + "&&passList=" + testLon + "," + testLat + "&searchOption=10");
                                Thread thread = new Thread(r);
                                thread.start();
                                try {
                                    thread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }

                    if (possibleLoute.size() > 0) {
                        //못가는 길의 길이가 적은 순으로 정렬
                        Collections.sort(possibleLoute);

                        System.out.println("갈수있는 경로 좌표 사이즈 : " + possibleLoute.get(0).Nodes.size());
                        System.out.println("경사도 초과 좌표 사이즈 : " + impossibleRoads.get(0).size());
                        System.out.println("시간 : " + possibleLoute.get(0).time);
                        System.out.println("총거리 : " + possibleLoute.get(0).totalDistance);
                        System.out.println(possibleLoute.get(0).impossibleDist);

                        polylineOverlay.setCoords(possibleLoute.get(0).Nodes);
                        polylineOverlay.setWidth(15);
                        polylineOverlay.setPattern(10, 5);
                        polylineOverlay.setColor(Color.GREEN);
                        polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
                        polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
                        polylineOverlay.setMap(naverMap);

                        //갈 수 없는 길이 포함된 경우
                        //갈 수 없는 길은 빨간색으로 칠함
                        if (possibleLoute.get(0).impossibleNodes.size() > 0) {
                            bannedOverlays.setCoordParts(possibleLoute.get(0).impossibleNodes);
                            bannedOverlays.setColorParts(Arrays.asList(new MultipartPathOverlay.ColorPart(Color.RED, Color.RED, Color.RED, Color.RED)));
                            bannedOverlays.setMap(naverMap);
                        }
                        // 시간에 1.5 배 곱하는 이유는 장애인의 평균적인 보행속도가 일반적인 경우에 비해 65% 정도라고 하기에
                        totalDistanceText.setText("총 거리 :" + possibleLoute.get(0).totalDistance / 1000 + " km");
                        totalDistanceText.setX(700);
                        totalTimeText.setText("총 거리 :" + (possibleLoute.get(0).time * 1.5) / 60 + "분");
                        // 영역이 온전히 보이는 좌표와 최대 줌 레벨로 카메라의 위치를 변경합니다.
                        // 경로의 첫번째포인트,마지막포인트를 지도에 꽉차게 보여줌
                        LatLng firstLatlng = possibleLoute.get(0).Nodes.get(0);
                        LatLng lastLatlng = possibleLoute.get(0).Nodes.get(possibleLoute.get(0).Nodes.size() - 1);
                        LatLngBounds latLngBounds = new LatLngBounds(firstLatlng, lastLatlng);
                        CameraUpdate cameraUpdate = CameraUpdate.fitBounds(latLngBounds);
                        naverMap.moveCamera(cameraUpdate);
                    } else {
                        System.out.println("좌표 => 주소 변환 문제");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("에러").setMessage("좌표=> 주소 변환에서 문제 발생");
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                }
            }
        });

        //최단거리
        sortByDistBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //못가는 길의 길이가 적은 순으로 정렬
                polylineOverlay.setMap(null);
                bannedOverlays.setMap(null);

                if (possibleLoute != null && possibleLoute.size() > 0) {


                    Collections.sort(possibleLoute, new Comparator<Loute>() {
                        @Override
                        public int compare(Loute o1, Loute o2) {
                            return Double.compare(o1.totalDistance, o2.totalDistance);
                        }
                    });

                    System.out.println("갈수있는 경로 좌표 사이즈 : " + possibleLoute.get(0).Nodes.size());
                    System.out.println("경사도 초과 좌표 사이즈 : " + impossibleRoads.get(0).size());
                    System.out.println("시간 : " + possibleLoute.get(0).time);
                    System.out.println("총거리 : " + possibleLoute.get(0).totalDistance);
                    System.out.println(possibleLoute.get(0).impossibleDist);
                    //갈 수 없는 길이 포함된 경우
                    //갈 수 없는 길은 빨간색 갈 수 있는 길은 노란색으로 칠함
                    polylineOverlay.setCoords(possibleLoute.get(0).Nodes);
                    polylineOverlay.setWidth(15);
                    polylineOverlay.setPattern(10, 5);
                    polylineOverlay.setColor(Color.GREEN);
                    polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
                    polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
                    polylineOverlay.setMap(naverMap);

                    if (possibleLoute.get(0).impossibleNodes.size() > 0) {
                        bannedOverlays.setCoordParts(possibleLoute.get(0).impossibleNodes);
                        bannedOverlays.setColorParts(Arrays.asList(new MultipartPathOverlay.ColorPart(Color.RED, Color.RED, Color.RED, Color.RED)));
                        bannedOverlays.setMap(naverMap);
                    }
                    // 시간에 1.5 배 곱하는 이유는 장애인의 평균적인 보행속도가 일반적인 경우에 비해 65% 정도라고 하기에
                    totalDistanceText.setText("총 거리 :" + possibleLoute.get(0).totalDistance / 1000 + " km");
                    totalTimeText.setText("총 거리 :" + (possibleLoute.get(0).time * 1.5) / 60 + "분");
                }
            }
        });

        //최적경로 (경사도 문제되는 부분이 제일 적은 길)
        bestWayBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //못가는 길의 길이가 적은 순으로 정렬
                polylineOverlay.setMap(null);
                bannedOverlays.setMap(null);

                if (possibleLoute != null && possibleLoute.size() > 0) {
                    Collections.sort(possibleLoute);
                    System.out.println("갈수있는 경로 좌표 사이즈 : " + possibleLoute.get(0).Nodes.size());
                    System.out.println("경사도 초과 좌표 사이즈 : " + impossibleRoads.get(0).size());
                    System.out.println("시간 : " + possibleLoute.get(0).time);
                    System.out.println("총거리 : " + possibleLoute.get(0).totalDistance);
                    System.out.println(possibleLoute.get(0).impossibleDist);

                    polylineOverlay.setCoords(possibleLoute.get(0).Nodes);
                    polylineOverlay.setWidth(15);
                    polylineOverlay.setPattern(10, 5);
                    polylineOverlay.setColor(Color.GREEN);
                    polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
                    polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
                    polylineOverlay.setMap(naverMap);
                    //갈 수 없는 길이 포함된 경우
                    //갈 수 없는 길은 빨간색 갈 수 있는 길은 노란색으로 칠함

                    if (possibleLoute.get(0).impossibleNodes.size() > 0) {
                        bannedOverlays.setCoordParts(possibleLoute.get(0).impossibleNodes);
                        bannedOverlays.setColorParts(Arrays.asList(new MultipartPathOverlay.ColorPart(Color.RED, Color.RED, Color.RED, Color.RED)));
                        bannedOverlays.setMap(naverMap);
                    }

                    // 시간에 1.5 배 곱하는 이유는 장애인의 평균적인 보행속도가 일반적인 경우에 비해 65% 정도라고 하기에
                    totalDistanceText.setText("총 거리 :" + possibleLoute.get(0).totalDistance / 1000 + " km");
                    totalTimeText.setText("총 거리 :" + (possibleLoute.get(0).time * 1.5) / 60 + "분");
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
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //사용자의 현재 위치
                Location userLocation = getMyLocation();
                if (userLocation != null) {
                    double latitude = userLocation.getLatitude();
                    double longitude = userLocation.getLongitude();

                    System.out.println("////////////현재 내 위치값 : " + latitude + "," + longitude);

                    editTextStart.setText("현위치");
                    curLat = latitude;
                    curLon = longitude;
                } else {
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
                tts.speak("길찾기를 시작합니다.", TextToSpeech.QUEUE_FLUSH,null);

                System.out.print("해치웠나?");
                LatLng firstLatlng = possibleLoute.get(0).Nodes.get(0);
                double latitute = firstLatlng.latitude;
                double longtitute = firstLatlng.longitude;
                System.out.println(firstLatlng.latitude);
                CameraPosition cameraPosition = new CameraPosition(
                        new LatLng(latitute, longtitute),   // 위치 지정
                        18,                           // 줌 레벨
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
        Toast.makeText(getApplicationContext(),"내 위치확인 요청함", Toast.LENGTH_SHORT).show();
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

                //현재위치와 포인트좌표가 10m밖에 있고 한번음성출력이된적이있으면 제외
                if(distance<10&&!(discription.contains(entry.getValue()))){
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



    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation(); //이건 써도되고 안써도 되지만, 전 권한 승인하면 즉시 위치값 받아오려고 썼습니다!
        } else {
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
            String appKey = BuildConfig.TMAP_API_KEY;

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
                    .url("https://maps.googleapis.com/maps/api/elevation/json?locations=" + lat + "," + lon + "&key=" + BuildConfig.GOOGLE_API_KEY)
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
            double impossibleDist = 0;
            int totalTime = 0;
            curElevation = 0;
            nextElevation = 0;
            int index = 0;
            ArrayList<String> Lats = new ArrayList<>();
            ArrayList<String> Lons = new ArrayList<>();
            ArrayList<Double> Dist = new ArrayList<>();
            ArrayList<LatLng> impossibleRoad = null;
            ArrayList<LatLng> possibleRoad = new ArrayList<>();
            List<List<LatLng>> impossibleLoute = new ArrayList<>();
            // 해당 URL로 부터 결과물을 얻어온다.
            try {
                //전체 데이터를 제이슨 객체로 변환
                System.out.println("result = \n" + result);
                JSONObject root = new JSONObject(result);
                //System.out.println("제일 상위 ");
                //총 경로 횟수 featuresArray에 저장
                JSONArray featuresArray = root.getJSONArray("features");
                double longitude = 0, nextLongitude = 0;
                double latitude = 0, nextLatitude = 0;
                double startLat = 0, startLon = 0;
                double endLat = 0, endLon = 0;
                //double elevation = 0, nextElevation = 0;
                double beforeLat = 0, beforeLon = 0;
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

                            Lats.add(pointArray.get(1).toString());
                            Lons.add(pointArray.get(0).toString());

                            if (beforeLon == 0 || beforeLat == 0) {

                            } else {
                                Dist.add(distance(beforeLat, beforeLon, latitude, longitude));
                            }
                            beforeLat = latitude;
                            beforeLon = longitude;
                            //System.out.println("latitude = " + latitude + " longitude = " + longitude);

                            possibleRoad.add(new LatLng(latitude, longitude));
                        }

                        JSONObject properties = featuresIndex.getJSONObject("properties");
                        double distance = Double.parseDouble(properties.getString("distance"));
                        int time = Integer.parseInt(properties.getString("time"));

                        //Dist.add(distance);

                        System.out.println("distance = " + distance);
                        totalDistance += distance;
                        totalTime += time;

                        curLatG = endLat;
                        curLonG = endLon;
                    }
                    if(type.equals("Point")){
                        JSONObject properties = featuresIndex.getJSONObject("properties");
                        double tts_longitude = Double.parseDouble(geometry.getJSONArray("coordinates").get(0).toString());
                        double tts_latitude = Double.parseDouble(geometry.getJSONArray("coordinates").get(1).toString());
                        LatLng tts_latlng=new LatLng(tts_latitude,tts_longitude);
                        String description=properties.getString("description");
                        ttsService.put(tts_latlng,description);

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(Lats.size() + "  " + Lons.size() + " " + Dist.size());

            if (Lats.size() < 2 || Lons.size() < 2) {
                System.out.println(" 오류 : 좌표가 없음");
            } else {
                ArrayList<Double> elevations = callElevationApi(Lats, Lons);
                System.out.println("elevations.size " + elevations.size());

                for (int i = 0; i < elevations.size() - 1; i++) {
                    double zDist = Math.abs(elevations.get(i + 1) - elevations.get(i));
                    double gradient = (zDist / Dist.get(i));
                    System.out.println("거리 " + Dist.get(i) + " 높이 " + zDist);
                    System.out.println("경사도 : " + gradient);
                    if (gradient > 0.083 && gradient < 0.3) {
                        System.out.println("경사도 초과");
                        impossibleRoad = new ArrayList<>();
                        impossibleRoad.add(new LatLng(Double.parseDouble(Lats.get(i)), Double.parseDouble(Lons.get(i))));
                        impossibleRoad.add(new LatLng(Double.parseDouble(Lats.get(i + 1)), Double.parseDouble(Lons.get(i + 1))));
                        impossibleLoute.add(impossibleRoad);
                        impossibleDist += Dist.get(i);
                        getInTrouble = true;
                        //break Loop;
                    }
                }

                impossibleRoads.add(impossibleLoute);
                possibleLoute.add(new Loute(possibleRoad, impossibleLoute, totalDistance, totalTime, impossibleDist));
            }

            System.out.println("totalDistance = " + totalDistance);
            System.out.println("totalTime = " + totalTime);

            totalDistanceG = totalDistance;
            totalTimeG = totalTime;
        }

        //위도 경도 좌표들 받아 elevation api 로 고도 따오고 계산하며 경사도가 문제되는 부분, 갈 수 있는 길을 반환
        public ArrayList<Double> callElevationApi(ArrayList<String> lat, ArrayList<String> lon) {
            String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=" + lat.get(0) + "%2C" + lon.get(0);
            for (int i = 1; i < lat.size(); i++) {
                url += "%7C" + lat.get(i) + "%2C" + lon.get(i);
            }
            url += "&key=" + BuildConfig.GOOGLE_API_KEY;

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            double beforeElevation = 0;
            double afterElevation = 0;

            ArrayList<Double> elevations = new ArrayList<>();
            //url 로 json 받아와 원하는 고도 정보만 빼먹는 로직
            try {
                JSONObject jsonObj = readJsonFromUrl(parsingResponse(response.toString()));
                JSONArray resultEl = jsonObj.getJSONArray("results");

                for (int i = 0; i < resultEl.length(); i++) {
                    JSONObject next = resultEl.getJSONObject(i);
                    afterElevation = Double.parseDouble(next.getString("elevation"));
                    elevations.add(afterElevation);
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return elevations;
        }

        private double distance(double lat1, double lon1, double lat2, double lon2) {

            double theta = lon1 - lon2;
            double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;

            dist = dist * 1609.344;

            return (dist);
        }


        // This function converts decimal degrees to radians
        private double deg2rad(double deg) {
            return (deg * Math.PI / 180.0);
        }

        // This function converts radians to decimal degrees
        private double rad2deg(double rad) {
            return (rad * 180 / Math.PI);
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

                            getInTrouble = true;
                            break Loop;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("totalDistance = " + totalDistance);
            System.out.println("totalTime = " + totalTime);

            totalDistanceG = totalDistance;
            totalTimeG = totalTime;
        }

    }

    static class Loute implements Comparable<Loute> {
        ArrayList<LatLng> Nodes;
        List<List<LatLng>> impossibleNodes;
        double totalDistance;
        double time;
        double impossibleDist;

        public Loute(ArrayList<LatLng> nodes, List<List<LatLng>> impossibleNodes, double totalDistance, double time, double impossibleDist) {
            Nodes = nodes;
            this.totalDistance = totalDistance;
            this.time = time;
            this.impossibleDist = impossibleDist;
            this.impossibleNodes = impossibleNodes;
        }

        @Override
        public int compareTo(Loute o) {
            return Double.compare(this.impossibleDist, o.impossibleDist);
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
                    .url("https://maps.googleapis.com/maps/api/elevation/json?locations=" + lat + "," + lon + "&key=" + BuildConfig.GOOGLE_API_KEY)
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


}




