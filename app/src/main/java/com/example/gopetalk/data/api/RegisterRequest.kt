package com.example.gopetalk.data.api

data class RegisterRequest(
    val name: String,
    val last_name: String,
    val age: Int,
    val email: String,
    val password: String,
    val confirm_password: String
)