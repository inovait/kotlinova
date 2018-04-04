/**
 * @author Matej Drobnic
 */
@file:JvmName("RecyclerUtils")

package si.inova.kotlinova.utils

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun RecyclerView.isAtBottom(): Boolean {
    val layoutManager = layoutManager as LinearLayoutManager
    val visibleItemCount = layoutManager.childCount
    val totalItemCount = layoutManager.itemCount
    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
    return pastVisibleItems + visibleItemCount >= totalItemCount - 1
}