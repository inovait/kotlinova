package si.inova.kotlinova.data.pagination

import io.reactivex.Flowable
import si.inova.kotlinova.data.Resource

/**
 * Query that broadcasts its data asynchronously and automatically notifies observers when
 * any data in the query changed.
 *
 * To start loading data, you must call [loadFirstPage()][loadFirstPage] first.
 **
 */
interface ObservablePaginatedQuery<T> {
    val data: Flowable<Resource<List<T>>>
    val isAtEnd: Boolean

    /**
     * Load first page of the query. Function will not return until the page is fully loaded.
     */
    suspend fun loadFirstPage()

    /**
     * Load next page of this query. Function will not return until the page is fully loaded.
     */
    suspend fun loadNextPage()
}

/**
 * Apply operations on raw observable of this query
 */
inline fun <I, O> ObservablePaginatedQuery<I>.mutateRaw(
    converter: Flowable<Resource<List<I>>>.() -> Flowable<Resource<List<O>>>
): ObservablePaginatedQuery<O> {
    val original = this
    val mutatedData = with(data) { converter() }

    return object : ObservablePaginatedQuery<O> {
        override val data: Flowable<Resource<List<O>>>
            get() = mutatedData
        override val isAtEnd: Boolean
            get() = original.isAtEnd

        override suspend fun loadFirstPage() {
            original.loadFirstPage()
        }

        override suspend fun loadNextPage() {
            original.loadNextPage()
        }
    }
}

/**
 * Apply operations on whole list of this query
 */
inline fun <I, O> ObservablePaginatedQuery<I>.mutate(crossinline converter: (List<I>) -> List<O>):
    ObservablePaginatedQuery<O> {
    return mutateRaw {
        map { resource ->
            when (resource) {
                is Resource.Success -> Resource.Success(converter(resource.data))
                is Resource.Error -> Resource.Error<List<O>>(resource.exception)
                else -> throw IllegalStateException(
                    "ObservablePaginatedQuery only supports" +
                        "Resource.Success and Resource.Error. Found: $resource"
                )
            }
        }
    }
}

/**
 * Apply operations on single element of this query
 */
inline fun <I, O> ObservablePaginatedQuery<I>.map(crossinline converter: (I) -> O):
    ObservablePaginatedQuery<O> {
    return mutate { list ->
        list.map(converter)
    }
}