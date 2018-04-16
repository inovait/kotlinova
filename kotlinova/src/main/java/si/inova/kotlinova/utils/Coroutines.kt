@file:JvmName("CoroutineUtils")

package si.inova.kotlinova.utils

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.sync.Mutex

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