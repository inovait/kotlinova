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