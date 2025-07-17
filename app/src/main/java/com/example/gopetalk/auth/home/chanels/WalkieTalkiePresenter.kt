package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.gopetalk.auth.home.listener.AudioStreamingService
import com.example.gopetalk.auth.home.listener.WebSocketListenerImpl
import com.example.gopetalk.data.api.ApiClient
import com.example.gopetalk.data.api.GoWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.WebSocket

class WalkieTalkiePresenter(
    private val view: WalkieTalkieContract.View,
    private val userId: String,
    private val audioService: AudioStreamingService // 💡 Inyectamos el servicio aquí
) : WalkieTalkieContract.Presenter {

    private var currentChannel = 1
    private var isTalking = false
    private var isConnected = false
    private var client: GoWebSocketClient? = null
    private var channels: List<String> = emptyList()
    private var currentChannelName: String = ""

    init {
        loadChannelsFromApi()
    }

    private fun loadChannelsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getChannelService().getChannels()
                if (response.isSuccessful) {
                    channels = (response.body() ?: emptyList()).map { it.toString() }
                    Log.d("CANALES", channels.toString())
                    if (channels.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            view.showError("No hay canales disponibles.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        view.showError("Error al obtener canales (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.showError("Error de red al obtener canales")
                }
            }
        }
    }

    override fun connectToChannelByName(channelName: String) {
        Log.d("CHANNEL_CONNECT", "🔍 Canal recibido: $channelName")
        if (channels.isEmpty()) {
            view.showError("Los canales aún no se han cargado.")
            return
        }

        val index = channels.indexOf(channelName)
        if (index == -1) {
            view.showError("El canal '$channelName' no existe.")
            return
        }

        connectToChannel(index + 1)
    }

    override fun disconnect() {
        if (isConnected) {
            stopTalking() // asegúrate de cortar el streaming si estaba hablando
            client?.disconnect()
            isConnected = false
            view.updateStatus("Desconectado")
        } else {
            view.showError("No estás conectado a ningún canal.")
        }
    }

    override fun connectToChannel(channel: Int) {
        if (channels.isEmpty()) {
            view.showError("No se han cargado los canales aún.")
            return
        }

        if (channel !in 1..channels.size) {
            view.showError("Canal fuera de rango (1-${channels.size})")
            return
        }

        currentChannel = channel
        currentChannelName = channels[channel - 1] // ← ¡Aquí aseguramos que se usa el nombre real!

        client?.disconnect()

        val listener = WebSocketListenerImpl(view, currentChannelName, userId)
        client = GoWebSocketClient(userId, listener)
        client?.connect(currentChannelName)

        isConnected = true
        view.setChannel(channel)
        view.updateStatus("Conectado al canal $currentChannelName")
    }

    override fun startTalking(receiverId: String) {
        if (!isConnected) {
            view.showError("No estás conectado a ningún canal")
            return
        }

        if (isTalking) return

        val context = view.getContextSafe()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            view.showError("Permiso de micrófono denegado")
            return
        }

        val socket: WebSocket? = client?.getWebSocket()
        if (socket == null) {
            view.showError("Socket no disponible para transmitir audio")
            return
        }

        isTalking = true
        socket.send("START")
        audioService.startStreaming(socket)

        view.onTalkingStarted()
        Log.d("WalkieTalkiePresenter", "🎤 START enviado, streaming comenzado")
    }

    override fun stopTalking() {
        if (!isTalking) return
        isTalking = false

        val socket: WebSocket? = client?.getWebSocket()
        if (socket != null) {
            socket.send("STOP")
            Log.d("WalkieTalkiePresenter", "🛑 STOP enviado")
        } else {
            Log.e("WalkieTalkiePresenter", "❌ Socket no disponible al detener")
        }

        audioService.stopStreaming()
        view.onTalkingStopped()
        Log.d("WalkieTalkiePresenter", "🎧 Streaming detenido")
    }

    override fun increaseChannel() {
        if (channels.isEmpty()) {
            view.showError("Los canales aún no se han cargado.")
            return
        }

        if (currentChannel < channels.size) {
            currentChannelName = channels[currentChannel]
            connectToChannel(currentChannel + 1)
        } else {
            view.showError("Ya estás en el canal máximo (${channels.size})")
        }
    }

    override fun decreaseChannel() {
        if (currentChannel > 1) {
            currentChannelName = channels[currentChannel - 2]
            connectToChannel(currentChannel - 1)
        } else {
            view.showError("Ya estás en el canal mínimo (1)")
        }
    }

    override fun getCurrentChannel(): Int = currentChannel
}
