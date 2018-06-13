package si.inova.kotlinova.ui.lists.sections

import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import si.inova.kotlinova.time.TimeProvider
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

        val elapsedTime = TimeProvider.elapsedRealtime() - lastShowTime
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
        lastShowTime = TimeProvider.elapsedRealtime()
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

    class PlaceholderViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}

private const val MIN_SHOWN_TIME = 500L // ms
private const val MIN_DELAY_UNTIL_SHOW = 250L // ms