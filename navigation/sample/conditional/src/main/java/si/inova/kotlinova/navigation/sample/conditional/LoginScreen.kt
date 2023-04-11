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

package si.inova.kotlinova.navigation.sample.conditional

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.instructions.GoBack
import si.inova.kotlinova.navigation.instructions.MultiNavigationInstructions
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.sample.keys.RootConditionalNavigationScreenKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen

class LoginScreen(
   private val loginRepository: LoginRepository,
   private val navigator: Navigator
) : Screen<LoginScreenKey>() {
   @Composable
   override fun Content(key: LoginScreenKey) {
      Column {
         Text("Login screen")

         Button(onClick = {
            loginRepository.setUserLoggedIn(true)
            // Close login screen and then navigate to the target screen
            navigator.navigate(MultiNavigationInstructions(GoBack, key.postLoginNavigation))
         }) {
            Text("Login")
         }

         // We cannot use navigator.goBack() here, because if this screen is opened via deep link, it is on the root
         // and going back from root should close the activity (which navigator.goBack() cannot do)
         // Instead, call back dispatcher directly and simulate the back button.
         val backDispatcher = LocalOnBackPressedDispatcherOwner.current
         Button(onClick = { backDispatcher!!.onBackPressedDispatcher.onBackPressed() }) {
            Text("Go back")
         }
      }
   }
}

@Parcelize
data class LoginScreenKey(val postLoginNavigation: NavigationInstruction) : ScreenKey()
