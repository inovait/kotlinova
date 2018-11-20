/**
 * @author Matej Drobnic
 */
@file:JvmName("RecyclerUtils")

package si.inova.kotlinova.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * This method only works with [LinearLayoutManager].
 *
 * @return whether RecyclerView's current scroll position is at the bottom
 */
fun RecyclerView.isAtBottom(numChildrenToCheck: Int? = null): Boolean {
    val layoutManager = layoutManager as LinearLayoutManager
    val totalItemCount = numChildrenToCheck ?: layoutManager.itemCount
    val visibleItemCount = layoutManager.childCount
    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
    return pastVisibleItems + visibleItemCount >= totalItemCount - 1
}