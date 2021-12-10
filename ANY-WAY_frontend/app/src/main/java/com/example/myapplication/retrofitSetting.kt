package com.example.myapplication

class retrofitSetting {

    companion object {
        //172.31.34.161
        private var ipv4 = "10.210.60.102"
        //private var ipv4 = "18.189.29.6"
        private var baseUrl = "http://" + ipv4 + ":8000/"

        fun getBaseurl(): String {
            return baseUrl.toString()
        }

    }
}