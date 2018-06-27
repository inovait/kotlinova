package si.inova.kotlinova.testing.pagination

import si.inova.kotlinova.data.pagination.PaginatedQuery

/**
 * [PaginatedQuery] that provides complete list of pages
 *
 * @author Matej Drobnic
 */
class PaginatedList<out T>(private val data: List<List<T>>) : PaginatedQuery<T> {
    private var curPage = 0

    override val isAtEnd: Boolean
        get() = curPage >= data.size

    override suspend fun reset() {
        curPage = 0
    }

    override suspend fun nextPage(): List<T> {
        if (isAtEnd) {
            return emptyList()
        }

        return data[curPage++]
    }
}