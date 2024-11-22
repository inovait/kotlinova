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

package si.inova.kotlinova.navigation.sample.sharedtransition

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.sample.common.LocalSharedTransitionScope
import si.inova.kotlinova.navigation.sample.keys.SharedTransitionDetailsScreenKey
import si.inova.kotlinova.navigation.sample.keys.SharedTransitionListScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.simplestack.LocalAnimatedVisibilityScope

@OptIn(ExperimentalSharedTransitionApi::class)
@InjectNavigationScreen
class SharedTransitionListScreen(private val navigator: Navigator) : Screen<SharedTransitionListScreenKey>() {
   @Composable
   override fun Content(key: SharedTransitionListScreenKey) = with(LocalSharedTransitionScope.current) {
      LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.fillMaxSize()) {
         itemsIndexed(SHARED_TRANSITION_COLORS) { index, color ->
            Box(
               Modifier
                  .sharedElement(rememberSharedContentState("color-${index}"), LocalAnimatedVisibilityScope.current)
                  .size(150.dp)
                  .background(color)
                  .clickable {
                     navigator.navigateTo(SharedTransitionDetailsScreenKey(index))
                  }
            )
         }
      }
   }
}
