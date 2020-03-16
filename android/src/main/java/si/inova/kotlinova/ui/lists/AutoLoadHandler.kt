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

import androidx.recyclerview.widget.RecyclerView
import si.inova.kotlinova.ui.lists.sections.ListSection
import si.inova.kotlinova.ui.lists.sections.SectionRecyclerAdapter
import si.inova.kotlinova.utils.isAtBottom

/**
 * Helper class that automatically executes provided callback whenever user reaches the bottom
 * of the recycler view or whenever list loading has finished,
 * with space for more items on the screen
 *
 * If your adapter is [SectionRecyclerAdapter], you must create this object AFTER attaching adapter
 * to the recycler view.
 *
 * @author Matej Drobnic
 */
class AutoLoadHandler(recyclerView: RecyclerView, vararg listSection: ListSection<*, *>) {
    private var callback: (() -> Unit)? = null

    fun setCallback(callback: () -> Unit) {
        this.callback = callback
    }

    private val adapter: SectionRecyclerAdapter? = recyclerView.adapter as? SectionRecyclerAdapter

    init {
        recyclerView.addOnScrollListener(BottomScrollListener { callback?.invoke() })

        for (section in listSection) {
            section.addUpdateListener {
                recyclerView.post {
                    if (recyclerView.isAtBottom(adapter?.realItemCount)) {
                        callback?.invoke()
                    }
                }
            }
        }
    }
}