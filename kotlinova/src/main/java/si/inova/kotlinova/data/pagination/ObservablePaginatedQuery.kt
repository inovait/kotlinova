package si.inova.kotlinova.data.pagination

import android.arch.lifecycle.LiveData
import si.inova.kotlinova.data.Resource
import si.inova.kotlinova.utils.map

/**
 * Query that broadcasts its data asynchronously and automatically notifies observers when
 * any data in the query changed
 *
 */
interface ObservablePaginatedQuery<T> {
    val data: LiveData<Resource<List<T>>>
    val isAtEnd: Boolean

    /**
     * Load next page of this query. Note that this method does NOT block or suspend, it returns
     * immediately and data is later updated asynchronously.
     */
    fun loadNextPage()
}

/**
 * Map all data in the paginated query into different data
 */
inline fun <T, R> ObservablePaginatedQuery<T>.map(
    crossinline mapMethod: (T) -> R
): ObservablePaginatedQuery<R> {
    return this.mapList {
        it.map(mapMethod)
    }
}

/**
 * Map entire query page at once
 */
fun <T, R> ObservablePaginatedQuery<T>.mapList(
    mapMethod: (List<T>) -> List<R>
): ObservablePaginatedQuery<R> {
    val originalQuery = this

    return object : ObservablePaginatedQuery<R> {
        override val data: LiveData<Resource<List<R>>>
            get() {
                return originalQuery.data.map {
                    if (it == null) {
                        throw NullPointerException()
                    }

                    when (it) {
                        is Resource.Error -> Resource.Error<List<R>>(it.exception)
                        is Resource.Success -> Resource.Success(mapMethod(it.data))
                        else -> throw UnsupportedOperationException(
                                "ObservablePaginatedQuery only supports " +
                                        "Resource.Error and Resource.Success"
                        )
                    }
                }
            }

        override val isAtEnd: Boolean
            get() = originalQuery.isAtEnd

        override fun loadNextPage() {
            return originalQuery.loadNextPage()
        }
    }
}