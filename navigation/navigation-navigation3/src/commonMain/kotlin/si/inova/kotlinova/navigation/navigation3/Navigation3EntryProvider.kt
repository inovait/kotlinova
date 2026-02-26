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
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.DialogSceneStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.navigation.backstack.Backstack
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.ScreenRegistry
import si.inova.kotlinova.navigation.screenkeys.DialogKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey
import si.inova.kotlinova.navigation.screens.Screen

/**
 * Provider for the [backstackEntries] that can be supplied directly to the NavDisplay
 */
class Navigation3EntryProvider(
   val backstack: Backstack,
   private val screenRegistry: ScreenRegistry,
   private val generateExtraNavEntryMetadata: ((ScreenKey) -> Map<String, String>) = { emptyMap() },
) {
   private val currentNavEntries = ArrayList<NavEntry<out ScreenKey>>()

   private val _navEntries = MutableStateFlow<List<NavEntry<out ScreenKey>>>(
      run {
         handleNewBackstack(backstack.backstack.value)
         ArrayList(currentNavEntries)
      }
   )
   val navEntries: StateFlow<List<NavEntry<out ScreenKey>>>
      get() = _navEntries

   suspend fun processBackstack() {
      backstack.backstack.collect { newKeys ->
         handleNewBackstack(newKeys)

         _navEntries.value = ArrayList(currentNavEntries)
      }
   }

   private fun handleNewBackstack(newKeys: List<ScreenKey>) {
      val matchingKeys = newKeys.withIndex().takeWhile { (index, it) -> currentNavEntries.elementAtOrNull(index)?.key() == it }
      val oldTop = currentNavEntries.lastOrNull()

      // Drop everything after matching keys
      currentNavEntries.subList(matchingKeys.size, currentNavEntries.size).clear()

      val addedKeys = newKeys.drop(matchingKeys.size)
      addedKeys.forEachIndexed { index, key ->
         if (key is SingleTopKey && index == addedKeys.lastIndex && key::class == oldTop?.key()?.let { it::class }) {
            // Single top key, copy previous Screen instance
            @Suppress("UNCHECKED_CAST")
            currentNavEntries.add(createNavEntry(key, oldTop.metadata.getValue(METADATA_SCREEN) as Screen<ScreenKey>))
         } else {
            currentNavEntries.add(createNavEntry(key))
         }
      }
   }

   private fun createNavEntry(
      key: ScreenKey,
      screen: Screen<ScreenKey> = screenRegistry.createScreen(key),
   ): NavEntry<ScreenKey> {
      return NavEntry(
         key,
         contentKey = if (key is SingleTopKey) {
            key::class.qualifiedName ?: error("Unnamed classes are not supported")
         } else {
            key.toString()
         },
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
}

@Composable
fun Backstack.rememberNavigation3EntryProvider(
   generateExtraNavEntryMetadata: ((ScreenKey) -> Map<String, String>) = { emptyMap() },
): Navigation3EntryProvider {
   val entryProvider = remember(this) {
      Navigation3EntryProvider(
         this,
         NavigationInjection.fromBackstack(this).screenRegistry(),
         generateExtraNavEntryMetadata,
      )
   }

   return entryProvider
}

fun NavEntry<out ScreenKey>.key(): ScreenKey {
   return metadata.getValue(METADATA_KEY) as ScreenKey
}

const val METADATA_KEY = "KEY"
private const val METADATA_SCREEN = "SCREEN"
