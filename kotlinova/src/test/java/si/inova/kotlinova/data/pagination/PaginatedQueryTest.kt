package si.inova.kotlinova.data.pagination

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import si.inova.kotlinova.testing.pagination.PaginatedList

/**
 * @author Matej Drobnic
 */
class PaginatedQueryConvertersTest {
    val data = listOf(
            listOf(1, 2),
            listOf(3, 4),
            listOf(5)
    )

    @Test
    fun testMap() = runBlocking<Unit> {
        val query = PaginatedList(data).map { it * 2 }

        var page = query.nextPage()
        assertFalse(query.isAtEnd)
        assertEquals(listOf(2, 4), page)

        page = query.nextPage()
        assertFalse(query.isAtEnd)
        assertEquals(listOf(6, 8), page)

        page = query.nextPage()
        assertTrue(query.isAtEnd)
        assertEquals(listOf(10), page)

        page = query.nextPage()
        assertTrue(query.isAtEnd)
        assertEquals(emptyList<Int>(), page)
    }
}