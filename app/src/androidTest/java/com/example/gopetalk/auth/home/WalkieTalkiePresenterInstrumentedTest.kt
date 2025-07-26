package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gopetalk.auth.Logger
import com.example.gopetalk.auth.home.listener.AudioPlaybackService
import com.example.gopetalk.auth.home.listener.AudioService
import com.example.gopetalk.data.api.GoWebSocketClient
import okhttp3.WebSocket
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.lang.reflect.Field

@RunWith(AndroidJUnit4::class)
class WalkieTalkiePresenterInstrumentedTest {

    @Mock
    private lateinit var view: WalkieTalkieContract.View

    @Mock
    private lateinit var audioService: AudioService

    @Mock
    private lateinit var playbackService: AudioPlaybackService

    @Mock
    private lateinit var logger: Logger

    private lateinit var context: android.content.Context
    private lateinit var presenter: WalkieTalkiePresenter

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        context = InstrumentationRegistry.getInstrumentation().targetContext
        `when`(view.getContextSafe()).thenReturn(context)

        presenter = WalkieTalkiePresenter(view, "user123", audioService, playbackService, logger)
    }

    @Test
    fun startTalking_whenNotConnected_showsError() {
        presenter.startTalking("otherUser")
        verify(view).showError("No estás conectado a ningún canal")
    }

    @Test
    fun getCurrentChannel_returnsCorrectNumber() {
        val field: Field = WalkieTalkiePresenter::class.java.getDeclaredField("currentChannelName")
        field.isAccessible = true
        field.set(presenter, "canal-7")

        val result = presenter.getCurrentChannel()

        Assert.assertEquals(7, result)
    }




    @Test
    fun startTalking_withPermission_startsAudioAndSendsSTART() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val client = mock(GoWebSocketClient::class.java)
            val socket = mock(WebSocket::class.java)
            `when`(client.getWebSocket()).thenReturn(socket)

            presenter.connectToFakeChannel(client)

            presenter.startTalking("otherUser")

            verify(socket).send("START")
            verify(audioService).startStreaming(client)
            verify(view).onTalkingStarted()
        }
    }

    @Test
    fun stopTalking_sendsSTOP_andStopsAudio() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val client = mock(GoWebSocketClient::class.java)
            val socket = mock(WebSocket::class.java)
            `when`(client.getWebSocket()).thenReturn(socket)

            presenter.connectToFakeChannel(client)

            presenter.startTalking("otherUser")
            presenter.stopTalking()

            verify(socket).send("STOP")
            verify(audioService).stopStreaming()
            verify(view).onTalkingStopped()
        }
    }

    // Helper para simular conexión a canal
    private fun WalkieTalkiePresenter.connectToFakeChannel(fakeClient: GoWebSocketClient) {
        val field = WalkieTalkiePresenter::class.java.getDeclaredField("client")
        field.isAccessible = true
        field.set(this, fakeClient)

        val connField = WalkieTalkiePresenter::class.java.getDeclaredField("isConnected")
        connField.isAccessible = true
        connField.setBoolean(this, true)
    }
}
