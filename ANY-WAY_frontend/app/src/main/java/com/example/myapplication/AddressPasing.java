package com.example.myapplication;

import android.location.Address;

import java.util.List;

/*
 텍스트창으로 받은 주소를  경도,위도로 파싱하는 클래스
 */
public class AddressPasing {
    private List<Address> addressList;
    public AddressPasing(List<Address> addressList) {
        this.addressList=addressList;
    }

    public String result() {
        System.out.println(addressList.get(0).toString());
        // 콤마를 기준으로 split
        String []splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
        System.out.println(address);

        String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
        String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
        System.out.println(latitude);
        System.out.println(longitude);
        String result= latitude+","+longitude;
        return result;
    }
    public String getlatitude() {
        System.out.println(addressList.get(0).toString());
        // 콤마를 기준으로 split
        String[] splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
        System.out.println(address);

        String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
        return latitude;
    }
    public String getlongitude(){
        System.out.println(addressList.get(0).toString());
        // 콤마를 기준으로 split
        String[] splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
        System.out.println(address);

        String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
        String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
        return longitude;
    }
}
