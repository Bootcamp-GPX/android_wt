package com.example.gopetalk.data.api

import android.content.Context
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
        if (!::retrofit.isInitialized || !::okHttpClient.isInitialized) {
            throw IllegalStateException("ApiClient no inicializado. Llama a ApiClient.init(context) primero.")
        }
    }

    fun getAuthService(): AuthService {
        checkInit()
        return retrofit.create(AuthService::class.java)
    }

    fun getChannelService(): ChannelService {
        return retrofit.create(ChannelService::class.java)
    }

    fun getWebSocket(channelName: String, userId: String, listener: WebSocketListener): WebSocket {
        val token = sessionManager.getAccessToken()

        val requestBuilder = Request.Builder()
            .url(Constants.WS_URL)

        // 🔐 Si el WebSocket requiere token, lo dejamos, si no, elimínalo.
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return okHttpClient.newWebSocket(requestBuilder.build(), listener)
    }
}

