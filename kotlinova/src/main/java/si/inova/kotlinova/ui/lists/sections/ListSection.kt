package si.inova.kotlinova.ui.lists.sections

import android.support.v7.widget.RecyclerView
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
        private set

    /**
     * When set to *true*, all items in the list will be refreshed on the next change
     * (Use for example when external settings change that affects items happens)
     */
    @Volatile
    var forceUpdateAllItems = true

    /**
     * Callback that gets called after [RecyclerView] received updated data.
     */
    var listUpdateListener: (() -> Unit)? = null

    private val asyncListDiffComputer = AsyncListDiffComputer()

    fun updateList(newData: List<T>) {
        if (data === newData) {
            Timber.w("Setting same list object twice. No change.")
            return
        }

        asyncListDiffComputer.computeDiffs(this, data, newData) { result ->
            data = newData.copy()
            updateCallback?.let {
                result.dispatchUpdatesTo(it)
            }
            forceUpdateAllItems = false
            listUpdateListener?.invoke()
        }
    }

    override val itemCount: Int
        get() = data?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data?.elementAtOrNull(position) ?: return
        onBindViewHolder(holder, position, item)
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