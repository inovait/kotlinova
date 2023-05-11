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

package si.inova.kotlinova.navigation.services

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.navigation.test.services.copyStateFrom

internal class SaveableScopedServiceTest {
   @Test
   internal fun `Save and restore state of the service`() = runTest {
      val service = TestService(backgroundScope)
      service.regularProperty = 10
      service.flowProperty.value = 20
      runCurrent()

      val newService = TestService(backgroundScope)
      newService.copyStateFrom(service)

      newService.regularProperty shouldBe 10
      newService.flowProperty.value shouldBe 20
   }

   @Test
   internal fun `Save and restore default values of the saved properties after they are read at least once`() = runTest {
      val service = TestService(backgroundScope, defaultValue = { 10 })

      service.regularProperty
      service.flowProperty.value
      runCurrent()

      val newService = TestService(backgroundScope)
      newService.copyStateFrom(service)

      newService.regularProperty shouldBe 10
      newService.flowProperty.value shouldBe 10
   }

   @Test
   internal fun `Defer default value read until properties are read for the first time`() = runTest {
      val defaultValue = mutableListOf<Int>()

      val service = TestService(backgroundScope, defaultValue = defaultValue::first)
      runCurrent()

      defaultValue.add(10)

      service.regularProperty
      service.flowProperty.value
      runCurrent()

      service.regularProperty shouldBe 10
      service.flowProperty.value shouldBe 10
   }
}

class TestService(coroutineScope: CoroutineScope, defaultValue: () -> Int = { 0 }) : SaveableScopedService(coroutineScope) {
   var regularProperty by saved(defaultValue)
   val flowProperty by savedFlow(defaultValue)
}
