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

package com.zhuinden.simplestack.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhuinden.simplestack.BackHandlingModel
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.Backstack.StateClearStrategy
import com.zhuinden.simplestack.DefaultKeyFilter
import com.zhuinden.simplestack.DefaultKeyParceler
import com.zhuinden.simplestack.DefaultStateClearStrategy
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestack.KeyFilter
import com.zhuinden.simplestack.KeyParceler
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChanger
import com.zhuinden.statebundle.StateBundle

/**
 * Create a [Backstack] for navigation and remember it across state changes and process kills.
 *
 * [stateChanger] wil not be remembered and will be re-initialized every time this composable
 * exits scope. It MUST be remembered by the caller.
 *
 * [init] argument will only be called once (or after process kill). In that lambda, you have to
 * call [ComposeNavigatorInitializer.createBackstack] and return provided value.
 * Backstack will not perform any navigation until you return from that lambda,
 * so you can initialize your own services that require a [Backstack] instance, before you return.
 *
 * optional [id] argument allows you to have multiple backstacks inside single screen. To do that,
 * you have to provide unique ID to every distinct [rememberBackstack] call.
 *
 * Note that backstack created with this method
 * uses [BackHandlingModel.AHEAD_OF_TIME] back handling model.
 *
 * This is a copy from https://github.com/Zhuinden/simple-stack-compose-integration/pull/22/ until
 * it gets merged.
 */
@Composable
internal fun rememberBackstack(
   stateChanger: StateChanger,
   id: String = "SINGLE",
   init: ComposeNavigatorInitializer.() -> Backstack,
): Backstack {
   val viewModel = viewModel<BackstackHolderViewModel>()
   val backstack = viewModel.getBackstack(id) ?: init(viewModel.createInitializer(id))

   SaveBackstackState(backstack)
   ListenToLifecycleEvents(backstack)

   remember(stateChanger) {
      // Attach state changer after init call to defer first navigation. That way,
      // caller can use backstack to init their own things with Backstack instance
      // before navigation is performed.
      backstack.setStateChanger(stateChanger)
      true
   }

   return backstack
}

@Composable
private fun SaveBackstackState(backstack: Backstack) {
   val stateSavingRegistry = LocalSaveableStateRegistry.current

   remember(backstack) {
      if (stateSavingRegistry == null) {
         return@remember true
      }

      val oldState = stateSavingRegistry.consumeRestored(STATE_SAVING_KEY) as StateBundle?
      oldState?.let {
         backstack.fromBundle(it)
      }

      stateSavingRegistry.registerProvider(STATE_SAVING_KEY) {
         backstack.toBundle()
      }
   }
}

@Composable
private fun ListenToLifecycleEvents(backstack: Backstack) {
   val lifecycle = LocalLifecycleOwner.current.lifecycle

   DisposableEffect(lifecycle) {
      val lifecycleListener = LifecycleEventObserver { _, event ->
         val isResumed = event.targetState.isAtLeast(Lifecycle.State.RESUMED)
         val isStateChangerAlreadyAttached = backstack.hasStateChanger()
         if (isResumed != isStateChangerAlreadyAttached) {
            if (isResumed) {
               backstack.reattachStateChanger()
            } else {
               backstack.detachStateChanger()
            }
         }
      }

      lifecycle.addObserver(lifecycleListener)

      onDispose {
         lifecycle.removeObserver(lifecycleListener)
         backstack.executePendingStateChange()
         backstack.setStateChanger(null)
      }
   }
}

interface ComposeNavigatorInitializer {
   fun createBackstack(
      initialKeys: List<*>,
      keyFilter: KeyFilter = DefaultKeyFilter(),
      keyParceler: KeyParceler = DefaultKeyParceler(),
      stateClearStrategy: StateClearStrategy = DefaultStateClearStrategy(),
      scopedServices: ScopedServices? = null,
      globalServices: GlobalServices? = null,
      parent: Backstack? = null,
      parentScope: String? = null,
      globalServicesFactory: GlobalServices.Factory? = null,
   ): Backstack
}

private const val STATE_SAVING_KEY = "BackstackState"
