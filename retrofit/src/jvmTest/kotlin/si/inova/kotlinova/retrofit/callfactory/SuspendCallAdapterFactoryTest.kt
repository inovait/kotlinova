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
import okhttp3.Cache
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
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
import si.inova.kotlinova.retrofit.mockWebServer
import si.inova.kotlinova.retrofit.setJsonBody
import java.io.File
import java.io.IOException

class SuspendCallAdapterFactoryTest {
   private lateinit var tempCache: Cache

   @BeforeEach
   internal fun setUp(
      @TempDir
      cacheDirectory: File
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
            setJsonBody("\"FIRST\"")
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
            socketPolicy = SocketPolicy.NO_RESPONSE
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
            socketPolicy = SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY
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
            setJsonBody("{")
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
            setJsonBody("\"THIRD\"")
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
            setJsonBody("\"THIRD\"")
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
            setStatus("HTTP/1.1 404 NOT FOUND")
            setJsonBody("\"TEST ERROR MESSAGE\"")
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
            setHeader("ETag", "a")
            setHeader("Cache-Control", "max-age=100000")
            setJsonBody("\"FIRST\"")
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
            setHeader("ETag", "a")
            setHeader("Cache-Control", "max-age=100000")
            setJsonBody("\"FIRST\"")
         }

         service.getEnumResult()

         mockResponse("/data") {
            setJsonBody("\"SECOND\"")
         }

         service.getEnumResult(force = true) shouldBe FakeEnumResult.SECOND
      }
   }

   @Test
   internal fun `Remove synthetic force refresh header when set`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest, cache = tempCache)

         mockResponse("/data") {
            setHeader("ETag", "a")
            setHeader("Cache-Control", "max-age=100000")
            setJsonBody("\"FIRST\"")
         }

         service.getEnumResult()

         mockResponse("/data") {
            setJsonBody("\"SECOND\"")
         }

         service.getEnumResult(force = true) shouldBe FakeEnumResult.SECOND

         server.takeRequest().getHeader(SyntheticHeaders.HEADER_FORCE_REFRESH).shouldBeNull()
         server.takeRequest().getHeader(SyntheticHeaders.HEADER_FORCE_REFRESH).shouldBeNull()
      }
   }

   @Test
   internal fun `Allow blank response response`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = createRetrofitService(this@runTest, cache = tempCache)

         mockResponse("/dataBlank") {
            setResponseCode(204)
            setBody(Buffer())
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
            setStatus("HTTP/1.1 404 NOT FOUND")
            setJsonBody("\"TEST ERROR MESSAGE\"")
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
            socketPolicy = SocketPolicy.NO_RESPONSE
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
         force: Boolean = false
      ): FakeEnumResult

      @GET("/dataBlank")
      suspend fun getUnitResult(
         @Header(SyntheticHeaders.HEADER_FORCE_REFRESH)
         force: Boolean = false
      )
   }

   private enum class FakeEnumResult {
      FIRST,
      SECOND
   }

   private class TestErrorResponseException(message: String? = null, cause: Throwable? = null) : CauseException(message, cause)
   private class TestIOException(message: String? = null, cause: Throwable? = null) : IOException(message, cause)
}
