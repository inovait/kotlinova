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

package si.inova.kotlinova.navigation.backstack

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Provider
import si.inova.kotlinova.navigation.di.MainBackstackWrapper
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

/**
 * Create a [Backstack] for navigation with the screens provided by this factory
 * and remember it across state changes and process kills.
 *
 * optional [id] argument allows you to have multiple backstacks inside single screen. To do that,
 * you have to provide unique ID to every distinct [rememberBackstack] call.
 */
@Composable
fun NavigationInjection.Factory.rememberBackstack(
   id: String = "SINGLE",
   overrideMainBackstack: Backstack? = null,
   parentBackstack: Backstack? = null,
   parentBackstackScope: String? = null,
   initialHistory: () -> List<ScreenKey>,
): Backstack {
   return rememberBackstack(id, init = {
      lateinit var navigationInjection: NavigationInjection

      val backstack = createBackstack(
         initialHistory(),
         scopedServicesFactories = lazy {
            navigationInjection.scopedServicesFactories() +
               mapOf(NavigationInjection::class to Provider { navigationInjection })
         },
         screenRegistry = lazy { navigationInjection.screenRegistry() },
         serializersModule = lazy { navigationInjection.serializers() },
         parent = parentBackstack,
         parentScope = parentBackstackScope,
         globalServices = listOf(NavigationInjection::class),
      )

      navigationInjection = create(backstack, MainBackstackWrapper(overrideMainBackstack ?: backstack))

      backstack
   })
}
