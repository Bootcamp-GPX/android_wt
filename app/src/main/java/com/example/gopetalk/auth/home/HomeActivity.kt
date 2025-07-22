package com.example.gopetalk.auth.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.gopetalk.R
import com.example.gopetalk.auth.home.chanels.WalkieTalkieActivity
import com.example.gopetalk.auth.login.LoginActivity
import com.example.gopetalk.data.api.ApiClient
import com.example.gopetalk.data.storage.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var sessionManager: SessionManager
    private lateinit var presenter: HomeContract.Presenter
    private lateinit var recycler: RecyclerView

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
        toolbar.inflateMenu(R.menu.bottom_nav_menu)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    presenter.logout()
                    true
                }
                else -> false
            }
        }

        recycler = findViewById(R.id.recycler_channels)
        loadChannels()
    }

    private fun loadChannels() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getChannelService().getChannels()
                if (response.isSuccessful) {
                    val channels = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        recycler.adapter = ChannelAdapter(channels) { selectedChannel ->
                            val intent = Intent(this@HomeActivity, WalkieTalkieActivity::class.java).apply {
                                putExtra("channel_name", selectedChannel)
                            }
                            startActivity(intent)
                        }
                    }
                } else {
                    showError("Error al obtener canales (${response.code()})")
                }
            } catch (e: Exception) {
                showError("Error de red al obtener canales")
            }
        }
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun showLogoutMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
