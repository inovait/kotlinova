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

package si.inova.kotlinova.navigation.tests

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.testutils.insertTestNavigation

class BasicNavigationTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun showScreen() {
      rule.insertTestNavigation(TestScreenAKey("Hello"))

      rule.onNodeWithText("Hello").assertIsDisplayed()
   }

   @Test
   internal fun navigateToAnotherScreen() {
      rule.insertTestNavigation(TestScreenAKey("Hello"))

      rule.onNodeWithText("Navigate").performClick()
      rule.onNodeWithText("World").assertIsDisplayed()
   }

   @Test
   internal fun navigateToAnotherScreenAndGoBack() {
      rule.insertTestNavigation(TestScreenAKey("Hello"))

      rule.onNodeWithText("Navigate").performClick()
      rule.onNodeWithText("Go back").performClick()
      rule.onNodeWithText("Hello").assertIsDisplayed()
   }

   @Parcelize
   data class TestScreenAKey(val text: String) : ScreenKey()
   class TestScreenA(
      private val navigator: Navigator
   ) : Screen<TestScreenAKey>() {
      @Composable
      override fun Content(key: TestScreenAKey) {
         Column {
            Text(key.text)
            Button(onClick = { navigator.navigateTo(TestScreenBKey("World")) }) {
               Text("Navigate")
            }
         }
      }
   }

   @Parcelize
   data class TestScreenBKey(val text: String) : ScreenKey()
   class TestScreenB(
      private val navigator: Navigator
   ) : Screen<TestScreenBKey>() {
      @Composable
      override fun Content(key: TestScreenBKey) {
         Column {
            Text(key.text)
            Button(onClick = { navigator.goBack() }) {
               Text("Go back")
            }
         }
      }
   }
}
