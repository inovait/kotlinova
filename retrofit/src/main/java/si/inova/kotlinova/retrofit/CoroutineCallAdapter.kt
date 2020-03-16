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

package si.inova.kotlinova.retrofit

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A [CallAdapter.Factory] for use with Kotlin coroutines.
 *
 * Adding this class to [Retrofit] allows you to return [Deferred] from
 * service methods.
 *
 * In addition to original CoroutineCallAdapterFactory class,
 * this class also supports optional [responseParser] constructor parameter that can provide
 * custom error message parsing.
 *
 * @author original CoroutineCallAdapterFactory by Jake Wharton, adapted by Matej Drobnic
 */
@Deprecated("Use suspend retrofit methods + ErrorHandlerCallAdapterFactory")
class CoroutineCallAdapterFactory constructor(
        private val responseParser: ResponseParser = DefaultResponseParser
) :
        CallAdapter.Factory() {
    override fun get(
            returnType: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                    "Deferred return type must be parameterized " +
                            "as Deferred<Foo> or Deferred<out Foo>"
            )
        }
        val responseType = getParameterUpperBound(0, returnType)

        return BodyCallAdapter<Any>(responseType)
    }

    private inner class BodyCallAdapter<T>(
            private val responseType: Type
    ) : CallAdapter<T, Deferred<T>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Deferred<T> {
            val deferred = CompletableDeferred<T>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    val cause = t.cause
                    if (t is IOException && cause != null) {
                        deferred.completeExceptionally(cause)
                    } else {
                        deferred.completeExceptionally(t)
                    }
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    try {
                        deferred.complete(responseParser.parseResponse(response))
                    } catch (e: Exception) {
                        deferred.completeExceptionally(e)
                    }
                }
            })

            return deferred
        }
    }

    interface ResponseParser {
        fun <T> parseResponse(response: Response<T>): T
    }
}