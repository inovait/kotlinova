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
class FirebasePaginatedQuery<out T>(
    private val baseQuery: Query,
    private val targetClass: Class<T>,
    private val itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
) : PaginatedQuery<Pair<String, T>> {

    private var lastDocument: DocumentSnapshot? = null

    override var isAtEnd = false
        private set

    override suspend fun nextPage(): List<Pair<String, T>> {
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
        return result.map { Pair(it.id, it.toObject(targetClass)) }
    }
}

/**
 * Convenience operator that converts regular [Query] into [PaginatedQuery]
 */
inline fun <reified T> Query.paginate(
    itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
): PaginatedQuery<Pair<String, T>> {
    return FirebasePaginatedQuery(this, T::class.java, itemsPerPage)
}