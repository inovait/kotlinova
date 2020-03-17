/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.coroutines

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Collection of Coroutine dispatchers that can be overriden for Unit tests.
 **/
object TestableDispatchers {
    /**
     * [Dispatchers.Default] dispatcher that can be overriden by unit tests.
     */
    @JvmStatic
    val Default
        get() = dispatcherOverride {
            Dispatchers.Default
        }

    /**
     * [Dispatchers.Main] dispatcher that can be overriden by unit tests.
     */
    @JvmStatic
    @Deprecated(
        "Use Dispatchers.Main instead as it can be injected",
        replaceWith = ReplaceWith(
            "Dispatchers.Main",
            "kotlinx.coroutines.Dispatchers"
        )
    )
    val Main
        get() = dispatcherOverride {
            Dispatchers.Main
        }

    /**
     * [Dispatchers.IO] dispatcher that can be overriden by unit tests.
     */
    @JvmStatic
    val IO
        get() = dispatcherOverride {
            Dispatchers.IO
        }

    @JvmStatic
    var dispatcherOverride: (() -> CoroutineContext) -> CoroutineContext = { it() }
}
