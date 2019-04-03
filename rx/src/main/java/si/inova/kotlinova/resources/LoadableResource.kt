package si.inova.kotlinova.resources

import io.reactivex.Flowable
import si.inova.kotlinova.data.resources.Resource

/**
 * Loadable resource that can change data mid-loading.
 * */
interface LoadableResource<T> {
    suspend fun load()
    val data: Flowable<Resource<T>>
}

inline fun <T, R> LoadableResource<T>.map(crossinline mapper: (Resource<T>) -> Resource<R>):
    LoadableResource<R> {
    val original = this

    return object : LoadableResource<R> {
        override suspend fun load() {
            original.load()
        }

        override val data: Flowable<Resource<R>>
            get() = original.data.map { mapper(it) }
    }
}