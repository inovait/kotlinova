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
import si.inova.kotlinova.navigation.conditions.NavigationCondition
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

/**
 * Navigate to [target] if all [conditions] are met.
 *
 * If conditions are not met, Navigator will first navigate to a screen where user can meet the conditions (for example, log-in
 * screen) and then redirect back to the target screen. You can intercept navigation to the condition meeting screen via
 * [conditionScreenWrapper] argument.
 */
@Parcelize
class NavigateWithConditions(
   val target: NavigationInstruction,
   vararg val conditions: NavigationCondition,
   val conditionScreenWrapper: (NavigationInstruction) -> NavigationInstruction = { it },
) : NavigationInstruction() {
   override fun performNavigation(backstack: List<ScreenKey>, context: NavigationContext): NavigationResult {
      for (condition in conditions) {
         val overriddenTarget = context.mainConditionalNavigationHandler.getNavigationRedirect(
            condition,
            target
         )

         if (overriddenTarget != null) {
            return conditionScreenWrapper(overriddenTarget).performNavigation(backstack, context)
         }
      }

      return target.performNavigation(backstack, context)
   }

   override fun toString(): String {
      return "NavigateWithConditions(target=$target, conditions=${conditions.contentToString()})"
   }
}
