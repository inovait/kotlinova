/**
 * @author Matej Drobnic
 */
@file:JvmName("RecyclerUtils")

package si.inova.kotlinova.utils

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun RecyclerView.isAtBottom(numChildrenToCheck: Int? = null): Boolean {
    val layoutManager = layoutManager as LinearLayoutManager
    val totalItemCount = numChildrenToCheck ?: layoutManager.itemCount
    val visibleItemCount = layoutManager.childCount
    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
    return pastVisibleItems + visibleItemCount >= totalItemCount - 1
}