package com.example.myapplication.Register
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RegisterService {
    @FormUrlEncoded
    @POST("/app_register/")
    fun requestRegister(
        @Field("username") username:String,
        @Field("useremail") useremail:String,
        @Field("userpw") userpw:String
    ) : Call<Register>

    @FormUrlEncoded
    @POST("/app_check_id/")
    fun checkId(
        @Field("useremail") useremail:String
    ) : Call<Register>
}