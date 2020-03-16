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

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
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