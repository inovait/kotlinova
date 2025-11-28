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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.conditions.ConditionalNavigationHandler
import si.inova.kotlinova.navigation.conditions.NavigationCondition
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.OpenScreen
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.testutils.BlankScreenKey
import si.inova.kotlinova.navigation.testutils.insertTestNavigation

class ConditionalNavigationTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun directlyNavigateWhenConditionIsMet() {
      TestConditionHandler.meetsCondition = true

      val backstack = rule.insertTestNavigation(BlankScreenKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         navigator.navigateTo(TargetScreenKey)
      }

      rule.onNodeWithText("Target screen").assertIsDisplayed()
      backstack.getHistory<ScreenKey>().shouldContainExactly(BlankScreenKey, TargetScreenKey)
   }

   @Test
   internal fun redirectToConditionResolvingScreenIfConditionIsNotMet() {
      TestConditionHandler.meetsCondition = false

      val backstack = rule.insertTestNavigation(BlankScreenKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()

      rule.runOnUiThread {
         navigator.navigateTo(TargetScreenKey)
      }

      rule.onNodeWithText("Condition Resolver").assertIsDisplayed()
      backstack.getHistory<ScreenKey>().shouldContainExactly(
         BlankScreenKey,
         ConditionResolvingScreenKey(OpenScreen(TargetScreenKey))
      )
   }

   @Parcelize
   object TestCondition : NavigationCondition

   object TestConditionHandler : ConditionalNavigationHandler {
      var meetsCondition: Boolean = false

      override fun getNavigationRedirect(
         condition: NavigationCondition,
         navigateToIfConditionMet: NavigationInstruction
      ): NavigationInstruction {
         return if (meetsCondition) {
            navigateToIfConditionMet
         } else {
            OpenScreen(ConditionResolvingScreenKey(navigateToIfConditionMet))
         }
      }
   }

   @Parcelize
   data object TargetScreenKey : ScreenKey() {
      override val navigationConditions: List<NavigationCondition>
         get() = listOf(TestCondition)
   }

   @InjectNavigationScreen
   class TargetScreen : Screen<TargetScreenKey>() {
      @Composable
      override fun Content(key: TargetScreenKey) {
         Text("Target screen")
      }
   }

   @Parcelize
   data class ConditionResolvingScreenKey(val targetNavigation: NavigationInstruction) : ScreenKey()

   @InjectNavigationScreen
   class ConditionResolvingScreen : Screen<ConditionResolvingScreenKey>() {
      @Composable
      override fun Content(key: ConditionResolvingScreenKey) {
         Text("Condition Resolver")
      }
   }

   @ContributesTo(OuterNavigationScope::class)
   interface ConditionTestProviders {
      @Provides
      @IntoMap
      @ClassKey(TestCondition::class)
      fun provideTestCondition(): ConditionalNavigationHandler =
         TestConditionHandler
   }
}
