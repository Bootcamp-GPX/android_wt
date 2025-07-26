package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gopetalk.R
import com.example.gopetalk.auth.DefaultLogger
import com.example.gopetalk.auth.home.listener.AudioPlaybackService
import com.example.gopetalk.auth.home.listener.AudioService
import com.example.gopetalk.data.storage.SessionManager
import com.example.gopetalk.auth.Logger


class WalkieTalkieActivity : AppCompatActivity(), WalkieTalkieContract.View {

    private lateinit var presenter: WalkieTalkieContract.Presenter
    private lateinit var btnTalk: Button 
    private lateinit var statusText: TextView

    private var currentChannel = 1
    private val RECORD_AUDIO_REQUEST_CODE = 100

    private val userId: String
        get() = SessionManager(this).getAccessToken().orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walkie_talkie)

        btnTalk = findViewById(R.id.btnTalk)
        statusText = findViewById(R.id.statusText)

        if (!hasAudioPermission()) {
            requestAudioPermission()
        }

        presenter = WalkieTalkiePresenter(
            this,
            userId,
            AudioService(),
            AudioPlaybackService(),
            DefaultLogger()
        )

        val channelName = intent.getStringExtra("channel_name")
        if (channelName.isNullOrBlank()) {
            Toast.makeText(this, "❌ Canal no recibido. Cerrando...", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        presenter.connectToChannelByName(channelName)
        setupTouchEvents()
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "🎙️ Permiso de audio concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "🚫 Se requiere permiso de micrófono", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun setupTouchEvents() {
        btnTalk.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    presenter.startTalking(userId)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    presenter.stopTalking()
                    true
                }
                else -> false
            }
        }
    }

    override fun onTalkingStarted() {
        runOnUiThread {
            btnTalk.text = "\uD83C\uDF99\uFE0F"
        }
    }

    override fun onTalkingStopped() {
        runOnUiThread {
            btnTalk.text = "\uD83D\uDD07"
        }
    }

    override fun onAudioReceived(data: ByteArray) {}
    override fun onAudioSent() {}
    override fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, "❌ $message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun updateStatus(status: String) {
        runOnUiThread {
            statusText.text = "Estado: $status"
        }
    }

    override fun getContextSafe(): Context = this
    override fun setChannel(channel: Int) { currentChannel = channel }
    override fun getChannel(): Int = currentChannel

    override fun onDestroy() {
        presenter.disconnect()
        super.onDestroy()
    }

    override fun setConnectedUsers(users: Int) {
        runOnUiThread{
            val usersText = findViewById<TextView>(R.id.connectedUsersText)
            usersText.text = "Conectados: $users"
        }
    }
}
