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

/**
 * @author Matej Drobnic
 */
@file:JvmName("ObservableQueryTesting")

package si.inova.kotlinova.testing

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.data.resources.Resource

fun <T> ObservablePaginatedQuery<T>.getAll(): List<T> {
    var receivedValue: Resource<List<T>>? = null
    var receivedException: Throwable? = null

    val subscriber = data.subscribe({
        receivedValue = it
    }, {
        receivedException = it
    })

    runBlocking {
        loadFirstPage()

        while (!isAtEnd) {
            loadNextPage()
        }
    }

    if (receivedException != null) {
        throw receivedException!!
    }

    Assert.assertNotNull("Received no value from the paginated query", receivedValue)

    assertIs(receivedValue, Resource.Success::class.java)

    subscriber.dispose()
    return (receivedValue as Resource.Success).data
}