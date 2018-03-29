@file:JvmName("LocationUtils")

package si.inova.kotlinova.utils

import android.location.Location

/**
 * @author Matej Drobnic
 */

/**
 * Convenience method to create [Location] from lat/lon
 */
fun createLocation(lat: Double, lon: Double): Location {
    return Location(null as String?).apply {
        latitude = lat
        longitude = lon
    }
}