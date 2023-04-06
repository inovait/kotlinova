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

package si.inova.kotlinova.navigation.instructions

import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

/**
 * Navigation instruction that implements standard deep link behavior.
 *
 * If [replaceBackstack] is *true*, it will replace the entire backstack with the provided [history]. Otherwise it will take
 * just the last screen of the provided [history] and put it on top of the backstack.
 *
 * This instruction also supports navigation conditions on the last screen. If screen has any unmet conditions, the condition
 * resolving screen will be displayed first, on an empty backstack. Then, action described in above paragraph will occur.
 */
@Parcelize
class ReplaceBackstackOrOpenScreen(val replaceBackstack: Boolean, vararg val history: ScreenKey) : NavigationInstruction() {
   override fun performNavigation(backstack: List<ScreenKey>, context: NavigationContext): NavigationResult {
      require(history.isNotEmpty()) { "You should provide at least one screen to ReplaceBackstackOrOpenScreen" }

      val finalScreen = history.last()
      val nextInstruction = if (replaceBackstack) {
         NavigateWithConditions(
            ReplaceBackstack(*history),
            *finalScreen.navigationConditions.toTypedArray(),
            conditionScreenWrapper = { ClearBackstackAnd(it) }
         )
      } else {
         NavigateWithConditions(
            OpenScreenOrMoveToTop(finalScreen),
            *finalScreen.navigationConditions.toTypedArray()
         )
      }

      return nextInstruction.performNavigation(backstack, context)
   }

   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is ReplaceBackstackOrOpenScreen) return false

      if (replaceBackstack != other.replaceBackstack) return false
      if (!history.contentEquals(other.history)) return false

      return true
   }

   override fun hashCode(): Int {
      var result = replaceBackstack.hashCode()
      result = 31 * result + history.contentHashCode()
      return result
   }

   override fun toString(): String {
      return "ReplaceBackstackOrOpenScreen(replaceBackstack=$replaceBackstack, history=${history.contentToString()})"
   }
}
