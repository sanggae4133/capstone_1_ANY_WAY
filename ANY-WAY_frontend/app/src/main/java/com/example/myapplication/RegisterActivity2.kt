package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_join.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity2 : AppCompatActivity() {
    var register:Register? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        //setContentView(R.layout.activity_login)
        setContentView(R.layout.activity_join)

        // 백엔드 통신  retrofit 설정
        var retrofit = Retrofit.Builder()
            .baseUrl("http://172.30.1.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var registerService: RegisterService = retrofit.create(RegisterService::class.java)

        //회원가입 버튼 눌렀을 때
        register_button.setOnClickListener{
            var name = join_name.text.toString()
            var id = join_email.text.toString()
            var password = join_password.text.toString()
            var passwordCheck = join_pwck.text.toString()


            //버튼 눌렀을 때 입력 부재 , 올바르지 못한 입력 예외처리 해야함
//            if (name == null || id == null || password == null || passwordCheck == null) {
//                var dialog = AlertDialog.Builder(this@RegisterActivity2)
//                dialog.setTitle("에러")
//                dialog.setMessage("빈칸을 채워주세요")
//                dialog.show()
//
//                return@setOnClickListener
//            }
//
//            if (password != passwordCheck) {
//                var dialog = AlertDialog.Builder(this@RegisterActivity2)
//                dialog.setTitle("에러")
//                dialog.setMessage("비밀번호를 다시 확인해주세요")
//                dialog.show()
//
//                return@setOnClickListener
//            }

            registerService.requestRegister(name, id, password).enqueue(object: Callback<Register> {
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
                    Log.d("LOGIN","msg : "+register?.msg)
                    Log.d("LOGIN","code : "+register?.code)
                    var dialog = AlertDialog.Builder(this@RegisterActivity2)
                    dialog.setTitle(register?.msg)
                    dialog.setMessage(register?.code)
                    dialog.show()
                }

            })

            var dialog = AlertDialog.Builder(this@RegisterActivity2)
            dialog.setTitle("알람")
            dialog.setMessage("name : " + name +"id : " + id+"pw : " + password)
            dialog.show()
        }

        //확인 버튼 눌렀을 때
        check_button.setOnClickListener{

        }

        //취소 버튼 눌렀을 때
        delete.setOnClickListener{
            finish()
        }


        }
}