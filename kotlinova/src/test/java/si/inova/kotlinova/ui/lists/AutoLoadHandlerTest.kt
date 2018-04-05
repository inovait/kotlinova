package si.inova.kotlinova.ui.lists

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
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