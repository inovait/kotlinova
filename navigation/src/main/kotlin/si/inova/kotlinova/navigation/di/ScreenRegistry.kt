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

package si.inova.kotlinova.navigation.di

import com.zhuinden.simplestack.Backstack
import me.tatarka.inject.annotations.Inject
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.ScopedService

/**
 * Registry of all screen currently known by the navigation.
 */
class ScreenRegistry @Inject constructor(
   private val backstack: Backstack,
   private val staticRegistrations: Map<Class<out ScreenKey>, ScreenRegistration<*>>,
   private val screenFactories: Map<Class<out Screen<out ScreenKey>>, ScreenFactory<*>>,
) {
   @Suppress("UNCHECKED_CAST")
   fun <T : ScreenKey> createScreen(key: T): Screen<T> {
      val registration = getRegistration(key)

      val factory = screenFactories[registration.mainScreenClass]
         ?: error("Missing screen factory for the ${registration.mainScreenClass.name}")

      return factory.create(key.scopeTag, backstack) as Screen<T>
   }

   @Suppress("UNCHECKED_CAST")
   fun <T : ScreenKey> getRegistration(key: T): ScreenRegistration<T> {
      val reg = staticRegistrations[key.javaClass]
         ?: error(
            "No screen registered for ${key.javaClass.name}. Did you add an @InjectNavigationScreen to its screen?"
         )

      return reg as ScreenRegistration<T>
   }

   fun getRequiredScopedServices(key: ScreenKey): List<Class<out ScopedService>> {
      val registration = getRegistration(key)

      val factory = screenFactories[registration.mainScreenClass]
         ?: error("Missing screen factory for the $key")

      return factory.getAllRequiredScopedServices()
   }
}

class ScreenRegistration<T : ScreenKey>(val mainScreenClass: Class<out Screen<T>>)
class ScreenFactory<T : Screen<*>>(
   val requiredScopedServices: List<Class<out ScopedService>>,
   val composedScreenFactories: List<ScreenFactory<*>>,
   private val factory: (String, Backstack) -> T
) {
   fun create(scope: String, backstack: Backstack): T {
      return factory(scope, backstack)
   }
}

private fun ScreenFactory<*>.getAllRequiredScopedServices(): List<Class<out ScopedService>> {
   return requiredScopedServices + composedScreenFactories.flatMap { it.getAllRequiredScopedServices() }
}
