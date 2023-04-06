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

package si.inova.kotlinova.navigation.sample.nested

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.di.MainNavigation
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.sample.keys.MainScreenKey
import si.inova.kotlinova.navigation.screenkeys.NoArgsScreenKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen

class NestedRootScreen(
   private val navigator: Navigator,
   @MainNavigation
   private val mainNavigator: Navigator
) : Screen<NestedRootScreenKey>() {
   @Composable
   override fun Content(key: NestedRootScreenKey) {
      Column {
         Text("This is nested navigation")

         Button(onClick = { navigator.navigateTo(MainScreenKey) }) {
            Text("Open main screen inside nested container")
         }

         Button(onClick = { mainNavigator.goBack() }) {
            Text("Go back with main navigation")
         }
      }
   }
}

@Parcelize
object NestedRootScreenKey : NoArgsScreenKey()
