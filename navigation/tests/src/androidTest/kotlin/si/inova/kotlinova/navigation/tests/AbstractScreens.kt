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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.zacsweers.metro.Inject
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.testutils.insertTestNavigation

class AbstractScreens {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun showScreenContainingAbstractScreenWithSeparateImplementation() {
      rule.insertTestNavigation(OuterScreenReferencingAbstractScreenKey)

      rule.onNodeWithText("Hello from Inner Screen").assertIsDisplayed()
   }

   @Test
   internal fun showScreenContainingAbstractScreenWithSeparateImplementationThatRequiresAService() {
      rule.insertTestNavigation(OuterScreenReferencingAbstractScreenWithServiceKey)

      rule.onNodeWithText("Hello from Inner Screen").assertIsDisplayed()
   }

   @Test
   internal fun showScreenContainingGenericScreenReferenceWithCustomKey() {
      rule.insertTestNavigation(OuterScreenReferencingGenericScreenKey)

      rule.onNodeWithText("Hello from Inner Screen").assertIsDisplayed()
   }

   @Test
   internal fun showScreenContainingGenericScreenReferenceWithCustomKeyAndExplicitBoundType() {
      rule.insertTestNavigation(OuterScreenReferencingGenericScreenWithExplicitBoundTypeKey)

      rule.onNodeWithText("Hello from Inner Screen").assertIsDisplayed()
   }

   @Parcelize
   data object OuterScreenReferencingAbstractScreenKey : ScreenKey()

   @Parcelize
   data object AbstractScreenKey : ScreenKey()

   @InjectNavigationScreen
   class OuterScreenReferencingAbstractScreen(
      private val innerScreen: TestAbstractScreen
   ) : Screen<OuterScreenReferencingAbstractScreenKey>() {
      @Composable
      override fun Content(key: OuterScreenReferencingAbstractScreenKey) {
         Column {
            innerScreen.Content(AbstractScreenKey)
         }
      }
   }

   abstract class TestAbstractScreen : Screen<AbstractScreenKey>()

   @ContributesScreenBinding
   @InjectNavigationScreen
   @Inject
   class TestAbstractScreenImpl : TestAbstractScreen() {
      @Composable
      override fun Content(key: AbstractScreenKey) {
         Column {
            Text("Hello from Inner Screen")
         }
      }
   }

   @Parcelize
   data object OuterScreenReferencingAbstractScreenWithServiceKey : ScreenKey()

   @InjectNavigationScreen
   class OuterScreenReferencingAbstractScreenWithService(
      private val innerScreen: TestAbstractScreenWithService
   ) : Screen<OuterScreenReferencingAbstractScreenWithServiceKey>() {
      @Composable
      override fun Content(key: OuterScreenReferencingAbstractScreenWithServiceKey) {
         Column {
            innerScreen.Content(key)
         }
      }
   }

   abstract class TestAbstractScreenWithService : Screen<ScreenKey>()

   @ContributesScreenBinding
   @Suppress("unused")
   @InjectNavigationScreen
   @Inject
   class TestAbstractScreenWithServiceImpl(
      private val service: ServiceScopes.SharedService
   ) : TestAbstractScreenWithService() {
      @Composable
      override fun Content(key: ScreenKey) {
         Column {
            Text("Hello from Inner Screen")
         }
      }
   }

   @Parcelize
   data object OuterScreenReferencingGenericScreenKey : ScreenKey()

   @InjectNavigationScreen
   class OuterScreenReferencingGenericScreen(
      private val innerScreen: Screen<InnerScreenKey>
   ) : Screen<OuterScreenReferencingGenericScreenKey>() {
      @Composable
      override fun Content(key: OuterScreenReferencingGenericScreenKey) {
         Column {
            innerScreen.Content(InnerScreenKey)
         }
      }
   }

   @Parcelize
   data object OuterScreenReferencingGenericScreenWithExplicitBoundTypeKey : ScreenKey()

   @InjectNavigationScreen
   class OuterScreenReferencingGenericScreenWithExplicitBoundType(
      private val innerScreen: Screen<InnerScreenKeyWithExplicitBoundType>
   ) : Screen<OuterScreenReferencingGenericScreenWithExplicitBoundTypeKey>() {
      @Composable
      override fun Content(key: OuterScreenReferencingGenericScreenWithExplicitBoundTypeKey) {
         Column {
            innerScreen.Content(InnerScreenKeyWithExplicitBoundType)
         }
      }
   }

   @Suppress("unused")
   @ContributesScreenBinding
   @InjectNavigationScreen
   @Inject
   class TestAbstractScreenReferencingCustomKey(
      private val service: ServiceScopes.SharedService
   ) : Screen<InnerScreenKey>() {
      @Composable
      override fun Content(key: InnerScreenKey) {
         Column {
            Text("Hello from Inner Screen")
         }
      }
   }

   @Parcelize
   data object InnerScreenKey : ScreenKey()

   @Suppress("unused")
   @ContributesScreenBinding(boundType = Screen::class)
   @InjectNavigationScreen
   @Inject
   class TestAbstractScreenReferencingCustomKeyWithExplicitBoundType(
      private val service: ServiceScopes.SharedService
   ) : Screen<InnerScreenKeyWithExplicitBoundType>() {
      @Composable
      override fun Content(key: InnerScreenKeyWithExplicitBoundType) {
         Column {
            Text("Hello from Inner Screen")
         }
      }
   }

   @Parcelize
   data object InnerScreenKeyWithExplicitBoundType : ScreenKey()
}
