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
    val processedColor = colorString.toLowerCase().removePrefix("#")
    val parsedColor = processedColor.toLong(16)

    return when {
        processedColor.length == 6 -> parsedColor or 0xFF000000
        processedColor.length == 8 -> {
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