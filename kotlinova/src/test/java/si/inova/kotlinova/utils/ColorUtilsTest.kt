package si.inova.kotlinova.utils

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author Matej Drobnic
 */
@RunWith(RobolectricTestRunner::class)
class ColorUtilsTest {
    @Test
    fun parseHexColorTest() {
        assertEquals(0xFF123456.toInt(), parseHexColor("123456"))
        assertEquals(0xAB123456.toInt(), parseHexColor("123456AB"))
        assertEquals(0xFFAACDBA.toInt(), parseHexColor("aacdba"))
        assertEquals(0xABDE5ECC.toInt(), parseHexColor("dE5ecCAb"))
        assertEquals(0xFFAADDBB.toInt(), parseHexColor("AADDBB"))
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