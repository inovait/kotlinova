/*
 * Copyright 2026 INOVA IT d.o.o.
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
 * Execute multiple navigation instructions sequentially
 * @param instructions a list of instructions to navigate to
 *   When not provided, direction of the last performed
 *   instruction is used instead.
 */
@Parcelize
class MultiNavigationInstructions(
   vararg val instructions: NavigationInstruction,
) : NavigationInstruction() {
   override fun performNavigation(backstack: List<ScreenKey>, context: NavigationContext): NavigationResult {
      var currentBackstack = backstack

      for (instruction in instructions) {
         val res = instruction.performNavigation(currentBackstack, context)
         currentBackstack = res.newBackstack
      }

      return NavigationResult(currentBackstack)
   }

   override fun toString(): String {
      return "MultiNavigationInstructions(instructions=${instructions.contentToString()})"
   }

   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is MultiNavigationInstructions) return false

      if (!instructions.contentEquals(other.instructions)) return false

      return true
   }

   override fun hashCode(): Int {
      return instructions.contentHashCode() * 31
   }
}
