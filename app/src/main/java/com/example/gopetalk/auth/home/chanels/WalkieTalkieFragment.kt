package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gopetalk.R
import com.example.gopetalk.auth.home.listener.AudioStreamingService
import com.example.gopetalk.data.storage.SessionManager


class WalkieTalkieFragment : Fragment(), WalkieTalkieContract.View {

    private lateinit var presenter: WalkieTalkieContract.Presenter
    private lateinit var btnTalk: Button
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var editChannelName: EditText
    private lateinit var statusText: TextView

    private var currentChannel = 1
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_walkie_talkie, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnTalk = view.findViewById(R.id.btnTalk)
        btnConnect = view.findViewById(R.id.btnConnect)
        btnDisconnect = view.findViewById(R.id.btnDisconnect)
        editChannelName = view.findViewById(R.id.editChannelName)
        statusText = view.findViewById(R.id.statusText)

        userId = SessionManager(requireContext()).getUserId()
        if (userId == -1) {
            showError("Usuario inválido")
            return
        }

        presenter = WalkieTalkiePresenter(this, userId.toString(), AudioStreamingService())

        btnConnect.setOnClickListener {
            val canalInput = editChannelName.text.toString().trim()
            if (canalInput.isEmpty()) {
                showError("Debes escribir un nombre de canal.")
            } else {
                presenter.connectToChannelByName(canalInput)
            }
        }

        btnDisconnect.setOnClickListener {
            presenter.disconnect()
            editChannelName.setText("")
        }

        btnTalk.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (checkAudioPermission()) {
                        presenter.startTalking(userId.toString())
                        btnTalk.text = "Grabando..."
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    presenter.stopTalking()
                    btnTalk.text = "Hablar"
                    true
                }
                else -> false
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1001
            )
        }

        return granted
    }

    override fun onAudioReceived(data: ByteArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            8000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize,
            AudioTrack.MODE_STREAM
        )

        track.write(data, 0, data.size)
        track.play()

        Log.d("WalkieTalkieFragment", "🎧 Audio recibido (${data.size} bytes)")

    }

    override fun onAudioSent() {
        Log.d("WalkieTalkieFragment", "Audio enviado")
    }

    override fun onTalkingStarted() {
        statusText.text = "🔴 Grabando..."
    }

    override fun onTalkingStopped() {
        statusText.text = "🟢 Conectado"
    }

    override fun showError(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getContextSafe() = requireContext()

    override fun setChannel(channel: Int) {
        currentChannel = channel
    }

    override fun getChannel(): Int = currentChannel

    override fun updateStatus(status: String) {
        statusText.text = status
    }
}