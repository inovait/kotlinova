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

package si.inova.kotlinova.navigation.test

import si.inova.kotlinova.navigation.conditions.ConditionalNavigationHandler
import si.inova.kotlinova.navigation.conditions.MainConditionalNavigationHandler
import si.inova.kotlinova.navigation.conditions.NavigationCondition
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

/**
 * Test fake for Navigator that performs operation on the local in-memory backstack.
 *
 * You specify the initial backstack in the constructor, perform navigation commands and observe resulting backstack
 * via [backstack] property.
 */
class FakeNavigator(
   vararg initialBackstack: ScreenKey,
   private val conditionalNavigationHandlers: Map<Class<NavigationCondition>, ConditionalNavigationHandler> = emptyMap()
) : Navigator {
   var backstack: List<ScreenKey> = initialBackstack.toList()

   override fun navigate(navigationInstruction: NavigationInstruction) {
      backstack = navigationInstruction.performNavigation(backstack, context).newBackstack
   }

   private val context = object : NavigationContext {
      override val mainConditionalNavigationHandler: ConditionalNavigationHandler = MainConditionalNavigationHandler(
         conditionalNavigationHandlers.mapValues { { it.value } }
      )
   }
}
