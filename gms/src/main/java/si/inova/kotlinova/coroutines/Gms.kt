/**
 * @author Matej Drobnic
 */
@file:JvmName("GmsCoroutines")

package si.inova.kotlinova.coroutines

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspend current coroutine until this task is finished and return the result
 * (or throw exception if Task did not finish successfully)
 */
suspend fun <T> Task<T>.await(): T {
    if (this.isSuccessful) {
        return this.result
    }

    return suspendCoroutine { continuation ->
        addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(it.result)
            } else {
                continuation.resumeWithException(it.exception!!)
            }
        }
    }
}