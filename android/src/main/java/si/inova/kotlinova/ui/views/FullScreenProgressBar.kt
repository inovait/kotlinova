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

package si.inova.kotlinova.ui.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import si.inova.kotlinova.android.R
import si.inova.kotlinova.time.AndroidTimeProvider
import si.inova.kotlinova.utils.setVisibilityWithoutLayoutAnimations
import kotlin.math.max

/**
 * Progress bar that fades all views and disables all touch behind it.
 *
 * It also prevents blinking (it will only be displayed after some time loading and then it will be
 * displayed for a minimum time)
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

        val elapsedTime = AndroidTimeProvider.elapsedRealtime() - lastShowTime
        val timeLeft = max(0, MIN_SHOWN_TIME - elapsedTime)
        postDelayed(hideRunnable, timeLeft)
    }

    private val showRunnable = Runnable {
        lastShowTime = AndroidTimeProvider.elapsedRealtime()

        shown = true

        activeAnimation?.cancel()
        animate()
                .alpha(1f)
                .withStartAction { setVisibilityWithoutLayoutAnimations(View.VISIBLE) }
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
                    setVisibilityWithoutLayoutAnimations(View.GONE)
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

        alpha = if (isVisible) {
            1f
        } else {
            0f
        }

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
private const val MIN_DELAY_UNTIL_SHOW = 250L // ms
private const val FADE_ANIMATION_DURATION = 250L // ms