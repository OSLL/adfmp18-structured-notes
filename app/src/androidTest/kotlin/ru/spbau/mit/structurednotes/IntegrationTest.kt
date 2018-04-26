package ru.spbau.mit.structurednotes

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.allOf

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule
import ru.spbau.mit.structurednotes.ui.MainActivity
import ru.spbau.mit.structurednotes.ui.constructor.ConstructorActivity

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class IntegrationTest {
    @Rule
    @JvmField
    val mainActivityRule = IntentsTestRule(MainActivity::class.java)

    @Test
    fun addCategoryWithTextFields() {
        for (categoryId in 1..2) {
            onView(withId(R.id.fab))
                    .perform(click())

            onView(withId(R.id.categoryNameEditText))
                    .check(matches(isCompletelyDisplayed()))
                    .perform(click())
                    .perform(clearText())
                    .perform(typeText("Category $categoryId"))
            onView(withId(R.id.addTextButton))
                    .check(matches(isCompletelyDisplayed()))
                    .perform(click())

            onView(withId(R.id.labelEditText))
                    .inRoot(isDialog())
                    .check(matches(isCompletelyDisplayed()))
                    .perform(click())
                    .perform(clearText())
                    .perform(typeText("Text prop $categoryId"))
            onView(withText("ok"))
                    .inRoot(isDialog())
                    .check(matches(isCompletelyDisplayed()))
                    .perform(click())

            onView(allOf(isDescendantOfA(withId(R.id.template)), withId(R.id.short_note_label)))
                    .check(matches(isCompletelyDisplayed()))
                    .check(matches(withText("Text prop $categoryId")))

            onView(withId(R.id.addCategoryButton)).perform(click())

            for (i in 1..categoryId) {
                onView(allOf(isDescendantOfA(withId(R.id.card_view)), withId(R.id.name), withText("Category $i")))
                        .check(matches(isCompletelyDisplayed()))
            }
        }
    }
}
