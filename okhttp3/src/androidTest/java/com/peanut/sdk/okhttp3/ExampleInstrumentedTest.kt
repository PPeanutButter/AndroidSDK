package com.peanut.sdk.okhttp3

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import kotlin.concurrent.thread

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val t = thread(start = false) { val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            assertEquals("com.peanut.sdk.okhttp3", appContext.packageName)
            println("CookieJarTest")
            CookieJarTest(appContext).test()
        }
        t.start()
        t.join()
    }
}