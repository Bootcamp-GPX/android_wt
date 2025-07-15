package com.example.gopetalk.data.api

/**
data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Int
)**/
data class LoginResponse(
    val token: String,
    val user_id: Int
)
