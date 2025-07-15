package com.example.gopetalk.data.api

import android.content.Context
import com.example.gopetalk.data.storage.SessionManager
import com.example.gopetalk.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private lateinit var retrofit: Retrofit
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        sessionManager = SessionManager(context)

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    fun getService(): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    fun getWebSocket(userId: String, channel: String, listener: WebSocketListener): WebSocket {
        val request = Request.Builder()
            .url("ws://tu-servidor.com/ws/audio?channel=$channel&user_id=$userId")
            .build()

        return okHttpClient.newWebSocket(request, listener)
    }
}







