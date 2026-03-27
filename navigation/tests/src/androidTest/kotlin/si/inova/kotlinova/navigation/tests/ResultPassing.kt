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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.compose.result.LocalResultPassingStore
import si.inova.kotlinova.compose.result.ResultKey
import si.inova.kotlinova.compose.result.ResultPassingStore
import si.inova.kotlinova.compose.result.registerResultReceiver

class ResultPassing {
   @get:Rule
   val rule = createComposeRule()

   private val resultPassingStore = ResultPassingStore()

   private val receivedResults = ArrayList<Int?>()

   @Test
   fun passResultWithComposableBeingActive() {
      lateinit var key: ResultKey<Int>
      rule.setContentWithLocals {
         key = registerResultReceiver<Int> {
            receivedResults += it
         }
      }
      rule.waitForIdle()

      resultPassingStore.sendResult(key, 10)
      rule.waitForIdle()

      receivedResults.shouldContainExactly(10)
   }

   @Test
   fun passResultWithComposableBeingInActive() {
      lateinit var key: ResultKey<Int>
      var active by mutableStateOf(true)

      rule.setContentWithLocals {
         if (active) {
            key = registerResultReceiver<Int> {
               receivedResults += it
            }
         }
      }
      rule.waitForIdle()

      active = false
      rule.waitForIdle()

      resultPassingStore.sendResult(key, 10)
      rule.waitForIdle()

      active = true
      rule.waitForIdle()

      receivedResults.shouldContainExactly(10)
   }

   @Test
   fun passNullResultWithComposableBeingActive() {
      lateinit var key: ResultKey<Int?>
      rule.setContentWithLocals {
         key = registerResultReceiver<Int?> {
            receivedResults += it
         }
      }
      rule.waitForIdle()

      resultPassingStore.sendResult(key, null)
      rule.waitForIdle()

      receivedResults.shouldContainExactly(null)
   }

   @Test
   fun passNullResultWithComposableBeingInActive() {
      lateinit var key: ResultKey<Int?>
      var active by mutableStateOf(true)

      rule.setContentWithLocals {
         if (active) {
            key = registerResultReceiver<Int?> {
               receivedResults += it
            }
         }
      }
      rule.waitForIdle()

      active = false
      rule.waitForIdle()

      resultPassingStore.sendResult(key, null)
      rule.waitForIdle()

      active = true
      rule.waitForIdle()

      receivedResults.shouldContainExactly(null)
   }

   @Test
   fun passResultWithMultipleRegistrationsAndComposableBeingActive() {
      lateinit var keyA: ResultKey<Int>
      lateinit var keyB: ResultKey<Int>
      lateinit var keyC: ResultKey<Int>
      rule.setContentWithLocals {
         keyA = registerResultReceiver<Int> {
            receivedResults += 10 + it
         }
         keyB = registerResultReceiver<Int> {
            receivedResults += 20 + it
         }
         keyC = registerResultReceiver<Int> {
            receivedResults += 30 + it
         }
      }
      rule.waitForIdle()

      resultPassingStore.sendResult(keyA, 1)
      resultPassingStore.sendResult(keyB, 2)
      resultPassingStore.sendResult(keyC, 3)
      rule.waitForIdle()

      receivedResults.shouldContainExactly(11, 22, 33)
   }

   @Test
   fun passResultWithMultipleRegistrationsAndComposableBeingInActive() {
      var active by mutableStateOf(true)

      lateinit var keyA: ResultKey<Int>
      lateinit var keyB: ResultKey<Int>
      lateinit var keyC: ResultKey<Int>

      rule.setContentWithLocals {
         if (active) {
            keyA = registerResultReceiver<Int> {
               receivedResults += 10 + it
            }
            keyB = registerResultReceiver<Int> {
               receivedResults += 20 + it
            }
            keyC = registerResultReceiver<Int> {
               receivedResults += 30 + it
            }
         }
      }
      rule.waitForIdle()

      active = false
      rule.waitForIdle()

      resultPassingStore.sendResult(keyA, 1)
      resultPassingStore.sendResult(keyB, 2)
      resultPassingStore.sendResult(keyC, 3)
      rule.waitForIdle()

      active = true
      rule.waitForIdle()

      receivedResults.shouldContainExactly(11, 22, 33)
   }

   private fun ComposeContentTestRule.setContentWithLocals(content: @Composable () -> Unit) {
      setContent {
         CompositionLocalProvider(LocalResultPassingStore provides resultPassingStore) {
            content()
         }
      }
   }
}
