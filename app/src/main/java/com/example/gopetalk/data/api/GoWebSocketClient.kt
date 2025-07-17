package com.example.gopetalk.data.api

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString.Companion.toByteString

class GoWebSocketClient(
    private val userId: String,
    private val listener: WebSocketListener
) {
    private var ws: WebSocket? = null
    private var sendJob: Job? = null
    private var audioRecord: AudioRecord? = null

    fun connect(channelName: String) {
        ws = ApiClient.getWebSocket(channelName, userId, listener)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startSending(onSent: () -> Unit) {
        val sampleRate = 8000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()

        sendJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(bufferSize)
            while (isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (read > 0) {
                    ws?.send(buffer.copyOf(read).toByteString())
                    withContext(Dispatchers.Main) { onSent() }
                }
            }
        }
    }

    fun stopSending() {
        sendJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun disconnect() {
        ws?.close(1000, "Usuario salió del canal")
        ws = null
    }

    fun getWebSocket(): WebSocket? = ws
}
