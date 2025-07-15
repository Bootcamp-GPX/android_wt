package com.example.gopetalk.auth.register

interface RegisterContract {
    interface View {
        fun showMessage(message: String)
        fun showRegisterFail(message: String)
    }

    interface Presenter {
        fun register(
            name: String,
            last_name: String,
            age: Int,
            email: String,
            password: String,
            confirm_password: String
        )
    }
}