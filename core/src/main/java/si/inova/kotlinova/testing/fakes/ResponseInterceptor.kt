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

package si.inova.kotlinova.testing.fakes

import kotlinx.coroutines.CompletableDeferred

/**
 * Test utility class that allows easy infinite loading and error response integration into
 * testing fakes
 */
class ResponseInterceptor {
    private var nextInterception: InterceptionStyle? = null
    private var defaultInterception: InterceptionStyle = InterceptionStyle.None

    private var infiniteLoadCompletable: CompletableDeferred<Unit>? = null
    private var immediatelyComplete = false

    fun interceptNextCallWith(style: InterceptionStyle) {
        nextInterception = style
    }

    fun interceptAllFutureCallsWith(style: InterceptionStyle) {
        defaultInterception = style
    }

    fun completeInfiniteLoad() {
        val infiniteLoadCompletable = infiniteLoadCompletable
        if (infiniteLoadCompletable == null) {
            immediatelyComplete = true
        } else {
            infiniteLoadCompletable.complete(Unit)
            this.infiniteLoadCompletable = null

            immediatelyComplete = false
        }
    }

    suspend fun intercept() {
        val nextInterception = nextInterception ?: defaultInterception
        this.nextInterception = null

        return when (nextInterception) {
            is InterceptionStyle.None -> Unit
            is InterceptionStyle.InfiniteLoad -> {
                if (immediatelyComplete) {
                    immediatelyComplete = false
                    return
                }

                val completable = CompletableDeferred<Unit>()
                this.infiniteLoadCompletable = completable

                completable.await()
            }
            is InterceptionStyle.Error -> throw nextInterception.exception
        }
    }

    fun reset() {
        completeInfiniteLoad()
        nextInterception = InterceptionStyle.None
        immediatelyComplete = false
    }
}

sealed class InterceptionStyle {
    object None : InterceptionStyle()
    object InfiniteLoad : InterceptionStyle()
    data class Error(val exception: Throwable) : InterceptionStyle()
}