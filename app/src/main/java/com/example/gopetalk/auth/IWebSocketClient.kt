package com.example.gopetalk.data.api

import okhttp3.WebSocket

interface  IWebSocketClient {
    fun connect(channel: String)
    fun send(data: ByteArray)
    fun send(message: String)
    fun disconnect()
    fun getWebSocket(): WebSocket?
}
