@file:JvmName("CoroutineUtils")

package si.inova.kotlinova.utils

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import kotlinx.coroutines.experimental.sync.Mutex
import kotlin.coroutines.experimental.CoroutineContext

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

fun <Any> CancellableContinuation<Any>.tryWithResumeException(exception: Throwable) {
    val res = tryResumeWithException(exception)
    if (res != null) {
        completeResume(res)
    }
}

fun <Any> CancellableContinuation<Any>.tryWithResume(result: Any) {
    val res = tryResume(result, null)
    if (res != null) {
        completeResume(res)
    }
}
