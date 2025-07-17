package com.example.gopetalk.data.storage

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "gopetalk_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_FIRST_NAME = "first_name"
        private const val KEY_USER_LAST_NAME = "last_name"
        private const val KEY_USER_EMAIL = "email"
    }

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun saveUserName(firstName: String) {
        prefs.edit().putString(KEY_USER_FIRST_NAME, firstName).apply()
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_FIRST_NAME, null)
    }

    fun saveUserLastName(lastName: String) {
        prefs.edit().putString(KEY_USER_LAST_NAME, lastName).apply()
    }

    fun getUserLastName(): String? {
        return prefs.getString(KEY_USER_LAST_NAME, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserIdAsString(): String {
        return getUserId().toString()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
}

