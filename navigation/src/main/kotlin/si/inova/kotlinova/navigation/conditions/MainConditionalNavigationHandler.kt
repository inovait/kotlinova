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

package si.inova.kotlinova.navigation.conditions

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import kotlin.reflect.KClass

@Inject
class MainConditionalNavigationHandler(
   private val handlers: Map<KClass<*>, Provider<ConditionalNavigationHandler>>,
) : ConditionalNavigationHandler {
   override fun getNavigationRedirect(
      condition: NavigationCondition,
      navigateToIfConditionMet: NavigationInstruction,
   ): NavigationInstruction? {
      val handler = handlers[condition.javaClass.kotlin]?.invoke()
         ?: error(
            "Failed to find condition handler for condition ${condition.javaClass.name}. " +
               "Did you add it to a Component with a @IntoMap @Provides function?"
         )

      return handler.getNavigationRedirect(condition, navigateToIfConditionMet)
   }
}
