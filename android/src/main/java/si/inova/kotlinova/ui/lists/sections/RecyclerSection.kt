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