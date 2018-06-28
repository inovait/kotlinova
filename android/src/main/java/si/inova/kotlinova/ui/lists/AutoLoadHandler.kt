package si.inova.kotlinova.ui.lists

import android.support.v7.widget.RecyclerView
import si.inova.kotlinova.ui.lists.sections.ListSection
import si.inova.kotlinova.ui.lists.sections.SectionRecyclerAdapter
import si.inova.kotlinova.utils.isAtBottom

/**
 * Helper class that automatically executes provided callback whenever user reaches the bottom
 * of the recycler view or whenever list loading has finished,
 * with space for more items on the screen
 *
 * If your adapter is [SectionRecyclerAdapter], you must create this object AFTER attaching adapter
 * to the recycler view.
 *
 * @author Matej Drobnic
 */
class AutoLoadHandler(recyclerView: RecyclerView, vararg listSection: ListSection<*, *>) {
    private var callback: (() -> Unit)? = null

    fun setCallback(callback: () -> Unit) {
        this.callback = callback
    }

    private val adapter: SectionRecyclerAdapter? = recyclerView.adapter as? SectionRecyclerAdapter

    init {
        recyclerView.addOnScrollListener(BottomScrollListener { callback?.invoke() })

        for (section in listSection) {
            section.addUpdateListener {
                recyclerView.post {
                    if (recyclerView.isAtBottom(adapter?.realItemCount)) {
                        callback?.invoke()
                    }
                }
            }
        }
    }
}