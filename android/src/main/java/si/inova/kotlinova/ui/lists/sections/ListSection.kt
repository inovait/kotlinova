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
     * Note that this method will trigger asynchronous operation that will perform actual update
     * later in the future.
     */
    fun updateList(newData: List<T>) {
        if (data === newData) {
            Timber.w("Setting same list object twice. No change.")
            return
        }

        asyncListDiffComputer.computeDiffs(this, data, newData) { result ->
            val oldList = data
            data = newData.copy()
            forceUpdateAllItems = false

            val listeners = updateListeners.toList()
            listeners.forEach { it.invoke() }

            onListUpdated(oldList, newData)
            updateCallback?.let {
                result.dispatchUpdatesTo(it)
            }
        }
    }

    /**
     * Clear this section immediatelly.
     *
     * Unlike performing *updateList(emptyList())*, this method clears the list immediatelly without
     * any asynchronous operations that performs actual list update some time in the future.
     */
    fun clearList() {
        val oldList = data
        data = emptyList()
        forceUpdateAllItems = false

        val listeners = updateListeners.toList()
        listeners.forEach { it.invoke() }

        onListUpdated(oldList, emptyList())
        updateCallback?.let {
            if (oldList != null) {
                it.onRemoved(0, oldList.size)
            }
        }
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