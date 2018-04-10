package si.inova.kotlinova.ui.lists.sections

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.LinkedList

/**
 * RecyclerView section that displays single view.
 *
 * View is created by either specifying layout parameter in the constructor or by overriding
 * [createView()][createView] method
 *
 * To update any data in the view after its creation,
 * use [updateView{}][updateView] method
 *
 * You can also remove / re-add view using [displayed] property.
 *
 * @author Matej Drobnic
 */
class SingleViewSection(
    protected val context: Context,
    @LayoutRes private val layout: Int = 0
) : RecyclerSection<SingleViewSection.SingleViewHolder>() {

    private val viewUpdateCallbacks = LinkedList<(View) -> Unit>()

    private var singletonHolder: SingleViewHolder? = null

    final var displayed: Boolean = true
        set(value) {
            if (value == field) {
                return
            }

            field = value

            if (value) {
                updateCallback?.onInserted(0, 1)
            } else {
                singletonHolder = null
                updateCallback?.onRemoved(0, 1)
            }
        }

    protected fun createView(parent: ViewGroup): View {
        if (layout == 0) {
            throw IllegalArgumentException(
                    "If layout is not provided in constructor, " +
                            "you must override createView() function"
            )
        }

        return LayoutInflater.from(context).inflate(layout, parent, false)
    }

    fun updateView(callback: (View) -> Unit) {
        viewUpdateCallbacks.addLast(callback)

        if (displayed) {
            updateCallback?.onChanged(0, 1, null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        return if (singletonHolder == null) {
            SingleViewHolder(createView(parent)).also { singletonHolder = it }
        } else {
            singletonHolder!!
        }
    }

    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        viewUpdateCallbacks.forEach { it.invoke(holder.itemView) }
        viewUpdateCallbacks.clear()
    }

    override val itemCount: Int
        get() {
            return if (displayed) {
                1
            } else {
                0
            }
        }

    class SingleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}