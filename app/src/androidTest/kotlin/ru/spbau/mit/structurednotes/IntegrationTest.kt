package ru.spbau.mit.structurednotes

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import org.hamcrest.Matchers.allOf
import org.junit.BeforeClass
import org.junit.FixMethodOrder

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule
import org.junit.runners.MethodSorters
import ru.spbau.mit.structurednotes.data.Audio
import ru.spbau.mit.structurednotes.ui.MainActivity

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class IntegrationTest {
    @Rule
    @JvmField
    val mainActivityRule = IntentsTestRule(MainActivity::class.java)

    companion object {
        @BeforeClass
        @JvmStatic
        fun clearData() {
            InstrumentationRegistry.getTargetContext().filesDir.resolve("data").delete()
            InstrumentationRegistry.getTargetContext().filesDir.resolve("cards").delete()
        }
    }

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

    @Test
    fun addItemsCategory1() {
        val NOTES = 10

        for (dataId in 1..NOTES) {
            onView(allOf(withText("Category 1"), withId(R.id.name)))
                    .check(matches(isCompletelyDisplayed()))
                    .perform(click())
            onView(withId(R.id.short_note_label))
                    .check(matches(withText("Text prop 1")))
                    .perform(click())
                    .check(matches(isCompletelyDisplayed()))
            onView(withId(R.id.short_note_note))
                    .check(matches(isCompletelyDisplayed()))
                    .perform(click())
                    .perform(clearText())
                    .perform(typeText("d$dataId"))
            onView(withId(R.id.addButton))
                    .check(matches(isCompletelyDisplayed()))
                    .perform(click())
        }

        onView(allOf(hasSibling(withText("Category 1")), withId(R.id.list_layout)))
                .check(matches(isCompletelyDisplayed()))
                .perform(click())

        for (i in 1..NOTES - 1) {
            onView(withId(R.id.notes_list_view))
                    .perform(scrollToPosition<RecyclerView.ViewHolder>(i + 1))
            onView(allOf(withText("d$i"), hasSibling(withText("Text prop 1"))))
                    .check(matches(isDisplayed()))
        }
    }
}
