package com.example.gopetalk.auth.home.listener

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log

class AudioPlaybackService {
    private val bufferSize = AudioTrack.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val player = AudioTrack(
        AudioManager.STREAM_VOICE_CALL,
        16000,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize,
        AudioTrack.MODE_STREAM
    )

    fun play(data: ByteArray) {
        if (player.state != AudioTrack.STATE_INITIALIZED) {
            Log.e("AudioPlaybackService", "❌ Player no inicializado")
            return
        }
        player.play()
        player.write(data, 0, data.size)
        //player.flush()
    }
}