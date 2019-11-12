package si.inova.kotlinova.testing

import io.reactivex.Single
import io.reactivex.SingleSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxCompletable
import si.inova.kotlinova.testing.fakes.ResponseInterceptor

inline fun <T> ResponseInterceptor.interceptSingle(
    crossinline singleProvider: () -> SingleSource<T>
): Single<T> {
    @Suppress("EXPERIMENTAL_API_USAGE")
    return rxCompletable(Dispatchers.Unconfined) {
        intercept()
    }.andThen(singleProvider())
}