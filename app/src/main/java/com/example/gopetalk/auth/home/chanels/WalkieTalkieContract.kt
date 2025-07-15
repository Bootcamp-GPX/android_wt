package com.example.gopetalk.auth.home.chanels

interface WalkieTalkieContract {
    interface View {
        fun onRecordingStarted()
        fun onRecordingStopped()
        fun onAudioReceived(audioData: ByteArray)
        fun showError(msg: String)
    }

    interface Presenter {
        fun startRecording(receiverID: String)
        fun stopRecording()
        fun connectToChannel(channel: String)
    }
}

