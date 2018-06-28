package si.inova.kotlinova.ui

import android.graphics.drawable.Drawable
import android.view.View
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * @author Matej Drobnic
 */
class PlaceholderManagerTest {
    @Mock
    private lateinit var placeholderDrawable: Drawable

    private lateinit var placeholderManager: PlaceholderManager

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        placeholderManager = PlaceholderManager { placeholderDrawable }
    }

    @Test
    fun simpleApplyClear() {
        val view: View = mock()

        inOrder(view) {
            placeholderManager.applyPlaceholder(view)
            verify(view).background = placeholderDrawable

            placeholderManager.clearPlaceholder(view, animate = false)
            verify(view).background = null
        }
    }

    @Test
    fun returnToPreviousDrawable() {
        val oldBackground: Drawable = mock()
        val view: View = mock {
            whenever(it.background).thenReturn(oldBackground)
        }

        inOrder(view) {
            placeholderManager.applyPlaceholder(view)
            verify(view).background = placeholderDrawable

            placeholderManager.clearPlaceholder(view, animate = false)
            verify(view).background = oldBackground
        }
    }

    @Test
    fun onlyClearViewsThatHaveBeenApplied() {
        val view: View = mock()

        placeholderManager.clearPlaceholder(view, animate = false)
        verifyZeroInteractions(view)
    }

    @Test
    fun clearUnknownViewsWithForce() {
        val view: View = mock()

        placeholderManager.clearPlaceholder(view, animate = false, force = true)
        verify(view).background = null
    }

    @Test
    fun applyMultiple() {
        val viewA: View = mock()
        val viewB: View = mock()

        placeholderManager.applyPlaceholder(viewA, viewB)
        verify(viewA).background = placeholderDrawable
        verify(viewB).background = placeholderDrawable
    }

    @Test
    fun clearMultiple() {
        val viewA: View = mock()
        val viewB: View = mock()

        placeholderManager.clearPlaceholder(viewA, viewB, force = true)
        verify(viewA).background = null
        verify(viewB).background = null
    }

    @Test
    fun applyCustom() {
        val customDrawable: Drawable = mock()

        val view: View = mock()

        placeholderManager.applyCustomPlaceholder(view, customDrawable)
        verify(view).background = customDrawable
    }

    @Test
    fun ignoreSubsequentClears() {
        val view: View = mock()

        inOrder(view) {
            placeholderManager.applyPlaceholder(view)
            verify(view).background = placeholderDrawable

            placeholderManager.clearPlaceholder(view, animate = false)
            verify(view).background = null

            placeholderManager.clearPlaceholder(view, animate = false)
            placeholderManager.clearPlaceholder(view, animate = false)
            placeholderManager.clearPlaceholder(view, animate = false)
            placeholderManager.clearPlaceholder(view, animate = false)
            placeholderManager.clearPlaceholder(view, animate = false)
            placeholderManager.clearPlaceholder(view, animate = false)
            placeholderManager.clearPlaceholder(view, animate = false)

            verifyNoMoreInteractions()
        }
    }
}