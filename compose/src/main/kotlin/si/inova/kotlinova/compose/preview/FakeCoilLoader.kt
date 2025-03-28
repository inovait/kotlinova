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

package si.inova.kotlinova.compose.preview

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

/**
 * Fake implementation of Coil's [ImageLoader] that displays single colors in place of images.
 *
 * This can be used in Preview and in tests to simulate image loading, without actually making any network requests.
 *
 * @param colors sequence of [ColorInt] colors to display.
 */
class FakeCoilLoader(
   private val colors: List<Int> = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.GRAY, Color.CYAN, Color.YELLOW, Color.MAGENTA)
) : ImageLoader {
   @ColorInt
   private var currentColor = 0

   override val components: ComponentRegistry
      get() = throw UnsupportedOperationException("Not supported in FakeCoilLoader")
   override val defaults: ImageRequest.Defaults
      get() = ImageRequest.Defaults(
         placeholderFactory = { generateNextColorDrawable().asImage(false) }
      )
   override val diskCache: DiskCache
      get() = throw UnsupportedOperationException("Not supported in FakeCoilLoader")
   override val memoryCache: MemoryCache
      get() = throw UnsupportedOperationException("Not supported in FakeCoilLoader")

   override fun enqueue(request: ImageRequest): Disposable {
      val res = executeInternal(request)

      return object : Disposable {
         override val isDisposed: Boolean
            get() = false
         override val job: Deferred<ImageResult>
            get() = CompletableDeferred(res)

         override fun dispose() {}
      }
   }

   override suspend fun execute(request: ImageRequest): ImageResult {
      return executeInternal(request)
   }

   private fun executeInternal(request: ImageRequest): ImageResult {
      return SuccessResult(generateNextColorDrawable().asImage(false), request, DataSource.NETWORK)
   }

   private fun generateNextColorDrawable(): Drawable {
      val nextColor = colors[currentColor++ % colors.size]
      return ColorDrawable(nextColor)
   }

   override fun newBuilder(): ImageLoader.Builder {
      throw UnsupportedOperationException("Not supported in FakeCoilLoader")
   }

   override fun shutdown() {}
}
