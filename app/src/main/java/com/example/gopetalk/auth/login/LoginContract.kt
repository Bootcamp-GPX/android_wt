package com.example.gopetalk.auth.login

interface LoginContract {
    interface View {
        fun showLoginSuccess()
        fun showLoginError(message: String)
        fun navigateToHome()
    }

    interface Presenter {
        fun login(email: String, password: String)
        fun checkSession()
    }
}