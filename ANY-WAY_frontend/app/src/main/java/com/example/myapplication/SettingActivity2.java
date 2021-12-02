package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SettingActivity2 extends AppCompatActivity {

    ArrayList<String> favorList;
    ArrayList<String> logList;

    JsonPlaceHolderAPI jsonPlaceHolderAPI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        favorList = new ArrayList<>();
        logList = new ArrayList<>();
        favorList.add("집");
        favorList.add("학교");
        logList.add("출발1" + " -> " + "도착1");
        logList.add("출발2" + " -> " + "도착2");

        String url = "http://" + "10.210.60.95" + ":8000/";

        ListView favorListView = (ListView) findViewById(R.id.favorList);
        ListView logListView = (ListView) findViewById(R.id.recent_search_list);
        ArrayAdapter favorAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, favorList);
        ArrayAdapter logAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, logList);

        CheckBox handwheelchair = (CheckBox) findViewById(R.id.checkBox_handwheelchair),
                crutches = (CheckBox) findViewById(R.id.checkBox_crutches),
                autowheelchair = (CheckBox) findViewById(R.id.checkBox_autowheelchair),
                stair = (CheckBox) findViewById(R.id.checkBox_stair);

        handwheelchair.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    // TODO : CheckBox is checked.
                    //((MainActivity)MainActivity.context).searchOption=

                } else {

                    // TODO : CheckBox is unchecked.
                }
            }
        }) ;


        favorListView.setAdapter(favorAdapter);
        logListView.setAdapter(logAdapter);

        favorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                // get TextView's Text.
                String strText = (String) parent.getItemAtPosition(position);
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(SettingActivity2.this)
                        .setTitle("즐겨찾기 삭제하겠습니까?")
                        .setMessage(strText)
                        .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNeutralButton("삭제하겠습니다", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                favorList.remove(strText);
                                favorListView.setAdapter(favorAdapter);
                            }
                        });
                msgBuilder.create().show();
            }
        });



    }
}
