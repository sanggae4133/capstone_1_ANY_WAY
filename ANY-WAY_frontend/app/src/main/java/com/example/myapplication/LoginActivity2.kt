package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity2 : AppCompatActivity() {
    var login:Login? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_login)
        //setContentView(R.layout.activity_join)

        // 백엔드 통신  retrofit 설정
        //baseURl http://~~~:8000/
        // ~~~ 부분에 ipconfig ipv4 주소 넣어야 함
        var retrofit = Retrofit.Builder()
            .baseUrl("http://172.30.1.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var loginService: LoginService = retrofit.create(LoginService::class.java)

        //로그인 버튼 눌렀을 때
        login_button.setOnClickListener{
            var text1 = login_email.text.toString()
            var text2 = login_password.text.toString()

            loginService.requestLogin(text1,text2).enqueue(object: Callback<Login> {
                override fun onFailure(call: Call<Login>, t: Throwable) {
                    //실패할 경우
                    Log.d("DEBUG", t.message.toString())
                    var dialog = AlertDialog.Builder(this@LoginActivity2)
                    dialog.setTitle("에러")
                    dialog.setMessage("통신에 실패했습니다.")
                    dialog.show()
                }

                override fun onResponse(call: Call<Login>, response: Response<Login>) {
                    //정상응답이 올경우
                    login = response.body()
                    Log.d("LOGIN","msg : "+login?.msg)
                    Log.d("LOGIN","code : "+login?.code)
                    var dialog = AlertDialog.Builder(this@LoginActivity2)
                    dialog.setTitle(login?.msg)
                    dialog.setMessage(login?.code)
                    dialog.show()
                }

            })

            var dialog = AlertDialog.Builder(this@LoginActivity2)
            dialog.setTitle("알람")
            dialog.setMessage("id : " + text1+"pw : " + text2)
            dialog.show()
        }

        // 회원가입 버튼 눌렀을 때 회원가입 창 띄우기
        register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity2::class.java)
            startActivity(intent)
        }
    }
}