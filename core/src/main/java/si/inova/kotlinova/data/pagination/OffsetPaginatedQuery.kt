package si.inova.kotlinova.data.pagination

/**
 * [PaginatedQuery] that provides page by offset property
 */
class OffsetPaginatedQuery<T>(
    private val queryPerformer: suspend (Int) -> List<T>
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

        val performQuery = queryPerformer(offset)
        offset += performQuery.size
        isAtEnd = performQuery.isEmpty()
        return performQuery
    }
}