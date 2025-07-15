package com.example.gopetalk.auth.register

import com.example.gopetalk.data.api.*
import com.example.gopetalk.utils.Constants
import kotlinx.coroutines.*
import android.util.Log

class RegisterPresenter(private val view: RegisterContract.View) : RegisterContract.Presenter {

    private val service = ApiClient.getService()

    override fun register(
        name: String,
        last_name: String,
        age: Int,
        email: String,
        password: String,
        confirm_password: String
    ) {
        Log.d("RegisterPresenter", "Preparando registro...")
        Log.d("RegisterPresenter", "Datos recibidos -> nombre: $name, apellido: $last_name, edad: $age, email: $email")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = RegisterRequest(
                    name = name,
                    last_name = last_name,
                    age = age,
                    email = email,
                    password = password,
                    confirm_password = confirm_password
                )
                Log.d("RegisterPresenter", "Enviando solicitud al servidor: $request")

                val response = service.register(request)

                withContext(Dispatchers.Main) {
                    Log.d("RegisterPresenter", "Respuesta recibida. Código: ${response.code()}")

                    if (response.isSuccessful) {
                        val message = response.body()?.message ?: Constants.DEFAULT_SUCCESS
                        Log.d("RegisterPresenter", "Registro exitoso: $message")
                        view.showMessage(message)
                    } else {
                        val errorJson = response.errorBody()?.string()
                        val errorResponse = ErrorUtils.parseError(errorJson)

                        if (errorResponse != null) {
                            Log.e("RegisterPresenter", "Error del servidor: ${errorResponse.error.message}")
                            view.showRegisterFail("Error: ${errorResponse.error.message}")
                        } else {
                            Log.e("RegisterPresenter", "No se pudo parsear error: $errorJson")
                            view.showRegisterFail(Constants.UNKNOWN_ERROR)
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("RegisterPresenter", "Excepción capturada en registro", e)
                    view.showRegisterFail("${Constants.NETWORK_ERROR}${e.localizedMessage}")
                }
            }
        }
    }
}
