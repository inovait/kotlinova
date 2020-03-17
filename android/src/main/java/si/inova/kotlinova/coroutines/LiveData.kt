/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

@file:JvmName("LiveDataCoroutines")

package si.inova.kotlinova.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import si.inova.kotlinova.utils.runOnUiThread
import kotlin.coroutines.resume

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
    return withContext(Dispatchers.Main) {
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