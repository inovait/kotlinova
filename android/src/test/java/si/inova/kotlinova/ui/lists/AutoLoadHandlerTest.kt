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

package si.inova.kotlinova.ui.lists

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import si.inova.kotlinova.testing.LocalFunction0
import si.inova.kotlinova.ui.lists.sections.ListSection
import si.inova.kotlinova.ui.lists.sections.SectionRecyclerAdapter

/**
 * @author Matej Drobnic
 */
class AutoLoadHandlerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var recyclerView: RecyclerView

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var layoutManager: LinearLayoutManager

    @Mock
    private lateinit var listSection: ListSection<String, RecyclerView.ViewHolder>

    @Mock
    private lateinit var callback: LocalFunction0<Unit>

    private var listLoadCallback: (() -> Unit)? = null
    private var recyclerScrollCallback: RecyclerView.OnScrollListener? = null

    private lateinit var autoLoadHandler: AutoLoadHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(recyclerView.layoutManager).thenReturn(layoutManager)

        @Suppress("UNCHECKED_CAST")
        doAnswer {
            listLoadCallback = it.arguments[0] as (() -> Unit)?
        }.whenever(listSection).addUpdateListener(any())

        doAnswer {
            recyclerScrollCallback = it.arguments[0] as RecyclerView.OnScrollListener?
        }.whenever(recyclerView).addOnScrollListener(any())

        whenever(recyclerView.post(any())).thenAnswer {
            (it.arguments[0] as Runnable).run()
            true
        }

        autoLoadHandler = AutoLoadHandler(recyclerView, listSection)
        autoLoadHandler.setCallback(callback)
    }

    @Test
    fun attaching() {
        verify(listSection).addUpdateListener(any())
        verify(recyclerView).addOnScrollListener(any())

        assertNotNull(listLoadCallback)
        assertNotNull(recyclerScrollCallback)
    }

    @Test
    fun scrollAtBottom() {
        moveToBottom()
        recyclerScrollCallback!!.onScrolled(recyclerView, 0, 100)

        verify(callback).invoke()
    }

    @Test
    fun scrollNotAtBottom() {
        moveToTop()
        recyclerScrollCallback!!.onScrolled(recyclerView, 0, 100)

        verifyZeroInteractions(callback)
    }

    @Test
    fun listUpdateAtBottom() {
        moveToBottom()
        listLoadCallback!!.invoke()

        verify(callback).invoke()
    }

    @Test
    fun listLoadNotAtBottom() {
        moveToTop()
        listLoadCallback!!.invoke()

        verifyZeroInteractions(callback)
    }

    @Test
    fun ignoreSectionPlaceholders() {
        val adapter: SectionRecyclerAdapter = mock()
        whenever(recyclerView.adapter).thenReturn(adapter)
        whenever(adapter.realItemCount).thenReturn(2)

        autoLoadHandler = AutoLoadHandler(recyclerView, listSection)
        autoLoadHandler.setCallback(callback)

        moveToIndex(2)
        listLoadCallback!!.invoke()

        verify(callback).invoke()
    }

    @Test
    fun notAtBottomWithSectionAdapter() {
        val adapter: SectionRecyclerAdapter = mock()
        whenever(recyclerView.adapter).thenReturn(adapter)
        whenever(adapter.realItemCount).thenReturn(10)

        autoLoadHandler = AutoLoadHandler(recyclerView, listSection)
        autoLoadHandler.setCallback(callback)

        moveToIndex(2)
        listLoadCallback!!.invoke()

        verifyZeroInteractions(callback)
    }

    private fun moveToBottom() {
        whenever(layoutManager.itemCount).thenReturn(10)
        whenever(layoutManager.childCount).thenReturn(5)
        whenever(layoutManager.findFirstVisibleItemPosition()).thenReturn(5)
    }

    private fun moveToTop() {
        whenever(layoutManager.itemCount).thenReturn(10)
        whenever(layoutManager.childCount).thenReturn(5)
        whenever(layoutManager.findFirstVisibleItemPosition()).thenReturn(0)
    }

    private fun moveToIndex(index: Int) {
        whenever(layoutManager.itemCount).thenReturn(10)
        whenever(layoutManager.childCount).thenReturn(0)
        whenever(layoutManager.findFirstVisibleItemPosition()).thenReturn(index)
    }
}