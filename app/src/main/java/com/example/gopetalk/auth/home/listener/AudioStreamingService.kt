package com.example.gopetalk.auth.home.listener

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString

class AudioStreamingService {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private val sampleRate = 16000 // 16kHz (común para voz)
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startStreaming(socket: WebSocket) {
        if (audioRecord != null) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        Log.d("AudioStreaming", "🎙️ Grabación iniciada")

        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(bufferSize)

            while (isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    socket.send(buffer.toByteString()) // WebSocket lo manda como binario
                }
            }
        }
    }

    fun stopStreaming() {
        recordingJob?.cancel()
        recordingJob = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        Log.d("AudioStreaming", "🛑 Grabación detenida")
    }
}