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

package si.inova.kotlinova.retrofit

import kotlinx.coroutines.test.TestScope
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.intellij.lang.annotations.Language

/**
 * Create and prepare [MockWebServer] for creating tests for web services.
 *
 * You can use [MockWebServerScope.baseUrl] inside the provided block to initialize your Retrofit service
 * and [MockWebServerScope.mockResponse] to create mock HTTP responses.
 */
inline fun TestScope.mockWebServer(
   block: MockWebServerScope.() -> Unit
) {
   val server = MockWebServer()

   val scope = MockWebServerScope(server, server.url("").toString(), this)
   server.dispatcher = scope

   try {
      block(scope)
   } finally {
      server.shutdown()
   }
}

class MockWebServerScope(val server: MockWebServer, val baseUrl: String, val testScope: TestScope) : Dispatcher() {
   private val responses = HashMap<String, MockResponse>()

   override fun dispatch(request: RecordedRequest): MockResponse {
      return responses[request.path] ?: error("Response to '${request.path ?: "null"}' not mocked")
   }

   fun mockResponse(url: String, response: MockResponse) {
      responses[url] = response
   }

   inline fun mockResponse(url: String, responseBuilder: MockResponse.() -> Unit) {
      val response = MockResponse()
      responseBuilder(response)

      mockResponse(url, response)
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
   addHeader("Content-Type", "application/json")

   val resource = MockWebServerScope::class.java.classLoader!!.getResourceAsStream(fileName)
      ?: error("Resource $fileName does not exist")

   val body = resource.bufferedReader().use { it.readText() }
   setJsonBody(body)
}
