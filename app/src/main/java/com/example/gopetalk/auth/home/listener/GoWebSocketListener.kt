package com.example.gopetalk.auth.home.listener

interface GoWebSocketListener {
    fun onAudioMessageReceived(data: ByteArray)
    fun onTextMessageReceived(message: String)
}