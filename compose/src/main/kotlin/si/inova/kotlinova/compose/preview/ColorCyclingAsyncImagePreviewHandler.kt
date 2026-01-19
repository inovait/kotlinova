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
import androidx.annotation.ColorInt
import coil3.ColorImage
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.asPainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.pxOrElse

/**
 * Fake implementation of Coil's [AsyncImagePreviewHandler] that displays single colors in place of images.
 *
 * This can be used in Preview and in tests to simulate image loading, without actually making any network requests.
 *
 * @param colors sequence of [ColorInt] colors to display.
 */
@OptIn(ExperimentalCoilApi::class)
class ColorCyclingAsyncImagePreviewHandler(
   private val colors: List<Int> = listOf(
      Color.RED,
      Color.GREEN,
      Color.BLUE,
      Color.GRAY,
      Color.CYAN,
      Color.YELLOW,
      Color.MAGENTA,
   ),
) : AsyncImagePreviewHandler {
   @ColorInt
   private var currentColor = 0

   override suspend fun handle(imageLoader: ImageLoader, request: ImageRequest): AsyncImagePainter.State {
      val nextColor = colors[currentColor++ % colors.size]
      val size = request.sizeResolver.size()

      val image = ColorImage(nextColor, size.width.pxOrElse { 0 }, size.height.pxOrElse { 0 })

      return AsyncImagePainter.State.Success(image.asPainter(request.context), SuccessResult(image, request))
   }
}
