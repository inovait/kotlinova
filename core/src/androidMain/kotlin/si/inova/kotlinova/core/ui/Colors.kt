@file:JvmName("ColorUtils")
@file:Suppress("MagicNumber")

package si.inova.kotlinova.core.ui

import android.graphics.Color
import kotlin.math.min
import kotlin.math.roundToInt

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
