package si.inova.kotlinova.ui.lists.sections

import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
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

    fun onAttachedToRecycler(callback: ListUpdateCallback) {
        updateCallback = callback
    }

    /**
     * Whether this section only contains placeholder items (items from this adapter will not
     * be counted towards "real item count")
     *
     * @see SectionRecyclerAdapter.realItemCount
     */
    val sectionContainsPlaceholderItems: Boolean
        get() = false
}