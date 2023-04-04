package si.inova.kotlinova.core.android

import android.location.Location

/**
 * Convenience method to create [Location] from lat/lon
 */
fun Location(lat: Double, lon: Double): Location {
   return Location(null as String?).apply {
      latitude = lat
      longitude = lon
   }
}
