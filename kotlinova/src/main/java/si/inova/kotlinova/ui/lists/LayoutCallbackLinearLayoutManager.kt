package si.inova.kotlinova.ui.lists

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Variant of [LinearLayoutManager] that provides callback whenever view changes
 * (either when view is re-laid or when recycler scrolls)
 *
 * @author Matej Drobnic
 */
class LayoutCallbackLinearLayoutManager(
    context: Context,
    orientation: Int,
    reverseLayout: Boolean
) : LinearLayoutManager(context, orientation, reverseLayout) {

    var callback: (() -> Unit)? = null

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        val scrolled = super.scrollVerticallyBy(dy, recycler, state)
        triggerCallback()
        return scrolled
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
        triggerCallback()
        return scrolled
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        if (childCount != 0) {
            triggerCallback()
        }
    }

    private fun triggerCallback() {
        callback?.invoke()
    }
}