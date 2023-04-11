/*
 * Copyright 2023 INOVA IT d.o.o.
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

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import com.zhuinden.simplestack.StateChange
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.simplestack.StateChangeResult

@Parcelize
data class SlideAnimationScreenKey(val argument: String) : ScreenKey() {
   @OptIn(ExperimentalAnimationApi::class)
   override fun forwardAnimation(scope: AnimatedContentScope<StateChangeResult>): ContentTransform {
      return if (scope.targetState.direction == StateChange.REPLACE) {
         fadeIn() with fadeOut()
      } else {
         scope.slideIntoContainer(AnimatedContentScope.SlideDirection.Left) with
            scope.slideOutOfContainer(AnimatedContentScope.SlideDirection.Left)
      }
   }

   @OptIn(ExperimentalAnimationApi::class)
   override fun backAnimation(scope: AnimatedContentScope<StateChangeResult>): ContentTransform {
      return scope.slideIntoContainer(AnimatedContentScope.SlideDirection.Right) with
         scope.slideOutOfContainer(AnimatedContentScope.SlideDirection.Right)
   }
}
