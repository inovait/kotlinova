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

package si.inova.kotlinova.navigation.tests

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.kotest.matchers.shouldBe
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.testutils.insertTestNavigation

private var numScreenCreations = 0

class SingleTopNavigation {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun navigateWhileKeepingSingleTop() {
      val backstack = rule.insertTestNavigation(SingleTopScreenKey("A"))
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.onNodeWithText("A").assertIsDisplayed()

      rule.runOnUiThread {
         navigator.navigateTo(SingleTopScreenKey("B"))
      }
      rule.onNodeWithText("B").assertIsDisplayed()

      rule.runOnUiThread {
         navigator.navigateTo(SingleTopScreenKey("C"))
      }
      rule.onNodeWithText("C").assertIsDisplayed()

      numScreenCreations shouldBe 1
   }

   @Parcelize
   data class SingleTopScreenKey(val text: String) : SingleTopKey()

   @InjectNavigationScreen
   class SingleTopScreen : Screen<SingleTopScreenKey>() {
      init {
         numScreenCreations++
      }

      @Composable
      override fun Content(key: SingleTopScreenKey) {
         Text(key.text)
      }
   }
}
