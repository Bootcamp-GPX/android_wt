package com.example.gopetalk.data.api

import android.content.Context
import android.util.Log
import com.example.gopetalk.data.storage.SessionManager
import com.example.gopetalk.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private lateinit var retrofit: Retrofit
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        sessionManager = SessionManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private fun checkInit() {
        if (!::retrofit.isInitialized || !::okHttpClient.isInitialized || !::sessionManager.isInitialized) {
            throw IllegalStateException("⚠️ ApiClient no inicializado. Llama a ApiClient.init(context) primero.")
        }
    }

    fun getAuthService(): AuthService {
        checkInit()
        return retrofit.create(AuthService::class.java)
    }

    fun getChannelService(): ChannelService {
        checkInit()
        return retrofit.create(ChannelService::class.java)
    }

    fun getWebSocket(channelName: String, userId: String, listener: WebSocketListener): WebSocket {
        checkInit()

        val token = sessionManager.getAccessToken()
        val url = Constants.WS_URL // ejemplo: "ws://159.203.187.94/ws"

        val request = Request.Builder()
            .url(url) // sin parámetros
            .addHeader("Authorization", "Bearer $token") // igual que en Swift
            .build()

        Log.d("WebSocket", "Conectando con headers: Authorization: Bearer ${token?.take(10)}...")

        return okHttpClient.newWebSocket(request, listener)
    }
}
