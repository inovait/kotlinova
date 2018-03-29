package si.inova.kotlinova.ui.lists

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Listener that will trigger callback when user scrolls to the RecyclerView bottom.
 *
 * @author Matej Drobnic
 */
class BottomScrollListener(private val callback: () -> Unit) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
        if (pastVisibleItems + visibleItemCount >= totalItemCount - 1) {
            //We have reached the end of the list.
            callback()
        }
    }
}