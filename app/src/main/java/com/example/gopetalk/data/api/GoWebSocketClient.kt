package com.example.gopetalk.data.api

import android.util.Log
import com.example.gopetalk.auth.home.listener.GoWebSocketListener
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class GoWebSocketClient(
    private val userId: String,
    private val listener: GoWebSocketListener
) {

    private var webSocket: WebSocket? = null

    fun connect(channel: String) {
        webSocket = ApiClient.getWebSocket(channel, userId, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: okhttp3.Response) {
                Log.d("WebSocket", "✅ Conectado al canal: $channel")
                val handshake = "{\"canal\":\"$channel\"}"
                ws.send(handshake)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WebSocket", "📨 Texto recibido: $text")
                listener.onTextMessageReceived(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "🔊 Audio recibido (${bytes.size} bytes)")
                listener.onAudioMessageReceived(bytes.toByteArray())
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WebSocket", "❌ Error: ${t.localizedMessage}", t)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "⚠️ Cerrando conexión: $code - $reason")
                ws.close(code, reason)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "🔌 WebSocket cerrado: $code - $reason")
            }
        })
    }

    fun send(data: ByteArray) {
        if (webSocket?.send(ByteString.of(*data)) == true) {
            Log.d("WebSocket", "📤 Audio enviado (${data.size} bytes)")
        } else {
            Log.e("WebSocket", "❌ Fallo al enviar audio")
        }
    }

    fun send(message: String) {
        if (webSocket?.send(message) == true) {
            Log.d("WebSocket", "📤 Mensaje enviado: $message")
        } else {
            Log.e("WebSocket", "❌ Fallo al enviar mensaje")
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Desconectado por el usuario")
        webSocket = null
        Log.d("WebSocket", "🧹 WebSocket cerrado manualmente")
    }

    fun getWebSocket(): WebSocket? = webSocket
}
