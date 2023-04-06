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

package si.inova.kotlinova.navigation.navigator

import com.squareup.anvil.annotations.ContributesBinding
import com.zhuinden.simplestack.Backstack
import dagger.Lazy
import si.inova.kotlinova.navigation.di.BackstackScope
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import javax.inject.Inject

@ContributesBinding(BackstackScope::class)
class SimpleStackNavigator @Inject constructor(
   private val backstack: Backstack,
   private val navigationContext: Lazy<NavigationContext>
) : Navigator {
   override fun navigate(navigationInstruction: NavigationInstruction) {
      val res = navigationInstruction.performNavigation(backstack.getHistory(), navigationContext.get())

      backstack.setHistory(res.newBackstack, res.direction)
   }
}
