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

import kotlinx.coroutines.CoroutineScope
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

/**
 * A ViewModel that is meant to be bound to a single screen. [key] will automatically get populated with that screen's key.
 *
 * Override [onServiceRegistered] method if you need to do any initialisation - key is guaranteed to be initialized here.
 *
 * @param [coroutineScope] See documentation for [CoroutineScopedService]
 */
abstract class SingleScreenViewModel<K : ScreenKey>(coroutineScope: CoroutineScope) :
   SaveableScopedService(coroutineScope) {
   lateinit var key: K
}
