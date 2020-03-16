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

import okhttp3.Request
import retrofit2.*
import si.inova.kotlinova.retrofit.ErrorHandlerCallAdapterFactory.ErrorHandler
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A [CallAdapter.Factory] for use with Kotlin coroutines and Retrofit.
 *
 * Adding this class to [Retrofit] allows you to define custom error handlers with [ErrorHandler]
 * to parse backend error responses into exceptions on suspending retrofit endpoints.
 *
 */
class ErrorHandlerCallAdapterFactory constructor(
        private val errorHandler: ErrorHandler
) :
        CallAdapter.Factory() {
    override fun get(
            returnType: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
    ): CallAdapter<*, *>? {
        return if (getRawType(returnType) == Call::class.java) {
            val resultType = getParameterUpperBound(0, returnType as ParameterizedType)
            ResultAdapter<Any>(resultType)
        } else {
            null
        }
    }

    private inner class ResultAdapter<T>(private val responseType: Type) : CallAdapter<T, Call<T>> {
        override fun adapt(call: Call<T>): Call<T> = ResultCall(call)

        override fun responseType(): Type = responseType
    }

    private inner class ResultCall<T>(proxy: Call<T>) : CallDelegate<T, T>(proxy) {
        override fun enqueueImpl(callback: Callback<T>) {
            proxy.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    callback.onFailure(call, t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        callback.onResponse(call, response)
                    } else {
                        val exception = try {
                            errorHandler.generateExceptionFromErrorBody(response)
                                    ?: HttpException(response)
                        } catch (e: Exception) {
                            e
                        }
                        callback.onFailure(call, exception)
                    }
                }
            })
        }

        override fun cloneImpl(): Call<T> = ResultCall(proxy.clone())
    }

    private abstract class CallDelegate<TIn, TOut>(
            protected val proxy: Call<TIn>
    ) : Call<TOut> {
        override fun execute(): Response<TOut> = throw NotImplementedError()
        final override fun enqueue(callback: Callback<TOut>) = enqueueImpl(callback)
        final override fun clone(): Call<TOut> = cloneImpl()

        override fun cancel() = proxy.cancel()
        override fun request(): Request = proxy.request()
        override fun isExecuted() = proxy.isExecuted
        override fun isCanceled() = proxy.isCanceled

        abstract fun enqueueImpl(callback: Callback<TOut>)
        abstract fun cloneImpl(): Call<TOut>
    }

    interface ErrorHandler {
        fun <T> generateExceptionFromErrorBody(response: Response<T>): Exception?
    }
}