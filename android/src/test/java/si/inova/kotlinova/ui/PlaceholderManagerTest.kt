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

package si.inova.kotlinova.ui

import android.graphics.drawable.Drawable
import android.view.View
import com.nhaarman.mockitokotlin2.*
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