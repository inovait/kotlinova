@file:JvmName("CoroutineUtils")

package si.inova.kotlinova.utils

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Deferred

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