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

package si.inova.kotlinova.navigation.sample.tabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.sample.keys.TabScreenKey
import si.inova.kotlinova.navigation.sample.tabs.subscreens.SubscreenA
import si.inova.kotlinova.navigation.sample.tabs.subscreens.SubscreenB
import si.inova.kotlinova.navigation.sample.tabs.subscreens.SubscreenC
import si.inova.kotlinova.navigation.screens.Screen

class TabScreen(
   private val navigator: Navigator,
   private val subscreenA: SubscreenA,
   private val subscreenB: SubscreenB,
   private val subscreenC: SubscreenC,
) : Screen<TabScreenKey>() {
   @OptIn(ExperimentalAnimationApi::class)
   @Composable
   override fun Content(key: TabScreenKey) {
      Column {
         val selectedTab = key.selectedTab
         TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
               selected = selectedTab == TabScreenKey.SelectedTab.A,
               onClick = { navigator.navigateTo(key.copy(selectedTab = TabScreenKey.SelectedTab.A)) },
               text = { Text("Sub-screen A") }
            )
            Tab(
               selected = selectedTab == TabScreenKey.SelectedTab.B,
               onClick = { navigator.navigateTo(key.copy(selectedTab = TabScreenKey.SelectedTab.B)) },
               text = { Text("Sub-screen B") }
            )
            Tab(
               selected = selectedTab == TabScreenKey.SelectedTab.C,
               onClick = { navigator.navigateTo(key.copy(selectedTab = TabScreenKey.SelectedTab.C)) },
               text = { Text("Sub-screen C") }
            )
         }

         AnimatedContent(selectedTab,
            transitionSpec = {
               if (targetState.ordinal > initialState.ordinal) {
                  slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) togetherWith
                     slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
               } else {
                  slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) togetherWith
                     slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
               }
            }) {
            val targetScreen = when (it) {
               TabScreenKey.SelectedTab.A -> subscreenA
               TabScreenKey.SelectedTab.B -> subscreenB
               TabScreenKey.SelectedTab.C -> subscreenC
            }

            targetScreen.Content(key)
         }

         Text(
            "You can also swap tabs with deep links: \n\n" +
               "   navigationdemo://tabs/a\n" +
               "   navigationdemo://tabs/b\n" +
               "   navigationdemo://tabs/c\n\n" +
               "For example: \n" +
               "   adb shell am start \"navigationdemo://tabs/a\"",
            fontSize = 10.sp
         )
      }
   }
}
