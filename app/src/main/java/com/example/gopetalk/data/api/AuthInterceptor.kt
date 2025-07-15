package com.example.gopetalk.data.api

import android.util.Log
import com.example.gopetalk.data.storage.SessionManager
import okhttp3.Interceptor


class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = sessionManager.getAccessToken()
        val originalRequest = chain.request()
        val isMultipart = originalRequest.body?.contentType()?.subtype == "form-data"

        val requestBuilder = originalRequest.newBuilder()

        if (!isMultipart) {
            requestBuilder.addHeader("Content-Type", "application/json")
        }

        token?.let {
            Log.d("AuthInterceptor", "Agregando token de autorización")
            requestBuilder.addHeader("Authorization", "Bearer $it")
        } ?: Log.w("AuthInterceptor", "Token no encontrado. Se enviará la solicitud sin Authorization.")

        return chain.proceed(requestBuilder.build())
    }
}




