package com.example.gopetalk.auth.home.listener

import com.example.gopetalk.data.api.GoWebSocketClient

interface IAudioService {
    fun startStreaming(socket: GoWebSocketClient)
    fun stopStreaming()
}