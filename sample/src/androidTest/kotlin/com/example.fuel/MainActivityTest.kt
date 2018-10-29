package com.example.fuel

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val mainActivityRule = IntentsTestRule(MainActivity::class.java)

    @Test
    fun testItDisplaysRequestInformationFromCoroutineCall() {
        onView(withId(R.id.mainGoCoroutineButton))
                .perform(click())

        Thread.sleep(4000) // Wait network to finish call the ugly way

        onView(withId(R.id.mainResultText))
                .check(matches(not(withText(""))))
    }
}
