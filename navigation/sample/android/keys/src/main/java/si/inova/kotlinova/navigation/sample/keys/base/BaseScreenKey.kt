/*
 * Copyright 2025 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.kotlinova.navigation.sample.keys.base

import androidx.activity.BackEventCompat
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.TransformOrigin
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

abstract class BaseScreenKey : ScreenKey() {
   override fun backAnimation(
      scope: AnimatedContentTransitionScope<*>,
      backSwipeEdge: @BackEventCompat.SwipeEdge Int?
   ): ContentTransform {
      // Animation spec that attempts to mimic Google's back preview guidelines as close as possible
      // https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back#back-preview

      // See https://issuetracker.google.com/issues/347047848 for feature request to be able
      // to implement the guidelines fully

      val scaleTransformOrigin = when (backSwipeEdge) {
         BackEventCompat.EDGE_LEFT -> TransformOrigin(1f, 0.5f)
         BackEventCompat.EDGE_RIGHT -> TransformOrigin(0f, 0.5f)
         else -> TransformOrigin.Center
      }

      return (fadeIn() + scaleIn(initialScale = 1.1f, transformOrigin = scaleTransformOrigin)) togetherWith
         (fadeOut() + scaleOut(targetScale = 0.9f, transformOrigin = scaleTransformOrigin))
   }
}
