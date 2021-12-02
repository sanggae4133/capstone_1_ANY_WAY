package com.example.myapplication

class retrofitSetting {

    companion object {

        private var ipv4 = "10.210.60.95"
        private var baseUrl = "http://" + ipv4 + ":8000/"

        fun getBaseurl(): String {
            return baseUrl.toString()
        }

    }
}