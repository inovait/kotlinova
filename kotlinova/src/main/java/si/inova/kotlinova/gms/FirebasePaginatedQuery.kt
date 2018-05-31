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

    override suspend fun reset() {
        lastDocument = null
    }

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