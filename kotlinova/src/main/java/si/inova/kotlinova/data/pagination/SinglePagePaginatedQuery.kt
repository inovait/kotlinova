package si.inova.kotlinova.data.pagination

/**
 * [PaginatedQuery] that only provides single page (one list of items)
 *
 * @author Matej Drobnic
 */
class SinglePagePaginatedQuery<out T>(private val data: List<T>) : PaginatedQuery<T> {
    override var isAtEnd: Boolean = false
        private set

    override suspend fun reset() {
        isAtEnd = false
    }

    override suspend fun nextPage(): List<T> {
        if (isAtEnd) {
            return emptyList()
        }

        isAtEnd = true
        return data
    }
}