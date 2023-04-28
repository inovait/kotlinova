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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screenkeys.NoArgsScreenKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.testutils.insertTestNavigation
import javax.inject.Inject

class AbstractScreens {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun showScreenAbstractScreenWithSeparateImplementation() {
      rule.insertTestNavigation(OuterScreenReferencingAbstractScreenKey)

      rule.onNodeWithText("Hello from Inner Screen").assertIsDisplayed()
   }

   @Parcelize
   object OuterScreenReferencingAbstractScreenKey : NoArgsScreenKey()

   class OuterScreenReferencingAbstractScreen(
      private val innerScreen: TestAbstractScreen
   ) : Screen<OuterScreenReferencingAbstractScreenKey>() {
      @Composable
      override fun Content(key: OuterScreenReferencingAbstractScreenKey) {
         Column {
            innerScreen.Content(key)
         }
      }
   }

   abstract class TestAbstractScreen : Screen<ScreenKey>()

   @ContributesScreenBinding
   class TestAbstractScreenImpl @Inject constructor() : TestAbstractScreen() {
      @Composable
      override fun Content(key: ScreenKey) {
         Column {
            Text("Hello from Inner Screen")
         }
      }
   }
}
