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

package si.inova.kotlinova.navigation.di

import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.ScopedService
import javax.inject.Inject
import javax.inject.Provider

/**
 * Registry of all screen currently known by the navigation.
 */
class ScreenRegistry @Inject constructor(
   private val staticRegistrations: Map<@JvmSuppressWildcards Class<*>, @JvmSuppressWildcards ScreenRegistration<*>>,
   private val screenFactories: Map<@JvmSuppressWildcards Class<*>, @JvmSuppressWildcards Provider<Screen<*>>>,
) {
   @Suppress("UNCHECKED_CAST")
   fun <T : ScreenKey> createScreen(key: T): Screen<T> {
      val registration = getRegistration(key)

      val factory = screenFactories[registration.screenClass]
         ?: error("Missing screen factory for the ${registration.screenClass.name}")

      return factory.get() as Screen<T>
   }

   @Suppress("UNCHECKED_CAST")
   fun <T : ScreenKey> getRegistration(key: T): ScreenRegistration<T> {
      val reg = staticRegistrations[key.javaClass]
         ?: error(
            "No screen registered for ${key.javaClass.name}. Did you include a module in your app that contains a screen " +
               "for that key, and does that module have navigation anvil compiler?"
         )

      return reg as ScreenRegistration<T>
   }
}

class ScreenRegistration<T : ScreenKey>(
   val screenClass: Class<out Screen<T>>,
   vararg val serviceClasses: Class<out ScopedService>
)
