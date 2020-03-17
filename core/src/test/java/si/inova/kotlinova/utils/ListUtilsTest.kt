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

package si.inova.kotlinova.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author Matej Drobnic
 */
class ListUtilsTest {
    @Test
    fun meanBy() {
        assertNull(emptyList<Int>().meanBy { it })

        assertEquals(4, listOf(
            Holder(3),
            Holder(5)
        ).meanBy { it.number })
        assertEquals(3, listOf(1, 2, 3, 4, 5, 6).meanBy { it })
    }

    @Test
    fun isNotNullOrEmpty() {
        assertFalse((null as List<String>?).isNotNullOrEmpty())
        assertFalse(emptyList<String>().isNotNullOrEmpty())
        assertTrue(listOf("A", "B", "C").isNotNullOrEmpty())
        assertTrue(listOf("A").isNotNullOrEmpty())
        assertTrue(listOf("A").isNotNullOrEmpty())
    }

    private class Holder(val number: Int)
}