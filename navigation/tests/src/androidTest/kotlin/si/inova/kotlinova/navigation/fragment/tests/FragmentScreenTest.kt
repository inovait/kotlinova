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

package si.inova.kotlinova.navigation.fragment.tests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.fragment.FragmentScreen
import si.inova.kotlinova.navigation.fragment.FragmentScreenKey
import si.inova.kotlinova.navigation.fragment.ScopeExitListener
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.testutils.BlankScreenKey
import si.inova.kotlinova.navigation.testutils.exists
import si.inova.kotlinova.navigation.testutils.insertTestNavigation
import java.util.UUID

class FragmentScreenTest {
   @get:Rule
   val rule = createAndroidComposeRule<TestFragmentActivity>()

   @Test
   internal fun showScreen() {
      rule.insertTestNavigation(TestFragmentScreenKey("Hello"))

      Espresso.onView(withText("Hello From Fragment")).check(matches(isDisplayed()))
   }

   @Test
   internal fun showScreenWhileStopped() {
      rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
      rule.waitForIdle()

      rule.insertTestNavigation(TestFragmentScreenKey("Hello"))

      rule.waitForIdle()
      rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

      rule.waitForIdle()

      // We cannot check for isDisplayed() due to https://issuetracker.google.com/issues/321086832 bug
      // (that bug appears to only happen in tests)
      // Instead we just check that the view exists
      Espresso.onView(withText("Hello From Fragment")).check(exists())
   }

   @Test
   internal fun keepFragmentResumedWhenInFront() {
      rule.insertTestNavigation(TestFragmentScreenKey("Hello"))

      rule.runOnUiThread {
         val fragment = rule.activity.supportFragmentManager.fragments.first() as TestFragment
         fragment.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
         fragment.viewLifecycleOwner.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
      }
   }

   @Test
   internal fun recreateFragmentsViewWhenInBackstack() {
      val backstack = rule.insertTestNavigation(TestFragmentScreenKey("Hello"))
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()
      lateinit var fragment: TestFragment

      rule.runOnUiThread {
         fragment = rule.activity.supportFragmentManager.fragments.first() as TestFragment
         navigator.navigateTo(BlankScreenKey)
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         navigator.goBack()
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         fragment.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
         fragment.viewLifecycleOwner.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
      }
   }

   @Test
   internal fun destroyFragmentsViewWhenInBackstack() {
      val backstack = rule.insertTestNavigation(TestFragmentScreenKey("Hello"))
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()
      lateinit var fragment: TestFragment

      rule.runOnUiThread {
         fragment = rule.activity.supportFragmentManager.fragments.first() as TestFragment
         navigator.navigateTo(BlankScreenKey)
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         fragment.lifecycle.currentState shouldBe Lifecycle.State.CREATED
         fragment.view.shouldBeNull()
         fragment.onDestroyCalled.shouldBeFalse()
      }
   }

