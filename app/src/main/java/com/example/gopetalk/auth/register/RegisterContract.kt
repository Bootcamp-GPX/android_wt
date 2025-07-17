package com.example.gopetalk.auth.register

interface RegisterContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showSuccess(message: String)
        fun showError(message: String)
        fun resetForm()
    }

    interface Presenter {
        fun register(
            name: String,
            lastName: String,
            email: String,
            password: String,
            confirmPassword: String
        )
    }
}
