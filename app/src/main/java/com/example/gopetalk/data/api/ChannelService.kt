package com.example.gopetalk.data.api

import retrofit2.Response
import retrofit2.http.GET

interface ChannelService {

    @GET("channels")
    suspend fun getChannels(): Response<List<String>> // o una clase Channel si tienes un modelo

    @GET("channel-users")
    suspend fun getChannelUsers(): Response<List<UserResponse>> // cambia UserResponse por tu modelo real
}
