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

package si.inova.kotlinova.retrofit.converter

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Wrapper around [Converter.Factory] that initializes the factory lazily on first request.
 *
 * This allows factory to be initialized lazily on the background thread instead of eagerly on the main thread on the app
 * startup, improving the user experience.
 *
 * This wrapper only works for converter factories that handle all values (such as Moshi converter factory).
 */
class LazyRetrofitConverterFactory(private val parentFactory: Lazy<Converter.Factory>) : Converter.Factory() {
   override fun responseBodyConverter(
      type: Type,
      annotations: Array<out Annotation>,
      retrofit: Retrofit
   ): Converter<ResponseBody, *> {
      val lazyConverter = lazy {
         requireNotNull(
            parentFactory.value.responseBodyConverter(
               type,
               annotations,
               retrofit
            )
         ) { "Moshi converter should never be null" }
      }

      return Converter { lazyConverter.value.convert(it) }
   }

   override fun requestBodyConverter(
      type: Type,
      parameterAnnotations: Array<out Annotation>,
      methodAnnotations: Array<out Annotation>,
      retrofit: Retrofit
   ): Converter<*, RequestBody> {
      val lazyConverter = lazy {
         @Suppress("UNCHECKED_CAST")
         requireNotNull(
            parentFactory.value.requestBodyConverter(
               type,
               parameterAnnotations,
               methodAnnotations,
               retrofit
            ) as Converter<Any?, RequestBody>
         ) { "Moshi converter should never be null" }
      }

      return Converter<Any?, RequestBody> { value -> lazyConverter.value.convert(value) }
   }
}
