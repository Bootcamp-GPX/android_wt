package com.example.gopetalk.auth.home

interface HomeContract {
    interface View {
        fun navigateToLogin()
        fun showLogoutMessage(message: String)
        fun showError(message: String)
    }

    interface Presenter {
        fun logout()
    }
}

