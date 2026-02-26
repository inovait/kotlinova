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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedState
import dev.zacsweers.metro.Provider
import kotlinx.serialization.modules.SerializersModule
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import kotlin.reflect.KClass

/**
 * Create a [Backstack] for navigation and remember it across state changes and process kills.
 *
 * [init] argument will only be called once (or after process kill). In that lambda, you have to
 * call [ComposeNavigatorInitializer.createBackstack] and return provided value.
 * Backstack will not perform any navigation until you return from that lambda,
 * so you can initialize your own services that require a [Backstack] instance, before you return.
 *
 * optional [id] argument allows you to have multiple backstacks inside single screen. To do that,
 * you have to provide unique ID to every distinct [rememberBackstack] call.
 */
@Composable
internal fun rememberBackstack(
   id: String = "SINGLE",
   init: ComposeNavigatorInitializer.() -> Backstack,
): Backstack {
   val viewModel = viewModel<BackstackHolderViewModel>()
   val backstack = viewModel.getBackstack(id) ?: init(viewModel.createInitializer(id))

   SaveBackstackState(backstack)

   return backstack
}

@Composable
private fun SaveBackstackState(backstack: Backstack) {
   val stateSavingRegistry = LocalSaveableStateRegistry.current

   remember(backstack) {
      if (stateSavingRegistry == null) {
         return@remember true
      }

      val oldState = stateSavingRegistry.consumeRestored(STATE_SAVING_KEY) as SavedState?
      backstack.init(oldState)

      stateSavingRegistry.registerProvider(STATE_SAVING_KEY) {
         backstack.saveState()
      }
   }
}

interface ComposeNavigatorInitializer {
   fun createBackstack(
      initialKeys: List<ScreenKey>,
      scopedServicesFactories: Lazy<Map<KClass<*>, Provider<out Any>>>,
      screenRegistry: Lazy<ScreenRegistry>,
      serializersModule: Lazy<SerializersModule>,
      parent: Backstack? = null,
      parentScope: String? = null,
      globalServices: List<KClass<*>> = emptyList(),
   ): Backstack
}

private const val STATE_SAVING_KEY = "BackstackState"
