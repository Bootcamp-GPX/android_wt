package com.example.gopetalk.auth

import android.content.Context

interface PermissionChecker {
    fun hasMicPermission(context: Context): Boolean
}
