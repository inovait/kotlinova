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

package si.inova.kotlinova.ui.lists.sections

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool

/**
 * Horizontal Section that displays horizontal list inside [RecyclerView].
 *
 * Create shared [RecycledViewPool()][RecycledViewPool] and use it in both [RecyclerView]'s.
 */
class HorizontalRecyclerListSection(
        private val adapter: Adapter<*>,
        private val recyclerViewPool: RecycledViewPool,
        private val onRecyclerCreated: ((RecyclerView) -> Unit)? = null
) : SingleViewSection() {
    constructor(
            section: RecyclerSection<*>,
            recyclerViewPool: RecycledViewPool,
            onRecyclerCreated: ((RecyclerView) -> Unit)? = null
    ) : this(
            SectionRecyclerAdapter().apply { attachSection(section) },
            recyclerViewPool,
            onRecyclerCreated
    )

    override fun createView(parent: ViewGroup): View {
        return RecyclerView(parent.context).apply {
            adapter = this@HorizontalRecyclerListSection.adapter
            setRecycledViewPool(recyclerViewPool)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            onRecyclerCreated?.invoke(this)
        }
    }
}