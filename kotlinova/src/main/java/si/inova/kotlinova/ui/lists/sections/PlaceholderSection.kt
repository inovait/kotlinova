package si.inova.kotlinova.ui.lists.sections

import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.postDelayed

/**
 * Section that displays very long list of scrolling placeholder views
 *
 * @author Matej Drobnic
 */
class PlaceholderSection(
    @LayoutRes private val placeholderResource: Int,
    private val placeholderCount : Int = 10
) : RecyclerSection<PlaceholderSection.PlaceholderViewHolder>() {
    private val handler = Handler()

    private var actuallyDisplayed: Boolean = true

    final var displayed: Boolean = true
        set(value) {
            if (value == field) {
                return
            }

            handler.removeCallbacksAndMessages(null)

            field = value

            if (value && actuallyDisplayed) {
                return
            }

            if (value) {
                actuallyDisplayed = true
                updateCallback?.onInserted(0, placeholderCount)
            } else {
                hideDelayed()
            }
        }

    private fun hideDelayed() {
        handler.postDelayed(HIDE_DELAY) {
            actuallyDisplayed = false
            updateCallback?.onRemoved(0, placeholderCount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceholderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(placeholderResource, parent, false)
        return PlaceholderViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceholderViewHolder, position: Int) = Unit

    override val itemCount: Int
        get() {
            return if (actuallyDisplayed) {
                placeholderCount
            } else {
                0
            }
        }

    override val sectionContainsPlaceholderItems: Boolean
        get() = true

    class PlaceholderViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}

private const val HIDE_DELAY = 500L
