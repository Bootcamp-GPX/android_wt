package com.example.gopetalk.data.api

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class GoWebSocketClient(private val userId: String) {

    private var webSocket: WebSocket? = null
    private var currentChannel: String? = null

    fun connect(channel: String, onReceive: (ByteArray) -> Unit) {
        Log.d("GoWebSocketClient", "Intentando conectar al canal: $channel con userId: $userId")

        disconnect() // Siempre asegurarse de cerrar conexión previa

        webSocket = ApiClient.getWebSocket(userId, channel, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GoWebSocketClient", "Conectado exitosamente al canal: $channel")
                currentChannel = channel
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("GoWebSocketClient", "Mensaje de audio recibido (tamaño: ${bytes.size} bytes) desde canal: $channel")
                onReceive(bytes.toByteArray())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("GoWebSocketClient", "Error en WebSocket del canal $channel", t)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("GoWebSocketClient", "WebSocket cerrando: [$code] $reason en canal: $channel")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GoWebSocketClient", "WebSocket cerrado: [$code] $reason en canal: $channel")
            }
        })
    }

    fun sendAudio(audio: ByteArray) {
        if (webSocket == null) {
            Log.w("GoWebSocketClient", "Intento de enviar audio sin WebSocket conectado")
            return
        }

        Log.d("GoWebSocketClient", "Enviando audio de ${audio.size} bytes al canal: $currentChannel")
        webSocket?.send(ByteString.of(*audio))
    }

    fun disconnect() {
        if (webSocket != null) {
            Log.d("GoWebSocketClient", "Desconectando del canal: $currentChannel")
            webSocket?.close(1000, "Desconexión solicitada por el cliente")
            webSocket = null
            currentChannel = null
        }
    }
}

