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

@file:JvmName("CoroutineUtils")

package si.inova.kotlinova.utils

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