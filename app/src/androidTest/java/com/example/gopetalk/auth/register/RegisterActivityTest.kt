package com.example.gopetalk.auth.register

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gopetalk.R
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterActivityTest {

    private fun launchActivity() {
        ActivityScenario.launch(RegisterActivity::class.java)
    }

    @Test
    fun test_emptyFields_showsErrors() {
        launchActivity()
        onView(withId(R.id.btnRegister)).perform(click())

        onView(withId(R.id.etName)).check(matches(hasErrorText("El nombre es obligatorio")))
        onView(withId(R.id.etLastName)).check(matches(hasErrorText("El apellido es obligatorio")))
        onView(withId(R.id.etEmailAddress)).check(matches(hasErrorText("El correo es obligatorio")))
        onView(withId(R.id.etPassword)).check(matches(hasErrorText("La contraseña es obligatoria")))
        onView(withId(R.id.etConfirmPassword)).check(matches(hasErrorText("Confirma tu contraseña")))
    }

    @Test
    fun test_invalidEmail_showsError() {
        launchActivity()

        onView(withId(R.id.etName)).perform(typeText("Juan"), closeSoftKeyboard())
        onView(withId(R.id.etLastName)).perform(replaceText("Pérez"), closeSoftKeyboard())
        onView(withId(R.id.etEmailAddress)).perform(typeText("correoInvalido"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.etConfirmPassword)).perform(typeText("123456"), closeSoftKeyboard())

        onView(withId(R.id.btnRegister)).perform(click())

        onView(withId(R.id.etEmailAddress)).check(matches(hasErrorText("Correo inválido")))
    }

    @Test
    fun test_passwordMismatch_showsError() {
        launchActivity()

        onView(withId(R.id.etName)).perform(typeText("Ana"), closeSoftKeyboard())
        onView(withId(R.id.etLastName)).perform(replaceText("López"), closeSoftKeyboard())
        onView(withId(R.id.etEmailAddress)).perform(typeText("ana@correo.com"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.etConfirmPassword)).perform(typeText("000000"), closeSoftKeyboard())

        onView(withId(R.id.btnRegister)).perform(click())

        onView(withId(R.id.etConfirmPassword)).check(matches(hasErrorText("Las contraseñas no coinciden")))
    }

    @Test
    fun test_typingClearsErrors() {
        launchActivity()

        // Forzar errores
        onView(withId(R.id.btnRegister)).perform(click())

        // Escribir nombre debe borrar error
        onView(withId(R.id.etName)).perform(typeText("Pedro"), closeSoftKeyboard())
        onView(withId(R.id.etName)).check(matches(not(hasErrorText("El nombre es obligatorio"))))
    }

    @Test
    fun test_clickBackToLogin_opensLoginActivity() {
        launchActivity()
        onView(withId(R.id.btnBackToLogin)).perform(click())
        // No falla = éxito. Si quieres comprobar la intent, se puede con Espresso-Intents.
    }
}
