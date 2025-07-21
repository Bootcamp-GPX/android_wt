package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.gopetalk.auth.home.listener.AudioPlaybackService
import com.example.gopetalk.auth.home.listener.AudioService
import com.example.gopetalk.auth.home.listener.GoWebSocketListener
import com.example.gopetalk.data.api.GoWebSocketClient

class WalkieTalkiePresenter(
    private val view: WalkieTalkieContract.View,
    private val userId: String,
    private val audioService: AudioService,
    private val playbackService: AudioPlaybackService
) : WalkieTalkieContract.Presenter {

    private var isTalking = false
    private var isConnected = false
    private var client: GoWebSocketClient? = null
    private var currentChannelName: String = ""

    override fun connectToChannelByName(channelName: String) {
        disconnect()

        if (!channelName.startsWith("canal-")) {
            view.showError("Canal inválido: $channelName")
            return
        }

        val listener = object : GoWebSocketListener {
            override fun onAudioMessageReceived(data: ByteArray) {
                playbackService.play(data)
            }

            override fun onTextMessageReceived(message: String) {
                if (message == "STOP") {
                    stopTalking()
                }
            }
        }

        client = GoWebSocketClient(userId, listener)
        client?.connect(channelName)

        isConnected = true
        currentChannelName = channelName
        val channelNumber = Regex("canal-(\\d+)").find(channelName)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        view.setChannel(channelNumber)

        view.updateStatus("Conectado al canal $channelName")
    }

    override fun disconnect() {
        if (!isConnected) return

        stopTalking()
        client?.disconnect()
        client = null
        isConnected = false
        view.updateStatus("Desconectado")
    }

    override fun startTalking(receiverId: String) {
        Log.d("WalkieTalkiePresenter", "Intentando empezar a hablar...")

        if (!isConnected) {
            view.showError("No estás conectado a ningún canal")
            return
        }

        if (isTalking) return

        val context = view.getContextSafe()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            view.showError("Permiso de micrófono denegado")
            return
        }

        val socket = client?.getWebSocket()
        if (socket == null) {
            view.showError("Socket no disponible")
            return
        }

        isTalking = true
        socket.send("START")
        audioService.startStreaming(client!!)

        view.onTalkingStarted()
        Log.d("WalkieTalkiePresenter", "🎤 START enviado")
    }

    override fun stopTalking() {
        if (!isTalking) return
        isTalking = false

        client?.getWebSocket()?.send("STOP")
            ?: Log.e("WalkieTalkiePresenter", "❌ No se pudo enviar STOP")

        audioService.stopStreaming()
        view.onTalkingStopped()
        Log.d("WalkieTalkiePresenter", "🛑 STOP enviado y streaming detenido")
    }

    override fun getCurrentChannel(): Int {
        return Regex("canal-(\\d+)").find(currentChannelName)?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }
}
