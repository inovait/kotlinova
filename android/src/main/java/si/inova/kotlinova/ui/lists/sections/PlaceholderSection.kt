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

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import si.inova.kotlinova.time.AndroidTimeProvider
import kotlin.math.max

/**
 * Section that displays very long list of scrolling placeholder views
 *
 * @author Matej Drobnic
 */
class PlaceholderSection(
    @LayoutRes private val placeholderResource: Int,
    private val placeholderCount: Int = 10
) : RecyclerSection<PlaceholderSection.PlaceholderViewHolder>() {
    private val handler = Handler()

    private var actuallyDisplayed: Boolean = false
    private var lastShowTime = 0L

    final var displayed: Boolean = false
        set(value) {
            if (value == field) {
                return
            }

            if (value) {
                show()
            } else {
                hide()
            }

            field = value
        }

    fun show() {
        removeCallbacks()
        if (actuallyDisplayed) {
            return
        }

        handler.postDelayed(showRunnable, MIN_DELAY_UNTIL_SHOW)
    }

    fun hide() {
        removeCallbacks()
        if (!actuallyDisplayed) {
            return
        }

        val elapsedTime = AndroidTimeProvider.elapsedRealtime() - lastShowTime
        val timeLeft = max(0, MIN_SHOWN_TIME - elapsedTime)
        handler.postDelayed(hideRunnable, timeLeft)
    }

    private fun removeCallbacks() {
        handler.removeCallbacks(hideRunnable)
        handler.removeCallbacks(showRunnable)
    }

    private val hideRunnable = Runnable {
        actuallyDisplayed = false
        updateCallback?.onRemoved(0, placeholderCount)
    }

    private val showRunnable = Runnable {
        actuallyDisplayed = true
        lastShowTime = AndroidTimeProvider.elapsedRealtime()
        updateCallback?.onInserted(0, placeholderCount)
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

    class PlaceholderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private const val MIN_SHOWN_TIME = 500L // ms
private const val MIN_DELAY_UNTIL_SHOW = 250L // ms