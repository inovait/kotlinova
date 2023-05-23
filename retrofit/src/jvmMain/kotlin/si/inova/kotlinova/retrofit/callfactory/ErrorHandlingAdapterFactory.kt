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

package si.inova.kotlinova.retrofit.callfactory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.CauseException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A [CallAdapter.Factory] that will throw proper [CauseException] exceptions on all retrofit calls.
 *
 * * If a call fails due to network issues, it throws [NoNetworkException]
 * * If a call falls due to bad JSON parsing, it throws [DataParsingException]
 * * If a call fails due to bad response, it calls supplied [errorHandler] to parse error response and returns [CauseException]
 *   supplied from that handler
 *
 *   @param coroutineScope A coroutine scope that all calls will be started on this scope. Ideally, it
 *          should have default dispatcher as a dispatcher to ensure first call initializes OkHttp on the background thread.
 *   @param errorHandler Error handler that parses the error responses
 */
class ErrorHandlingAdapterFactory(
   private val coroutineScope: CoroutineScope,
   private val errorHandler: ErrorHandler? = null
) : CallAdapter.Factory() {
   override fun get(
      returnType: Type,
      annotations: Array<Annotation>,
      retrofit: Retrofit
   ): CallAdapter<*, *>? {
      if (returnType !is ParameterizedType) {
         return null
      }

      if (getRawType(returnType) != Call::class.java) {
         return null
      }

      val actualNestedTypeToDecode = getParameterUpperBound(0, returnType)

      return ResultAdapter<Any>((actualNestedTypeToDecode))
   }

   private inner class ResultAdapter<T>(private val responseType: Type) : CallAdapter<T, Call<T>> {
      override fun adapt(call: Call<T>): Call<T> = ResultCall(call)
      override fun responseType(): Type = responseType
   }

   private inner class ResultCall<T>(proxy: Call<T>) : CallDelegate<T, T>(proxy) {
      override fun enqueueImpl(callback: Callback<T>) {
         coroutineScope.launch {
            proxy.enqueue(object : Callback<T> {
               override fun onFailure(call: Call<T>, t: Throwable) {
                  callback.onFailure(call, t.transformRetrofitException(request().url.toString()))
               }

               override fun onResponse(call: Call<T>, response: Response<T>) {
                  return try {
                     val result = response.bodyOrThrow(errorHandler)
                     callback.onResponse(call, Response.success(result))
                  } catch (e: Exception) {
                     callback.onFailure(call, e.transformRetrofitException(request().url.toString()))
                  }
               }
            })
         }
      }

      override fun cloneImpl(): Call<T> = ResultCall(proxy.clone())

      override fun executeImpl(): Response<T> {
         val response = proxy.execute()
         if (!response.isSuccessful) {
            throw errorHandler?.generateExceptionFromErrorBody(response, response.createParentException())
               ?: response.createParentException()
         }

         return Response.success(response.bodyOrThrow(errorHandler))
      }

      override fun timeout(): Timeout = proxy.timeout()
   }

   private abstract class CallDelegate<TIn, TOut>(
      protected val proxy: Call<TIn>
   ) : Call<TOut> {
      override fun execute(): Response<TOut> = executeImpl()
      final override fun enqueue(callback: Callback<TOut>) = enqueueImpl(callback)
      final override fun clone(): Call<TOut> = cloneImpl()

      override fun cancel() = proxy.cancel()
      override fun request(): Request = proxy.request()
      override fun isExecuted() = proxy.isExecuted
      override fun isCanceled() = proxy.isCanceled

      abstract fun enqueueImpl(callback: Callback<TOut>)
      abstract fun cloneImpl(): Call<TOut>
      abstract fun executeImpl(): Response<TOut>
   }
}
