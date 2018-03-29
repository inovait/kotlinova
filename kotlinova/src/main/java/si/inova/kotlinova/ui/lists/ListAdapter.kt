package si.inova.kotlinova.ui.lists

import android.content.Context
import android.support.v7.widget.RecyclerView
import si.inova.kotlinova.testing.OpenForTesting
import si.inova.kotlinova.utils.copy
import timber.log.Timber

/**
 * RecyclerView Adapter that displays items from the list, with automatic diffing on the background thread.
 *
 * @author Matej Drobnic
 */
abstract class ListAdapter<T, VH : RecyclerView.ViewHolder>(context: Context) :
        LoadingRecyclerAdapter<VH>(context), ListDiffProvider<T> {
    var data: List<T>? = null
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
    @OpenForTesting
    var listUpdateListener: (() -> Unit)? = null

    private val asyncListDiffComputer = AsyncListDiffComputer()

    open fun updateList(newData: List<T>) {
        if (data === newData) {
            Timber.w("Setting same list object twice. No change.")
            return
        }

        asyncListDiffComputer.computeDiffs(this, data, newData) {
            data = newData.copy()
            it.dispatchUpdatesTo(this)
            forceUpdateAllItems = false
            listUpdateListener?.invoke()
        }
    }

    override fun getItemCountEx(): Int {
        return data?.size ?: 0
    }

    override fun areContentsSame(first: T, second: T): Boolean {
        return if (forceUpdateAllItems) {
            false
        } else {
            areContentsSameEx(first, second)
        }
    }

    abstract fun areContentsSameEx(first: T, second: T): Boolean
}