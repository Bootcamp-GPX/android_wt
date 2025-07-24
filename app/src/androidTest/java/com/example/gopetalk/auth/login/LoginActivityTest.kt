package com.example.gopetalk.auth.login


import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gopetalk.R
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    private fun launchActivity() {
        ActivityScenario.launch(LoginActivity::class.java)
    }

    @Test
    fun test_emptyFields_showErrors() {
        launchActivity()
        onView(withId(R.id.btnEnter)).perform(click())
        onView(withId(R.id.tilEmail)).check(matches(hasDescendant(withText("El correo es obligatorio"))))
        onView(withId(R.id.tilPassword)).check(matches(hasDescendant(withText("La contraseña es obligatoria"))))
    }

    @Test
    fun test_invalidEmail_showsError() {
        launchActivity()
        onView(withId(R.id.etEmail)).perform(typeText("correo@invalido"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.btnEnter)).perform(click())
        onView(withId(R.id.tilEmail)).check(matches(hasDescendant(withText("Correo inválido"))))
    }

    @Test
    fun test_typingClearsEmailError() {
        launchActivity()
        onView(withId(R.id.btnEnter)).perform(click())
        onView(withId(R.id.etEmail)).perform(typeText("a"), closeSoftKeyboard())
        onView(withId(R.id.tilEmail)).check(matches(not(hasDescendant(withText("El correo es obligatorio")))))
    }

    @Test
    fun test_clickRegisterButton_navigatesToRegister() {
        launchActivity()
        onView(withId(R.id.btnRegister)).perform(click())

    }


    @Test
    fun test_loginSuccess_withValidCredentials() {
        launchActivity()
        onView(withId(R.id.etEmail)).perform(typeText("daniel@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.btnEnter)).perform(click())

    }
}
