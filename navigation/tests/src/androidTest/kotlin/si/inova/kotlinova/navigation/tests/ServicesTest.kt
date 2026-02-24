/*
 * Copyright 2026 INOVA IT d.o.o.
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
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.di.BackstackScope
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SaveableScopedService
import si.inova.kotlinova.navigation.services.ScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import si.inova.kotlinova.navigation.testutils.BlankScreenKey
import si.inova.kotlinova.navigation.testutils.clearBackstackFromMemory
import si.inova.kotlinova.navigation.testutils.goBack
import si.inova.kotlinova.navigation.testutils.insertTestNavigation
import si.inova.kotlinova.navigation.testutils.removeBackstackFromMemory

private var lastReceivedKey: Any? = null
private var numRegisterCalled: Int = 0
private var unregisterCalled: Boolean = false

class ServicesTest {
   @get:Rule
   val rule = createComposeRule()

   @Before
   fun setUp() {
      numRegisterCalled = 0
      unregisterCalled = false
   }

   @Test
   internal fun allowScreensToInteractWithServices() {
      rule.insertTestNavigation(ScreenWithBasicServiceKey)
      rule.waitForIdle()

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
      rule.onNodeWithText("Increase").performClick()
      rule.onNodeWithText("Number: 1").assertIsDisplayed()
   }

   @Test
   internal fun preserveServicesWhenScreenGoesToTheBackstack() {
      val backstack = rule.insertTestNavigation(ScreenWithBasicServiceKey)

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
      rule.onNodeWithText("Increase").performClick()

      rule.runOnUiThread {
         backstack.updateBackstack(listOf(ScreenWithBasicServiceKey, BlankScreenKey))
      }

      rule.runOnUiThread {
         backstack.updateBackstack(listOf(ScreenWithBasicServiceKey))
      }

      rule.onNodeWithText("Number: 1").assertIsDisplayed()
      unregisterCalled shouldBe false
   }

   @Test
   internal fun preserveServicesAcrossConfigurationChanges() {
      val stateRestorationTester = StateRestorationTester(rule)
      stateRestorationTester.insertTestNavigation(rule, ScreenWithBasicServiceKey)

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
      rule.onNodeWithText("Increase").performClick()

      numRegisterCalled = 0

      rule.waitForIdle()
      stateRestorationTester.emulateSavedInstanceStateRestore()

      rule.onNodeWithText("Number: 1").assertIsDisplayed()
      numRegisterCalled shouldBe 0
   }

   @Test
   internal fun doNotPreserveServiceStateAcrossProcessKillsWhenNotUsingStateSavingService() {
      // Assurance test that tests if our removeBackstackFromMemory() still works, to ensure
      // following tests is not just a false positive

      val stateRestorationTester = StateRestorationTester(rule)
      stateRestorationTester.insertTestNavigation(rule, ScreenWithBasicServiceKey)

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
      rule.onNodeWithText("Increase").performClick()

      rule.waitForIdle()
      rule.removeBackstackFromMemory()

      stateRestorationTester.emulateSavedInstanceStateRestore()

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
   }

   @Test
   internal fun preserveServiceStateAcrossProcessKillsWhenUsingStateSavingService() {
      val stateRestorationTester = StateRestorationTester(rule)
      stateRestorationTester.insertTestNavigation(rule, ScreenWithStateSavingServiceKey)

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
      rule.onNodeWithText("Increase").performClick()

      rule.waitForIdle()
      numRegisterCalled = 0
      rule.removeBackstackFromMemory()
      stateRestorationTester.emulateSavedInstanceStateRestore()

      rule.onNodeWithText("Number: 1").assertIsDisplayed()
      numRegisterCalled shouldBe 1
   }

   @Test
   internal fun provideScreenKeyToSingleScreenViewModel() {
      val key = ScreenWithSingleScreenViewModelKey("Lorem Ipsum")
      rule.insertTestNavigation(key)

      lastReceivedKey shouldBe key
   }

   @Test
   internal fun allowScreensInjectInterfacesOfScopedServices() {
      rule.insertTestNavigation(ScreenWithBasicServiceWithInterfaceKey)

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
      rule.onNodeWithText("Increase").performClick()
      rule.onNodeWithText("Number: 1").assertIsDisplayed()
   }

   @Test
   internal fun allowScreensInjectInterfacesAndMultipleParentsOfScopedServices() {
      rule.insertTestNavigation(ScreenWithBasicServiceWithInterfaceKey)

      rule.onNodeWithText("Number: 0").assertIsDisplayed()
      rule.onNodeWithText("Increase").performClick()
      rule.onNodeWithText("Number: 1").assertIsDisplayed()
   }

   @Test
   internal fun callOnServiceUnregisteredWhenActivityFinishes() {
      rule.insertTestNavigation(ScreenWithBasicServiceKey)
      rule.waitForIdle()

      rule.clearBackstackFromMemory()
      rule.waitForIdle()

      unregisterCalled shouldBe true
   }

   @Test
   internal fun callOnServiceUnregisteredWhenScreenGoesOffBackstack() {
      val backstack = rule.insertTestNavigation(BlankScreenKey)
      rule.waitForIdle()

      rule.runOnUiThread {
         backstack.updateBackstack(listOf(BlankScreenKey, ScreenWithBasicServiceKey))
      }
      rule.waitForIdle()

      rule.runOnUiThread {
         backstack.goBack()
      }
      rule.waitForIdle()

      unregisterCalled shouldBe true
   }

   @Test
   internal fun callOnServiceRegisteredOnInitialServices() {
      rule.insertTestNavigation(ScreenWithBasicServiceKey)
      rule.waitForIdle()

      numRegisterCalled shouldBe 1
   }

   @Test
   internal fun callOnServiceRegisteredOnNewServices() {
      val backstack = rule.insertTestNavigation(BlankScreenKey)
      rule.waitForIdle()

      rule.runOnUiThread {
         backstack.updateBackstack(listOf(BlankScreenKey, ScreenWithBasicServiceKey))
      }
      rule.waitForIdle()
      numRegisterCalled shouldBe 1
   }

   @Serializable
   data object ScreenWithBasicServiceKey : ScreenKey()

   @InjectNavigationScreen
   class ScreenWithBasicService(
      private val service: BasicService,
   ) : Screen<ScreenWithBasicServiceKey>() {
      @Composable
      override fun Content(key: ScreenWithBasicServiceKey) {
         Column {
            Text("Number: ${service.data.collectAsStateWithLifecycle().value}")
            Button(onClick = { service.data.update { it + 1 } }) {
               Text("Increase")
            }
         }
      }
   }

   @ContributesScopedService
   @Inject
   class BasicService : ScopedService, ScopedService.Registered {
      val data = MutableStateFlow(0)

      override fun onServiceRegistered() {
         numRegisterCalled++
      }

      override fun onServiceUnregistered() {
         unregisterCalled = true
      }
   }

   @Serializable
   data object ScreenWithStateSavingServiceKey : ScreenKey()

   @InjectNavigationScreen
   class ScreenWithStateSavingService(
      private val service: StateSavingService,
   ) : Screen<ScreenWithStateSavingServiceKey>() {
      @Composable
      override fun Content(key: ScreenWithStateSavingServiceKey) {
         Column {
            Text("Number: ${service.data.collectAsStateWithLifecycle().value}")
            Button(onClick = { service.data.update { it + 1 } }) {
               Text("Increase")
            }
         }
      }
   }

   @ContributesScopedService
   @Inject
   class StateSavingService(coroutineScope: CoroutineScope) : SaveableScopedService(coroutineScope) {
      val data by savedFlow(0)

      override fun onServiceRegistered() {
         numRegisterCalled++
      }
   }

   @Serializable
   data class ScreenWithSingleScreenViewModelKey(val text: String) : ScreenKey()

   @InjectNavigationScreen
   class ScreenWithSingleScreenViewModel(
      @Suppress("UnusedPrivateProperty") // Needed for DI
      service: SingleScreenViewModelService,
   ) : Screen<ScreenWithSingleScreenViewModelKey>() {
      @Composable
      override fun Content(key: ScreenWithSingleScreenViewModelKey) {
      }
   }

   @ContributesScopedService
   @Inject
   class SingleScreenViewModelService(coroutineScope: CoroutineScope) :
      SingleScreenViewModel<ScreenWithSingleScreenViewModelKey>(coroutineScope) {
      override fun onServiceRegistered() {
         lastReceivedKey = key
      }
   }

   @Serializable
   data object ScreenWithBasicServiceWithInterfaceKey : ScreenKey()

   @InjectNavigationScreen
   class ScreenWithBasicServiceWithInterface(
      private val service: BasicServiceWithInterface,
   ) : Screen<ScreenWithBasicServiceWithInterfaceKey>() {
      @Composable
      override fun Content(key: ScreenWithBasicServiceWithInterfaceKey) {
         Column {
            Text("Number: ${service.data.collectAsStateWithLifecycle().value}")
            Button(onClick = { service.data.update { it + 1 } }) {
               Text("Increase")
            }
         }
      }
   }

   @Serializable
   data object ScreenWithBasicServiceWithInterfaceAndMultipleParentsKey : ScreenKey()

   @InjectNavigationScreen
   class ScreenWithBasicServiceWithInterfaceAndMultipleParents(
      private val service: BasicServiceWithInterfaceAndMultipleParents,
   ) : Screen<ScreenWithBasicServiceWithInterfaceAndMultipleParentsKey>() {
      @Composable
      override fun Content(key: ScreenWithBasicServiceWithInterfaceAndMultipleParentsKey) {
         Column {
            Text("Number: ${service.data.collectAsStateWithLifecycle().value}")
            Button(onClick = { service.data.update { it + 1 } }) {
               Text("Increase")
            }
         }
      }
   }

   @ContributesScopedService
   @ContributesBinding(BackstackScope::class)
   @Inject
   class BasicServiceWithInterfaceImpl : BasicServiceWithInterface {
      override val data = MutableStateFlow(0)
   }

   @ContributesScopedService(BasicServiceWithInterfaceAndMultipleParents::class)
   @ContributesBinding(BackstackScope::class, binding<BasicServiceWithInterfaceAndMultipleParents>())
   @Inject
   class BasicServiceWithInterfaceAndMultipleParentsImpl : DummyParent, BasicServiceWithInterfaceAndMultipleParents {
      override val data = MutableStateFlow(0)
   }

   interface BasicServiceWithInterface : ScopedService {
      val data: MutableStateFlow<Int>
   }

   interface BasicServiceWithInterfaceAndMultipleParents : ScopedService {
      val data: MutableStateFlow<Int>
   }

   interface DummyParent
}
