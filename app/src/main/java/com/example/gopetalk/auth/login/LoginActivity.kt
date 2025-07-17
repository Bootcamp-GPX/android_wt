package com.example.gopetalk.auth.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gopetalk.R
import com.example.gopetalk.auth.home.HomeActivity
import com.example.gopetalk.auth.register.RegisterActivity
import com.example.gopetalk.data.api.ApiClient
import com.example.gopetalk.data.storage.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ApiClient.init(applicationContext)


        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        val btnIniciarSesion = findViewById<Button>(R.id.btnEnter)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegister)

        sessionManager = SessionManager(applicationContext)
        presenter = LoginPresenter(this, ApiClient.getAuthService(), sessionManager)

        presenter.checkSession()

        btnIniciarSesion.setOnClickListener {
            if (validateFields()) {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()
                presenter.login(email, password)
            }
        }

        btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        setupTextWatchers()
    }

    private fun validateFields(): Boolean {
        var valido = true

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty()) {
            tilEmail.error = "El correo es obligatorio"
            valido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Correo inválido"
            valido = false
        } else {
            tilEmail.error = null
        }


        if (password.isEmpty()) {
            tilPassword.error = "La contraseña es obligatoria"
            valido = false
        } else {
            tilPassword.error = null
        }

        return valido
    }

    private fun setupTextWatchers() {
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tilEmail.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tilPassword.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun showLoginSuccess() {
        Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun showLoginError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
