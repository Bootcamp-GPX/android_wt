package com.example.gopetalk.auth

import android.util.Log

class DefaultLogger : Logger {
    override fun log(tag: String, message: String) {
        Log.d(tag, message)
    }
}