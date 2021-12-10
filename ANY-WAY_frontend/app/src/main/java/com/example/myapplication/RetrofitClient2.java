package com.example.myapplication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient2 {
    private static final String BASE_URL = "http://10.210.60.102:8000/";
    //private static final String BASE_URL = "http://18.189.29.6:8000/";
    public static JsonPlaceHolderAPI2 getApiService(){return getInstance().create(JsonPlaceHolderAPI2.class);}

    private static Retrofit getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
