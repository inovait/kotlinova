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

package si.inova.kotlinova.navigation.sample.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.sample.keys.DemoFragmentScreenKey
import si.inova.kotlinova.navigation.sample.keys.MainScreenKey
import si.inova.kotlinova.navigation.sample.keys.NestedScreenKey
import si.inova.kotlinova.navigation.sample.keys.RootConditionalNavigationScreenKey
import si.inova.kotlinova.navigation.sample.keys.SharedViewModelScreenKey
import si.inova.kotlinova.navigation.sample.keys.SlideAnimationScreenKey
import si.inova.kotlinova.navigation.sample.keys.TabScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.util.Date

@InjectNavigationScreen
class MainScreen(
   private val viewModel: MainScreenViewModel,
   private val navigator: Navigator
) : Screen<MainScreenKey>() {
   @Composable
   override fun Content(key: MainScreenKey) {
      Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
         Text("Hello from navigation")
         CurrentTime(viewModel.currentTime)

         NavigationButtons(navigator)

         Text(
            "You can also navigate using deep links: \n\n" +
               "   navigationdemo://slide/{argument}\n" +
               "   navigationdemo://tabs\n" +
               "   navigationdemo://nested\n" +
               "   navigationdemo://fragment/{argument}\n" +
               "   navigationdemo://postlogin\n" +
               "\nFor example: \n" +
               "   adb shell am start \"navigationdemo://slide/Hello\"",
            fontSize = 10.sp
         )

      }
   }
}

@Composable
private fun CurrentTime(flow: StateFlow<Date?>) {
   val time = flow.collectAsStateWithLifecycle().value

   Text("Current time is $time")
}

@Composable
fun NavigationButtons(navigator: Navigator) {
   Button(onClick = { navigator.navigateTo(SlideAnimationScreenKey("Hello")) }) {
      Text("Screen with custom animation with \"Hello\" Argument")
   }

   Button(onClick = { navigator.navigateTo(SlideAnimationScreenKey("World")) }) {
      Text("Screen with custom animation with \"World\" Argument")
   }

   Button(onClick = { navigator.navigateTo(TabScreenKey()) }) {
      Text("Tabs")
   }

   Button(onClick = { navigator.navigateTo(NestedScreenKey) }) {
      Text("Nested screen")
   }

   Button(onClick = { navigator.navigateTo(DemoFragmentScreenKey(42)) }) {
      Text("Fragment")
   }

   Button(onClick = { navigator.navigateTo(RootConditionalNavigationScreenKey) }) {
      Text("Conditional navigation")
   }

   Button(onClick = { navigator.navigateTo(SharedViewModelScreenKey()) }) {
      Text("Shared scoped services")
   }
}
