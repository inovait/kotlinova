@file:JvmName("LiveDataCoroutines")

package si.inova.kotlinova.coroutines

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import kotlinx.coroutines.experimental.withContext
import si.inova.kotlinova.utils.runOnUiThread

/**
 * Return first value from this LiveData.
 *
 * @param runAfterObserve When present, this block will be executed after LiveData becomes active
 * @param ignoreExistingValue When *true*, this method will not return existing cached value (it will wait
 * until fresh new value is received). Note that this parameter does NOT support nullable values.
 * @param runAfterCompletionBeforeRemoveObserver When present, this block will be executed after
 * LiveData has received its first value, but before its observer is removed.
 * Use it to register another observer to prevent LiveData from deinitializing itself.
 */
suspend fun <T> LiveData<T>.awaitFirstValue(
    ignoreExistingValue: Boolean = false,
    runAfterObserve: (() -> Unit)? = null,
    runAfterCompletionBeforeRemoveObserver: (() -> Unit)? = null
): T? {
    return withContext(UI) {
        var ignoreAnyValues = ignoreExistingValue

        suspendCancellableCoroutine<T?> { continuation ->
            val observer = object : Observer<T> {
                override fun onChanged(value: T?) {
                    if (ignoreAnyValues) {
                        return
                    }

                    continuation.resume(value)
                    runAfterCompletionBeforeRemoveObserver?.invoke()
                    runOnUiThread { removeObserver(this) }
                    ignoreAnyValues = true
                }
            }

            observeForever(observer)
            ignoreAnyValues = false

            runAfterObserve?.invoke()

            continuation.invokeOnCancellation {
                runAfterCompletionBeforeRemoveObserver?.invoke()
                runOnUiThread { removeObserver(observer) }
            }
        }
    }
}