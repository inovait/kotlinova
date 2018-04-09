package si.inova.kotlinova.ui.views

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.core.view.isVisible
import si.inova.kotlinova.R
import si.inova.kotlinova.time.TimeProvider
import kotlin.math.max

/**
 * Progress bar that fades all views and disables all touch behind it.
 *
 * It also prevents blinking (it will only be displayed after 500ms of loading and then it will be
 * displayed for at least another 500ms)
 *
 * @author Bojan Kseneman, adapted by Matej Drobnic
 */

class FullScreenProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
        TouchTakeOverFrameLayout(context, attrs, defStyleAttr) {

    private var lastShowTime = 0L
    private var shown = false
    private var activeAnimation: ViewPropertyAnimator? = null

    var isLoadingShown: Boolean
        get() = isVisible
        set(value) {
            if (value) {
                show()
            } else {
                hide()
            }
        }

    fun show() {
        removeCallbacks()
        if (shown) {
            return
        }

        // If there is already active transition, override it with show transition immediately
        if (activeAnimation != null) {
            showRunnable.run()
        } else {
            postDelayed(showRunnable, MIN_DELAY_UNTIL_SHOW)
        }
    }

    fun hide() {
        removeCallbacks()
        if (!shown) {
            return
        }

        val elapsedTime = TimeProvider.elapsedRealtime() - lastShowTime
        val timeLeft = max(0, MIN_SHOWN_TIME - elapsedTime)
        postDelayed(hideRunnable, timeLeft)
    }

    private val showRunnable = Runnable {
        lastShowTime = TimeProvider.elapsedRealtime()

        shown = true

        activeAnimation?.cancel()
        animate()
                .alpha(1f)
                .withStartAction { isVisible = true }
                .withEndAction { activeAnimation = null }
                .setDuration(FADE_ANIMATION_DURATION)
                .also { activeAnimation = it }
                .start()
    }

    private val hideRunnable = Runnable {
        shown = false

        activeAnimation?.cancel()
        animate()
                .alpha(0f)
                .withEndAction {
                    isVisible = false
                    activeAnimation = null
                }
                .setDuration(FADE_ANIMATION_DURATION)
                .also { activeAnimation = it }
                .start()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks()
    }

    private fun removeCallbacks() {
        removeCallbacks(hideRunnable)
        removeCallbacks(showRunnable)
    }

    init {
        val args = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FullScreenProgressBar,
                0, 0
        )

        val visibleAtStart = try {
            args.getBoolean(R.styleable.FullScreenProgressBar_visibleAtStart, false)
        } finally {
            args.recycle()
        }

        val root = View.inflate(context, R.layout.view_full_screen_progress, this)
        isVisible = false

        if (visibleAtStart) {
            show()
        }
        root.setBackgroundColor(ContextCompat.getColor(context, R.color.fullScreenProgressDimColor))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android will display views with higher elevation on top of other views.
            // Lets set maximum elevation so we cover everything
            elevation = Float.MAX_VALUE
        }
    }
}

private const val MIN_SHOWN_TIME = 500L // ms
private const val MIN_DELAY_UNTIL_SHOW = 500L // ms
private const val FADE_ANIMATION_DURATION = 250L // ms