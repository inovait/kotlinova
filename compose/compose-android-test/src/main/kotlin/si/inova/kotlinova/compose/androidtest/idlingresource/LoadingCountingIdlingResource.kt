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

package si.inova.kotlinova.compose.androidtest.idlingresource

import androidx.compose.ui.test.IdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import si.inova.kotlinova.compose.androidtest.idlingresource.LoadingCountingIdlingResource.registerResource
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.reporting.ErrorReporter
import java.util.Collections
import java.util.WeakHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Idling resource that will stay busy until none of the resources provided to it are in the [Outcome.Progress] state.
 *
 * You can add resources manually via [registerResource] call or inject [RegisteringCoroutineResourceManager] into your app
 * as a replacement for [CoroutineResourceManager] to automatically register all resources in the app.
 */
object LoadingCountingIdlingResource : IdlingResource {
   private val flows = Collections.newSetFromMap<MutableStateFlow<*>>(WeakHashMap())

   fun registerResource(resource: MutableStateFlow<*>) {
      flows += resource
   }

   override val isIdleNow: Boolean
      get() {
         return !flows.any { it.value is Outcome.Progress<*> }
      }

   override fun getDiagnosticMessageIfBusy(): String {
      return "Resources ${flows.filter { it.value is Outcome.Progress<*> }.map { it.value }.toList()} are busy"
   }
}

class RegisteringCoroutineResourceManager(scope: CoroutineScope, reportService: ErrorReporter) :
   CoroutineResourceManager(scope, reportService) {
   override fun <T> launchResourceControlTask(
      resource: MutableStateFlow<Outcome<T>>,
      currentValue: T?,
      context: CoroutineContext,
      keepDataOnExceptions: Boolean,
      block: suspend ResourceControlBlock<T>.() -> Unit
   ) {
      LoadingCountingIdlingResource.registerResource(resource)
      super.launchResourceControlTask(resource, currentValue, context, keepDataOnExceptions, block)
   }
}
