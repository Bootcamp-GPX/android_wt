package com.example.gopetalk.data.api

import com.google.gson.Gson

object ErrorUtils {
    fun parseError(json: String?): ErrorResponse? {
        return try {
            Gson().fromJson(json, ErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
}