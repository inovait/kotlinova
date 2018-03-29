package si.inova.kotlinova.ui.lists

import android.content.Context
import android.support.annotation.CallSuper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import si.inova.kotlinova.R

private const val VIEW_TYPE_LOADING = 888

/**
 * Adadpter that can display loading bar at the bottom.
 *
 * View type *888* is reserved for the loading view. Do not use that one for your own views.
 *
 * @author Matej Drobnic
 */
abstract class LoadingRecyclerAdapter<VH : RecyclerView.ViewHolder>(private val context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    open var displayLoading = false
        set(value) {
            if (field == value) {
                return
            }

            field = value

            if (value) {
                notifyItemInserted(getItemCountEx())
            } else {
                notifyItemRemoved(getItemCountEx())
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_LOADING) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        } else {
            onCreateViewHolderEx(parent, viewType)
        }
    }

    abstract fun onCreateViewHolderEx(parent: ViewGroup, viewType: Int): VH

    override fun getItemCount(): Int {
        return if (displayLoading) {
            getItemCountEx() + 1
        } else {
            getItemCountEx()
        }
    }

    abstract fun getItemCountEx(): Int

    @CallSuper
    override fun getItemViewType(position: Int): Int {
        if (position == getItemCountEx()) {
            return VIEW_TYPE_LOADING
        }

        return super.getItemViewType(position)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is LoadingViewHolder) {
            onBindViewHolderEx(holder as VH, position)
        }
    }

    abstract fun onBindViewHolderEx(holder: VH, position: Int)

    private class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
