package si.inova.kotlinova.ui.lists

import androidx.recyclerview.widget.RecyclerView
import si.inova.kotlinova.utils.isAtBottom

/**
 * Listener that will trigger callback when user scrolls to the RecyclerView bottom.
 *
 * @author Matej Drobnic
 */
class BottomScrollListener(private val callback: () -> Unit) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (recyclerView.isAtBottom()) {
            callback()
        }
    }
}