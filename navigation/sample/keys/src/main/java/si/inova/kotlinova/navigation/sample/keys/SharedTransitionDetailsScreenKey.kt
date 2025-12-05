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

package si.inova.kotlinova.navigation.sample.keys

import androidx.activity.BackEventCompat
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.sample.keys.base.BaseScreenKey

@Parcelize
data class SharedTransitionDetailsScreenKey(val colorIndex: Int) : BaseScreenKey() {
   override fun forwardAnimation(scope: AnimatedContentTransitionScope<*>): ContentTransform {
      //  Shared transition needs a bit longer transition time so it does not look that jarring
      return fadeIn(tween(500)) togetherWith fadeOut(tween(500))
   }

   override fun backAnimation(
      scope: AnimatedContentTransitionScope<*>,
      backSwipeEdge: @BackEventCompat.SwipeEdge Int?
   ): ContentTransform {
      //  Override default transition so screen does not move when going back
      return fadeIn(tween(500)) togetherWith fadeOut(tween(500))
   }
}
