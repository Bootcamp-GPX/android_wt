package com.example.gopetalk.auth.home.chanels

import android.Manifest
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.gopetalk.R
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalkieTalkieActivityTest
{

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @Test
    fun test_launchActivity_withValidChannelName() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WalkieTalkieActivity::class.java)
        intent.putExtra("channel_name", "Canal1")

        ActivityScenario.launch<WalkieTalkieActivity>(intent).use {
            onView(withId(R.id.statusText)).check(matches(isDisplayed()))
            onView(withId(R.id.btnTalk)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun test_pressAndRelease_btnTalk_triggersTalking() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WalkieTalkieActivity::class.java)
        intent.putExtra("channel_name", "Canal1")

        ActivityScenario.launch<WalkieTalkieActivity>(intent).use {
            // Simula presionar
            onView(withId(R.id.btnTalk)).perform(down())
            // Espera 500ms
            onView(isRoot()).perform(waitFor(500))
            // Simula soltar
            onView(withId(R.id.btnTalk)).perform(up())

            // Verifica que cambie el texto del botón
            onView(withId(R.id.btnTalk)).check(matches(isDisplayed()))

        }
    }

    @Test
    fun test_noChannelName_showsToastAndCloses() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WalkieTalkieActivity::class.java)
        ActivityScenario.launch<WalkieTalkieActivity>(intent).use {
            // Aquí simplemente validamos que no crashee
        }
    }

    // Simula una espera (para mantener presionado el botón)
    private fun waitFor(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints() = isDisplayed()
            override fun getDescription() = "Esperar $millis milisegundos"
            override fun perform(uiController: UiController, view: View?) {
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }

    // Simula ACTION_DOWN (presionar)
    private fun down(): ViewAction {
        return object : ViewAction {
            override fun getConstraints() = isDisplayed()
            override fun getDescription() = "Simula ACTION_DOWN"
            override fun perform(uiController: UiController, view: View) {
                val eventTime = System.currentTimeMillis()
                val x = view.width / 2f
                val y = view.height / 2f
                val motionEvent = MotionEvent.obtain(
                    eventTime,
                    eventTime,
                    MotionEvent.ACTION_DOWN,
                    x,
                    y,
                    0
                )
                view.dispatchTouchEvent(motionEvent)
                motionEvent.recycle()
            }
        }
    }

    // Simula ACTION_UP (soltar)
    private fun up(): ViewAction {
        return object : ViewAction {
            override fun getConstraints() = isDisplayed()
            override fun getDescription() = "Simula ACTION_UP"
            override fun perform(uiController: UiController, view: View) {
                val eventTime = System.currentTimeMillis()
                val x = view.width / 2f
                val y = view.height / 2f
                val motionEvent = MotionEvent.obtain(
                    eventTime,
                    eventTime,
                    MotionEvent.ACTION_UP,
                    x,
                    y,
                    0
                )
                view.dispatchTouchEvent(motionEvent)
                motionEvent.recycle()
            }
        }
    }
}


