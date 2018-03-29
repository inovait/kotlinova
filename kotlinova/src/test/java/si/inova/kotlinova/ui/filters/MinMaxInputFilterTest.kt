package si.inova.kotlinova.ui.filters

import android.text.Spanned
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * @author Matej Drobnic
 */
class MinMaxInputFilterTest {
    private lateinit var filter: MinMaxInputFilter

    @Before
    fun init() {
        filter = MinMaxInputFilter(1.0, 100.0)
    }

    @Test
    fun testAllow() {
        val before = StringSpanned("9")
        val addition = "9" //Becomes "99"
        val result = filter.filter(addition, 0, 1, before, 1, 2)

        assertNull(result)
    }

    @Test
    fun testTooHigh() {
        val before = StringSpanned("10")
        val addition = "9" //Becomes "109"
        val result = filter.filter(addition, 0, 2, before, 2, 3)

        assertEquals("", result)
    }

    @Test
    fun testTooLow() {
        val before = StringSpanned("")
        val addition = "0" //Becomes "0"
        val result = filter.filter(addition, 0, 0, before, 0, 1)

        assertEquals("", result)
    }

    private class StringSpanned(private val input: String) : CharSequence by input, Spanned {
        override fun <T : Any?> getSpans(start: Int, end: Int, type: Class<T>?): Array<T> {
            throw UnsupportedOperationException()
        }

        override fun nextSpanTransition(start: Int, limit: Int, type: Class<*>?): Int {
            throw UnsupportedOperationException()
        }

        override fun getSpanEnd(tag: Any?): Int {
            throw UnsupportedOperationException()
        }

        override fun getSpanFlags(tag: Any?): Int {
            throw UnsupportedOperationException()
        }

        override fun getSpanStart(tag: Any?): Int {
            throw UnsupportedOperationException()
        }

        override fun toString(): String {
            return input
        }
    }
}