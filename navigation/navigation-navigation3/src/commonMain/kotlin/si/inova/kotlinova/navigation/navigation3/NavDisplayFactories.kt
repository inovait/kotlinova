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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import si.inova.kotlinova.navigation.backstack.Backstack
import si.inova.kotlinova.navigation.backstack.BackstackProvider
import si.inova.kotlinova.navigation.backstack.rememberBackstack
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import kotlin.jvm.JvmSuppressWildcards

@Composable
fun NavigationInjection.Factory.NavDisplay(
   initialHistory: () -> List<ScreenKey>,
   modifier: Modifier = Modifier,
   generateExtraNavEntryMetadata: ((ScreenKey) -> Map<String, String>) = { emptyMap() },
   contentAlignment: Alignment = Alignment.TopStart,
   entryDecorators: List<NavEntryDecorator<ScreenKey>> =
      listOf(rememberSaveableStateHolderNavEntryDecorator()),
   sceneStrategy: SceneStrategy<ScreenKey> = SinglePaneSceneStrategy(),
   sizeTransform: SizeTransform? = null,
): Backstack {
   val backstack = rememberBackstack { initialHistory() }

   val entryProvider = backstack.rememberNavigation3EntryProvider(
      generateExtraNavEntryMetadata
   )

   entryProvider.NavDisplay(
      modifier,
      contentAlignment,
      entryDecorators,
      sceneStrategy,
      sizeTransform,
   )

   return backstack
}

@Composable
fun Navigation3EntryProvider.NavDisplay(
   modifier: Modifier = Modifier,
   contentAlignment: Alignment = Alignment.TopStart,
   entryDecorators: List<NavEntryDecorator<out ScreenKey>> =
      listOf(rememberSaveableStateHolderNavEntryDecorator()),
   sceneStrategy: SceneStrategy<ScreenKey> = SinglePaneSceneStrategy(),
   sizeTransform: SizeTransform? = null,
) {
   BackstackProvider(this.backstack) {
      LaunchedEffect(this.backstack) {
         processBackstack()
      }
      val backstackEntries = navEntries.collectAsState().value

      @Suppress("UNCHECKED_CAST")
      val decoratedNavEntries =
         rememberDecoratedNavEntries(
            backstackEntries as List<NavEntry<ScreenKey>>,
            entryDecorators as List<@JvmSuppressWildcards NavEntryDecorator<ScreenKey>>
         )

      NavDisplay(
         entries = decoratedNavEntries,
         sceneStrategy = sceneStrategy,
         modifier = modifier,
         contentAlignment = contentAlignment,
         sizeTransform = sizeTransform,
         transitionSpec = fun AnimatedContentTransitionScope<Scene<ScreenKey>>.(): ContentTransform {
            return targetState.entries.last().key().forwardAnimation(this)
         },
         popTransitionSpec = fun AnimatedContentTransitionScope<Scene<ScreenKey>>.(): ContentTransform {
            return initialState.entries.last().key().backAnimation(this, null)
         },
         predictivePopTransitionSpec = fun AnimatedContentTransitionScope<Scene<ScreenKey>>.(it: Int): ContentTransform {
            return initialState.entries.last().key().backAnimation(this, it)
         },
         onBack = { this.backstack.run { updateBackstack(backstack.value.dropLast(1)) } },
      )
   }
}
