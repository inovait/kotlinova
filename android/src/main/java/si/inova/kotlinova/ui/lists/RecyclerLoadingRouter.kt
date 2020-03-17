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
import si.inova.kotlinova.ui.lists.sections.SingleViewSection

/**
 * Class that handles routing of loading calls to either [SwipeRefreshLayout] or to
 * optional [SingleViewSection] that contains loading spinner:
 *
 * SwiperefreshLayout's loading is used:
 * * When view is first opened
 * * When behavior is forced using with [resetToSwipeRefreshLoading()][resetToSwipeRefreshLoading] call
 * * When user reloads uses swipe-to-refresh
 *
 * SingleViewSection's loading is used:
 * * When user gets to the bottom and new page loads (you neded to manually call  [notifyBottomReached()][notifyBottomReached])
 *
 * Note that when using this class, you should not call [SwipeRefreshLayout.setOnRefreshListener()][SwipeRefreshLayout.setOnRefreshListener].
 * Instead use [refreshListener] provided inside this class.
 *
 * To display loading, use [updateLoading()][updateLoading] method.
 * This method must be called AFTER all above methods.
 *
 * @author Matej Drobnic
 */

class RecyclerLoadingRouter(
    private val swipeRefreshLayout: SwipeRefreshLayout,
    private val bottomLoadingDisplay: SingleViewSection? = null
) {
    var refreshListener: SwipeRefreshLayout.OnRefreshListener? = null
    private var nextLoadingStyle = LoadingStyle.SWIPE_REFRESH

    private var firstLoading = true

    init {
        swipeRefreshLayout.setOnRefreshListener(this::onSwipeToRefresh)
    }

    fun updateLoading(loading: Boolean) {
        if (!loading) {
            swipeRefreshLayout.isRefreshing = false
            bottomLoadingDisplay?.displayed = false
            firstLoading = false
            return
        }

        when (nextLoadingStyle) {
            LoadingStyle.SWIPE_REFRESH -> {
                swipeRefreshLayout.isRefreshing = true
                bottomLoadingDisplay?.displayed = false
            }
            LoadingStyle.RECYCLER_BOTTOM -> {
                swipeRefreshLayout.isRefreshing = false
                bottomLoadingDisplay?.displayed = true
            }
        }
    }

    fun notifyBottomReached() {
        if (firstLoading) {
            // User will always reach the bottom at first since page is empty. Ignore this request
            // for first-time loading

            return
        }

        nextLoadingStyle = LoadingStyle.RECYCLER_BOTTOM
    }

    fun resetToSwipeRefreshLoading() {
        nextLoadingStyle = LoadingStyle.SWIPE_REFRESH
    }

    private fun onSwipeToRefresh() {
        resetToSwipeRefreshLoading()
        refreshListener?.onRefresh()
    }

    enum class LoadingStyle {
        SWIPE_REFRESH,
        RECYCLER_BOTTOM
    }
}