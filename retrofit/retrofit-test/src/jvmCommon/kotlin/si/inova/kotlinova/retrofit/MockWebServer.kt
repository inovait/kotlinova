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

package si.inova.kotlinova.retrofit

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import org.intellij.lang.annotations.Language
import java.net.HttpURLConnection

/**
 * Create and prepare [MockWebServer] for creating tests for web services.
 *
 * You can use [MockWebServerScope.baseUrl] inside the provided block to initialize your Retrofit service
 * and [MockWebServerScope.mockResponse] to create mock HTTP responses.
 */
inline fun mockWebServer(
   block: MockWebServerScope.() -> Unit
) {
   MockWebServerScope().runServer(block)
}

inline fun MockWebServerScope.runServer(block: MockWebServerScope.() -> Unit) {
   try {
      block()
   } catch (e: Throwable) {
      deferredExceptions += e
   } finally {
      server.close()
   }

   if (deferredExceptions.size == 1) {
      throw deferredExceptions.first()
   } else if (deferredExceptions.size > 1) {
      for (exception in deferredExceptions) {
         exception.printStackTrace()
      }

      val exceptionDescriptions = deferredExceptions.joinToString { "${it.javaClass.name}: ${it.message.orEmpty()}" }
      throw AssertionError("Got multiple exceptions: $exceptionDescriptions")
   }
}

class MockWebServerScope : Dispatcher() {
   val server: MockWebServer = MockWebServer()
   val baseUrl: String
      get() = server.url("").toString()
   val deferredExceptions = ArrayList<Throwable>()

   init {
      server.start()
      server.dispatcher = this
   }

   private val responses = HashMap<String, MockResponse>()
   private val responsesWithFullUrl = HashMap<String, MockResponse>()
   var defaultResponse: (RecordedRequest) -> MockResponse = ::defaultMissingResponseRequest

   override fun dispatch(request: RecordedRequest): MockResponse {
      return responsesWithFullUrl[request.target] ?: responses[request.url.encodedPath] ?: defaultResponse(request)
   }

   fun mockResponse(url: String, response: MockResponse, includeQueryParameters: Boolean = false) {
      if (includeQueryParameters) {
         responsesWithFullUrl[url] = response
      } else {
         responses[url] = response
      }
   }

   /**
    * @param includeQueryParameters when *true*, you will also need to include query parameters in the *url* to be matched.
    *                               Otherwise, it will only match by path.
    */
   inline fun mockResponse(url: String, includeQueryParameters: Boolean = false, responseBuilder: () -> MockResponse) {
      val response = responseBuilder()

      mockResponse(url, response, includeQueryParameters)
   }

   inline fun setDefaultResponse(crossinline responseBuilder: MockResponse.(RecordedRequest) -> Unit) {
      defaultResponse = { request ->
         val response = MockResponse()
         responseBuilder(response, request)
         response
      }
   }

   private fun defaultMissingResponseRequest(request: RecordedRequest): MockResponse {
      val url = request.target

      deferredExceptions += IllegalStateException("Response to $url not mocked")

      return MockResponse(
         HttpURLConnection.HTTP_INTERNAL_ERROR,
         headersOf(
            "Content-Type", "text/plain"
         ),
         body = "Response to $url not mocked"
      )
   }
}

fun createJsonMockResponse(
   @Language("JSON")
   body: String,
   code: Int = HttpURLConnection.HTTP_OK,
   headers: Headers = headersOf(),
): MockResponse {
   return MockResponse(
      code,
      headers.newBuilder().add("Content-Type", "application/json").build(),
      body
   )
}

fun createJsonMockResponseFromResource(
   fileName: String,
   code: Int = HttpURLConnection.HTTP_OK,
   headers: Headers = headersOf(),
   classLoader: ClassLoader = MockWebServerScope::class.java.classLoader,
): MockResponse {
   val resource = classLoader.getResourceAsStream(fileName)
      ?: error("Resource $fileName does not exist")

   val body = resource.bufferedReader().use { it.readText() }
   return createJsonMockResponse(body, code, headers)
}
