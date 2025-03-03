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

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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
      server.shutdown()
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
      server.dispatcher = this
   }

   private val responses = HashMap<String, MockResponse>()
   private val responsesWithFullUrl = HashMap<String, MockResponse>()
   var defaultResponse: (RecordedRequest) -> MockResponse = ::defaultMissingResponseRequest

   override fun dispatch(request: RecordedRequest): MockResponse {
      return responsesWithFullUrl[request.path] ?: responses[request.requestUrl?.encodedPath] ?: defaultResponse(request)
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
   inline fun mockResponse(url: String, includeQueryParameters: Boolean = false, responseBuilder: MockResponse.() -> Unit) {
      val response = MockResponse()
      responseBuilder(response)

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
      val url = request.path ?: "UNKNOWN URL"

      deferredExceptions += IllegalStateException("Response to $url not mocked")

      return MockResponse().apply {
         setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
         addHeader("Content-Type", "text/plain")
         setBody("Response to $url not mocked")
      }
   }
}

fun MockResponse.setJsonBody(
   @Language("JSON")
   body: String
) {
   addHeader("Content-Type", "application/json")
   setBody(body)
}

fun MockResponse.setJsonBodyFromResource(fileName: String) {
   val resource = MockWebServerScope::class.java.classLoader!!.getResourceAsStream(fileName)
      ?: error("Resource $fileName does not exist")

   val body = resource.bufferedReader().use { it.readText() }
   setJsonBody(body)
}
