package com.example.gopetalk.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ChannelService {

    @GET("channels")
    suspend fun getChannels(): Response<List<String>>

    @GET("channel-users")
    suspend fun getChannelUsers(@Query("canal") canal: String): Response<List<String>>
}
