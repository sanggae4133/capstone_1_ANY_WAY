package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.Login.LoginActivity2;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    //?????????, ????????? ????????? ????????? ????????? ???????????? ?????????
    // ?????? ????????????????????? FusedLocationProviderClient??? ????????????.
    //FusedLocationProviderClient??? ?????? ?????? ???????????? ?????? ???????????? ?????? ?????? ?????????.
    //???????????? ???????????? ???????????? ????????? ???????????? ????????????
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Geocoder geocoder;
    ArrayList<LatLng> latLngArrayList;
    List<List<List<LatLng>>> impossibleRoads, possibleRoads;
    PolylineOverlay polylineOverlay, bannedOverlay;
    MultipartPathOverlay bannedOverlays;
    Marker markerStart, markerEnd, curMarker;
    TextView totalDistanceText;
    TextView totalTimeText;


    //???????????? ??????????????????
    private JsonPlaceHolderAPI jsonPlaceHolderAPI;

    public static EditText editTextStart, editTextEnd;

    Button Button, getCurPosition, bestWayBtn, settingBtn, sortByDistBtn, searchBarBtn, slideBtn;
    ImageButton searchBtn, routbutton, changeBtn;
    getAltitude getAltitudes;
    getCoordinate getCoordinates;
    Handler handler = new Handler();
    static Context context;
    ConstraintLayout resultBar;
    Animation translate_up, translate_down, translate_up2, translate_down2;
    Dialog curlocation_dialog;

    static String startLatG, endLatG, startLonG, endLonG;
    static ArrayList<LatLng> addressList;
    static Double Elevation, curLatG, curLonG, troubleLat, troubleLon;
    static boolean getInTrouble, isCurP;
    static ArrayList<Loute> possibleLoute;
    static double totalDistanceG, curLat, curLon, lonGap, latGap;
    static int totalTimeG, latOp, lonOp;
    public String searchOption;
    public Double gradient;
    static boolean isPageOpen;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    TextToSpeech tts;
    Map<LatLng, String> ttsService = new HashMap<>();

    public static ArrayList<String> favorList, logList;
    public static boolean CheckBox_handwheelchair, CheckBox_crutches,CheckBox_autowheelchair,CheckBox_stair;
    public static String userid;
    ListView favorListView, logListView ;
    ArrayAdapter favorAdapter, logAdapter;



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
        settingBtn = findViewById(R.id.settingBtn);

        getCurPosition = findViewById(R.id.getCurPosition);
        polylineOverlay = new PolylineOverlay();
        bannedOverlay = new PolylineOverlay();
        bannedOverlays = new MultipartPathOverlay();
        context = getApplicationContext();
        isPageOpen = true;
        isCurP = false;
        //?????? ??????????????? ?????? ??????.
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


        //******************?????? ??????**************************************
        //?????? ?????? ?????? ????????? db ????????? ?????? ????????? ????????? ???????????? ?????? ???????????????!
        searchOption = "&searchOption=30"; //??????????????? ????????????
        gradient = 0.083;
        context = this;
        favorList = new ArrayList<>();
        logList = new ArrayList<>();
        userid = ((LoginActivity2) LoginActivity2.context).ID;

        Likeresponse(userid);
        System.out.println("userid "+userid);

        CheckBox_handwheelchair = true;
        CheckBox_autowheelchair = false;
        CheckBox_crutches = false;
        CheckBox_stair = false;

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != -1) {
                    //????????????
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        //tts???????????? ?????? ??????
        tts.setPitch(1.0f);
        tts.setSpeechRate(1.3f);


        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        //getMapAsync??? ???????????? ???????????? onMapReady?????? ????????? ??????
        //onMapReady?????? NaverMap????????? ??????
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
        System.out.println("????????? ?????????");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // ?????? ?????????


            }
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    //????????? ??????????????? ??????
    public void setCurlocation_dialog(String target) {
        curlocation_dialog.show();
        Button startBtn, endBtn, favorBtn;
        String finalTarget = target;
        startBtn = curlocation_dialog.findViewById(R.id.setStartBtn);
        endBtn = curlocation_dialog.findViewById(R.id.setEndBtn);
        favorBtn = curlocation_dialog.findViewById(R.id.setFavorBtn);

        TextView address = curlocation_dialog.findViewById(R.id.address);
        final EditText editText = new EditText(MainActivity.this);

        address.setText(target);
        //???????????? ??????
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextStart.setText(finalTarget);
                curlocation_dialog.dismiss();
            }
        });

        //???????????? ??????
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextEnd.setText(finalTarget);
                curlocation_dialog.dismiss();
            }
        });
        //???????????? ?????? ??????
        favorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                alt_bld.setTitle("???????????? ?????? ??????")
                        .setMessage(finalTarget)
                        .setCancelable(true)
                        .setView(editText)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String nickname = editText.getText().toString();
                                System.out.println(nickname);
                                Likeresponse(nickname,finalTarget);
                                favorList.add(nickname + " : " + finalTarget);
                                //????????? ???????????? ?????? ?????? finalTarget?????? ???????????? ??????

                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.show();

                curlocation_dialog.dismiss();
            }
        });
    }
    public void Likeresponse(String likename,String location){
        String useremail=userid;
        LikeList likeList=new LikeList(likename,location,useremail);
        System.out.println(likeList.getLocation());
        Call<LikeList> call=RetrofitClient.getApiService().likeResponse(likeList);
        call.enqueue(new Callback<LikeList>() {
            @Override
            public void onResponse(Call<LikeList> call, retrofit2.Response<LikeList> response) {
                if(!response.isSuccessful()){
                    Log.e("????????? ???????????? : ", "error code : " + response.code());
                    return;
                }
                Log.d("????????? ????????? : ", response.body().toString());
            }

            @Override
            public void onFailure(Call<LikeList> call, Throwable t) {
                Log.e("????????????", t.getMessage());

            }
        });

    }

    public void Likeresponse(String useremail) {
        //LikeList likeList = new LikeList("dd", "dd", useremail);
        Call<List<LikeList>> call = RetrofitClient2.getApiService().likeRequest(useremail);
        call.enqueue(new Callback<List<LikeList>>() {
            @Override
            public void onResponse(Call<List<LikeList>> call, retrofit2.Response<List<LikeList>> response) {
                System.out.println(response.body());
                List<LikeList> lists=response.body();
                for(LikeList likeList:lists){
                    String content = "";
                    String content1="";
                    content= likeList.getLocation();
                    content1=likeList.getLikename();
                    String result=content1+" : "+content;
                    favorList.add(result);
                    System.out.println(result);
                }
            }

            @Override
            public void onFailure(Call<List<LikeList>> call, Throwable t) {
                Log.e("????????????", t.getMessage());
            }

        });
    }

    //??? ????????? ???????????? ????????? ?????? ????????? ??????????????? ??????
    public void createSelection(double lat, double lon) {
        String target = "";
        List<Address> list = null;

        try {

            list = geocoder.getFromLocation(
                    lat, // ??????
                    lon, // ??????
                    10); // ????????? ?????? ??????
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "????????? ?????? - ???????????? ??????????????? ????????????");
        }
        if (list != null) {
            if (list.size() == 0) {
                System.out.println("???????????? ?????? ????????? ????????????");
            } else {
                target = list.get(0).getAddressLine(0);
            }
        }

        curlocation_dialog = new Dialog(MainActivity.this);       // Dialog ?????????

        curlocation_dialog.setContentView(R.layout.location_info_dialog);             // xml ???????????? ????????? ??????
        setCurlocation_dialog(target);


    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        geocoder = new Geocoder(this, Locale.KOREAN);
        //????????? ?????? locationSource??? ????????? ?????? ?????? ????????? ?????? ??? ??? ??????
        naverMap.setLocationSource(locationSource);
        //?????? ?????? ?????? ?????? ?????? ??? ????????? ??????
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
//        //???????????? ?????? ????????????
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        LatLng initialPosition = new LatLng(37.506855, 127.066242);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);

        //???????????? ??????
        SlidingAnimationListener listener = new SlidingAnimationListener();
        translate_up.setAnimationListener(listener);
        translate_down.setAnimationListener(listener);


        // ????????? ?????? ?????? ?????? ?????? ?????????
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int reason, boolean animated) {


            }
        });

        //???????????? ??????????????? ????????? ????????? ??? ?????? ???????????? createSelection ????????? ??????
        //createSelection ???????????? ?????? ?????? => ?????? ?????? ??? ????????? or ???????????? ????????? ??? ?????? ??????
        naverMap.setOnMapClickListener(
                (point, coord) -> createSelection(coord.latitude, coord.longitude)
        );

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SettingActivity2.class);
                startActivity(intent);
            }
        });

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
                    slideBtn.setY(1370);
                }
            }
        });

        //????????? ?????? ????????? ????????? ????????? ????????????.
        // ?????? ?????????
        searchBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //??????????????? ????????? ????????? ??????????????? ???????????? ??????????????? null??????
                markerStart.setMap(null);
                markerEnd.setMap(null);
                polylineOverlay.setMap(null);
                bannedOverlay.setMap(null);
                bannedOverlays.setMap(null);

                startLatG = null;
                startLonG = null;
                endLatG = null;
                endLonG = null;
                //getAltitudes = new getAltitude(MainActivity.this); // ?????? ?????? ??????
                //getCoordinates = new getCoordinate(MainActivity.this);
                //???????????? ?????? ??????
                String strStart = editTextStart.getText().toString();
                String strEnd = editTextEnd.getText().toString();
                getInTrouble = false;
                isCurP = false;

                ArrayList<String> coordinate = null;
                List<Address> addressListStart = null, addressListEnd = null;

                Log.i("??????????????? ????????? ??????", " ");

                //????????????????????? == ?????? ->????????????  ????????? ????????????
                BackgroundThread coordinateThread = new BackgroundThread(strStart, strEnd);
                coordinateThread.start();

                try {
                    coordinateThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("??????????????? ????????? ??????", " ");

                //?????? ????????????(??????) ??????
                System.out.println(startLatG + " " + startLonG + " " + endLatG + " " + endLonG);

                //???????????? ????????? ??????
                if (startLatG == null || startLonG == null || endLatG == null || endLonG == null ||
                        startLatG.equals("true") || startLonG.equals("true") || endLatG.equals("true") || endLonG.equals("true")) {
                    System.out.println("?????? ?????? ????????????");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("??????").setMessage("?????? ?????? ??????");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    curLatG = Double.parseDouble(startLatG);
                    curLonG = Double.parseDouble(startLonG);
                    double nextLat = Double.parseDouble(endLatG);
                    double nextLon = Double.parseDouble(endLonG);
                    LatLng Startpoint = new LatLng(curLatG, curLonG);
                    LatLng Endpoint = new LatLng(Double.parseDouble(endLatG), Double.parseDouble(endLonG));

                    logList.add(strStart + " -> " + strEnd);
                    //?????? ??? ?????? ??????
                    troubleLat = (curLatG + nextLat) / 2;
                    troubleLon = (curLonG + nextLon) / 2;
                    //?????? ?????? ??? ?????????
                    latGap = Math.abs(curLatG - nextLat);
                    lonGap = Math.abs(curLonG - nextLon);

                    //???????????? ??????????????? ?????????
                    if ((curLatG - nextLat) * (curLonG - nextLon) < 0) {
                        System.out.println("??????" + (curLonG - nextLon) + " " + (curLatG - nextLat));
                        lonOp = 1;
                        latOp = 1;
                    } else {
                        System.out.println("??????" + (curLonG - nextLon) + " " + (curLatG - nextLat));
                        latOp = 1;
                        lonOp = -1;
                    }

                    // ?????? ??????
                    markerStart.setPosition(Startpoint);
                    markerEnd.setPosition(Endpoint);
                    markerStart.setIconTintColor(Color.BLUE);
                    markerEnd.setIconTintColor(Color.CYAN);
                    // ?????? ??????
                    markerStart.setMap(naverMap);
                    markerEnd.setMap(naverMap);

                    //url ??????
                    String url = TMapWalkerTrackerURL(Startpoint, Endpoint);

                    //????????? ????????? ??????
                    totalDistanceG = 0;
                    totalTimeG = 0;
                    // ??? ?????????, ????????? ??? ?????????
                    latLngArrayList = new ArrayList<>();
                    latLngArrayList.add(Startpoint);
                    possibleLoute = new ArrayList<>();
                    impossibleRoads = new ArrayList<>();
                    //????????? ?????????
                    MyRunnable first = new MyRunnable(url + searchOption);
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
                                //?????? a, b ??? gap??? ????????? ??????
                                double moveLat = (latOp * j * (1 * i * lonGap)) / 5;
                                double moveLon = (lonOp * j * (1 * i * latGap)) / 5;

                                String testLat = String.valueOf(troubleLat - moveLat);
                                String testLon = String.valueOf(troubleLon - moveLon);

                                System.out.println("latGap = " + latGap + " lonGap = " + lonGap);
                                System.out.println("moveLat = " + moveLat + " moveLon = " + moveLon);
                                System.out.println("testLat = " + testLat + " testLon = " + testLon);

                                /*//?????? ?????? ?????? ?????????
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
                                // ????????? ???????????? ??????
                                MyRunnable r = new MyRunnable(url + "&&passList=" + testLon + "," + testLat + searchOption);
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
                        //????????? ?????? ????????? ?????? ????????? ??????
                        Collections.sort(possibleLoute);

                        System.out.println("???????????? ?????? ?????? ????????? : " + possibleLoute.get(0).Nodes.size());
                        System.out.println("????????? ?????? ?????? ????????? : " + impossibleRoads.get(0).size());
                        System.out.println("?????? : " + possibleLoute.get(0).time);
                        System.out.println("????????? : " + possibleLoute.get(0).totalDistance);
                        System.out.println(possibleLoute.get(0).impossibleDist);

                        polylineOverlay.setCoords(possibleLoute.get(0).Nodes);
                        polylineOverlay.setWidth(15);
                        polylineOverlay.setPattern(10, 5);
                        polylineOverlay.setColor(Color.GREEN);
                        polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
                        polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
                        polylineOverlay.setMap(naverMap);

                        //??? ??? ?????? ?????? ????????? ??????
                        //??? ??? ?????? ?????? ??????????????? ??????
                        if (possibleLoute.get(0).impossibleNodes.size() > 0) {
                            bannedOverlays.setCoordParts(possibleLoute.get(0).impossibleNodes);
                            bannedOverlays.setColorParts(Arrays.asList(new MultipartPathOverlay.ColorPart(Color.RED, Color.RED, Color.RED, Color.RED)));
                            bannedOverlays.setMap(naverMap);
                        }
                        // ????????? 1.5 ??? ????????? ????????? ???????????? ???????????? ??????????????? ???????????? ????????? ?????? 65% ???????????? ?????????
                        totalDistanceText.setText("??? ?????? :" + possibleLoute.get(0).totalDistance / 1000 + " km");
                        ///totalDistanceText.setX(700);
                        totalTimeText.setText("??? ?????? :" + (possibleLoute.get(0).time * 1.5) / 60 + "???");
                        // ????????? ????????? ????????? ????????? ?????? ??? ????????? ???????????? ????????? ???????????????.
                        // ????????? ??????????????????,????????????????????? ????????? ????????? ?????????
                        LatLng firstLatlng = possibleLoute.get(0).Nodes.get(0);
                        LatLng lastLatlng = possibleLoute.get(0).Nodes.get(possibleLoute.get(0).Nodes.size() - 1);
                        LatLngBounds latLngBounds = new LatLngBounds(firstLatlng, lastLatlng);
                        CameraUpdate cameraUpdate = CameraUpdate.fitBounds(latLngBounds);
                        naverMap.moveCamera(cameraUpdate);

                        System.out.println("searchOption = " + searchOption + " gradient = " + gradient);

                    } else {
                        System.out.println("?????? => ?????? ?????? ??????");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("??????").setMessage("??????=> ?????? ???????????? ?????? ??????");
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                }
            }
        });

        //????????????
        sortByDistBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????? ?????? ????????? ?????? ????????? ??????
                polylineOverlay.setMap(null);
                bannedOverlays.setMap(null);

                if (possibleLoute != null && possibleLoute.size() > 0) {


                    Collections.sort(possibleLoute, new Comparator<Loute>() {
                        @Override
                        public int compare(Loute o1, Loute o2) {
                            return Double.compare(o1.totalDistance, o2.totalDistance);
                        }
                    });

                    System.out.println("???????????? ?????? ?????? ????????? : " + possibleLoute.get(0).Nodes.size());
                    System.out.println("????????? ?????? ?????? ????????? : " + impossibleRoads.get(0).size());
                    System.out.println("?????? : " + possibleLoute.get(0).time);
                    System.out.println("????????? : " + possibleLoute.get(0).totalDistance);
                    System.out.println(possibleLoute.get(0).impossibleDist);
                    //??? ??? ?????? ?????? ????????? ??????
                    //??? ??? ?????? ?????? ????????? ??? ??? ?????? ?????? ??????????????? ??????
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
                    // ????????? 1.5 ??? ????????? ????????? ???????????? ???????????? ??????????????? ???????????? ????????? ?????? 65% ???????????? ?????????
                    totalDistanceText.setText("??? ?????? :" + possibleLoute.get(0).totalDistance / 1000 + " km");
                    totalTimeText.setText("??? ?????? :" + (possibleLoute.get(0).time * 1.5) / 60 + "???");
                }
            }
        });

        //???????????? (????????? ???????????? ????????? ?????? ?????? ???)
        bestWayBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????? ?????? ????????? ?????? ????????? ??????
                polylineOverlay.setMap(null);
                bannedOverlays.setMap(null);

                if (possibleLoute != null && possibleLoute.size() > 0) {
                    Collections.sort(possibleLoute);
                    System.out.println("???????????? ?????? ?????? ????????? : " + possibleLoute.get(0).Nodes.size());
                    System.out.println("????????? ?????? ?????? ????????? : " + impossibleRoads.get(0).size());
                    System.out.println("?????? : " + possibleLoute.get(0).time);
                    System.out.println("????????? : " + possibleLoute.get(0).totalDistance);
                    System.out.println(possibleLoute.get(0).impossibleDist);

                    polylineOverlay.setCoords(possibleLoute.get(0).Nodes);
                    polylineOverlay.setWidth(15);
                    polylineOverlay.setPattern(10, 5);
                    polylineOverlay.setColor(Color.GREEN);
                    polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
                    polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
                    polylineOverlay.setMap(naverMap);
                    //??? ??? ?????? ?????? ????????? ??????
                    //??? ??? ?????? ?????? ????????? ??? ??? ?????? ?????? ??????????????? ??????

                    if (possibleLoute.get(0).impossibleNodes.size() > 0) {
                        bannedOverlays.setCoordParts(possibleLoute.get(0).impossibleNodes);
                        bannedOverlays.setColorParts(Arrays.asList(new MultipartPathOverlay.ColorPart(Color.RED, Color.RED, Color.RED, Color.RED)));
                        bannedOverlays.setMap(naverMap);
                    }

                    // ????????? 1.5 ??? ????????? ????????? ???????????? ???????????? ??????????????? ???????????? ????????? ?????? 65% ???????????? ?????????
                    totalDistanceText.setText("??? ?????? :" + possibleLoute.get(0).totalDistance / 1000 + " km");
                    totalTimeText.setText("??? ?????? :" + (possibleLoute.get(0).time * 1.5) / 60 + "???");
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
                //???????????? ?????? ????????? ?????? ??????
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //???????????? ?????? ??????
                Location userLocation = getMyLocation();
                if (userLocation != null) {
                    double latitude = userLocation.getLatitude();
                    double longitude = userLocation.getLongitude();

                    System.out.println("////////////?????? ??? ????????? : " + latitude + "," + longitude);

                    editTextStart.setText("?????????");
                    curLat = latitude;
                    curLon = longitude;
                } else {
                    System.out.println("?????? : getMyLocation ?????? ????????? ????????? null");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("??????").setMessage("????????? ?????? ??? ?????????");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }
        });

        //????????? ????????? gps??????, ?????? ???????????????????????? ???????????? ????????? ?????? tts??? ????????????
        routbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????????????????? ????????? ??????????????? tts?????????
                tts.speak("???????????? ???????????????.", TextToSpeech.QUEUE_FLUSH, null);

                System.out.print("?????????????");
                LatLng firstLatlng = possibleLoute.get(0).Nodes.get(0);
                double latitute = firstLatlng.latitude;
                double longtitute = firstLatlng.longitude;
                System.out.println(firstLatlng.latitude);
                CameraPosition cameraPosition = new CameraPosition(
                        new LatLng(latitute, longtitute),   // ?????? ??????
                        18,                           // ??? ??????
                        45,                          // ????????? ??????
                        45                           // ??????
                );
                naverMap.setCameraPosition(cameraPosition);
                naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
                startLocationService();
            }
        });


    }

    //??????gps????????? ?????????????????? LocationManager????????????
    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

        }

        //3?????? ???????????? or 2m ?????????????????? ???????????????
        GPSListener gpsListener = new GPSListener(ttsService);
        long minTime = 2000;
        float minDistance = 2;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
        Toast.makeText(getApplicationContext(), "??? ???????????? ?????????", Toast.LENGTH_SHORT).show();
    }


    //gps?????????????????? ???????????? ????????????
    class GPSListener implements LocationListener {
        Map<LatLng, String> ttsService;
        List<String> discription = new ArrayList<String>();
        LatLng ttsLatlng;
        double distance;

        public GPSListener(Map<LatLng, String> ttsService) {
            this.ttsService = ttsService;
        }

        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();


            for (Map.Entry<LatLng, String> entry : ttsService.entrySet()) {
                ttsLatlng = entry.getKey();
                distance = getDistance(latitude, longitude, ttsLatlng.latitude, ttsLatlng.longitude);

                //??????????????? ?????????????????? 10m?????? ?????? ??????????????????????????????????????? ??????
                if (distance < 10 && !(discription.contains(entry.getValue()))) {
                    tts.speak(entry.getValue(), TextToSpeech.QUEUE_FLUSH, null);
                    discription.add(entry.getValue());
                }
            }

        }

        //??????gps????????? point??????????????? ?????? ??????, ??????m
        public double getDistance(double lat1, double lng1, double lat2, double lng2) {
            double distance;

            Location locationA = new Location("point A");
            locationA.setLatitude(lat1);
            locationA.setLongitude(lng1);

            Location locationB = new Location("point B");
            locationB.setLatitude(lat2);
            locationB.setLongitude(lng2);

            distance = locationA.distanceTo(locationB);
            return distance;
        }
    }


    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////??????????????? ????????? ???????????????");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation(); //?????? ???????????? ????????? ?????????, ??? ?????? ???????????? ?????? ????????? ??????????????? ????????????!
        } else {
            System.out.println("////////////???????????? ????????????");

            // ???????????? ?????? ?????????
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            } else {
                System.out.println("?????? : ???????????? ????????????");
            }
        }
        return currentLocation;
    }

    // ?????? ???????????? ???????????? ??????
    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }


    //TMap????????? ????????? ??????????????? ?????????
    //??????????????? ?????? ????????? ???????????? ????????????????????? ????????????
    public String TMapWalkerTrackerURL(LatLng startPoint, LatLng endPoint) {

        String url = null;

        try {
            String appKey = BuildConfig.TMAP_API_KEY;

            String startX = new Double(startPoint.longitude).toString();
            String startY = new Double(startPoint.latitude).toString();
            String endX = new Double(endPoint.longitude).toString();
            String endY = new Double(endPoint.latitude).toString();

            System.out.println("?????? ??????: " + startX + " " + startY + " ?????? ??????: " + endX + " " + endY);
            String startName = URLEncoder.encode("?????????", "UTF-8");

            //System.out.println(getAltitudes.execute(startX, startY).get());
            //getAltitudes.execute(startX, startY) ?????? ?????? ?????? get() ?????? ??????(?????????)??? ?????????

            String endName = URLEncoder.encode("?????????", "UTF-8");
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


    // ?????? ?????? ???????????? ????????? ??????
    private Vector<LatLng> markersPosition;
    private Vector<Marker> activeMarkers;


    // ????????? ????????? ????????? ????????????(???????????? ???????????? ?????? ?????? 3km ???)??? ????????? ??????
    public final static double REFERANCE_LAT = 1 / 109.958489129649955;
    public final static double REFERANCE_LNG = 1 / 88.74;
    public final static double REFERANCE_LAT_X3 = 3 / 109.958489129649955;
    public final static double REFERANCE_LNG_X3 = 3 / 88.74;

    public boolean withinSightMarker(LatLng currentPosition, LatLng markerPosition) {
        boolean withinSightMarkerLat = Math.abs(currentPosition.latitude - markerPosition.latitude) <= REFERANCE_LAT_X3;
        boolean withinSightMarkerLng = Math.abs(currentPosition.longitude - markerPosition.longitude) <= REFERANCE_LNG_X3;
        return withinSightMarkerLat && withinSightMarkerLng;
    }

    // ???????????? ?????????????????? ????????? ???????????? ??????
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

            // google elevation api ??? response ????????? response ??? json ????????? ????????? json ??? ?????? ??? ?????? url??? ?????? ?????? ??????
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

            //url ??? json ????????? ????????? ?????? ????????? ????????? ??????
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

        //?????? ?????? ????????? ????????? ?????????
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        // url ?????? json ???????????? ?????????
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

        // response ?????? json ???????????? ?????? url ??? ??????
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
            // ?????? ????????? ????????? ??????.
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
            // ?????? URL??? ?????? ???????????? ????????????.
            try {
                //?????? ???????????? ????????? ????????? ??????
                System.out.println("result = \n" + result);
                JSONObject root = new JSONObject(result);
                //System.out.println("?????? ?????? ");
                //??? ?????? ?????? featuresArray??? ??????
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
                    //type??? LineString??? ?????? ???????????? ????????? ????????? ???????????? ????????? ??????.
                    //?????? ????????? ??????????????? ???????????????.
                    //type??? Point??? ???????????? ?????????, ?????????, ???????????? ??? ????????? ?????????
                    //???????????? ???????????? ????????? properties??? pointType?????? ?????? ????????????.

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

                    if (type.equals("Point")) {
                        JSONObject properties = featuresIndex.getJSONObject("properties");
                        double tts_longitude = Double.parseDouble(geometry.getJSONArray("coordinates").get(0).toString());
                        double tts_latitude = Double.parseDouble(geometry.getJSONArray("coordinates").get(1).toString());
                        LatLng tts_latlng = new LatLng(tts_latitude, tts_longitude);
                        String description = properties.getString("description");
                        ttsService.put(tts_latlng, description);

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(Lats.size() + "  " + Lons.size() + " " + Dist.size());

            if (Lats.size() < 2 || Lons.size() < 2) {
                System.out.println(" ?????? : ????????? ??????");
            } else {
                ArrayList<Double> elevations = callElevationApi(Lats, Lons);
                System.out.println("elevations.size " + elevations.size());

                for (int i = 0; i < elevations.size() - 1; i++) {
                    double zDist = Math.abs(elevations.get(i + 1) - elevations.get(i));
                    double curGradient = (zDist / Dist.get(i));
                    System.out.println("?????? " + Dist.get(i) + " ?????? " + zDist);
                    System.out.println("????????? : " + curGradient);
                    if (curGradient > gradient && curGradient < 0.3) {
                        System.out.println("????????? ??????");
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

        //?????? ?????? ????????? ?????? elevation api ??? ?????? ????????? ???????????? ???????????? ???????????? ??????, ??? ??? ?????? ?????? ??????
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
            //url ??? json ????????? ????????? ?????? ????????? ????????? ??????
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

        //?????? ?????? ????????? ????????? ?????????
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        // url ?????? json ???????????? ?????????
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

        // response ?????? json ???????????? ?????? url ??? ??????
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
            // ?????? ????????? ????????? ??????.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, new ContentValues());
            double totalDistance = 0;
            int totalTime = 0;
            // ?????? URL??? ?????? ???????????? ????????????.
            try {
                //?????? ???????????? ????????? ????????? ??????
                JSONObject root = new JSONObject(result);
                //System.out.println("?????? ?????? ");
                System.out.println("result = \n" + result);
                //??? ?????? ?????? featuresArray??? ??????
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
                    //type??? LineString??? ?????? ???????????? ????????? ????????? ???????????? ????????? ??????.
                    //?????? ????????? ??????????????? ???????????????.
                    //type??? Point??? ???????????? ?????????, ?????????, ???????????? ??? ????????? ?????????
                    //???????????? ???????????? ????????? properties??? pointType?????? ?????? ????????????.

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

                        //System.out.println("?????? ??? ?????? elevation = " + elevation);

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
                        System.out.println("?????? " + distance + " ?????? " + zDistance + " " + zDistance / distance);
                        System.out.println("????????? : " + gradient);

                        curLatG = endLat;
                        curLonG = endLon;

                        //?????????
                        if (gradient > 0.083 && gradient < 0.3) {
                            System.out.println("????????? ??????");

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

            System.out.println("????????? ??????");
            System.out.println("strStart = " + strStart + " strEnd = " + strEnd);

            List<Address> addressListStart = null, addressListEnd = null;
            if (strStart.equals("?????????")) {
                try {
                    addressListEnd = geocoder.getFromLocationName(strEnd, // ??????
                            10); // ?????? ?????? ?????? ??????
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // editText??? ????????? ?????????(??????, ??????, ?????? ???)??? ?????? ????????? ????????? ??????
                    addressListStart = geocoder.getFromLocationName(strStart, // ??????
                            10); // ?????? ?????? ?????? ??????
                    addressListEnd = geocoder.getFromLocationName(strEnd, // ??????
                            10); // ?????? ?????? ?????? ??????
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // ???????????? ?????? ?????? ?????? ?????? ????????? 0??? ?????? ??????
            if ((strStart.equals("?????????") && addressListEnd != null) | (addressListStart != null && addressListEnd != null && addressListStart.size() > 0 && addressListEnd.size() > 0)) {

                if (strStart.equals("?????????")) {
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
                System.out.println("?????? : ???????????? ??????");
            }
        }

        public String getlatitude(String input) {
            // ????????? ???????????? split
            String[] splitStr = input.toString().split(",");
            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // ??????
            return latitude;
        }

        public String getlongitude(String input) {
            // ????????? ???????????? split
            String[] splitStr = input.toString().split(",");
            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // ??????
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
            // google elevation api ??? response ????????? response ??? json ????????? ????????? json ??? ?????? ??? ?????? url??? ?????? ?????? ??????
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

            //url ??? json ????????? ????????? ?????? ????????? ????????? ??????
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

        // url ?????? json ???????????? ?????????
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

        // response ?????? json ???????????? ?????? url ??? ??????
        public String parsingResponse(String Response) {

            String[] temp = Response.split("url=");

            String url = temp[1].substring(0, temp[1].length() - 1);
            //System.out.println("url = " + url);
            return url;
        }
    }


}




