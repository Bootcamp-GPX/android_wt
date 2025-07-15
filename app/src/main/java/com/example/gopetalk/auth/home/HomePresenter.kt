package com.example.gopetalk.auth.home

import android.util.Log
import com.example.gopetalk.data.api.AuthService
import com.example.gopetalk.data.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePresenter(
    private val view: HomeContract.View,
    private val authService: AuthService,
    private val sessionManager: SessionManager
) : HomeContract.Presenter {

    override fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = authService.logout()
                if (response.isSuccessful) {
                    Log.d("Logout", "Sesión cerrada en servidor")
                } else {
                    Log.e("Logout", "Error al cerrar sesión: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("Logout", "Error inesperado: ${e.localizedMessage}")
            }

            sessionManager.clearSession()

            withContext(Dispatchers.Main) {
                view.showLogoutMessage("Sesión cerrada")
                view.navigateToLogin()
            }
        }
    }
}