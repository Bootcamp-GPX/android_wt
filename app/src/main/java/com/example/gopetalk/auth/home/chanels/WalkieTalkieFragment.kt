package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioTrack
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gopetalk.R
import com.example.gopetalk.data.api.GoWebSocketClient
import com.example.gopetalk.data.storage.SessionManager

class WalkieTalkieFragment : Fragment(), WalkieTalkieContract.View {

    private lateinit var presenter: WalkieTalkieContract.Presenter
    private lateinit var btnTalk: Button
    private lateinit var txtChannel: EditText
    private lateinit var btnConnect: Button
    private lateinit var statusText: TextView

    private lateinit var sessionManager: SessionManager

    private var isTalking = false
    private var canalSeleccionado: String? = null // almacena el canal actual

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_walkie_talkie, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Log.d("WalkieTalkieFragment", "onViewCreated llamado")

        sessionManager = SessionManager(requireContext())

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Log.e("WalkieTalkieFragment", "ID de usuario no válido (-1).")
            Toast.makeText(context, "ID de usuario inválido. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show()
            return
        }

        val userIdString = userId.toString()
        Log.d("WalkieTalkieFragment", "UserID obtenido: $userIdString")

        val socketClient = GoWebSocketClient(userId.toString())
        presenter = WalkieTalkiePresenter(this, socketClient)

        btnTalk = view.findViewById(R.id.btnTalk)
        txtChannel = view.findViewById(R.id.txtChannel)
        btnConnect = view.findViewById(R.id.btnConnect)
        statusText = view.findViewById(R.id.statusText)

        btnConnect.setOnClickListener {
            val channel = txtChannel.text.toString().trim()
            if (channel.isNotEmpty()) {
                canalSeleccionado = channel
                Log.d("WalkieTalkieFragment", "Botón conectar presionado. Canal: $channel")
                presenter.connectToChannel(channel)
                Toast.makeText(context, "Conectado al canal: $channel", Toast.LENGTH_SHORT).show()
            } else {
                Log.w("WalkieTalkieFragment", "Intento de conexión sin canal")
                Toast.makeText(context, "Ingresa un canal", Toast.LENGTH_SHORT).show()
            }
        }


        btnTalk.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    Log.d("WalkieTalkieFragment", "Botón presionado - Comienza grabación")
                    if (checkAudioPermission()) {
                        isTalking = true
                        presenter.startRecording()
                        btnTalk.text = "Grabando..."
                    }
                    true
                }

                android.view.MotionEvent.ACTION_UP -> {
                    Log.d("WalkieTalkieFragment", "Botón soltado - Termina grabación")
                    if (isTalking) {
                        isTalking = false
                        presenter.stopRecording()
                        btnTalk.text = "Hablar"
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 101)
            return false
        }
        return true
    }

    override fun onRecordingStarted() {
        statusText.text = "🔴 Grabando..."
    }

    override fun onRecordingStopped() {
        statusText.text = "🟢 Conectado"
    }

    override fun onAudioReceived(audioData: ByteArray) {
        playAudio(audioData)
    }

    private fun playAudio(audioData: ByteArray) {
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            audioData.size,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(audioData, 0, audioData.size)
        audioTrack.play()
    }

    override fun showError(msg: String) {
        Toast.makeText(requireContext(), "Error: $msg", Toast.LENGTH_SHORT).show()
    }
}