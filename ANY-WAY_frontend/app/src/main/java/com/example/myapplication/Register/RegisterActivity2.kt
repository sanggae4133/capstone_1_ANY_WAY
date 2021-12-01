package com.example.myapplication.Register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.R
import com.example.myapplication.retrofitSetting
import kotlinx.android.synthetic.main.activity_join.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity2 : AppCompatActivity() {
    var register: Register? = null
    var name: String? = null
    var id: String? = null
    var password: String? = null
    var passwordCheck: String? = null
    var verifiedid: String? = null

    // 백엔드 통신  retrofit 설정
    var retrofit = Retrofit.Builder()
        .baseUrl(retrofitSetting.getBaseurl())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var registerService: RegisterService = retrofit.create(RegisterService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //setContentView(R.layout.activity_main)
        //setContentView(R.layout.activity_login)
        setContentView(R.layout.activity_join)

        // 백엔드 통신  retrofit 설정
//        var retrofit = Retrofit.Builder()
//            .baseUrl("http://172.30.1.21:8000/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()

        //회원가입 버튼 눌렀을 때
        register_button.setOnClickListener {
            name = join_name.text.toString()
            id = join_email.text.toString()
            password = join_password.text.toString()
            passwordCheck = join_pwck.text.toString()

            if (validateInput()) {
                registerService.requestRegister(name!!, id!!, password!!)
                    .enqueue(object : Callback<Register> {
                        override fun onFailure(call: Call<Register>, t: Throwable) {
                            //실패할 경우
                            Log.d("DEBUG", t.message.toString())
                            var dialog = AlertDialog.Builder(this@RegisterActivity2)
                            dialog.setTitle("에러")
                            dialog.setMessage("통신에 실패했습니다.")
                            dialog.show()
                        }

                        override fun onResponse(call: Call<Register>, response: Response<Register>) {
                            //정상응답이 올경우
                            register = response.body()
                            Log.d("LOGIN", "msg : " + register?.msg)
                            Log.d("LOGIN", "code : " + register?.code)
                            var dialog = AlertDialog.Builder(this@RegisterActivity2)
                            dialog.setTitle(register?.msg)
                            dialog.setMessage(register?.code)
                            dialog.show()

                            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 5000)

                        }
                    })
            }
        }
        //확인 버튼 눌렀을 때
        check_button.setOnClickListener {
            id = join_email.text.toString()

            if (validateID()) {
                registerService.checkId(id!!).enqueue(object : Callback<Register> {
                    override fun onResponse(call: Call<Register>, response: Response<Register>) {
                        //정상응답이 올경우
                        register = response.body()
                        Log.d("ID_CHECK", "msg : " + register?.msg)
                        Log.d("ID_CHECK", "code : " + register?.code)

                        if(register?.code == "1001"){
                            var dialog = AlertDialog.Builder(this@RegisterActivity2)
                            dialog.setTitle("에러")
                            dialog.setMessage("ID가 중복되었습니다.")
                            dialog.show()
                        }else if(register?.code == "0007"){
                            var dialog = AlertDialog.Builder(this@RegisterActivity2)
                            dialog.setTitle("에러")
                            dialog.setMessage("ID 중복 체크 통과되었습니다.")
                            dialog.show()
                        }

                    }

                    override fun onFailure(call: Call<Register>, t: Throwable) {
                        //실패할 경우
                        Log.d("DEBUG", t.message.toString())
                        var dialog = AlertDialog.Builder(this@RegisterActivity2)
                        dialog.setTitle("에러")
                        dialog.setMessage("통신에 실패했습니다.")
                        dialog.show()
                    }
                })
            }
        }

            //취소 버튼 눌렀을 때 회원가입 창 닫기
        delete.setOnClickListener {
            finish()
        }
    }

    fun validateInput(): Boolean {
        if (name.equals("") || id.equals("") || password.equals("") || passwordCheck.equals("")) {
            var dialog = AlertDialog.Builder(this@RegisterActivity2)
            dialog.setTitle("에러")
            dialog.setMessage("빈칸을 채워주세요")
            dialog.show()

            return false
        }else if (password != passwordCheck) {
            var dialog = AlertDialog.Builder(this@RegisterActivity2)
            dialog.setTitle("에러")
            dialog.setMessage("비밀번호를 다시 확인해주세요")
            dialog.show()

            return false
        } else if (id != verifiedid) {
            var dialog = AlertDialog.Builder(this@RegisterActivity2)
            dialog.setTitle("에러")
            dialog.setMessage("중복 체크되지 않은 ID 입니다")
            dialog.show()
            return false
        } else {
            return true
        }
    }

    fun validateID(): Boolean {
        if (id.equals("")) {
            var dialog = AlertDialog.Builder(this@RegisterActivity2)
            dialog.setTitle("에러")
            dialog.setMessage("빈칸을 채워주세요")
            dialog.show()

            return false
        }
        else {
            verifiedid = id
            return true
        }
    }
}