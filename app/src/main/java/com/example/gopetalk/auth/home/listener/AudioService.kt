package com.example.gopetalk.auth.home.listener

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.gopetalk.data.api.GoWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AudioService {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var job: Job? = null

    private val bufferSize = 2048 * 2 // 4096 bytes = 2048 frames (16-bit mono)

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startStreaming(socket: GoWebSocketClient) {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000, // Frecuencia que iOS espera
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        isRecording = true
        audioRecord?.startRecording()
        Log.d("AudioService", "🎙️ Grabación iniciada")

        job = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (read == bufferSize) {
                    socket.send(buffer)
                } else if (read > 0) {
                    val fixedBuffer = ByteArray(bufferSize)
                    System.arraycopy(buffer, 0, fixedBuffer, 0, read)
                    // Rellenamos con ceros el resto
                    for (i in read until bufferSize) {
                        fixedBuffer[i] = 0
                    }
                    socket.send(fixedBuffer)
                    Log.w("AudioService", "⚠️ Enviado buffer incompleto de $read bytes, rellenado")
                }
            }
        }
    }

    fun stopStreaming() {
        isRecording = false
        job?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        Log.d("AudioService", "🛑 Grabación detenida")
    }
}
