/**
 * @author Matej Drobnic
 */
@file:JvmName("ObservableQueryTesting")

package si.inova.kotlinova.testing

import kotlinx.coroutines.experimental.runBlocking
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