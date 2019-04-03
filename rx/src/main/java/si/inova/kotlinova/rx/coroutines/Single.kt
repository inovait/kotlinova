@file:JvmName("SingleCoroutines")

package si.inova.kotlinova.rx.coroutines

import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Deprecated("Use official version from kotlinx-coroutines-rx2 artifact instead")
suspend fun <T> Single<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        var disposable: Disposable? = null
        val observer = object : SingleObserver<T> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onSuccess(value: T) {
                continuation.resume(value)
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
