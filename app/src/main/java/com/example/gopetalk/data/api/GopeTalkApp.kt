package com.example.gopetalk.data.api

import android.app.Application
import android.util.Log

class GopeTalkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("GopeTalkApp", "Inicializando ApiClient")
        ApiClient.init(this)
    }
}
