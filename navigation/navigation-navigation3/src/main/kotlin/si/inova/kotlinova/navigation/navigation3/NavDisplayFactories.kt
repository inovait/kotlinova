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

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.zhuinden.simplestack.Backstack
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.simplestack.BackstackProvider

@Composable
@SuppressLint("ComposableNaming") // Backstack return is only there as a convenience, it's mostly meant to be used without return
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
   val entryProvider = rememberNavigation3EntryProvider(initialHistory, generateExtraNavEntryMetadata)

   entryProvider.NavDisplay(
      modifier,
      contentAlignment,
      entryDecorators,
      sceneStrategy,
      sizeTransform,
   )

   return entryProvider.simpleStackBackstack
}

@Composable
fun Navigation3EntryProvider.NavDisplay(
   modifier: Modifier = Modifier,
   contentAlignment: Alignment = Alignment.TopStart,
   entryDecorators: List<NavEntryDecorator<ScreenKey>> =
      listOf(rememberSaveableStateHolderNavEntryDecorator()),
   sceneStrategy: SceneStrategy<ScreenKey> = SinglePaneSceneStrategy(),
   sizeTransform: SizeTransform? = null,
) {
   BackstackProvider(this.simpleStackBackstack) {
      val decoratedNavEntries =
         rememberDecoratedNavEntries(this.backstackEntries, entryDecorators)

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
         onBack = { this.simpleStackBackstack.goBack() },
      )
   }
}
