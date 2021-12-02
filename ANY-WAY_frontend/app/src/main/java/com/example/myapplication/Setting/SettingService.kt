package com.example.myapplication.Setting
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET

interface SettingService{

    @FormUrlEncoded
    @GET("/favorite_list/")
    fun requestFavorite(
        @Field("userid") userid: Int,
//        @Field("nickname") nickname:String,
//        @Field("address") address:String
    ) : Call<Setting>

}