/**
 * @author Matej Drobnic
 */
@file:JvmName("LocationCoroutines")

package si.inova.kotlinova.coroutines

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import si.inova.kotlinova.utils.runOnUiThread

/**
 * Start listening for speficied [LocationRequest] and return first received Location result.
 */
@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitSingleLocation(
    locationRequest: LocationRequest
): Location {
    return suspendCancellableCoroutine { continuation ->
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    continuation.resume(it)

                    runOnUiThread {
                        removeLocationUpdates(this)
                    }
                }
            }
        }

        continuation.invokeOnCancellation {
            runOnUiThread {
                removeLocationUpdates(locationCallback)
            }
        }

        runOnUiThread {
            requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
}