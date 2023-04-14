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
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.Screen
import si.inova.kotlinova.navigation.services.SaveableScopedService
import si.inova.kotlinova.navigation.testutils.insertTestNavigation
import java.util.UUID
import javax.inject.Inject

class ServiceScopes {
   @get:Rule
   val rule = createComposeRule()

   @Test
   internal fun doNotShareServicesByDefault() {
      val backstack = rule.insertTestNavigation(
         NotSharedServiceScreenKey()
      )

      rule.onNodeWithText("+").performClick()
      rule.onNodeWithText("Open another screen").performClick()
      rule.onNodeWithText("0").assertIsDisplayed()

      rule.runOnUiThread {
         backstack.goBack()
      }

      rule.onNodeWithText("1").assertIsDisplayed()
   }

   @Test
   internal fun shareServicesWhenKeysHaveSameServiceTag() {
      rule.insertTestNavigation(
         SharedServiceScreenKey()
      )

      rule.onNodeWithText("+").performClick()
      rule.onNodeWithText("Open another screen").performClick()

      rule.onNodeWithText("1").assertIsDisplayed()
   }

   @Parcelize
   data class NotSharedServiceScreenKey(val id: UUID = UUID.randomUUID()) : ScreenKey()

   class NotSharedServiceScreen(sharedService: SharedService, navigator: Navigator) :
      CommonSharedServiceTestScreen<NotSharedServiceScreenKey>(sharedService, navigator) {
      override fun getNewScreen(): NotSharedServiceScreenKey {
         return NotSharedServiceScreenKey()
      }
   }

   @Parcelize
   data class SharedServiceScreenKey(val id: UUID = UUID.randomUUID()) : ScreenKey() {
      override fun getScopeTag(): String {
         return "SharedScope"
      }
   }

   class SharedServiceScreen(sharedService: SharedService, navigator: Navigator) :
      CommonSharedServiceTestScreen<SharedServiceScreenKey>(sharedService, navigator) {
      override fun getNewScreen(): SharedServiceScreenKey {
         return SharedServiceScreenKey()
      }
   }

   abstract class CommonSharedServiceTestScreen<K : ScreenKey>(
      private val sharedService: SharedService,
      private val navigator: Navigator
   ) : Screen<K>() {
      abstract fun getNewScreen(): K

      @Composable
      override fun Content(key: K) {
         Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
               Button(onClick = { sharedService.number.update { it - 1 } }) {
                  Text("-")
               }

               Text(sharedService.number.collectAsState().value.toString())

               Button(onClick = { sharedService.number.update { it + 1 } }) {
                  Text("+")
               }
            }

            Button(onClick = { navigator.navigateTo(getNewScreen()) }) {
               Text("Open another screen")
            }
         }
      }
   }

   class SharedService @Inject constructor(coroutineScope: CoroutineScope) : SaveableScopedService(coroutineScope) {
      val number by savedFlow(0)
   }
}
