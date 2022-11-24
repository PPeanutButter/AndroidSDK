package com.peanut.sdk.petlin

import com.peanut.sdk.petlin.Extend.describeAsFileSize
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals("11.77 MB", 12345678L.describeAsFileSize(" "))
    }
}