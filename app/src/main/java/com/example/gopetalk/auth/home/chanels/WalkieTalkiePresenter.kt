package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.gopetalk.data.api.GoWebSocketClient
import kotlinx.coroutines.*

class WalkieTalkiePresenter(
    private val view: WalkieTalkieContract.View,
    private val api: GoWebSocketClient
) : WalkieTalkieContract.Presenter {

    private var recorder: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private var receiverId: String = ""


    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(receiverID: String) {
        Log.d("WalkieTalkiePresenter", "Iniciando grabación de audio")

        try {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("WalkieTalkiePresenter", "AudioRecord no se inicializó correctamente")
                view.showError("Error al inicializar el micrófono")
                return
            }

            recorder?.startRecording()
            isRecording = true
            view.onRecordingStarted()

            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val audioData = ByteArray(bufferSize)
                while (isRecording && isActive) {
                    val read = recorder?.read(audioData, 0, bufferSize) ?: -1
                    if (read > 0) {
                        api.sendAudio(audioData.copyOf(read), receiverId)
                        Log.d("WalkieTalkiePresenter", "Audio enviado (${read} bytes)")
                    } else {
                        Log.w("WalkieTalkiePresenter", "Error al leer audio del micrófono: $read")
                    }
                }
                Log.d("WalkieTalkiePresenter", "Terminó ciclo de grabación")
            }

        } catch (e: Exception) {
            Log.e("WalkieTalkiePresenter", "Error iniciando grabación", e)
            view.showError("Error al iniciar grabación: ${e.localizedMessage}")
        }
    }

    override fun stopRecording() {
        Log.d("WalkieTalkiePresenter", "Deteniendo grabación")
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            recorder?.stop()
            recorder?.release()
            recorder = null
        } catch (e: Exception) {
            Log.e("WalkieTalkiePresenter", "Error al detener el micrófono", e)
        }

        view.onRecordingStopped()
    }

    override fun connectToChannel(channel: String) {
        Log.d("WalkieTalkiePresenter", "Conectando al canal: $channel")
        receiverId = channel
        api.connect(channel) { audioData ->
            Log.d("WalkieTalkiePresenter", "Audio recibido (${audioData.size} bytes)")
            CoroutineScope(Dispatchers.Main).launch {
                view.onAudioReceived(audioData)
            }
        }
    }
}
