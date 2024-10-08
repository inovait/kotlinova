/*
 * Copyright 2024 INOVA IT d.o.o.
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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.zhuinden.simplestack.StateChange
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.sample.keys.base.BaseScreenKey
import si.inova.kotlinova.navigation.simplestack.StateChangeResult

@Parcelize
data class SlideAnimationScreenKey(val argument: String) : BaseScreenKey() {
   override fun forwardAnimation(scope: AnimatedContentTransitionScope<StateChangeResult>): ContentTransform {
      return if (scope.targetState.direction == StateChange.REPLACE) {
         fadeIn() togetherWith fadeOut()
      } else {
         scope.slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) togetherWith
            scope.slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
      }
   }

   override fun backAnimation(scope: AnimatedContentTransitionScope<StateChangeResult>): ContentTransform {
      return scope.slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) togetherWith
         scope.slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
   }
}
