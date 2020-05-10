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

import androidx.recyclerview.widget.RecyclerView
import si.inova.kotlinova.ui.lists.AsyncListDiffComputer
import si.inova.kotlinova.ui.lists.ListDiffProvider
import si.inova.kotlinova.utils.copy
import timber.log.Timber

/**
 * RecyclerView Section that displays items from the list, with automatic diffing on the background thread.
 *
 * @author Matej Drobnic
 */
abstract class ListSection<T, VH : RecyclerView.ViewHolder> : RecyclerSection<VH>(),
    ListDiffProvider<T> {
    final var data: List<T>? = null
        protected set

    /**
     * When set to *true*, all items in the list will be refreshed on the next change
     * (Use for example when external settings change that affects items happens)
     */
    @Volatile
    var forceUpdateAllItems = true

    private val updateListeners = ArrayList<() -> Unit>()

    private val asyncListDiffComputer = AsyncListDiffComputer()

    /**
     * Update list of items that this section displays.
     *
     * Note that this method may only trigger asynchronous operation that will perform actual update
     * later in the future.
     */
    fun updateList(newData: List<T>) {
        if (data === newData) {
            Timber.w("Setting same list object twice. No change.")
            return
        }

        if (data.isNullOrEmpty()) {
            data = newData.copy()
            forceUpdateAllItems = false

            invokeUpdateListeners(emptyList(), newData)
            updateCallback?.onInserted(0, newData.size)

            return
        }

        if (newData.isEmpty()) {
            val oldList = data
            data = newData.copy()
            forceUpdateAllItems = false

            invokeUpdateListeners(oldList, newData)
            if (oldList?.isNotEmpty() == true) {
                updateCallback?.onRemoved(0, oldList.size)
            }

            return
        }

        asyncListDiffComputer.computeDiffs(this, data, newData) { result ->
            val oldList = data
            data = newData.copy()
            forceUpdateAllItems = false

            invokeUpdateListeners(oldList, newData)

            onListUpdated(oldList, newData)
            updateCallback?.let {
                result.dispatchUpdatesTo(it)
            }
        }
    }

    private fun invokeUpdateListeners(oldList: List<T>?, newList: List<T>) {
        val listeners = updateListeners.toList()
        listeners.forEach { it.invoke() }

        onListUpdated(oldList, newList)
    }

    /**
     * Clear this section immediately.
     */
    @Deprecated(
        "updateList also has synchronous behavior now when inserting empty list",
        replaceWith = ReplaceWith("updateList(emptyList())")
    )
    fun clearList() {
        updateList(emptyList())
    }

    protected fun onListUpdated(oldList: List<T>?, newList: List<T>) {
    }

    override val itemCount: Int
        get() = data?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data?.elementAtOrNull(position) ?: return
        onBindViewHolder(holder, position, item)
    }

    /**
     * Add callback that gets called after [RecyclerView] received updated data.
     */
    fun addUpdateListener(listener: () -> Unit) {
        updateListeners.add(listener)
    }

    /**
     * Remove added update listener.
     *
     * @see addUpdateListener
     */
    fun removeUpdateListener(listener: () -> Unit) {
        updateListeners.remove(listener)
    }

    abstract fun onBindViewHolder(holder: VH, position: Int, item: T)

    override fun areContentsSame(first: T, second: T): Boolean {
        return if (forceUpdateAllItems) {
            false
        } else {
            areContentsSameEx(first, second)
        }
    }

    abstract fun areContentsSameEx(first: T, second: T): Boolean
}