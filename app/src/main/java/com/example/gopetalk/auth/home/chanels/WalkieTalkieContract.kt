package com.example.gopetalk.auth.home.chanels

import android.content.Context

interface WalkieTalkieContract {
    interface View {
        fun onTalkingStarted()
        fun onTalkingStopped()
        fun onAudioReceived(data: ByteArray)
        fun onAudioSent()
        fun showError(message: String)
        fun updateStatus(status: String)
        fun getContextSafe(): Context
        fun setChannel(channel: Int)
        fun getChannel(): Int
    }

    interface Presenter {
        fun connectToChannelByName(channelName: String)
        fun disconnect()
        fun startTalking(channelName: String)
        fun stopTalking()
        fun getCurrentChannel(): Int
    }
}



