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

package si.inova.kotlinova.core.dispatchers

import dispatch.core.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Wrapper for a [DispatcherProvider] that triggers [onDispatcherAccess] callback every time dispatcher is accessed.
 *
 * You can use it to override DefaultDispatcherProvider to ensure it is never actually used
 * (dispatchers should be passed down via structured concurrency, not using default dispatcher singleton)
 *
 * For example, you can do this in your Applicaton.onCreate:
 *
 * ```kotlin
 * DefaultDispatcherProvider.set(
 *  AccessCallbackDispatcherProvider(DefaultDispatcherProvider.get()) {
 *     if (BuildConfig.DEBUG) {
 *        throw IllegalStateException("Dispatchers not provided via coroutine scope.")
 *     }
 *  }
 * )
 * ```
 */
class AccessCallbackDispatcherProvider(
   private val parent: DispatcherProvider,
   private val onDispatcherAccess: () -> Unit,
) : DispatcherProvider {
   override val default: CoroutineDispatcher
      get() {
         onDispatcherAccess()
         return parent.default
      }
   override val io: CoroutineDispatcher
      get() {
         onDispatcherAccess()
         return parent.io
      }
   override val key: CoroutineContext.Key<*>
      get() {
         onDispatcherAccess()
         return parent.key
      }
   override val main: CoroutineDispatcher
      get() {
         onDispatcherAccess()
         return parent.main
      }
   override val mainImmediate: CoroutineDispatcher
      get() {
         onDispatcherAccess()
         return parent.mainImmediate
      }
   override val unconfined: CoroutineDispatcher
      get() {
         onDispatcherAccess()
         return parent.unconfined
      }
}
