package si.inova.kotlinova.data.pagination

/**
 * [PaginatedQuery] that provides page by offset property
 *
 * @author Kristjan Kotnik
 */
class OffsetPaginatedQuery<T>(
    private val queryPerformer: (Int) -> Deferred<List<T>>
) : PaginatedQuery<T> {
    @Volatile
    override var isAtEnd: Boolean = false
        private set

    @Volatile
    private var offset: Int = 0

    override suspend fun nextPage(): List<T> {
        if (isAtEnd) {
            return emptyList()
        }

        val performQuery = queryPerformer(offset).await()
        offset += performQuery.size
        isAtEnd = performQuery.isEmpty()
        return performQuery
    }
}