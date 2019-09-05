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