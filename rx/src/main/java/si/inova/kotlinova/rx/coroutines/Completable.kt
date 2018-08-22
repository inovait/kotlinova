/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC.   If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
 */

@file:JvmName("CompletableCoroutines")

package si.inova.kotlinova.rx.coroutines

import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

suspend fun Completable.await() {
    return suspendCancellableCoroutine { continuation ->
        var disposable: Disposable? = null
        val observer = object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onComplete() {
                continuation.resume(Unit)
            }

            override fun onError(error: Throwable) {
                continuation.resumeWithException(error)
            }
        }

        subscribe(observer)

        continuation.invokeOnCancellation {
            disposable?.dispose()
        }
    }
}
