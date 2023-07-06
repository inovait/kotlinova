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

package si.inova.kotlinova.navigation.simplestack

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger.Callback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey

/**
 * A state changer that allows switching between composables, animating the transition.
 *
 * Based on https://github.com/Zhuinden/simple-stack-compose-integration/blob/c5fa2d2d7bec305af93f84d2af0a91f4cdf17227/core/src/main/java/com/zhuinden/simplestackcomposeintegration/core/ComposeIntegrationCore.kt
 */
class ComposeStateChanger : AsyncStateChanger.NavigationHandler {
   private var currentStateChange by mutableStateOf<StateChangeData?>(null)
   private var lastCompletedCallback by mutableStateOf<Callback?>(null)

   private lateinit var screenRegistry: ScreenRegistry

   override fun onNavigationEvent(stateChange: StateChange, completionCallback: Callback) {
      if (!::screenRegistry.isInitialized) {
         screenRegistry = NavigationInjection.fromBackstack(stateChange.backstack).screenRegistry()
      }
      currentStateChange = StateChangeData(stateChange, completionCallback)
   }

   @OptIn(ExperimentalAnimationApi::class)
   @Composable
   fun Content(screenWrapper: @Composable (key: ScreenKey, screen: @Composable () -> Unit) -> Unit = { _, screen -> screen() }) {
      val saveableStateHolder = rememberSaveableStateHolder()
      val viewModelStores = viewModel<StoreHolderViewModel>()

      val currentStateChange = currentStateChange
      val topKey = currentStateChange?.stateChange?.getNewKeys<ScreenKey>()?.lastOrNull() ?: return
      val stateChangeResult = StateChangeResult(currentStateChange.stateChange.direction, topKey)

      val transition = updateTransition(stateChangeResult, "AnimatedContent")
      transition.AnimatedContent(
         transitionSpec = {
            if (targetState.direction == StateChange.BACKWARD) {
               with(initialState.newTopKey) { backAnimation(this@AnimatedContent) }
            } else {
               with(targetState.newTopKey) { forwardAnimation(this@AnimatedContent) }
            }
         },
         contentKey = { it.newTopKey.contentKey() }
      ) { (_, topKey) ->
         TriggerCompletionCallback(saveableStateHolder, viewModelStores)
         val contentKey = topKey.contentKey()

         saveableStateHolder.SaveableStateProvider(contentKey) {
            viewModelStores.WithLocalViewModelStore(contentKey) {
               LocalDestroyedLifecycle {
                  screenWrapper(topKey) {
                     ShowScreen(topKey)
                  }
               }
            }
         }
      }
   }

   @Composable
   private fun ShowScreen(key: ScreenKey) {
      val screen = remember(key.contentKey()) {
         screenRegistry.createScreen(key)
      }

      screen.Content(key)
   }

   private fun cleanupStaleSaveStates(
      currentStateChange: StateChangeData,
      saveableStateHolder: SaveableStateHolder,
      viewModelStores: StoreHolderViewModel
   ) {
      val stateChange = currentStateChange.stateChange
      val previousKeys = stateChange.getPreviousKeys<Any>()
      val newKeys = stateChange.getNewKeys<Any>()
      previousKeys.fastForEach { previousKey ->
         if (!newKeys.contains(previousKey)) {
            saveableStateHolder.removeState(previousKey)
            viewModelStores.removeKey(previousKey)
         }
      }
   }

   @Composable
   @OptIn(ExperimentalAnimationApi::class)
   private fun AnimatedVisibilityScope.TriggerCompletionCallback(
      saveableStateHolder: SaveableStateHolder,
      viewModelStores: StoreHolderViewModel
   ) {
      LaunchedEffect(currentStateChange) {
         val currentStateChange = currentStateChange ?: return@LaunchedEffect

         snapshotFlow { transition.totalDurationNanos to lastCompletedCallback }
            .takeWhile { (remainingAnimationDuration, lastCallback) ->
               if (remainingAnimationDuration == 0L && lastCallback != currentStateChange.completionCallback) {
                  lastCompletedCallback = currentStateChange.completionCallback
                  currentStateChange.completionCallback.stateChangeComplete()

                  cleanupStaleSaveStates(currentStateChange, saveableStateHolder, viewModelStores)

                  return@takeWhile false
               }

               true
            }.collect()
      }
   }

   class StateChangeData(val stateChange: StateChange, val completionCallback: Callback)

   private fun ScreenKey.contentKey(): Any {
      return if (this is SingleTopKey && isSingleTop) {
         this.javaClass.name
      } else {
         this
      }
   }
}

/**
 * Wrapper that puts provided [child] into local lifecycle. Whenever this child is removed from
 * composition, its [LocalLifecycleOwner] will also get destroyed.
 */
@Composable
private fun LocalDestroyedLifecycle(child: @Composable () -> Unit) {
   val childLifecycleOwner = remember {
      object : LifecycleOwner {
         override val lifecycle = LifecycleRegistry(this)
      }
   }

   val childLifecycle = childLifecycleOwner.lifecycle

   val parentLifecycle = LocalLifecycleOwner.current.lifecycle

   DisposableEffect(parentLifecycle) {
      val parentListener = LifecycleEventObserver { _, event ->
         childLifecycle.handleLifecycleEvent(event)
      }

      parentLifecycle.addObserver(parentListener)

      onDispose {
         parentLifecycle.removeObserver(parentListener)

         if (childLifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            childLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
         }
      }
   }

   CompositionLocalProvider(LocalLifecycleOwner provides childLifecycleOwner) {
      child()
   }
}

class StoreHolderViewModel : ViewModel() {
   private val viewModelStores = HashMap<Any, ViewModelStoreOwner>()

   fun removeKey(key: Any) {
      viewModelStores.remove(key)?.viewModelStore?.clear()
   }

   @Composable
   fun WithLocalViewModelStore(key: Any, block: @Composable () -> Unit) {
      val storeOwner = viewModelStores.getOrPut(key) {
         object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
         }
      }

      CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
         block()
      }
   }

   override fun onCleared() {
      for (store in viewModelStores.values) {
         store.viewModelStore.clear()
      }
   }
}

/**
 * Composition local to access the Backstack within screens.
 */
val LocalBackstack =
   staticCompositionLocalOf<Backstack> {
      throw IllegalStateException(
         "You must ensure that the BackstackProvider provides the backstack, but it currently doesn't exist."
      )
   }

/**
 * Provider for the backstack composition local.
 */
@Composable
fun BackstackProvider(backstack: Backstack, content: @Composable () -> Unit) {
   CompositionLocalProvider(LocalBackstack provides (backstack)) {
      content()
   }
}
