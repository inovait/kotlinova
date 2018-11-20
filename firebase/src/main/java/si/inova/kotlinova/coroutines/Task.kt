/**
 * @author Matej Drobnic
 */
@file:JvmName("FirebaseTaskCoroutines")

package si.inova.kotlinova.coroutines

import com.google.firebase.storage.CancellableTask
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspend current coroutine until this task is finished and return the result
 * (or throw exception if Task did not finish successfully)
 */
suspend fun <T> CancellableTask<T>.await(): T {
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

        continuation.invokeOnCancellation {
            cancel()
        }
    }
}