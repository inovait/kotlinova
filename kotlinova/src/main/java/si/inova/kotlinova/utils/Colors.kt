@file:JvmName("ColorUtils")

package si.inova.kotlinova.utils

import android.graphics.Color
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author Matej Drobnic
 */

/**
 * Method that parses hex color strings and converts them into Android colors.
 *
 * @param colorString Color in either "RRGGBBAA" or "RRGGBB" hex formats.
 *
 * @return Android's [Color] int (0xAARRGGBB)
 */
fun parseHexColor(colorString: String): Int {
    val parsedColor = colorString.toLowerCase().toLong(16)

    return when {
        colorString.length == 6 -> parsedColor or 0xFF000000
        colorString.length == 8 -> {
            val rgb = parsedColor and 0xFFFFFF00
            val alpha = parsedColor and 0x000000FF

            (rgb shr 8) or (alpha shl 24)
        }
        else -> throw IllegalArgumentException("Unknown color string format: $colorString")
    }.toInt()
}

/**
 * Method that lightens specified color using provided [multiplier].
 */
fun lightenColor(color: Int, multiplier: Double): Int {
    val a = Color.alpha(color)
    val r = min(255, (Color.red(color) * multiplier).roundToInt())
    val g = min(255, (Color.green(color) * multiplier).roundToInt())
    val b = min(255, (Color.blue(color) * multiplier).roundToInt())

    return Color.argb(a, r, g, b)
}

/**
 * @return Luminance of the color between 0-1
 */
fun colorLuminance(color: Int): Double {
    return 0.2126 * Color.red(color) / 255 +
            0.7152 * Color.green(color) / 255 +
            0.0722 * Color.blue(color) / 255
}