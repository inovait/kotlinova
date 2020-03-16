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

import android.view.ViewGroup
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import si.inova.kotlinova.testing.OpenForTesting

/**
 * @author Matej Drobnic
 */
@OpenForTesting
abstract class RecyclerSection<VH : RecyclerView.ViewHolder> {
    protected var updateCallback: ListUpdateCallback? = null

    /**
     * Create ViewHolder for this section.
     *
     * @see RecyclerView.Adapter.onCreateViewHolder
     */
    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    /**
     * Bind created viewholder to the data.
     *
     * @see RecyclerView.Adapter.onBindViewHolder
     */
    abstract fun onBindViewHolder(holder: VH, position: Int)

    /**
     * @return item count in this section
     */
    abstract val itemCount: Int

    /**
     * @return View type for specific view holder.
     * Must be in range from 0 (inclusive) to 100_000 (exclusive).
     *
     * @see RecyclerView.Adapter.getItemViewType
     */
    fun getItemViewType(position: Int): Int {
        return 0
    }

    fun onAttachedToAdapter(callback: ListUpdateCallback) {
        updateCallback = callback
    }

    fun onDetachedFromAdapter() {
        updateCallback = null
    }

    /**
     * Whether this section only contains placeholder items (items from this adapter will not
     * be counted towards "real item count")
     *
     * @see SectionRecyclerAdapter.realItemCount
     */
    val sectionContainsPlaceholderItems: Boolean
        get() = false

    /**
     * Whether provided item type in this section can blend into placeholders
     * that are positioned after this section.
     * If *true*, placeholder crossfade animation is played instead of insert animation.
     */
    fun canBlendIntoPlaceholder(itemType: Int): Boolean = false
}