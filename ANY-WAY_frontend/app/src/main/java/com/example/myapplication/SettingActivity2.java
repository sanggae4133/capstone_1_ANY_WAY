package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity2 extends AppCompatActivity {

    ArrayList<String> favorList;
    ArrayList<String> logList;
    boolean CheckBox_handwheelchair, CheckBox_crutches,CheckBox_autowheelchair,CheckBox_stair;
    String userid;

    JsonPlaceHolderAPI2 jsonPlaceHolderAPI2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);


        //db에서 받을것
        favorList = ((MainActivity) MainActivity.context).favorList;
        logList = ((MainActivity) MainActivity.context).logList;
        userid = ((MainActivity) MainActivity.context).userid;
        CheckBox_autowheelchair = ((MainActivity) MainActivity.context).CheckBox_autowheelchair;
        CheckBox_handwheelchair = ((MainActivity) MainActivity.context).CheckBox_handwheelchair;
        CheckBox_crutches = ((MainActivity) MainActivity.context).CheckBox_crutches;
        CheckBox_stair = ((MainActivity) MainActivity.context).CheckBox_stair;

        System.out.println("userid = " + userid);
        //Likeresponse(userid);

        TextView username = findViewById(R.id.user_name_text);
        username.setText("사용자 " + userid);

        ListView favorListView = (ListView) findViewById(R.id.favorList);
        ListView logListView = (ListView) findViewById(R.id.recent_search_list);

        ArrayAdapter favorAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, favorList);
        ArrayAdapter logAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, logList);

        CheckBox handwheelchair = (CheckBox) findViewById(R.id.checkBox_handwheelchair),
                crutches = (CheckBox) findViewById(R.id.checkBox_crutches),
                autowheelchair = (CheckBox) findViewById(R.id.checkBox_autowheelchair),
                stair = (CheckBox) findViewById(R.id.checkBox_stair);






        if (CheckBox_autowheelchair) {
            autowheelchair.setChecked(true);
        }
        if (CheckBox_handwheelchair) {
            handwheelchair.setChecked(true);
        }
        if (CheckBox_crutches) {
            crutches.setChecked(true);
        }
        if (CheckBox_stair) {
            stair.setChecked(true);
        }

        if (!(CheckBox_handwheelchair | CheckBox_autowheelchair | CheckBox_crutches)) {
            handwheelchair.setChecked(true);
        }

        //수동 휠체어 버튼리스너
        handwheelchair.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    // TODO : CheckBox is checked.
                    System.out.println("체크박스 체크될때 => 수동휠체어 ON");

                    //메인 체크 변수 true, 각도 변수 수정, + 세팅 액티비티에서 true
                    ((MainActivity) MainActivity.context).CheckBox_handwheelchair = true;
                    ((MainActivity) MainActivity.context).gradient = 0.083;
                    CheckBox_handwheelchair = true;
                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크해제 =>수동휠체어 OFF");

                    //만약 모든 버튼 안눌렸다면 => 수동 휠체어 버튼 눌린 것으로 취급
                    if (!(!CheckBox_handwheelchair | CheckBox_autowheelchair | CheckBox_crutches)) {
                        // 수동 휠체어 on
                        handwheelchair.setChecked(true);
                    }else {
                        ((MainActivity) MainActivity.context).CheckBox_handwheelchair = false;
                        CheckBox_handwheelchair = false;
                    }

                }
            }
        });

        autowheelchair.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    // TODO : CheckBox is checked.
                    System.out.println("체크박스 체크되어있음 => 전동휠체어 ON");
                    ((MainActivity) MainActivity.context).gradient = 0.176;
                    ((MainActivity) MainActivity.context).CheckBox_autowheelchair = true;
                    CheckBox_autowheelchair = true;
                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크되어있음 => 전동휠체어 OFF");
                    //다 선택되있지 않으면 다시 체크해버리자
                    if (!(CheckBox_handwheelchair | !CheckBox_autowheelchair | CheckBox_crutches)) {
                        autowheelchair.setChecked(true);
                    }else{
                        ((MainActivity) MainActivity.context).CheckBox_autowheelchair = false;
                        CheckBox_autowheelchair = false;
                    }

                }
            }
        });

        //목발
        crutches.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    // TODO : CheckBox is checked.
                    System.out.println("체크박스 체크되어있음 => 목발 ON");
                    ((MainActivity) MainActivity.context).gradient =0.1;
                    ((MainActivity) MainActivity.context).CheckBox_crutches = true;
                    CheckBox_crutches = true;
                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크되어있음 => 목발 OFF");
                    if (!(CheckBox_handwheelchair | CheckBox_autowheelchair | !CheckBox_crutches)) {
                        crutches.setChecked(true);
                    }else {
                        CheckBox_crutches = false;
                        ((MainActivity) MainActivity.context).CheckBox_crutches = false;
                    }
                }
            }
        });
        //계단
        stair.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    // TODO : CheckBox is checked.
                    System.out.println("체크박스 체크되어있음 => 계단 ok");
                    ((MainActivity) MainActivity.context).searchOption = "&searchOption=10";
                    ((MainActivity) MainActivity.context).CheckBox_stair = true;
                    CheckBox_stair = true;

                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크안되어있음=> 계단 no");
                    ((MainActivity) MainActivity.context).searchOption = "&searchOption=30";
                    ((MainActivity) MainActivity.context).CheckBox_stair = false;
                    CheckBox_stair = false;
                }
            }
        });

        favorListView.setAdapter(favorAdapter);
        logListView.setAdapter(logAdapter);

        favorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {



                // get TextView's Text.
                String strText = (String) parent.getItemAtPosition(position);
                int index = strText.indexOf(":");
                String address = strText.substring(index + 1);
                System.out.println(address);
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(SettingActivity2.this)
                        .setTitle("즐겨찾기 선택지")
                        .setMessage(strText)
                        .setPositiveButton("도착", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity) MainActivity.context).editTextEnd.setText(address);
                            }
                        })
                        .setNegativeButton("출발", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) MainActivity.context).editTextStart.setText(address);
                            }
                        })
                        .setNeutralButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                favorList.remove(strText);
                                favorListView.setAdapter(favorAdapter);
                                int index = strText.indexOf(":");
                                int length=strText.length();

                                String likename=strText.substring(0,index-1);
                                String location=strText.substring(index+2,length);
                                System.out.println("맞나?"+likename+"진짜"+location);
                                Likeresponse(likename,location,userid);


                            }
                        });
                msgBuilder.create().show();
            }
        });


        logListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get TextView's Text.
                String strText = (String) parent.getItemAtPosition(position);
                int index = strText.indexOf(">");
                String startAddress = strText.substring(0, index - 2);
                String endAddress = strText.substring(index + 2);
                System.out.println(startAddress + " " + endAddress);
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(SettingActivity2.this)
                        .setTitle("최근기록 불러오기")
                        .setMessage(strText)
                        .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNeutralButton("다시 검색?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity) MainActivity.context).editTextStart.setText(startAddress);
                                ((MainActivity) MainActivity.context).editTextEnd.setText(endAddress);
                            }
                        });
                msgBuilder.create().show();
            }
        });

    }
    public void Likeresponse(String likename,String location,String useremail) {
        LikeList likeList = new LikeList(likename, location, useremail);
        System.out.println(likeList.getLocation());
        Call<LikeList> call = RetrofitClient2.getApiService().likeResponse(likeList);
        call.enqueue(new Callback<LikeList>() {
            @Override
            public void onResponse(Call<LikeList> call, retrofit2.Response<LikeList> response) {
                if (!response.isSuccessful()) {
                    Log.e("연결이 비정상적 : ", "error code : " + response.code());
                    return;
                }
                Log.d("연결이 성공적 : ", response.body().toString());
            }

            @Override
            public void onFailure(Call<LikeList> call, Throwable t) {
                Log.e("연결실패", t.getMessage());

            }
        });
    }

}
