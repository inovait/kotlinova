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

import com.zhuinden.simplestack.StateChange
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey

/**
 * Open provided screen. If screen has any [ScreenKey.navigationConditions], they will be navigated to if they are not met.
 */
@Parcelize
data class OpenScreen(val screen: ScreenKey) : NavigationInstruction() {
   override fun performNavigation(backstack: List<ScreenKey>, context: NavigationContext): NavigationResult {
      return if (backstack.isNotEmpty() && screen is SingleTopKey && backstack.last().javaClass == screen.javaClass) {
         val newBackstack = backstack.dropLast(1) + screen
         NavigationResult(newBackstack, StateChange.REPLACE)
      } else {
         if (backstack.lastOrNull() == screen) {
            error(
               "Cannot add $screen to the backstack twice back to back. If you want same screen on the" +
                  " backstack twice, add a random identifier to the key, such as an UUID. Current backstack $backstack"
            )
         }
         NavigationResult(backstack + screen, StateChange.FORWARD)
      }
   }
}

/**
 * Either open this screen or navigate to condition resolving first, if this screen has any unmet conditions.
 */
fun OpenScreen.withConditions(): NavigationInstruction {
   return if (screen.navigationConditions.isEmpty()) {
      this
   } else {
      NavigateWithConditions(this, *screen.navigationConditions.toTypedArray())
   }
}

/**
 * Open provided screen.  If screen has any [ScreenKey.navigationConditions], they will be navigated to if they are not met.
 */
fun Navigator.navigateTo(screen: ScreenKey) {
   navigate(OpenScreen(screen).withConditions())
}
