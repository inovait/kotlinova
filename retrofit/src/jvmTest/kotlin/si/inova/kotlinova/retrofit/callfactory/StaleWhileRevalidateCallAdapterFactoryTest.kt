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

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import okhttp3.Cache
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.outcomes.shouldBeErrorWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWith
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.util.testWithExceptions
import si.inova.kotlinova.retrofit.SyntheticHeaders
import si.inova.kotlinova.retrofit.mockWebServer
import si.inova.kotlinova.retrofit.setJsonBody
import java.io.File

internal class StaleWhileRevalidateCallAdapterFactoryTest {
   private lateinit var tempCache: Cache

   @BeforeEach
   internal fun setUp(
      @TempDir
      cacheDirectory: File
   ) {
      tempCache = Cache(cacheDirectory, 100_000)
   }

   @Test
   internal fun `Throw NoNetworkException if request timeouts`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService()

         mockResponse("/data") {
            socketPolicy = SocketPolicy.NO_RESPONSE
         }

         service.getEnumResult().testWithExceptions {
            awaitItem().shouldBeErrorWith(exceptionType = NoNetworkException::class.java)
            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Throw NoNetworkException if request fails to download`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService()

         mockResponse("/data") {
            socketPolicy = SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY
         }

         service.getEnumResult().testWithExceptions {
            awaitItem().shouldBeErrorWith(exceptionType = NoNetworkException::class.java)
            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Throw DataParsingException if json parsing fails`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService()

         mockResponse("/data") {
            setJsonBody("{")
         }

         service.getEnumResult().testWithExceptions {
            awaitItem().shouldBeErrorWith(exceptionType = DataParsingException::class.java)
            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Throw DataParsingException if json parsing has wrong fields`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService()

         mockResponse("/data") {
            setJsonBody("\"THIRD\"")
         }

         service.getEnumResult().testWithExceptions {
            awaitItem().shouldBeErrorWith(exceptionType = DataParsingException::class.java)
            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Include URL in the json parsing exceptions`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService()

         mockResponse("/data") {
            setJsonBody("\"THIRD\"")
         }

         service.getEnumResult().testWithExceptions {
            val exception = awaitItem().shouldBeErrorWith(exceptionType = DataParsingException::class.java)

            exception.message.shouldNotBeNull().apply {
               shouldContain(" http://localhost")
               shouldContain("/data")
            }

            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Parse error with error handler`() = runTest {
      mockWebServer {
         val service: TestRetrofitService =
            createRetrofitService(errorHandler = { response: Response<*>, cause: Exception ->
               val text = response.errorBody()?.string() ?: "NO_ERROR_MESSAGE"
               TestErrorResponseException(text, cause)
            })

         mockResponse("/data") {
            setStatus("HTTP/1.1 404 NOT FOUND")
            setJsonBody("\"TEST ERROR MESSAGE\"")
         }

         service.getEnumResult().testWithExceptions {
            val exception = awaitItem().shouldBeErrorWith(exceptionType = TestErrorResponseException::class.java)

            exception.cause.shouldNotBeNull().message.apply {
               shouldContain(" http://localhost")
               shouldContain("/data")
            }

            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Just return single success when there is no cache`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(cache = tempCache)

         mockResponse("/data") {
            setJsonBody("\"FIRST\"")
         }

         service.getEnumResult().testWithExceptions {
            awaitItem().shouldBeSuccessWithData(FakeEnumResult.FIRST)
            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Return cached version as Progress, followed by real request as Success`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(cache = tempCache)

         mockResponse("/data") {
            setHeader("ETag", "a")
            setJsonBody("\"FIRST\"")
         }

         service.getEnumResult().collect()

         mockResponse("/data") {
            setJsonBody("\"SECOND\"")
         }

         service.getEnumResult().testWithExceptions {
            awaitItem().shouldBeProgressWith(FakeEnumResult.FIRST)
            awaitItem().shouldBeSuccessWithData(FakeEnumResult.SECOND)
            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Return cache as success if cached version is still fresh`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(cache = tempCache)

         mockResponse("/data") {
            setHeader("ETag", "a")
            setHeader("Cache-Control", "max-age=100000")
            setJsonBody("\"FIRST\"")
         }

         service.getEnumResult().collect()

         service.getEnumResult().testWithExceptions {
            awaitItem().shouldBeSuccessWithData(FakeEnumResult.FIRST)
            awaitComplete()
         }

         server.requestCount shouldBe 1
      }
   }

   @Test
   internal fun `Ignore cache freshness when synthetic force refresh header is set`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(cache = tempCache)

         mockResponse("/data") {
            setHeader("ETag", "a")
            setHeader("Cache-Control", "max-age=100000")
            setJsonBody("\"FIRST\"")
         }

         service.getEnumResult().collect()

         mockResponse("/data") {
            setJsonBody("\"SECOND\"")
         }

         service.getEnumResult(force = true).testWithExceptions {
            awaitItem().shouldBeProgressWith(FakeEnumResult.FIRST)
            awaitItem().shouldBeSuccessWithData(FakeEnumResult.SECOND)
            awaitComplete()
         }
      }
   }

   @Test
   internal fun `Remove synthetic force refresh header when set`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(cache = tempCache)

         mockResponse("/data") {
            setHeader("ETag", "a")
            setHeader("Cache-Control", "max-age=100000")
            setJsonBody("\"FIRST\"")
         }

         service.getEnumResult().collect()

         mockResponse("/data") {
            setJsonBody("\"SECOND\"")
         }

         service.getEnumResult(force = true).testWithExceptions {
            awaitItem().shouldBeProgressWith(FakeEnumResult.FIRST)
            awaitItem().shouldBeSuccessWithData(FakeEnumResult.SECOND)
            awaitComplete()
         }

         server.takeRequest().getHeader(SyntheticHeaders.HEADER_FORCE_REFRESH).shouldBeNull()
         server.takeRequest().getHeader(SyntheticHeaders.HEADER_FORCE_REFRESH).shouldBeNull()
      }
   }

   private interface TestRetrofitService {
      @GET("/data")
      fun getEnumResult(
         @Header(SyntheticHeaders.HEADER_FORCE_REFRESH)
         force: Boolean = false
      ): Flow<Outcome<FakeEnumResult>>
   }

   private enum class FakeEnumResult {
      FIRST,
      SECOND
   }

   private class TestErrorResponseException(message: String? = null, cause: Throwable? = null) : CauseException(message, cause)
}
