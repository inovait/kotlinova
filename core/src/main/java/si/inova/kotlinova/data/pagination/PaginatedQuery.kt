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

/**
 * Common interface for paginated source of data
 *
 * @author Matej Drobnic
 */
interface PaginatedQuery<out T> {
    val isAtEnd: Boolean

    suspend fun nextPage(): List<T>
}

/**
 * Map all data in the paginated query into different data
 */
inline fun <T, R> PaginatedQuery<T>.map(crossinline mapMethod: (T) -> R): PaginatedQuery<R> {
    return this.mapList { it.map(mapMethod) }
}

/**
 * Map entire query page at once
 */
fun <T, R> PaginatedQuery<T>.mapList(mapMethod: suspend (List<T>) -> List<R>): PaginatedQuery<R> {
    val originalQuery = this
    return object : PaginatedQuery<R> {
        override val isAtEnd: Boolean
            get() = originalQuery.isAtEnd

        override suspend fun nextPage(): List<R> {
            return mapMethod(originalQuery.nextPage())
        }
    }
}