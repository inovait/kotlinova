@file:JvmName("CompletableCoroutines")

package si.inova.kotlinova.rx.coroutines

import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Deprecated("Use official version from kotlinx-coroutines-rx2 artifact instead")
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
