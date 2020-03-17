/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.ui.lists.sections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
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
    @LayoutRes private val layout: Int = 0
) : RecyclerSection<SingleViewSection.SingleViewHolder>() {

    private val viewUpdateCallbacks = LinkedList<(View) -> Unit>()

    private var singletonHolder: SingleViewHolder? = null

    /**
     * View of this section. This is only provided for read-only operations.
     *
     * DO NOT MAKE ANY CHANGES TO THIS INSTANCE. USE [updateView()][updateView] INSTEAD.
     */
    val view: View?
        get() = singletonHolder?.itemView

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

        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
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