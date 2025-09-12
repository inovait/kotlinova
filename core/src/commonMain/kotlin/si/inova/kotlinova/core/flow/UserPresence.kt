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

package si.inova.kotlinova.core.flow

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.CoroutineContext

/**
 * Only collect upstream flow when user is present and is actively using the app. This can be used to, for example, only activate
 * GPS location when app is open, to conserve battery life.
 *
 * You can use optional [onStartWithUserNotPresent] parameter, which mimics [Flow.onStart] and will get invoked whenever this flow
 * starts getting collected without user present. You can emit some sort of default value in that block to ensure flow does not
 * get stuck until user comes back.
 */
fun <T> Flow<T>.onlyFlowWhenUserPresent(onStartWithUserNotPresent: suspend FlowCollector<T>.() -> Unit = {}): Flow<T> {
   return flow {
      val presenceProvider = currentCoroutineContext()[UserPresenceProvider]
         ?: error("This flow needs UserPresenceProvider from upstream to function")
      var startedWithoutPresence = true

      val dataFlow = presenceProvider.isUserPresentFlow().flatMapLatest { userPresent ->
         if (userPresent) {
            startedWithoutPresence = false
            this@onlyFlowWhenUserPresent
         } else {
            if (startedWithoutPresence) {
               startedWithoutPresence = false
               flow(onStartWithUserNotPresent)
            } else {
               emptyFlow()
            }
         }
      }

      emitAll(dataFlow)
   }
}

fun UserPresenceProvider(sharedFlow: MutableSharedFlow<*>): UserPresenceProvider {
   @Suppress("ObjectLiteralToLambda") // Can't due to https://github.com/Kotlin/kotlinx.coroutines/issues/4512
   return object : UserPresenceProvider {
      override fun isUserPresentFlow(): Flow<Boolean> {
         return sharedFlow.hasActiveSubscribersFlow()
      }
   }
}

fun interface UserPresenceProvider : CoroutineContext.Element {
   override val key: CoroutineContext.Key<*>
      get() = UserPresenceProvider

   fun isUserPresentFlow(): Flow<Boolean>

   companion object : CoroutineContext.Key<UserPresenceProvider>
}
