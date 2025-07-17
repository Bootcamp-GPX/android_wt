package com.example.gopetalk.auth.home.listener

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import com.example.gopetalk.auth.home.chanels.WalkieTalkieContract
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketListenerImpl(
    private val view: WalkieTalkieContract.View,
    private val channelName: String,
    private val userId: String
) : WebSocketListener() {

    private var audioTrack: AudioTrack? = null

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "🔗 Conexión WebSocket abierta")
        view.updateStatus("🔗 Conectado a canal: $channelName")
        initAudioTrack()

        // Validación adicional antes de enviar el JOIN
        if (!channelName.startsWith("canal-")) {
            val error = "❌ Canal inválido para JOIN: '$channelName'"
            Log.e("WebSocket", error)
            view.showError(error)
            webSocket.close(1008, error) // Código 1008 = policy violation
            return
        }

        val joinMessage = """
        {
            "canal": "$channelName"
        }
    """.trimIndent()

        Log.d("WebSocket", "📡 JOIN final: $joinMessage")
        webSocket.send(joinMessage)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "📩 Mensaje de texto recibido: $text")
        if (text.contains("Puedes hablar")) {
            view.updateStatus("🎙️ Tienes permiso para hablar")
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        val audioData = bytes.toByteArray()
        Log.d("WebSocket", "🔊 Bytes recibidos: ${audioData.size}")
        audioTrack?.write(audioData, 0, audioData.size)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "🔌 Cerrando WebSocket: $code / $reason")
        webSocket.close(code, reason)
        view.updateStatus("🔌 Desconectado: $reason")
        stopAudioTrack()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "❌ Fallo en WebSocket", t)
        view.showError("Error de conexión: ${t.localizedMessage}")
        stopAudioTrack()
    }

    private fun initAudioTrack() {
        val sampleRate = 8000
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        audioTrack?.play()
        Log.d("WebSocket", "▶️ AudioTrack inicializado y en reproducción")
    }

    private fun stopAudioTrack() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        Log.d("WebSocket", "⏹️ AudioTrack detenido y liberado")
    }
}
