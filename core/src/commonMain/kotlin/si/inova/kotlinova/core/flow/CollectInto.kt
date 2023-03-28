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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Collect this flow and forward all emissions into provided [target].
 * Upstream flows will get access to the [onlyFlowWhenUserPresent], where user is considered as present if [target] has any
 * collectors.
 *
 * This method suspends until flow is completed.
 */
suspend fun <T> Flow<T>.collectInto(target: MutableSharedFlow<in T>) {
   val userPresenceProvider = UserPresenceProvider(target)
   withContext(userPresenceProvider) {
      target.emitAll(this@collectInto)
   }
}

/**
 * Launch a new job that will collect this flow and forward all emissions into provided [target].
 * Upstream flows will get access to the [onlyFlowWhenUserPresent], where user is considered as present if [target] has any
 * collectors.
 *
 * Job finishes once this flow is completed.
 */
fun <T> Flow<T>.launchAndCollectInto(scope: CoroutineScope, target: MutableSharedFlow<in T>): Job {
   return scope.launch {
      collectInto(target)
   }
}
