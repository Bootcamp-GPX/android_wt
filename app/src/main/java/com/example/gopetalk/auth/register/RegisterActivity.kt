package com.example.gopetalk.auth.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gopetalk.R
import com.example.gopetalk.auth.login.LoginActivity

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private lateinit var presenter: RegisterContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // UI references
        val etName = findViewById<EditText>(R.id.etName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etEmail = findViewById<EditText>(R.id.etEmailAddress)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBacktoLogin = findViewById<Button>(R.id.btnBackToLogin)

        presenter = RegisterPresenter(this)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val last_name = etLastName.text.toString().trim()
            val ageText = etAge.text.toString().trim()
            val age = ageText.toIntOrNull() ?: -1
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirm_password = etConfirmPassword.text.toString()

            // Validaciones
            if (name.isEmpty() || last_name.isEmpty() || ageText.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirm_password.isEmpty()) {
                showRegisterFail("Por favor complete todos los campos.")
                return@setOnClickListener
            }

            if (age <= 0) {
                showRegisterFail("Edad inválida. Ingrese un número mayor que 0.")
                return@setOnClickListener
            }

            if (age > 120) {
                showRegisterFail("Edad inválida. Ingrese una edad que sea valida.")
                return@setOnClickListener
            }


            if (password != confirm_password) {
                showRegisterFail("Las contraseñas no coinciden.")
                return@setOnClickListener
            }

            presenter.register(name, last_name, age, email, password, confirm_password)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnBacktoLogin.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showRegisterFail(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
