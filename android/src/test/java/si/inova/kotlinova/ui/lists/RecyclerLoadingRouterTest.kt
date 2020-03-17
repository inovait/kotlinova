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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import si.inova.kotlinova.ui.lists.sections.SingleViewSection

/**
 * @author Matej Drobnic
 */
class RecyclerLoadingRouterTest {
    @Mock
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @Mock
    lateinit var loadingRecyclerAdapter: SingleViewSection

    var refreshCallback: SwipeRefreshLayout.OnRefreshListener? = null

    lateinit var recyclerLoadingRouter: RecyclerLoadingRouter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(swipeRefreshLayout.setOnRefreshListener(any())).then {
            refreshCallback = it.arguments[0] as SwipeRefreshLayout.OnRefreshListener?
            Unit
        }

        recyclerLoadingRouter = RecyclerLoadingRouter(swipeRefreshLayout, loadingRecyclerAdapter)
    }

    @Test
    fun initialState() {
        verify(swipeRefreshLayout, only()).setOnRefreshListener(any())
        verifyZeroInteractions(loadingRecyclerAdapter)
        assertNotNull(refreshCallback)
    }

    @Test
    fun firstLoading() {
        recyclerLoadingRouter.updateLoading(true)

        verify(swipeRefreshLayout).isRefreshing = true
        verify(loadingRecyclerAdapter).displayed = false
    }

    @Test
    fun forceSwipeRefreshLoading() {
        recyclerLoadingRouter.notifyBottomReached()
        recyclerLoadingRouter.resetToSwipeRefreshLoading()

        recyclerLoadingRouter.updateLoading(true)

        verify(swipeRefreshLayout).isRefreshing = true
        verify(loadingRecyclerAdapter).displayed = false
    }

    @Test
    fun reachBottomLoadingFirstTime() {
        recyclerLoadingRouter.notifyBottomReached()
        recyclerLoadingRouter.updateLoading(true)

        verify(swipeRefreshLayout).isRefreshing = true
        verify(loadingRecyclerAdapter).displayed = false
    }

    @Test
    fun reachBottomLoadingAfterFirstTime() {
        recyclerLoadingRouter.updateLoading(true)
        recyclerLoadingRouter.updateLoading(false)

        reset(swipeRefreshLayout, loadingRecyclerAdapter)

        recyclerLoadingRouter.notifyBottomReached()
        recyclerLoadingRouter.updateLoading(true)

        verify(swipeRefreshLayout).isRefreshing = false
        verify(loadingRecyclerAdapter).displayed = true
    }

    @Test
    fun cancelLoading() {
        recyclerLoadingRouter.updateLoading(false)

        verify(swipeRefreshLayout).isRefreshing = false
        verify(loadingRecyclerAdapter).displayed = false
    }

    @Test
    fun swipeRefreshLoading() {
        refreshCallback!!.onRefresh()
        recyclerLoadingRouter.updateLoading(true)

        verify(swipeRefreshLayout).isRefreshing = true
        verify(loadingRecyclerAdapter).displayed = false
    }

    @Test
    fun refreshCallbackPassthrough() {
        val targetCallback: SwipeRefreshLayout.OnRefreshListener = mock()
        recyclerLoadingRouter.refreshListener = targetCallback

        verifyZeroInteractions(targetCallback)
        refreshCallback!!.onRefresh()
        verify(targetCallback).onRefresh()
    }

    @Test
    fun refreshCallbackNotForwardingLoading() {
        val targetCallback: SwipeRefreshLayout.OnRefreshListener = mock()
        recyclerLoadingRouter.refreshListener = refreshCallback
        recyclerLoadingRouter.updateLoading(true)

        verifyZeroInteractions(targetCallback)
    }
}