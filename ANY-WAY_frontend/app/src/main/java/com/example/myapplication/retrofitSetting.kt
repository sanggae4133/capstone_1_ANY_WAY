package com.example.myapplication

class retrofitSetting {
    companion object {

        private var ipv4 = "172.30.1.23"
        private var baseUrl = "http://" + ipv4 + ":8000/"

        fun getBaseurl(): String {
            return baseUrl.toString()
        }
    }
}