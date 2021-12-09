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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity2 extends AppCompatActivity {

    ArrayList<String> favorList;
    ArrayList<String> logList;

    JsonPlaceHolderAPI2 jsonPlaceHolderAPI2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        favorList = ((MainActivity) MainActivity.context).favorList;
        logList = ((MainActivity) MainActivity.context).logList;

        ListView favorListView = (ListView) findViewById(R.id.favorList);
        ListView logListView = (ListView) findViewById(R.id.recent_search_list);
        ArrayAdapter favorAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, favorList);
        ArrayAdapter logAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, logList);

        CheckBox handwheelchair = (CheckBox) findViewById(R.id.checkBox_handwheelchair),
                crutches = (CheckBox) findViewById(R.id.checkBox_crutches),
                autowheelchair = (CheckBox) findViewById(R.id.checkBox_autowheelchair),
                stair = (CheckBox) findViewById(R.id.checkBox_stair);

        String useremail="doori";
        Likeresponse(useremail);


        handwheelchair.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    // TODO : CheckBox is checked.
                    System.out.println("체크박스 체크되어있음 => 수동휠체어 ON");
                    ((MainActivity) MainActivity.context).gradient = 0.083;

                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크되어있음 => 수동휠체어 OFF");
                    ((MainActivity) MainActivity.context).gradient = 0.083;
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

                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크되어있음 => 전동휠체어 OFF");
                    ((MainActivity) MainActivity.context).gradient = 0.083;
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

                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크되어있음 => 목발 OFF");
                    ((MainActivity) MainActivity.context).gradient = 0.083;
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

                } else {
                    // TODO : CheckBox is unchecked.
                    System.out.println("체크박스 체크안되어있음=> 계단 no");
                    ((MainActivity) MainActivity.context).searchOption = "&searchOption=30";
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
                int index = strText.indexOf("-");
                String startAddress = strText.substring(0, index - 1);
                String endAddress = strText.substring(index + 3);
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
    public void Likeresponse(String useremail) {
        //LikeList likeList = new LikeList("dd", "dd", useremail);
        Call<List<LikeList>> call = RetrofitClient2.getApiService().likeRequest(useremail);
        call.enqueue(new Callback<List<LikeList>>() {
            @Override
            public void onResponse(Call<List<LikeList>> call, Response<List<LikeList>> response) {
                System.out.println(response.body());
                List<LikeList> lists=response.body();
                for(LikeList likeList:lists){
                    String content = "";
                    String content1="";
                    content= likeList.getLocation();
                    content1=likeList.getLikename();
                    System.out.println("location = "+content+"  name = "+content1);
                }
            }

            @Override
            public void onFailure(Call<List<LikeList>> call, Throwable t) {
                Log.e("연결실패", t.getMessage());
            }

        });
    }
}
