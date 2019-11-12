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