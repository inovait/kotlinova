package si.inova.kotlinova.coroutines

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.CancellableTask
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * @author Matej Drobnic
 */

suspend fun <T> Task<T>.await(): T {
    if (this.isSuccessful) {
        return this.result
    }

    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(it.result)
            } else {
                continuation.resumeWithException(it.exception!!)
            }
        }

        if (this is CancellableTask) {
            continuation.invokeOnCompletion {
                if (continuation.isCancelled) {
                    cancel()
                }
            }
        }
    }
}

/**
 * Return first value from this LiveData.
 *
 * @param runAfterObserve When present, this block will be executed after LiveData becomes active
 * @param ignoreExistingValue When *true*, this method will not return existing cached value (it will wait
 * until fresh new value is received). Note that this parameter does NOT support nullable values.
 */
suspend fun <T> LiveData<T>.awaitFirstValue(
    ignoreExistingValue: Boolean = false,
    runAfterObserve: (() -> Unit)? = null
): T? {
    var ignoreAnyValues = ignoreExistingValue

    return suspendCancellableCoroutine { continuation ->
        val observer = Observer<T> {
            if (ignoreAnyValues) {
                return@Observer
            }

            continuation.resume(it)
            ignoreAnyValues = true
        }

        observeForever(observer)
        ignoreAnyValues = false

        runAfterObserve?.invoke()

        continuation.invokeOnCompletion {

            this@awaitFirstValue.removeObserver(observer)
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitSingleLocation(
    locationRequest: LocationRequest
): Location {
    return suspendCancellableCoroutine { continuation ->
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    continuation.resume(it)
                }
            }
        }

        continuation.invokeOnCompletion {
            removeLocationUpdates(locationCallback)
        }

        requestLocationUpdates(locationRequest, locationCallback, null)
    }
}
