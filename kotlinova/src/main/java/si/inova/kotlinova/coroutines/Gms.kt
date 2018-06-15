/**
 * @author Matej Drobnic
 */
@file:JvmName("GmsCoroutines")

package si.inova.kotlinova.coroutines

import com.google.android.gms.tasks.Task
import com.google.firebase.storage.CancellableTask
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Suspend current coroutine until this task is finished and return the result
 * (or throw exception if Task did not finish successfully)
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
            continuation.invokeOnCancellation {
                cancel()
            }
        }
    }
}