package ru.spbau.mit.structurednotes

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(ExampleInstrumentedTest::class)
class ExampleTestSuite {}

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("ru.spbau.mit.structurednotes", appContext.packageName)
    }
}
