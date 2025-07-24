package com.example.gopetalk.auth.home

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.gopetalk.R
import com.example.gopetalk.auth.home.chanels.WalkieTalkieActivity
import com.example.gopetalk.auth.login.LoginActivity
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeActivityTest {

    @Before
    fun setUp() {
        init() // Espresso Intents
    }

    @After
    fun tearDown() {
        release()
    }

    @Test
    fun toolbarAndRecyclerViewAreDisplayed() {
        ActivityScenario.launch(HomeActivity::class.java)

        onView(withId(R.id.myToolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.recycler_channels)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingChannelStartsWalkieTalkieActivity() {
        val scenario = ActivityScenario.launch(HomeActivity::class.java)


        Thread.sleep(2000)
        onView(withId(R.id.recycler_channels))
            .perform(
                androidx.test.espresso.contrib.RecyclerViewActions
                    .actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(
                        0, click()
                    )
            )

        intended(hasComponent(WalkieTalkieActivity::class.java.name))
    }

    @Test
    fun logoutMenuItemRedirectsToLogin() {
        val scenario = ActivityScenario.launch(HomeActivity::class.java)

        onView(withId(R.id.logout)).perform(click())


        intended(hasComponent(LoginActivity::class.java.name))
    }
}
