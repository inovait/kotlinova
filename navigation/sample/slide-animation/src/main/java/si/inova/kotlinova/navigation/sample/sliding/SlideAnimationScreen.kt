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

package si.inova.kotlinova.navigation.sample.sliding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import si.inova.kotlinova.navigation.sample.keys.SlideAnimationScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class SlideAnimationScreen(
   private val viewModel: SlideAnimationScreenViewModel
) : Screen<SlideAnimationScreenKey>() {
   @Composable
   override fun Content(key: SlideAnimationScreenKey) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
         Text("Received argument \"${key.argument}\"")
         Text("Animations of this screen are overriden in MainScreenKey.")

         Button(onClick = { viewModel.goBack() }) {
            Text("Go back (called through ViewModel)")
         }
      }
   }
}
