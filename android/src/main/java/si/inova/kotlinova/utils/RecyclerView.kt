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

/**
 * @author Matej Drobnic
 */
@file:JvmName("RecyclerUtils")

package si.inova.kotlinova.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import si.inova.kotlinova.ui.lists.sections.ListSection

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

/**
 * This method gets triggered after [data][ListSection.data] is updated.
 */
inline fun <T, VH : RecyclerView.ViewHolder> ListSection<T, VH>.doOnUpdate(
    crossinline action: () -> Unit
) {
    lateinit var actionListener: () -> Unit
    actionListener = {
        action()
        removeUpdateListener(actionListener)
    }

    addUpdateListener(actionListener)
}