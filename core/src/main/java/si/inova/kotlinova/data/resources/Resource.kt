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

package si.inova.kotlinova.data.resources

/**
 * @author Matej Drobnic
 */
sealed class Resource<T> {
    class Cancelled<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val exception: Throwable) : Resource<T>()
    data class Loading<T>(val data: T? = null, val progress: Float? = null) : Resource<T>()
}

val <T> Resource<T>.value: T?
    get() = when {
        this is Resource.Success -> data
        this is Resource.Loading -> data
        else -> null
    }

/**
 * Apply function on data of this Resource (if it contains any data)
 */
inline fun <I, O> Resource<I>.mapData(mapper: (I) -> O): Resource<O> {
    return when (this) {
        is Resource.Success -> Resource.Success(
            mapper(data)
        )
        is Resource.Error -> Resource.Error(
            exception
        )
        is Resource.Cancelled -> Resource.Cancelled()
        is Resource.Loading -> {
            val newData = data?.let(mapper)
            return Resource.Loading(newData)
        }
    }
}

/**
 * Returns resource's value or throws an exception if resource contains an exception.
 *
 * This method only supports [Resource.Success] and [Resource.Error]
 */
fun <T> Resource<T>.unwrap(): T {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> throw exception
        else -> error("Resource.unwrap() only supports final resource (Success or Error)")
    }
}