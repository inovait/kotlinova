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

import com.squareup.moshi.JsonEncodingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.SocketEffect
import okhttp3.Cache
import okhttp3.Headers.Companion.headersOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import si.inova.kotlinova.core.exceptions.DataParsingException
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.retrofit.SyntheticHeaders
import si.inova.kotlinova.retrofit.createJsonMockResponse
import si.inova.kotlinova.retrofit.mockWebServer
import java.io.File
import java.io.IOException

class SuspendCallAdapterFactoryTest {
   private lateinit var tempCache: Cache

   @BeforeEach
   internal fun setUp(
      @TempDir
      cacheDirectory: File,
   ) {
      tempCache = Cache(cacheDirectory, 100_000)
   }

   @Test
   internal fun `Provide response normally`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(
            this@runTest,
            cache = tempCache,
         )

         mockResponse("/data") {
            createJsonMockResponse("\"FIRST\"")
         }

         service.getEnumResult() shouldBe FakeEnumResult.FIRST

         server.requestCount shouldBe 1
      }
   }

   @Test
   internal fun `Throw NoNetworkException if request timeouts`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest)

         mockResponse("/data") {
            MockResponse.Builder()
               .onResponseStart(SocketEffect.Stall)
               .build()
         }

         shouldThrow<NoNetworkException> {
            service.getEnumResult()
         }
      }
   }

   @Test
   internal fun `Throw NoNetworkException if request fails to download`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest)

         mockResponse("/data") {
            MockResponse.Builder()
               .onResponseStart(SocketEffect.CloseStream())
               .build()
         }

         shouldThrow<NoNetworkException> {
            service.getEnumResult()
         }
      }
   }

   @Test
   internal fun `Throw DataParsingException if json parsing fails`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest)

         mockResponse("/data") {
            createJsonMockResponse("{")
         }

         shouldThrow<DataParsingException> {
            service.getEnumResult()
         }
      }
   }

   @Test
   internal fun `Throw DataParsingException if json parsing has wrong fields`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest)

         mockResponse("/data") {
            createJsonMockResponse("\"THIRD\"")
         }

         shouldThrow<DataParsingException> {
            service.getEnumResult()
         }
      }
   }

   @Test
   internal fun `Include URL in the json parsing exceptions`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest)

         mockResponse("/data") {
            createJsonMockResponse("\"THIRD\"")
         }

         val exception = shouldThrow<DataParsingException> {
            service.getEnumResult()
         }

         exception.message.shouldNotBeNull().apply {
            shouldContain(" http://localhost")
            shouldContain("/data")
         }
      }
   }

   @Test
   internal fun `Parse error with error handler`() = runTest {
      mockWebServer {
         val service: TestRetrofitService =
            createRetrofitService(
               this@runTest,
               errorHandler = { response: Response<*>, cause: Exception ->
                  val text = response.errorBody()?.string() ?: "NO_ERROR_MESSAGE"
                  TestErrorResponseException(text, cause)
               }
            )

         mockResponse("/data") {
            createJsonMockResponse("\"TEST ERROR MESSAGE\"", code = 404)
         }

         val exception = shouldThrow<TestErrorResponseException> {
            service.getEnumResult()
         }

         exception.cause.shouldNotBeNull().message.apply {
            shouldContain(" http://localhost")
            shouldContain("/data")
         }
      }
   }

   @Test
   internal fun `Return cached version if cached version is still fresh`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest, cache = tempCache)

         mockResponse("/data") {
            createJsonMockResponse("\"FIRST\"", headers = headersOf("ETag", "a", "Cache-Control", "max-age=100000"))
         }

         service.getEnumResult()

         service.getEnumResult() shouldBe FakeEnumResult.FIRST

         server.requestCount shouldBe 1
      }
   }

   @Test
   internal fun `Ignore cache freshness when synthetic force refresh header is set`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest, cache = tempCache)

         mockResponse("/data") {
            createJsonMockResponse("\"FIRST\"", headers = headersOf("ETag", "a", "Cache-Control", "max-age=100000"))
         }

         service.getEnumResult()

         mockResponse("/data") {
            createJsonMockResponse("\"SECOND\"")
         }

         service.getEnumResult(force = true) shouldBe FakeEnumResult.SECOND
      }
   }

   @Test
   internal fun `Remove synthetic force refresh header when set`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest, cache = tempCache)

         mockResponse("/data") {
            createJsonMockResponse("\"FIRST\"", headers = headersOf("ETag", "a", "Cache-Control", "max-age=100000"))
         }

         service.getEnumResult()

         mockResponse("/data") {
            createJsonMockResponse("\"SECOND\"")
         }

         service.getEnumResult(force = true) shouldBe FakeEnumResult.SECOND

         server.takeRequest().headers[SyntheticHeaders.HEADER_FORCE_REFRESH].shouldBeNull()
         server.takeRequest().headers[SyntheticHeaders.HEADER_FORCE_REFRESH].shouldBeNull()
      }
   }

   @Test
   internal fun `Allow blank response response`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest, cache = tempCache)

         mockResponse("/dataBlank") {
            MockResponse(code = 204)
         }

         service.getUnitResult()

         server.requestCount shouldBe 1
      }
   }

   @Test
   internal fun `Report proper error when error body parsing fails`() = runTest {
      mockWebServer {
         val service: TestRetrofitService =
            createRetrofitService(
               this@runTest,
               errorHandler = { _, _ ->
                  throw JsonEncodingException("Failed to parse error body")
               }
            )

         mockResponse("/data") {
            MockResponse.Builder()
               .status("HTTP/1.1 404 NOT FOUND")
               .addHeader("Content-Type", "application/json")
               .body("\"TEST ERROR MESSAGE\"")
               .build()
         }

         val exception = shouldThrow<DataParsingException> {
            service.getEnumResult()
         }

         exception.shouldNotBeNull().message.apply {
            shouldContain(" http://localhost")
            shouldContain("/data")
            shouldContain("404")
            shouldContain("NOT FOUND")
         }
      }
   }

   @Test
   internal fun `Transform network errors using exceptionInterceptor`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(
            this@runTest,
            okHttpSetup = {
               addInterceptor {
                  throw TestIOException()
               }
            },
            exceptionInterceptor = {
               if (it is TestIOException) {
                  TestErrorResponseException(
                     cause = it.cause
                  )
               } else {
                  null
               }
            }
         )

         mockResponse("/data") {
            MockResponse.Builder()
               .onResponseStart(SocketEffect.Stall)
               .build()
         }

         shouldThrow<TestErrorResponseException> {
            service.getEnumResult()
         }
      }
   }

   private interface TestRetrofitService {
      @GET("/data")
      suspend fun getEnumResult(
         @Header(SyntheticHeaders.HEADER_FORCE_REFRESH)
         force: Boolean = false,
      ): FakeEnumResult

      @GET("/dataBlank")
      suspend fun getUnitResult(
         @Header(SyntheticHeaders.HEADER_FORCE_REFRESH)
         force: Boolean = false,
      )
   }

   private enum class FakeEnumResult {
      FIRST,
      SECOND,
   }

   private class TestErrorResponseException(message: String? = null, cause: Throwable? = null) : CauseException(message, cause)
   private class TestIOException(message: String? = null, cause: Throwable? = null) : IOException(message, cause)
}
