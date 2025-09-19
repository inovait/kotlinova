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

package si.inova.kotlinova.retrofit.callfactory

import dispatch.core.withDefault
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import okhttp3.CacheControl
import okhttp3.Request
import okhttp3.internal.cache.CacheStrategy
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.parseResponse
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.catchIntoOutcome
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.SyntheticHeaders
import si.inova.kotlinova.retrofit.okhttp.enqueueAndAwait
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/**
 * Call adapter factory that handles Flow<Outcome<T>> return types.
 *
 * When Retrofit function returns Flow<Outcome<T>>, stale while revalidate strategy will be used. First, [Outcome.Progress] will
 * be emitted with stale cached data, while new fresh data will be loaded from the server. Afterwards, [Outcome.Success] will
 * be emitted with the new fresh server data.
 *
 *  Factory also handles errors:
 *
 * * If a call fails due to network issues, it returns [Outcome.Error] with [NoNetworkException]
 * * If a call falls due to bad JSON parsing, it returns [Outcome.Error] with [DataParsingException]
 * * If a call fails due to bad response, it calls supplied [errorHandler] to parse error response and returns [Outcome.Error]
 *   with [CauseException] from that handler
 *
 * Output will not reflect any cache read fails, but they will be reported to the passed [errorReporter].
 *
 *   @param errorHandler Error handler that parses the error responses
 *   @param errorReporter an error reporter class that will receive all cache parsinge errors.
 *   @param exceptionInterceptor A callback that is called for all problems with talking to the server,
 *                               before regular exception handling happens.
 *                               Use it to convert your custom IOExceptions, thrown inside interceptors,
 *                               into CauseException that can then be consumed upstream. This is not called when a full response
 *                               is received. Use [errorHandler] for that.
 */
class StaleWhileRevalidateCallAdapterFactory(
   private val errorHandler: ErrorHandler?,
   private val errorReporter: ErrorReporter = ErrorReporter {},
   private val exceptionInterceptor: (Throwable) -> CauseException? = { null }
) : CallAdapter.Factory() {
   override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
      if (returnType !is ParameterizedType) {
         return null
      }

      if (getRawType(returnType) != Flow::class.java) {
         return null
      }

      val firstNestedType = getParameterUpperBound(0, returnType)
      if (firstNestedType !is ParameterizedType) {
         return null
      }

      if (getRawType(firstNestedType) != Outcome::class.java) {
         return null
      }

      val secondNestedType = getParameterUpperBound(0, firstNestedType)
      return ResultAdapter<Any>(secondNestedType, retrofit)
   }

   private inner class ResultAdapter<T>(
      private val responseType: Type,
      private val retrofit: Retrofit,
   ) : CallAdapter<T, Flow<Outcome<T>>> {
      @DelicateCoroutinesApi
      override fun adapt(call: Call<T>): Flow<Outcome<T>> {
         // Perform enqueue on the background thread to ensure OkHttp initialization does not block the main thread
         return channelFlow {
            withDefault {
               val (networkRequest, dataFromCache) = makeCacheRequest(call)
               networkRequest?.let { makeMainRequest(it, call, dataFromCache) }
            }
         }
      }

      private suspend fun ProducerScope<Outcome<T>>.makeCacheRequest(originalCall: Call<T>): Pair<Request?, T?> {
         var networkRequest: Request? = originalCall.request()

         return try {
            val forceNetwork = originalCall.request().header(SyntheticHeaders.HEADER_FORCE_REFRESH)?.toBoolean() ?: false

            val cacheRequest = originalCall.request().newBuilder()
               .cacheControl(CacheControl.FORCE_CACHE)
               .removeHeader(SyntheticHeaders.HEADER_FORCE_REFRESH)
               .build()

            val rawCacheResponse = retrofit.callFactory().newCall(cacheRequest).enqueueAndAwait()

            if (rawCacheResponse.code == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
               // OkHttp returns gateway timeout when there is no cache or cache is not valid
               return originalCall.request().removeSyntheticHeaders() to null
            }

            val responseWithAddedCacheParameters = if (forceNetwork) {
               rawCacheResponse.newBuilder()
                  .header("Cache-Control", CacheControl.Builder().maxAge(1, TimeUnit.MILLISECONDS).build().toString())
                  .build()
            } else {
               rawCacheResponse
            }

            val strategy = CacheStrategy.Factory(
               System.currentTimeMillis(),
               originalCall.request(),
               responseWithAddedCacheParameters
            ).compute()

            networkRequest = strategy.networkRequest?.removeSyntheticHeaders()

            val cacheResponse = strategy.cacheResponse
            if (networkRequest == null && cacheResponse == null) {
               send(
                  Outcome.Error(
                     UnknownCauseException(
                        message = "Stale while revalidate call to ${cacheRequest.url} failed" +
                           " - both cache and network are null"
                     )
                  )
               )
               return null to null
            }

            if (cacheResponse == null) {
               return originalCall.request() to null
            }

            val parsedResponse = originalCall.parseResponse(cacheResponse)

            parseResultFromCacheResponse(parsedResponse, networkRequest, originalCall)
         } catch (e: Exception) {
            handleCacheError(
               networkRequest,
               exceptionInterceptor(e) ?: e.transformRetrofitException(originalCall.request().url.toString()),
               originalCall
            )
            networkRequest to null
         }
      }

      private suspend fun ProducerScope<Outcome<T>>.makeMainRequest(
         networkRequest: Request,
         call: Call<T>,
         dataFromCache: T?
      ) {
         try {
            val networkResponse = retrofit.callFactory().newCall(networkRequest).enqueueAndAwait()
            if (networkResponse.code == HttpURLConnection.HTTP_NOT_MODIFIED && dataFromCache != null) {
               // Data has not been modified from the cached version, so just return the cached version
               return send(Outcome.Success(dataFromCache))
            }

            val parsedResponse = call.parseResponse(networkResponse)

            val result = catchIntoOutcome {
               Outcome.Success(parsedResponse.bodyOrThrow(errorHandler))
            }

            send(result)
         } catch (e: CancellationException) {
            throw e
         } catch (e: Exception) {
            send(
               Outcome.Error(
                  exceptionInterceptor(e) ?: e.transformRetrofitException(networkRequest.url.toString()),
                  dataFromCache
               )
            )
         }
      }

      private suspend fun ProducerScope<Outcome<T>>.parseResultFromCacheResponse(
         parsedResponse: Response<T>,
         networkRequest: Request?,
         originalCall: Call<T>
      ): Pair<Request?, T?> {
         val result = catchIntoOutcome {
            val data = parsedResponse.bodyOrThrow(errorHandler)

            if (networkRequest != null) {
               Outcome.Progress(data)
            } else {
               Outcome.Success(data)
            }
         }

         if (result is Outcome.Error) {
            handleCacheError(networkRequest, result.exception, originalCall)
         } else {
            send(result)
         }

         return networkRequest to result.data
      }

      private suspend fun ProducerScope<Outcome<T>>.handleCacheError(
         networkRequest: Request?,
         e: CauseException,
         originalCall: Call<T>
      ) {
         if (networkRequest == null) {
            // Cache is our main request - forward all errors
            send(Outcome.Error(e))
         } else {
            // Do not surface cache errors to the user, user will receive a new result anyway. Just report it
            // so we can fix it
            errorReporter.report(Exception("Cache call to ${originalCall.request().url} failed", e))
         }
      }

      override fun responseType(): Type = responseType
   }
}

internal fun Request.removeSyntheticHeaders(): Request {
   return newBuilder().removeHeader(SyntheticHeaders.HEADER_FORCE_REFRESH).build()
}
