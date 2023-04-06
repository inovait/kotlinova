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

package si.inova.kotlinova.navigation.conditions

import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import javax.inject.Inject
import javax.inject.Provider

class MainConditionalNavigationHandler @Inject constructor(
   private val handlers: Map<
      @JvmSuppressWildcards Class<*>,
      @JvmSuppressWildcards Provider<ConditionalNavigationHandler>
      >
) : ConditionalNavigationHandler {
   override fun getNavigationRedirect(
      condition: NavigationCondition,
      navigateToIfConditionMet: NavigationInstruction
   ): NavigationInstruction? {
      val handler = handlers[condition.javaClass]?.get()
         ?: error(
            "Failed to find condition handler for condition ${condition.javaClass.name}. " +
               "Did you add @ContributesMultibinding and @ClassKey annotations to it?"
         )

      return handler.getNavigationRedirect(condition, navigateToIfConditionMet)
   }
}
