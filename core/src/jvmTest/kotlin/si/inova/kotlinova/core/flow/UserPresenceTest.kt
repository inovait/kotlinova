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

package si.inova.kotlinova.core.flow

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.flow.FakeUserPresenceProvider

internal class UserPresenceTest {
   @Test
   internal fun `Update UserPresenceProvider when number of collectors change`() = runTest {
      val flow = MutableStateFlow(Unit)
      val presenceProvider = UserPresenceProvider(flow)

      presenceProvider.isUserPresentFlow().test {
         awaitItem() shouldBe false

         val collectorJob = flow.launchIn(this@runTest)
         runCurrent()
         awaitItem() shouldBe true

         collectorJob.cancel()
         runCurrent()
         awaitItem() shouldBe false

         expectNoEvents()
      }
   }

   @Test
   internal fun `Only flow when user is present`() = runTest {
      val presenceProvider = FakeUserPresenceProvider()
      presenceProvider.isPresent = true

      withContext(presenceProvider) {
         val source = MutableSharedFlow<Int>()

         source.onlyFlowWhenUserPresent().test {
            source.subscriptionCount.value shouldBe 1
            source.emit(1)
            awaitItem() shouldBe 1

            presenceProvider.isPresent = false
            source.subscriptionCount.value shouldBe 0
            source.emit(2)
            expectNoEvents()

            presenceProvider.isPresent = true
            source.subscriptionCount.value shouldBe 1
            source.emit(3)
            awaitItem() shouldBe 3

            expectNoEvents()
         }
      }
   }

   @Test
   internal fun `Trigger optional start parameter when flow starts without user present`() = runTest {
      val presenceProvider = FakeUserPresenceProvider()
      presenceProvider.isPresent = false

      withContext(presenceProvider) {
         val source = MutableSharedFlow<Int>()

         source.onlyFlowWhenUserPresent { emit(4) }.test {
            runCurrent()

            source.subscriptionCount.value shouldBe 0
            awaitItem() shouldBe 4

            presenceProvider.isPresent = true
            source.subscriptionCount.value shouldBe 1
            source.emit(1)
            awaitItem() shouldBe 1

            presenceProvider.isPresent = false
            source.subscriptionCount.value shouldBe 0
            source.emit(2)
            expectNoEvents()

            presenceProvider.isPresent = true
            source.subscriptionCount.value shouldBe 1
            source.emit(3)
            awaitItem() shouldBe 3

            expectNoEvents()
         }
      }
   }
}
