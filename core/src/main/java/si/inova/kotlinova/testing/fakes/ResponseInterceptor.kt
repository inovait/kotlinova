/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC. If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
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