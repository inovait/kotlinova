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