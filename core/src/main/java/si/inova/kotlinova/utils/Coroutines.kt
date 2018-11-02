@file:JvmName("CoroutineUtils")

package si.inova.kotlinova.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

/**
 * @author Matej Drobnic
 */

/**
 * @return result of the coroutine or *null* if interrupted.
 */
suspend fun <T> Deferred<T>.awaitOrNull(): T? {
    return try {
        await()
    } catch (e: CancellationException) {
        null
    }
}

/**
 * Lock current Mutex and then wait for other coroutine to unlock it.
 */
suspend fun Mutex.lockAndWaitForUnlock() {
    lock()
    lock()
    unlock()
}

/**
 * Suspend coroutine if mutex is locked until it gets unlocked
 */
suspend fun Mutex.awaitUnlockIfLocked() {
    if (tryLock()) {
        unlock()
        return
    }

    lock()
    unlock()
}

inline val CoroutineContext.isActive: Boolean
    get() = this[Job]!!.isActive

suspend fun awaitCancellation() = suspendCancellableCoroutine<Unit> { }

fun <T> CancellableContinuation<T>.tryWithResumeException(exception: Throwable) {
    val res = tryResumeWithException(exception)
    if (res != null) {
        completeResume(res)
    }
}

fun <T> CancellableContinuation<T>.resumeIfNotAlreadyResumed(result: T) {
    val res = tryResume(result, null)
    if (res != null) {
        completeResume(res)
    }
}
