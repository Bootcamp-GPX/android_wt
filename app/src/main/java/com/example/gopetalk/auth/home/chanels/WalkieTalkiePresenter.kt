package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.gopetalk.auth.home.listener.AudioPlaybackService
import com.example.gopetalk.auth.home.listener.AudioService
import com.example.gopetalk.auth.home.listener.GoWebSocketListener
import com.example.gopetalk.data.api.ApiClient
import com.example.gopetalk.data.api.GoWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var pollingJob: Job? = null

    override fun connectToChannelByName(channelName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.getChannelService().getChannelUsers(channelName)

            withContext(Dispatchers.Main) {
                if (!channelName.startsWith("canal-")) {
                    view.showError("Canal inválido: $channelName")
                    return@withContext
                }

                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()

                    //con esta funcion sabemos cuantos usuarios estan en el mismo canal en el que estamos nosotros
                    view.setConnectedUsers(users.size)

                    //aca verificamos si el canal esta lleno
                    if (users.size >= 5) {
                        view.showError("El canal está lleno (5 usuarios máximo)")
                        return@withContext
                    }
                } else {
                    view.showError("No se pudo verificar el canal: ${response.code()}")
                    return@withContext
                }

                // Si pasó todas las validaciones anteriores, conectamos al canal
                disconnect()

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

                val channelNumber = Regex("canal-(\\d+)")
                    .find(channelName)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                view.setChannel(channelNumber)
                view.updateStatus("Conectado al $channelName")

                startPollingUserCount(channelName)
            }
        }
    }

    override fun disconnect() {
        if (!isConnected) return

        stopTalking()
        client?.disconnect()
        stopPollingUserCount()
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

    private fun startPollingUserCount(channelName: String) {
        pollingJob?.cancel()
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isConnected) {
                try {
                    val response = ApiClient.getChannelService().getChannelUsers(channelName)
                    if (response.isSuccessful) {
                        val users = response.body() ?: emptyList()
                        withContext(Dispatchers.Main) {
                            view.setConnectedUsers(users.size)
                        }
                    } else {
                        Log.e("Polling", "Respuesta fallida: ${response.code()}")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        view.showError("Error al obtener usuarios conectados: ${e.message}")
                        Log.e("Polling", "Excepción: ${e.message}")
                    }
                }

                // Esperar siempre, haya éxito o error
                delay(5000)
            }
        }
    }

    private fun stopPollingUserCount() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
