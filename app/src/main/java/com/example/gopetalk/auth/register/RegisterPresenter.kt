package com.example.gopetalk.auth.register

import android.util.Log
import com.example.gopetalk.data.api.ApiClient
import com.example.gopetalk.data.api.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterPresenter(private val view: RegisterContract.View) : RegisterContract.Presenter {

    override fun register(
        name: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        Log.d("RegisterPresenter", "Iniciando registro...")

        // Validaciones básicas
        if (name.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            view.showError("Por favor, completa todos los campos.")
            return
        }

        if (password != confirmPassword) {
            view.showError("Las contraseñas no coinciden.")
            return
        }

        val user = RegisterRequest(
            first_name = name,
            last_name = lastName,
            email = email,
            password = password,
            confirm_password = confirmPassword
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getAuthService().register(user)

                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    if (response.isSuccessful) {
                        Log.d("RegisterPresenter", "Registro exitoso: ${response.body()?.message}")
                        view.showSuccess("¡Registro exitoso! Revisa tu correo para validarlo.")
                        view.resetForm()
                    } else {
                        Log.e("RegisterPresenter", "Error al registrar: ${response.errorBody()?.string()}")
                        view.showError("Error en el registro. Revisa los datos o intenta más tarde.")
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterPresenter", "Excepción al registrar", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Ha ocurrido un error de red. Intenta más tarde.")
                }
            }
        }
    }
}