package si.inova.kotlinova.ui.lists.sections

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Section that displays very long list of scrolling placeholder views
 *
 * @author Matej Drobnic
 */
class PlaceholderSection(
    @LayoutRes private val placeholderResource: Int
) : RecyclerSection<PlaceholderSection.PlaceholderViewHolder>() {
    final var displayed: Boolean = true
        set(value) {
            if (value == field) {
                return
            }

            field = value

            if (value) {
                updateCallback?.onInserted(0, Short.MAX_VALUE.toInt())
            } else {
                updateCallback?.onRemoved(0, Short.MAX_VALUE.toInt())
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceholderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(placeholderResource, parent, false)
        return PlaceholderViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceholderViewHolder, position: Int) = Unit

    override val itemCount: Int
        get() {
            return if (displayed) {
                Short.MAX_VALUE.toInt()
            } else {
                0
            }
        }

    class PlaceholderViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}