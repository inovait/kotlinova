package si.inova.kotlinova.compose.preview

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
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
   override val defaults: DefaultRequestOptions
      get() = DefaultRequestOptions(
         placeholder = generateNextColorDrawable()
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
      return SuccessResult(generateNextColorDrawable(), request, DataSource.NETWORK)
   }

   private fun generateNextColorDrawable(): Drawable {
      val nextColor = colors[currentColor++]
      return ColorDrawable(nextColor)
   }

   override fun newBuilder(): ImageLoader.Builder {
      throw UnsupportedOperationException("Not supported in FakeCoilLoader")
   }

   override fun shutdown() {}
}
