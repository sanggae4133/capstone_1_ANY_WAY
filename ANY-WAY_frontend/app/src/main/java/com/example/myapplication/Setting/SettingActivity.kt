package com.example.myapplication.Setting

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.JsonPlaceHolderAPI
import com.example.myapplication.Login.Login
import com.example.myapplication.R
import com.example.myapplication.retrofitSetting
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class SettingActivity : AppCompatActivity() {
    var favorList: ArrayList<String?>? = null
    var logList: ArrayList<String?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)
        favorList = ArrayList()
        logList = ArrayList()
        favorList!!.add("집")
        favorList!!.add("학교")
        logList!!.add("출발1" + " -> " + "도착1")
        logList!!.add("출발2" + " -> " + "도착2")

        var retrofit = Retrofit.Builder()
            .baseUrl(retrofitSetting.getBaseurl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var settingService: SettingService = retrofit.create(SettingService::class.java)

        settingService.requestFavorite(1).enqueue(object: Callback<Setting> {
            override fun onFailure(call: Call<Setting>, t: Throwable) {
                //실패할 경우
                Log.d("DEBUG", t.message.toString())
                var dialog = androidx.appcompat.app.AlertDialog.Builder(this@SettingActivity)
                dialog.setTitle("에러")
                dialog.setMessage("통신에 실패했습니다.")
                dialog.show()
            }

            override fun onResponse(call: Call<Setting>, response: Response<Setting>) {
                //정상응답이 올경우
                var setting = response.body()
                Log.d("LOGIN", "msg : " + setting?.msg)
                Log.d("LOGIN", "code : " + setting?.code)

            }
        })

        var settingValue = 1;

        val favorListView = findViewById<View>(R.id.favorList) as ListView
        val logListView = findViewById<View>(R.id.recent_search_list) as ListView
        val favorAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this, android.R.layout.simple_list_item_1,
            favorList!! as List<Any?>
        )

        val logAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this, android.R.layout.simple_list_item_1,
            logList!! as List<Any?>
        )

        favorListView.adapter = favorAdapter
        logListView.adapter = logAdapter

        favorListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, v, position, id -> // get TextView's Text.
                val strText = parent.getItemAtPosition(position) as String
                val msgBuilder = AlertDialog.Builder(this@SettingActivity)
                    .setTitle("즐겨찾기 삭제하겠습니까?")
                    .setMessage(strText)
                    .setPositiveButton(
                        "취소"
                    ) { dialogInterface, i -> }
                    .setNeutralButton(
                        "삭제하겠습니다"
                    ) { dialogInterface, i ->
                        favorList!!.remove(strText)
                        favorListView.adapter = favorAdapter
                    }
                msgBuilder.create().show()
            }



    }
}