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

package si.inova.kotlinova.ui.lists.swiping

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import androidx.annotation.VisibleForTesting
import si.inova.kotlinova.utils.fromDpToPixels
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Helper class that manages horizontal view movement when user drags the view.
 *
 * This class will cause provided view to move with user's finger,
 * stick to the defined [leftAnchor] and [rightAnchor] and swipe away if user makes large
 * movement on the view
 *
 * Use [swipeListener] to listen to the events of this helper.
 *
 * @author Matej Drobnic
 */
class HorizontalSwipeHelper(private val view: View) {
    var swipeListener: Listener? = null

    var leftAnchor = 0f
    var rightAnchor = 0f

    private val velocityTracker = VelocityTracker.obtain()

    // Scale constants from px to dp
    private val escapeVelocity = view.context.fromDpToPixels(SWIPE_ESCAPE_VELOCITY_UNSCALED)
    private val returnAnimationVelocity =
            view.context.fromDpToPixels(RETURN_ANIMATION_VELOCITY_UNSCALED)
    private val maxDismissVelocity = view.context.fromDpToPixels(MAX_DISMISS_VELOCITY_UNSCALED)

    private var initialX = 0f
    private var prevX = 0f
    private var dragging = false

    @VisibleForTesting
    var currentAnimation: SelfMovementAnimation? = null

    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                prevX = initialX

                velocityTracker.clear()
                velocityTracker.addMovement(event)

                val currentAnimation = currentAnimation
                if (currentAnimation != null) {
                    currentAnimation.cancel()
                    startDrag()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) {
                    val delta = abs(event.rawX - initialX)
                    if (delta >= ViewConfiguration.get(view.context).scaledTouchSlop) {
                        startDrag()
                    }
                }
            }
        }

        return dragging
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(event)
                val dragAmount = event.rawX - prevX
                prevX = event.rawX
                view.translationX += dragAmount
                swipeListener?.onMoved(view.translationX)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                val captured = dragging

                finishDrag()

                dragging = false
                velocityTracker.clear()

                if (captured) {
                    return true
                }
            }
        }

        return dragging
    }

    private fun startDrag() {
        dragging = true
        view.parent?.requestDisallowInterceptTouchEvent(true)
    }

    private fun finishDrag() {
        /// 1000 means pixels per second
        velocityTracker.computeCurrentVelocity(1000, maxDismissVelocity)
        val swiped = abs(velocityTracker.xVelocity) >= escapeVelocity

        val movementDirection = Math.signum(prevX - initialX)

        currentAnimation = if (swiped) {
            val targetX = movementDirection * view.width
            // Fast sweipt to either direction
            SelfMovementAnimation(targetX, abs(velocityTracker.xVelocity), false) {
                if (movementDirection < 0) {
                    swipeListener?.onSwipedLeft()
                } else {
                    swipeListener?.onSwipedRight()
                }
            }
        } else if (movementDirection > 0) {
            if (view.translationX < leftAnchor) {
                // User just slightly moved the view. Return to start position
                SelfMovementAnimation(0f, returnAnimationVelocity)
            } else {
                val midPoint = (view.width - leftAnchor) / 2 + leftAnchor

                val stickToAnchor = view.translationX < midPoint
                if (stickToAnchor) {
                    // User moved the view slightly away from left anchor. Stick to the anchor.
                    SelfMovementAnimation(leftAnchor, returnAnimationVelocity)
                } else {
                    // User moved the view significantly away from left anchor. Swipe to the end.
                    SelfMovementAnimation(
                            view.width.toFloat(),
                            returnAnimationVelocity,
                            true
                    ) {
                        swipeListener?.onSwipedRight()
                    }
                }
            }
        } else {
            if (-view.translationX < rightAnchor) {
                // User just slightly moved the view. Return to start position
                SelfMovementAnimation(0f, returnAnimationVelocity)
            } else {
                val midPoint = (view.width - rightAnchor) / 2 + rightAnchor

                val stickToAnchor = -view.translationX < midPoint
                if (stickToAnchor) {
                    // User moved the view slightly away from right anchor. Stick to the anchor.
                    SelfMovementAnimation(-rightAnchor, returnAnimationVelocity)
                } else {
                    // User moved the view significantly away from right anchor. Swipe to the end.
                    SelfMovementAnimation(
                            -view.width.toFloat(),
                            returnAnimationVelocity,
                            true
                    ) {
                        swipeListener?.onSwipedLeft()
                    }
                }
            }
        }

        currentAnimation!!.start()
    }

    fun resetSmooth() {
        SelfMovementAnimation(0f, SMOOTH_SCROLL_VELOCITY_UNSCALED).start()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    inner class SelfMovementAnimation(
            @get:VisibleForTesting
            val targetX: Float,
            velocity: Float,
            decelerateTowardsEnd: Boolean = true,
            private val endAction: (() -> Unit)? = null
    ) : ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private val valueAnimator: ValueAnimator

        init {
            val currentX = view.translationX
            val distanceToTravel = abs(currentX - targetX)
            val durationSeconds = distanceToTravel / velocity

            valueAnimator = ValueAnimator.ofFloat(currentX, targetX).apply {
                duration = (durationSeconds * 1000).roundToLong()

                if (!decelerateTowardsEnd) {
                    interpolator = LinearInterpolator()
                }
            }

            valueAnimator.addUpdateListener(this)
            valueAnimator.addListener(this)
        }

        fun start(): SelfMovementAnimation {
            valueAnimator.start()
            return this
        }

        fun cancel() {
            valueAnimator.cancel()
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            view.translationX = animation.animatedValue as Float
            swipeListener?.onMoved(view.translationX)
        }

        override fun onAnimationEnd(animation: Animator?) {
            if (currentAnimation == this) {
                currentAnimation = null

                endAction?.invoke()
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
            if (currentAnimation == this) {
                currentAnimation = null
            }
        }

        override fun onAnimationRepeat(animation: Animator?) = Unit
        override fun onAnimationStart(animation: Animator?) = Unit
    }

    interface Listener {
        fun onSwipedLeft()
        fun onSwipedRight()
        fun onMoved(translationX: Float)
    }
}

private const val SWIPE_ESCAPE_VELOCITY_UNSCALED = 500f // dp/sec
private const val MAX_DISMISS_VELOCITY_UNSCALED = 200f // dp/sec
private const val RETURN_ANIMATION_VELOCITY_UNSCALED = 400f // dp/sec
private const val SMOOTH_SCROLL_VELOCITY_UNSCALED = 800f // dp/sec