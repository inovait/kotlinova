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

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author Matej Drobnic
 */
class ColorUtilsTest {
    @Test
    fun parseHexColorTest() {
        assertEquals(0xFF123456.toInt(), parseHexColor("123456"))
        assertEquals(0xAB123456.toInt(), parseHexColor("123456AB"))
        assertEquals(0xFFAACDBA.toInt(), parseHexColor("aacdba"))
        assertEquals(0xABDE5ECC.toInt(), parseHexColor("dE5ecCAb"))
        assertEquals(0xFFAADDBB.toInt(), parseHexColor("AADDBB"))

        assertEquals(0xFFAADDBB.toInt(), parseHexColor("#AADDBB"))
        assertEquals(0xAB123456.toInt(), parseHexColor("#123456AB"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseHexColorInvalidFormatTest() {
        parseHexColor("FF")
    }

    @Test
    fun testLightening() {
        assertEquals(
            Color.rgb(80, 80, 80),
            lightenColor(Color.rgb(100, 100, 100), 0.8)
        )

        assertEquals(
            Color.rgb(120, 120, 120),
            lightenColor(Color.rgb(100, 100, 100), 1.2)
        )

        assertEquals(
            Color.rgb(15, 75, 150),
            lightenColor(Color.rgb(10, 50, 100), 1.5)
        )

        assertEquals(
            Color.rgb(255, 255, 255),
            lightenColor(Color.rgb(255, 255, 255), 1.5)
        )
    }
}