   @Test
   internal fun destroyMultipleFragmentsViewWhenInBackstack() {
      val backstack = rule.insertTestNavigation(ScreenThatContainsTwoFragmentsKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()
      lateinit var fragment: TestFragment
      lateinit var fragment2: TestFragment2

      rule.runOnUiThread {
         fragment = rule.activity.supportFragmentManager.fragments.first() as TestFragment
         fragment2 = rule.activity.supportFragmentManager.fragments.elementAt(1) as TestFragment2
         navigator.navigateTo(BlankScreenKey)
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         fragment.lifecycle.currentState shouldBe Lifecycle.State.CREATED
         fragment.view.shouldBeNull()
         fragment.onDestroyCalled.shouldBeFalse()
         fragment2.lifecycle.currentState shouldBe Lifecycle.State.CREATED
         fragment2.view.shouldBeNull()
         fragment2.onDestroyCalled.shouldBeFalse()
      }
   }

   @Test
   internal fun recreateMultipleFragmentsViewWhenComingFromBackstack() {
      val backstack = rule.insertTestNavigation(ScreenThatContainsTwoFragmentsKey)
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()
      lateinit var fragment: TestFragment
      lateinit var fragment2: TestFragment2

      rule.waitForIdle()

      rule.runOnUiThread {
         fragment = rule.activity.supportFragmentManager.fragments.first() as TestFragment
         fragment2 = rule.activity.supportFragmentManager.fragments.elementAt(1) as TestFragment2
         navigator.navigateTo(BlankScreenKey)
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         navigator.goBack()
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         fragment.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
         fragment.viewLifecycleOwner.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
         fragment2.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
         fragment2.viewLifecycleOwner.lifecycle.currentState shouldBe Lifecycle.State.RESUMED
      }
   }

   @Test
   internal fun destroyFragmentsWhenRemovedFromBackstack() {
      val backstack = rule.insertTestNavigation(TestFragmentScreenKey("Hello"))
      val navigator = NavigationInjection.fromBackstack(backstack).navigator()
      lateinit var fragment: TestFragment

      rule.runOnUiThread {
         fragment = rule.activity.supportFragmentManager.fragments.first() as TestFragment
         navigator.navigateTo(BlankScreenKey)
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         fragment.onDestroyCalled.shouldBeFalse()
         navigator.navigate(ReplaceBackstack(BlankScreenKey))
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         fragment.lifecycle.currentState shouldBe Lifecycle.State.INITIALIZED
         fragment.view shouldBe null
         fragment.onDestroyCalled.shouldBeTrue()
      }
   }

   class TestFragment : Fragment() {
      var onDestroyCalled = false
      override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
         println("onCreateView")

         return TextView(requireContext()).apply {
            text = "Hello From Fragment"

            doOnLayout {
               println("on text layout")
            }
         }
      }

      override fun onDestroy() {
         super.onDestroy()
         onDestroyCalled = true
      }
   }

   class TestFragment2 : Fragment() {
      var onDestroyCalled = false
      override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
         return TextView(requireContext()).apply {
            text = "Hello From Fragment 2"
         }
      }

      override fun onDestroy() {
         super.onDestroy()
         onDestroyCalled = true
      }
   }

   @Parcelize
   data class TestFragmentScreenKey(override val tag: String = UUID.randomUUID().toString()) : ScreenKey(), FragmentScreenKey

   class TestFragmentScreen(scopeExitListener: ScopeExitListener) : FragmentScreen<TestFragmentScreenKey>(scopeExitListener) {
      override fun createFragment(key: TestFragmentScreenKey, fragmentManager: FragmentManager): Fragment {
         return TestFragment()
      }

      @Composable
      override fun Content(key: TestFragmentScreenKey) {
         println("Fragment Screen Content ${LocalLifecycleOwner.current.lifecycle.currentState}")
         super.Content(key)
      }
   }

   @Parcelize
   data class TestFragmentScreen2Key(override val tag: String = UUID.randomUUID().toString()) : ScreenKey(), FragmentScreenKey

   class TestFragmentScreen2(scopeExitListener: ScopeExitListener) : FragmentScreen<TestFragmentScreen2Key>(scopeExitListener) {
      override fun createFragment(key: TestFragmentScreen2Key, fragmentManager: FragmentManager): Fragment {
         return TestFragment2()
      }
   }

   @Parcelize
   data object ScreenThatContainsTwoFragmentsKey : ScreenKey()

   class ScreenThatContainsTwoFragments(
      private val testFragmentScreen: TestFragmentScreen,
      private val testFragmentScreen2: TestFragmentScreen2
   ) : Screen<ScreenThatContainsTwoFragmentsKey>() {
      @Composable
      override fun Content(key: ScreenThatContainsTwoFragmentsKey) {
         Column {
            testFragmentScreen.Content(TestFragmentScreenKey("Hello"))
            testFragmentScreen2.Content(TestFragmentScreen2Key("Hello2"))
         }
      }
   }
}
