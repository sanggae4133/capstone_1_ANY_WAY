package com.example.myapplication;

public class LikeList {
    private String likename;
    private String location;
    private String useremail;

    public LikeList(String likename,String location,String useremail){
        this.likename=likename;
        this.location=location;
        this.useremail=useremail;
    }
    public String getLikename(){
        return likename;
    }
    public String getLocation(){
        return location;
    }
    public String getUseremail(){
        return useremail;
    }

}
