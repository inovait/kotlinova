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

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Provider
import kotlinx.serialization.modules.SerializersModule
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import kotlin.reflect.KClass

internal class BackstackHolderViewModel : ViewModel() {
   private val backstacks = HashMap<String, Backstack>()

   fun getBackstack(id: String): Backstack? {
      return backstacks[id]
   }

   fun createInitializer(id: String) = object : ComposeNavigatorInitializer {
      override fun createBackstack(
         initialKeys: List<ScreenKey>,
         scopedServicesFactories: Lazy<Map<KClass<*>, Provider<out Any>>>,
         screenRegistry: Lazy<ScreenRegistry>,
         serializersModule: Lazy<SerializersModule>,
         parent: Backstack?,
         parentScope: String?,
         globalServices: List<KClass<*>>,
      ): Backstack {
         return Backstack(
            initialKeys,
            scopedServicesFactories,
            screenRegistry,
            serializersModule,
            parent,
            parentScope,
            globalServices,
         ).also {
            backstacks[id] = it
         }
      }
   }

   override fun onCleared() {
      for (backstack in backstacks.values) {
         backstack.onDestroy()
      }
   }
}
