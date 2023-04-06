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

package si.inova.kotlinova.retrofit.caching

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.WorkerThread
import okhttp3.Cache
import si.inova.kotlinova.core.reporting.ErrorReporter
import java.io.File
import kotlin.math.roundToLong

/**
 * Class that manages creation of global OKHttp's [Cache].
 *
 * Cache size is based on Android's provided [StorageManager.getCacheQuotaBytes] method.
 * That way we can achieve best balance between having big cache and not eating up user's disk space.
 *
 * To create cache, just access [cache] object. Please note that the operations makes some blocking disk accesses and thus, first
 * call to [cache] MUST be done on the worker thread.
 *
 * @param fallbackCacheSize Cache size in bytes if we cannot get the quota from the Android system (for example due to
 *                               older API level)
 * @param cacheSubfolderName Name of the subfolder inside cache folder where OkHttp cache will be created
 * @param cacheQuotaFraction Fraction of the total cache quota that can be used for OkHttp Disk cache.
 */
class GlobalOkHttpDiskCacheManager constructor(
   private val context: Context,
   private val errorReporter: ErrorReporter,
   private val fallbackCacheSize: Long = DEFAULT_FALLBACK_CACHE_SIZE_BYTES,
   private val cacheSubfolderName: String = DEFAULT_CACHE_SUBFOLDER,
   private val cacheQuotaFraction: Float = DEFAULT_CACHE_QUOTA_PERCENTAGE
) {
   @get:WorkerThread
   val cache: Cache by lazy {
      if (Thread.currentThread().name == "main") {
         error("Disk cache must not be initialized on the main thread")
      }

      val storageManager = context
         .getSystemService(Context.STORAGE_SERVICE) as StorageManager

      val cacheSize = determineCacheSizeBytes(storageManager)
      createCache(storageManager, cacheSize)
   }

   @WorkerThread
   private fun determineCacheSizeBytes(storageManager: StorageManager): Long {
      return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
         // Older device with unknown cache quota. Just use default size all the time.
         fallbackCacheSize
      } else {
         try {
            val cacheQuota = storageManager.getCacheQuotaBytes(
               storageManager.getUuidForPath(context.cacheDir)
            )

            (cacheQuota * cacheQuotaFraction).roundToLong()
         } catch (e: Exception) {
            errorReporter.report(e)
            // Cache determining error. Just fallback to default value
            fallbackCacheSize
         }
      }
   }

   private fun createCache(storageManager: StorageManager, cacheSizeBytes: Long): Cache {
      val absoluteCacheFolder = File(context.cacheDir, cacheSubfolderName)
      absoluteCacheFolder.mkdirs()

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         storageManager.setCacheBehaviorGroup(absoluteCacheFolder, true)
      }

      return Cache(absoluteCacheFolder, cacheSizeBytes)
   }
}

private const val DEFAULT_FALLBACK_CACHE_SIZE_BYTES = 20_000_000L // 20 MB
private const val DEFAULT_CACHE_SUBFOLDER = "okdisk"

// Using third of our cache quota for OK HTTP disk cache requests seems reasonable.
private const val DEFAULT_CACHE_QUOTA_PERCENTAGE = 0.3f
