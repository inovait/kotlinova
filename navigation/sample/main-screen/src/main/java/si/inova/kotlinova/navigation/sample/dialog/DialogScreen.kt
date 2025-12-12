/*
 * Copyright 2025 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.sample.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import si.inova.kotlinova.compose.result.LocalResultPassingStore
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.sample.keys.DemoDialogScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class DialogScreen(
   private val navigator: Navigator
) : Screen<DemoDialogScreenKey>() {
   @Composable
   override fun Content(key: DemoDialogScreenKey) {
      Surface {
         Column(
            modifier = Modifier
               .fillMaxWidth()
               .padding(24.dp)
         ) {
            Text("A Dialog", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
            Text("Select Yes or No", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 16.dp))

            val resultPassingStore = LocalResultPassingStore.current

            Row(Modifier.align(Alignment.End)) {
               TextButton(
                  onClick = {
                     resultPassingStore.sendResult(key.resultKey, "Yes")
                     navigator.goBack()
                  }
               ) { Text("Yes") }

               TextButton(
                  onClick = {
                     resultPassingStore.sendResult(key.resultKey, "No")
                     navigator.goBack()
                  }
               ) { Text("No") }
            }
         }
      }
   }
}
