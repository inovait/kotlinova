package si.inova.kotlinova.rx

import io.reactivex.Flowable
import kotlinx.coroutines.reactive.awaitFirst
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.unwrap

/**
 * Await first value from this resource stream (or throw exception if error happens)
 *
 * @exception NoSuchElementException if stream terminates before Error or Success resource
 *
 * @see si.inova.kotlinova.data.resources.Resource.unwrap
 */
suspend fun <T> Flowable<Resource<T>>.awaitFirstUnpacked(): T {
    return filter {
        it is Resource.Success || it is Resource.Error
    }
        .awaitFirst()
        .unwrap()
}
