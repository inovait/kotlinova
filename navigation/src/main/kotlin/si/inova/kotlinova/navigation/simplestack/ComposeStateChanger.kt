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

package si.inova.kotlinova.navigation.simplestack

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import com.zhuinden.simplestack.AheadOfTimeWillHandleBackChangedListener
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger.Callback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey

/**
 * A state changer that allows switching between composables, animating the transition.
 *
 * Based on https://github.com/Zhuinden/simple-stack-compose-integration/blob/c5fa2d2d7bec305af93f84d2af0a91f4cdf17227/core/src/main/java/com/zhuinden/simplestackcomposeintegration/core/ComposeIntegrationCore.kt
 */
class ComposeStateChanger(
   private val coroutineScope: CoroutineScope,
   private val interceptBack: Boolean,
) : AsyncStateChanger.NavigationHandler {
   private lateinit var currentStateChange: SeekableTransitionState<StateChangeResult>
   private var saveableStateHolder: SaveableStateHolder? = null
   private var viewModelStores: StoreHolderViewModel? = null

   private lateinit var screenRegistry: ScreenRegistry
   private lateinit var backstack: Backstack

   private var animationJob: Job = Job()

   override fun onNavigationEvent(stateChange: StateChange, completionCallback: Callback) {
      if (!::screenRegistry.isInitialized) {
         backstack = stateChange.backstack
         screenRegistry = NavigationInjection.fromBackstack(stateChange.backstack).screenRegistry()

         currentStateChange = SeekableTransitionState(
            StateChangeResult(
               stateChange.direction,
               stateChange.getNewKeys<ScreenKey>().last(),
               null
            )
         )

         completionCallback.stateChangeComplete()
         return
      }

      animationJob.cancel()
      animationJob = coroutineScope.launch {
         currentStateChange.animateTo(
            StateChangeResult(
               stateChange.direction,
               stateChange.getNewKeys<ScreenKey>().last(),
               null
            )
         )

         saveableStateHolder?.let { saveableStateHolder ->
            viewModelStores?.let { viewModelStores ->
               cleanupStaleSaveStates(
                  stateChange.getPreviousKeys(),
                  stateChange.getNewKeys(),
                  saveableStateHolder,
                  viewModelStores
               )
            }
         }

         completionCallback.stateChangeComplete()
      }
   }

   @Composable
   fun Content(screenWrapper: @Composable (key: ScreenKey, screen: @Composable () -> Unit) -> Unit = { _, screen -> screen() }) {
      val saveableStateHolder = rememberSaveableStateHolder()
      val viewModelStores = viewModel<StoreHolderViewModel>()
      this.saveableStateHolder = saveableStateHolder
      this.viewModelStores = viewModelStores

      PredictiveBackHandler(canUserGoBack()() && interceptBack) { events ->
         if (backstack.isStateChangePending()) {
            // If we are alraedy doing a state change, we cannot interrupt it until it is done
            events.firstOrNull() // Collect the flow once to satisfy the contract
            return@PredictiveBackHandler
         }

         val history = backstack.getHistory<ScreenKey>()
         val oldStateChange = currentStateChange.currentState

         try {
            val targetStateChange =
               StateChangeResult(
                  StateChange.BACKWARD,
                  history.elementAt(history.size - 2),
                  null
               )

            events.collect {
               currentStateChange.seekTo(it.progress, targetStateChange.copy(backSwipeEdge = it.swipeEdge))
            }

            backstack.goBack()
         } catch (e: CancellationException) {
            withContext(NonCancellable) {
               currentStateChange.snapTo(oldStateChange)
            }
            throw e
         }
      }

      val transition = rememberTransition(currentStateChange)

      transition.AnimatedContent(
         transitionSpec = {
            if (targetState.direction == StateChange.BACKWARD) {
               with(initialState.newTopKey) { backAnimation(this@AnimatedContent) }
            } else {
               with(targetState.newTopKey) { forwardAnimation(this@AnimatedContent) }
            }
         },
         contentKey = { it.newTopKey.contentKey() }
      ) { (_, newTopKey) ->
         val contentKey = newTopKey.contentKey()

         saveableStateHolder.SaveableStateProvider(contentKey) {
            viewModelStores.WithLocalViewModelStore(contentKey) {
               LocalDestroyedLifecycle {
                  screenWrapper(newTopKey) {
                     ShowScreen(newTopKey)
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
      previousKeys: List<ScreenKey>,
      newKeys: List<ScreenKey>,
      saveableStateHolder: SaveableStateHolder,
      viewModelStores: StoreHolderViewModel
   ) {
      previousKeys.fastForEach { previousKey ->
         if (!newKeys.contains(previousKey)) {
            saveableStateHolder.removeState(previousKey)
            viewModelStores.removeKey(previousKey)
         }
      }
   }

   private fun ScreenKey.contentKey(): Any {
      return if (this is SingleTopKey && isSingleTop) {
         this.javaClass.name
      } else {
         this
      }
   }

   @Composable
   private fun canUserGoBack(): () -> Boolean {
      val backButtonEnabled = remember { mutableStateOf(false) }

      DisposableEffect(backstack) {
         backButtonEnabled.value = backstack.willHandleAheadOfTimeBack()

         val listener = AheadOfTimeWillHandleBackChangedListener {
            backButtonEnabled.value = it
         }

         backstack.addAheadOfTimeWillHandleBackChangedListener(listener)

         onDispose {
            backstack.removeAheadOfTimeWillHandleBackChangedListener(listener)
         }
      }

      return backButtonEnabled::value
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
