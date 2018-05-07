package si.inova.kotlinova.utils

import java.util.Locale

/**
 * @author Kristjan Kotnik
 */

/**
 * Return true if user have metrics locale
 */
fun isUserLocaleMetric(): Boolean {
    when (Locale.getDefault().country) {
        "US" -> return false
        "LR" -> return false
        "MM" -> return false
        "" -> return false
        null -> return false
    }
    return true
}