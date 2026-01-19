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

import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.TestScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.test.outcomes.ThrowingErrorReporter
import si.inova.kotlinova.retrofit.MockWebServerScope
import si.inova.kotlinova.retrofit.converter.LazyRetrofitConverterFactory
import si.inova.kotlinova.retrofit.interceptors.BypassCacheInterceptor
import si.inova.kotlinova.retrofit.moshi.adapters.JavaTimeMoshiAdapter
import java.util.concurrent.TimeUnit

inline fun <reified T> MockWebServerScope.createRetrofitService(
   testScope: TestScope,
   cache: Cache? = null,
   errorHandler: ErrorHandler? = null,
   okHttpSetup: OkHttpClient.Builder.() -> Unit = {},
   noinline exceptionInterceptor: (Throwable) -> CauseException? = { null },
): T {
   val client = OkHttpClient.Builder()
      .addInterceptor(BypassCacheInterceptor())
      .callTimeout(200, TimeUnit.MILLISECONDS)
      .apply { if (cache != null) cache(cache) }
      .apply(okHttpSetup)
      .build()

   val moshi = Moshi.Builder()
      .add(JavaTimeMoshiAdapter)
      .build()

   val retrofit = Retrofit.Builder()
      .client(client)
      .baseUrl(baseUrl)
      .addConverterFactory(LazyRetrofitConverterFactory(lazy { MoshiConverterFactory.create(moshi) }))
      .addCallAdapterFactory(
         StaleWhileRevalidateCallAdapterFactory(
            errorHandler,
            ThrowingErrorReporter(testScope),
            exceptionInterceptor
         )
      )
      .addCallAdapterFactory(ErrorHandlingAdapterFactory(testScope, errorHandler, exceptionInterceptor))
      .build()

   return retrofit.create()
}
