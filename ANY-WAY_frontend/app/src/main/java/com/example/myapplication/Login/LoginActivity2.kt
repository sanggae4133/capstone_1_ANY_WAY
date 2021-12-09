package com.example.myapplication.Login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.Register.RegisterActivity2
import com.example.myapplication.retrofitSetting
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity2 : AppCompatActivity() {
    lateinit var ID: String
    var login: Login? = null
    companion object {
        @kotlin.jvm.JvmField
        var context: Context? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_login)
        //setContentView(R.layout.activity_join)
        context = this


        // 백엔드 통신  retrofit 설정
        //baseURl http://~~~:8000/
        // ~~~ 부분에 ipconfig ipv4 주소 넣어야 함
        var retrofit = Retrofit.Builder()
            .baseUrl(retrofitSetting.getBaseurl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var loginService: LoginService = retrofit.create(LoginService::class.java)

        //로그인 버튼 눌렀을 때
        login_button.setOnClickListener{
            var text1 = login_email.text.toString()
            var text2 = login_password.text.toString()
            ID = text1
            val intent = Intent(this, MainActivity::class.java)
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

                    if(login?.code =="0000"){
                        startActivity(intent)
                    }else{
                        var dialog = AlertDialog.Builder(this@LoginActivity2)
                        dialog.setTitle("에러")
                        dialog.setMessage("로그인 실패")
                        dialog.show()
                    }
                }

            })

//            var dialog = AlertDialog.Builder(this@LoginActivity2)
//            dialog.setTitle("알람")
//            dialog.setMessage("id : " + text1+"pw : " + text2)
//            dialog.show()
        }

        // 회원가입 버튼 눌렀을 때 회원가입 창 띄우기
        register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity2::class.java)
            startActivity(intent)
        }
    }

}