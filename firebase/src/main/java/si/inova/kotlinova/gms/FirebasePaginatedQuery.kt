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

package si.inova.kotlinova.gms

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import si.inova.kotlinova.coroutines.await
import si.inova.kotlinova.data.pagination.PaginatedQuery

const val DEFAULT_PAGINATION_LIMIT = 20

/**
 * @author Matej Drobnic
 *
 * Wrapper around Firebase Firestore's query that can retrieve data in pages.
 */
class FirebasePaginatedQuery(
    private val baseQuery: Query,
    private val itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
) : PaginatedQuery<DocumentSnapshot> {

    private var lastDocument: DocumentSnapshot? = null

    override var isAtEnd = false
        private set

    override suspend fun nextPage(): List<DocumentSnapshot> {
        val queryStart = with(lastDocument) {
            when (this) {
                null -> baseQuery
                else -> baseQuery.startAfter(this)
            }
        }

        val result = queryStart.limit(itemsPerPage.toLong()).get().await()
        isAtEnd = result.isEmpty
        if (!isAtEnd) {
            lastDocument = result.documents.lastOrNull()
        }
        return result.documents
    }
}

/**
 * Convenience operator that converts regular [Query] into [PaginatedQuery]
 */
fun Query.paginate(
    itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
): PaginatedQuery<DocumentSnapshot> {
    return FirebasePaginatedQuery(this, itemsPerPage)
}