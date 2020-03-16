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

package si.inova.kotlinova.data.pagination

import io.reactivex.Flowable
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.mapData

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
            resource.mapData(converter)
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