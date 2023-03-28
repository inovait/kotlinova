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
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class CollectIntoTest {
   @Test
   internal fun collectInto() = runTest {
      val target = MutableSharedFlow<Int>()
      val source = flowOf(1, 2, 3)

      target.test {
         source.collectInto(target)

         awaitItem() shouldBe 1
         awaitItem() shouldBe 2
         awaitItem() shouldBe 3
         expectNoEvents()
      }
   }

   @Test
   internal fun launchAndCollectInto() = runTest {
      val target = MutableSharedFlow<Int>()
      val source = flowOf(1, 2, 3)

      target.test {
         source.launchAndCollectInto(this@runTest, target)
         runCurrent()

         awaitItem() shouldBe 1
         awaitItem() shouldBe 2
         awaitItem() shouldBe 3
         expectNoEvents()
      }
   }

   @Test
   internal fun provideUserPresenceWhenUsingCollectInto() = runTest {
      val target = MutableStateFlow(false)
      val source = flow {
         val presenceProvider = currentCoroutineContext()[UserPresenceProvider].shouldNotBeNull()
         emitAll(presenceProvider.isUserPresentFlow())
      }

      val collectIntoJob = source.launchAndCollectInto(this, target)

      target.value shouldBe false

      val collectorJob = target.launchIn(this@runTest)
      runCurrent()
      target.value shouldBe true

      collectorJob.cancel()
      runCurrent()
      target.value shouldBe false

      collectIntoJob.cancel()
   }
}
