package com.example.myapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface JsonPlaceHolderAPI2 {
    @GET("like_list/")
    Call<List<LikeList>> likeRequest(@Query("useremail") String useremail);

    @POST("like_list/delete/")
    Call<LikeList> likeResponse(@Body LikeList likeList);
}
