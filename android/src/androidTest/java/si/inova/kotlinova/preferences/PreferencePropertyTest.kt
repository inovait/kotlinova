/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.preferences

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import si.inova.kotlinova.utils.put

/**
 * @author Matej Drobnic
 */
class PreferencePropertyTest {
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun init() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        sharedPreferences.edit()
            .clear()
            .put("a", 10)
            .put("b", "Test")
            .apply()
    }

    @Test
    fun testGetAndDefaults() {
        val dataObject = object {
            var a by preference(sharedPreferences, 50)
            var b by preference(sharedPreferences, "NotTest")
            var c by preference(sharedPreferences, 99)
        }

        assertEquals(10, dataObject.a)
        assertEquals("Test", dataObject.b)
        assertEquals(99, dataObject.c)
    }

    @Test
    fun testSavingAndGetting() {
        val dataObject = object {
            var d by preference(sharedPreferences, -1)
        }

        dataObject.d = 86

        val secondDataObject = object {
            var d by preference(sharedPreferences, -1)
        }

        assertEquals(86, secondDataObject.d)
    }

    @Test
    fun testSavingAndGettingNull() {
        val dataObject = object {
            var d by preference<Int?>(sharedPreferences, -1)
        }

        dataObject.d = 86

        val secondDataObject = object {
            var d by preference<Int?>(sharedPreferences, -1)
        }

        assertEquals(86, secondDataObject.d)
        secondDataObject.d = null

        val thirdDataObject = object {
            var d by preference<Int?>(sharedPreferences, -1)
        }

        assertEquals(-1, thirdDataObject.d)
    }

    @Test
    fun testNullDefault() {
        val dataObject = object {
            var e by preference<String?>(sharedPreferences, null)
        }

        assertNull(dataObject.e)

        dataObject.e = "abc"

        val secondDataObject = object {
            var e by preference<String?>(sharedPreferences, null)
        }

        assertEquals("abc", secondDataObject.e)
    }

    @Test
    fun testReturnNullWhenDefaultNumberIsNullAndPropertyNotPresent() {
        val dataObject = object {
            var e by preference<Int?>(sharedPreferences, null)
        }

        assertNull(dataObject.e)
    }

    @Test
    fun testReturnNumberWhenDefaultNumberIsNullAndPropertyPresent() {
        val dataObject = object {
            var a by preference<Int?>(sharedPreferences, null)
        }

        assertEquals(10, dataObject.a)
    }
}