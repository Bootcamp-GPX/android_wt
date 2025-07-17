package com.example.gopetalk.auth.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.gopetalk.R
import com.example.gopetalk.auth.home.chanels.WalkieTalkieFragment
import com.example.gopetalk.auth.login.LoginActivity
import com.example.gopetalk.data.api.ApiClient
import com.example.gopetalk.data.storage.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var sessionManager: SessionManager
    private lateinit var presenter: HomeContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ApiClient.init(applicationContext)




        sessionManager = SessionManager(this)

        presenter = HomePresenter(
            this,
            ApiClient.getAuthService(),
            sessionManager
        )

        val toolbar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(toolbar)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, WalkieTalkieFragment())
                        .commit()
                    true
                }
                R.id.logout -> {
                    presenter.logout()
                    true
                }
                else -> false
            }
        }

    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun showLogoutMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }
}
