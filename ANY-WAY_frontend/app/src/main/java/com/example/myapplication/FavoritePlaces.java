package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class FavoritePlaces {

//    {
//        "nickname": "home",
//            "address": "asdf",
//            "userid": 1
//    }
    int userId;
    String address;
    @SerializedName("body")
    String nickname;

    public int getUserId() {
        return userId;
    }

    public String getAddress() {
        return address;
    }

    public String getNickname() {
        return nickname;
    }
}
