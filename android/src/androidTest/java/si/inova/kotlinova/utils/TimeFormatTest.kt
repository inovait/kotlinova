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

package si.inova.kotlinova.utils

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {
    @Test
    fun toHoursMinutesSeconds() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("00:00:00", TimeFormat.toHoursMinutesSeconds(0, context))
        assertEquals("01:00:00", TimeFormat.toHoursMinutesSeconds(3600, context))
        assertEquals("00:01:00", TimeFormat.toHoursMinutesSeconds(60, context))
        assertEquals("00:00:01", TimeFormat.toHoursMinutesSeconds(1, context))
        assertEquals("00:02:38", TimeFormat.toHoursMinutesSeconds(158, context))
        assertEquals("150:00:00", TimeFormat.toHoursMinutesSeconds(540000, context))
        assertEquals("01:30:32", TimeFormat.toHoursMinutesSeconds(5432, context))

        assertEquals(
            "02:38",
            TimeFormat.toHoursMinutesSeconds(158, context, alwaysShowHours = false)
        )

        assertEquals(
            "01:30:32",
            TimeFormat.toHoursMinutesSeconds(5432, context, alwaysShowHours = false)
        )
    }

    @Test
    fun toMinutesSeconds() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("00:00", TimeFormat.toMinutesSeconds(0, context))
        assertEquals("01:00", TimeFormat.toMinutesSeconds(60, context))
        assertEquals("00:01", TimeFormat.toMinutesSeconds(1, context))
        assertEquals("02:38", TimeFormat.toMinutesSeconds(158, context))
        assertEquals("150:00", TimeFormat.toMinutesSeconds(9000, context))
        assertEquals("01:30", TimeFormat.toMinutesSeconds(90, context))
    }
}