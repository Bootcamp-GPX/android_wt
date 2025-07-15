package com.example.gopetalk.auth.login

import android.util.Log
import com.example.gopetalk.data.api.AuthService
import com.example.gopetalk.data.api.LoginRequest
import com.example.gopetalk.data.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private val view: LoginContract.View,
    private val api: AuthService,
    private val sessionManager: SessionManager
) : LoginContract.Presenter {

    override fun login(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    sessionManager.saveAccessToken(body.token)
                    sessionManager.saveUserId(body.user_id)

                    Log.d("LoginPresenter", "Guardado userId: ${body.user_id}")

                    withContext(Dispatchers.Main) {
                        view.showLoginSuccess()
                    }
                } else {
                    val message = when (response.code()) {
                        401 -> "Usuario o contraseña incorrectos"
                        404 -> "Usuario no encontrado"
                        else -> "Error desconocido (${response.code()})"
                    }
                    withContext(Dispatchers.Main) {
                        view.showLoginError(message)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.showLoginError("Error: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun checkSession() {
        if (!sessionManager.getAccessToken().isNullOrEmpty()) {
            view.navigateToHome()
        }
    }
}
