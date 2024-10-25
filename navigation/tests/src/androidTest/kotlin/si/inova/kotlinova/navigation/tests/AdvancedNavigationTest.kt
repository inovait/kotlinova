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

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.instructions.ClearBackstackAnd
import si.inova.kotlinova.navigation.instructions.MultiNavigationInstructions
import si.inova.kotlinova.navigation.instructions.OpenScreen
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.instructions.navigateToSingle
import si.inova.kotlinova.navigation.screenkeys.NoArgsScreenKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.testutils.BlankScreenKey
import si.inova.kotlinova.navigation.testutils.insertTestNavigation

class AdvancedNavigationTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun clearBackstackAnd() {
      val backstack = rule.insertTestNavigation(BlankScreenKey, AdvancedTestScreenBKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         navigator.navigate(ClearBackstackAnd(OpenScreen(AdvancedTestScreenAKey)))
      }

      rule.waitForIdle()
      backstack.getHistory<ScreenKey>().shouldContainExactly(AdvancedTestScreenAKey)
   }

   @Test
   internal fun multiNavigationInstruction() {
      val backstack = rule.insertTestNavigation(BlankScreenKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         navigator.navigate(
            MultiNavigationInstructions(
               OpenScreen(AdvancedTestScreenAKey),
               OpenScreen(AdvancedTestScreenBKey)
            )
         )
      }

      rule.waitForIdle()
      backstack.getHistory<ScreenKey>().shouldContainExactly(
         BlankScreenKey,
         AdvancedTestScreenAKey,
         AdvancedTestScreenBKey
      )
   }

   @Test
   internal fun replaceBackstack() {
      val backstack = rule.insertTestNavigation(AdvancedTestScreenBKey, BlankScreenKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         navigator.navigate(
            ReplaceBackstack(
               AdvancedTestScreenBKey
            )
         )
      }

      rule.waitForIdle()
      backstack.getHistory<ScreenKey>().shouldContainExactly(
         AdvancedTestScreenBKey
      )
   }

   @Test
   internal fun replaceCurrentScreen() {
      val backstack = rule.insertTestNavigation(BlankScreenKey, AdvancedTestScreenAKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         navigator.navigate(
            ReplaceBackstack(
               AdvancedTestScreenBKey
            )
         )
      }

      rule.waitForIdle()
      backstack.getHistory<ScreenKey>().shouldContainExactly(
         AdvancedTestScreenBKey
      )
   }

   @Test
   fun navigateToSingle() {
      val backstack = rule.insertTestNavigation(AdvancedTestScreenBKey, AdvancedTestScreenAKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         navigator.navigateToSingle(AdvancedTestScreenBKey)
      }

      rule.waitForIdle()
      backstack.getHistory<ScreenKey>().shouldContainExactly(
         AdvancedTestScreenAKey,
         AdvancedTestScreenBKey
      )
   }

   @Test
   fun disallowNavigationToTheSameScreenTwice() {
      val backstack = rule.insertTestNavigation(BlankScreenKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         shouldThrow<IllegalStateException> {
            navigator.navigateTo(BlankScreenKey)
         }
      }
   }

   @Parcelize
   object AdvancedTestScreenAKey : NoArgsScreenKey()

   @InjectNavigationScreen
   class AdvancedTestScreenA : Screen<AdvancedTestScreenAKey>() {
      @Composable
      override fun Content(key: AdvancedTestScreenAKey) {
      }
   }

   @Parcelize
   object AdvancedTestScreenBKey : NoArgsScreenKey()

   @InjectNavigationScreen
   class AdvancedTestScreenB : Screen<AdvancedTestScreenBKey>() {
      @Composable
      override fun Content(key: AdvancedTestScreenBKey) {
      }
   }
}
