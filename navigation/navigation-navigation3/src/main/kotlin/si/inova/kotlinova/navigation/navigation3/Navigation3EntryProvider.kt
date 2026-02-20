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

package si.inova.kotlinova.navigation.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.DialogSceneStrategy
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.DialogKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.simplestack.rememberBackstack

/**
 * Provider for the [backstackEntries] that can be supplied directly to the NavDisplay
 */
class Navigation3EntryProvider(
   private val generateExtraNavEntryMetadata: ((ScreenKey) -> Map<String, String>) = { emptyMap() },
) : StateChanger {
   lateinit var simpleStackBackstack: Backstack
      private set

   private lateinit var screenRegistry: ScreenRegistry
   private var initialized: Boolean = false

   private val _backstackEntries = mutableStateListOf<NavEntry<ScreenKey>>()
   val backstackEntries: List<NavEntry<ScreenKey>> get() = _backstackEntries

   private fun createNavEntry(
      key: ScreenKey,
      screen: Screen<ScreenKey> = screenRegistry.createScreen(key),
   ): NavEntry<ScreenKey> {
      return NavEntry(
         key,
         contentKey = if (key is SingleTopKey) key.javaClass.name else key,
         metadata = buildMap {
            put(METADATA_KEY, key)
            put(METADATA_SCREEN, screen)
            if (key is DialogKey) {
               putAll(DialogSceneStrategy.dialog(key.dialogProperties))
            }

            putAll(generateExtraNavEntryMetadata(key))
         }
      ) {
         screen.Content(key)
      }
   }

   private fun initIfNeeded(backstack: Backstack) {
      if (initialized) {
         return
      }
      this.simpleStackBackstack = backstack

      val navigationInjection = NavigationInjection.fromBackstack(backstack)
      screenRegistry = navigationInjection.screenRegistry()

      initialized = true
   }

   override fun handleStateChange(
      stateChange: StateChange,
      completionCallback: StateChanger.Callback,
   ) {
      initIfNeeded(stateChange.backstack)

      val newKeys = stateChange.getNewKeys<ScreenKey>()

      val matchingKeys = newKeys.withIndex().takeWhile { (index, it) -> _backstackEntries.elementAtOrNull(index)?.key() == it }
      val oldTop = _backstackEntries.lastOrNull()

      // Drop everything after matching keys
      _backstackEntries.removeRange(matchingKeys.size, _backstackEntries.size)

      val addedKeys = newKeys.drop(matchingKeys.size)
      addedKeys.forEachIndexed { index, key ->
         if (key is SingleTopKey && index == addedKeys.lastIndex && key.javaClass == oldTop?.key()?.javaClass) {
            // Single top key, copy previous Screen instance
            @Suppress("UNCHECKED_CAST")
            _backstackEntries.add(createNavEntry(key, oldTop.metadata.getValue(METADATA_SCREEN) as Screen<ScreenKey>))
         } else {
            _backstackEntries.add(createNavEntry(key))
         }
      }

      completionCallback.stateChangeComplete()
   }
}

@Composable
fun NavigationInjection.Factory.rememberNavigation3EntryProvider(
   initialHistory: () -> List<ScreenKey>,
   generateExtraNavEntryMetadata: ((ScreenKey) -> Map<String, String>) = { emptyMap() },
): Navigation3EntryProvider {
   val entryProvider = remember { Navigation3EntryProvider(generateExtraNavEntryMetadata) }

   rememberBackstack(entryProvider) { initialHistory() }

   return entryProvider
}

fun NavEntry<ScreenKey>.key(): ScreenKey {
   return metadata.getValue(METADATA_KEY) as ScreenKey
}

const val METADATA_KEY = "KEY"
private const val METADATA_SCREEN = "SCREEN"
