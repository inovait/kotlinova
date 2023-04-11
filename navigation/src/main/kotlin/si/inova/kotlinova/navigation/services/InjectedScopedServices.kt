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

package si.inova.kotlinova.navigation.services

import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.ServiceBinder
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import javax.inject.Provider

class InjectedScopedServices : ScopedServices {
   lateinit var scopedServicesFactories: Map<@JvmSuppressWildcards Class<*>, @JvmSuppressWildcards Provider<ScopedService>>
   lateinit var screenRegistry: ScreenRegistry

   override fun bindServices(serviceBinder: ServiceBinder) {
      if (!::screenRegistry.isInitialized) {
         val injection = NavigationInjection.fromBackstack(serviceBinder.backstack)
         screenRegistry = injection.screenRegistry()
         scopedServicesFactories = injection.scopedServicesFactories()
      }

      val serviceKeys = screenRegistry.getRegistration(serviceBinder.getKey()).serviceClasses

      for (key in serviceKeys) {
         val service = scopedServicesFactories.getValue(key).get()
         if (service is SingleScreenViewModel<*>) {
            @Suppress("UNCHECKED_CAST")
            (service as SingleScreenViewModel<ScreenKey>).key = serviceBinder.getKey() as ScreenKey
         }
         serviceBinder.addService(key.name, service)
      }
   }
}
