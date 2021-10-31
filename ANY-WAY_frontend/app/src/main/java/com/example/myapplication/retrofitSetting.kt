package com.example.myapplication

class retrofitSetting {
    companion object {

        private var ipv4 = "192.168.219.109"
        private var baseUrl = "http://" + ipv4 + ":8000/"

        fun getBaseurl(): String {
            return baseUrl.toString()
        }
    }
}