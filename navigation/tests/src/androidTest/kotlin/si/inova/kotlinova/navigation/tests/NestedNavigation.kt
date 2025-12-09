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

package si.inova.kotlinova.navigation.tests

import android.annotation.SuppressLint
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.conditions.NavigationCondition
import si.inova.kotlinova.navigation.di.MainNavigation
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.navigation3.NestedBackstackScreen
import si.inova.kotlinova.navigation.navigation3.NestedNavigationScreenKey
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.NoArgsScreenKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.Inherited
import si.inova.kotlinova.navigation.testutils.insertTestNavigation

class NestedNavigation {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun navigateNestedScreens() {
      val backstack = rule.insertTestNavigation(
         NestedNavigationScreenKey(listOf(BasicNavigationTest.TestScreenAKey("Hello")))
      )

      rule.onNodeWithText("Navigate").performClick()
      rule.onNodeWithText("World").assertIsDisplayed()

      backstack.getHistory<ScreenKey>().shouldContainExactly(
         NestedNavigationScreenKey(listOf(BasicNavigationTest.TestScreenAKey("Hello")))
      )
   }

   @Test
   internal fun navigateMainNavigationFromNestedScreen() {
      rule.insertTestNavigation(
         BasicNavigationTest.TestScreenAKey("Hello"),
         NestedNavigationScreenKey(listOf(NestedScreenGoingBackKey))
      )

      rule.onNodeWithText("Go back").performClick()
      rule.onNodeWithText("Hello").assertIsDisplayed()
   }

   @Test
   internal fun shareScopedServicesBetweenParentAndNestedScreen() {
      rule.insertTestNavigation(ParentScreenWithSharedServiceKey())

      rule.onNodeWithText("333").assertIsDisplayed()
   }

   @Test
   internal fun onlyInheritServicesFromTheParentScreen() {
      val backstack = rule.insertTestNavigation(ParentScreenWithSharedServiceKey())

      rule.runOnUiThread {
         backstack.goTo(ParentScreenWithSharedServiceKey(666))
      }

      rule.waitForIdle()

      rule.runOnUiThread {
         backstack.goBack()
      }

      rule.waitForIdle()
      Thread.sleep(1000)

      rule.onNodeWithText("333").assertIsDisplayed()
   }

   @Test
   internal fun preserveInheritanceAfterProcessKill() {
      val stateRestorationTester = StateRestorationTester(rule)
      stateRestorationTester.insertTestNavigation(rule, ParentScreenWithSharedServiceKey())

      rule.waitForIdle()
      stateRestorationTester.emulateSavedInstanceStateRestore()

      rule.onNodeWithText("333").assertIsDisplayed()
   }

   @Parcelize
   data object NestedScreenGoingBackKey : ScreenKey() {
      override val navigationConditions: List<NavigationCondition>
         get() = listOf(ConditionalNavigationTest.TestCondition)
   }

   @InjectNavigationScreen
   class NestedScreenGoingBack(
      @MainNavigation
      private val mainNavigator: Navigator
   ) : Screen<NestedScreenGoingBackKey>() {
      @Composable
      override fun Content(key: NestedScreenGoingBackKey) {
         Button(onClick = { mainNavigator.goBack() }) {
            Text("Go back")
         }
      }
   }

   @Parcelize
   data class ParentScreenWithSharedServiceKey(val number: Int = 333) : NoArgsScreenKey()

   @InjectNavigationScreen
   class ParentScreenWithSharedService(
      private val service: ServiceScopes.SharedService,
      private val nestedScreen: NestedBackstackScreen
   ) : Screen<ParentScreenWithSharedServiceKey>() {
      @SuppressLint("StateFlowValueCalledInComposition")
      @Composable
      override fun Content(key: ParentScreenWithSharedServiceKey) {
         service.number.value = key.number

         nestedScreen.Content(NestedNavigationScreenKey(listOf(ChildScreenWithSharedServiceKey)))
      }
   }

   @Parcelize
   data object ChildScreenWithSharedServiceKey : ScreenKey()

   @InjectNavigationScreen
   class ChildScreenWithSharedService(
      @Inherited
      private val service: ServiceScopes.SharedService
   ) : Screen<ChildScreenWithSharedServiceKey>() {
      @Composable
      override fun Content(key: ChildScreenWithSharedServiceKey) {
         Text(service.number.collectAsState().value.toString())
      }
   }
}
