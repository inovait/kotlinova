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
 * If the screen already exists on the backstack, it moves it to the top. Otherwise, it adds it to the backstack.
 */
@Parcelize
data class OpenScreenOrMoveToTop(val screen: ScreenKey) : NavigationInstruction() {
   override fun performNavigation(backstack: List<ScreenKey>, context: NavigationContext): NavigationResult {
      return if (backstack.lastOrNull() == screen) {
         NavigationResult(backstack, StateChange.REPLACE)
      } else if (backstack.isNotEmpty() && screen is SingleTopKey && backstack.last().javaClass == screen.javaClass) {
         val newBackstack = backstack.dropLast(1) + screen
         NavigationResult(newBackstack, StateChange.REPLACE)
      } else {
         val backstackWithoutTargetScreen = backstack.filter { it != screen }
         NavigationResult(backstackWithoutTargetScreen + screen, StateChange.FORWARD)
      }
   }
}

/**
 * If the provided screen already exists on the backstack, it moves the existing screen in the backstack it to the top.
 * Otherwise, it opens it on normally.
 *
 * If screen has any [ScreenKey.navigationConditions], they will be navigated to if they are not met.
 */

fun Navigator.navigateToSingle(screen: ScreenKey) {
   val instruction = if (screen.navigationConditions.isEmpty()) {
      OpenScreenOrMoveToTop(screen)
   } else {
      NavigateWithConditions(OpenScreenOrMoveToTop(screen), *screen.navigationConditions.toTypedArray())
   }

   navigate(instruction)
}
