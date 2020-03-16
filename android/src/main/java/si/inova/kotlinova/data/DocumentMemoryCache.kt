/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.data

import androidx.collection.SimpleArrayMap
import si.inova.kotlinova.testing.OpenForTesting
import si.inova.kotlinova.time.AndroidTimeProvider

/**
 * Global memory cache for storing documents and other objects which size cannot be measured easily.
 *
 * Each entry in cache gets stale after [cacheDuration] milliseconds. When stale,
 * entry will not be retrievable
 * anymore. Cleanup does not happen automatically, but only when entry gets replaced with new entry
 * or when either [evictStaleEntries()][evictStaleEntries] or [clear()][clear] is called.
 *
 * It is recommended to bind this class to
 * [onTrimMemory()][android.content.ComponentCallbacks2.onTrimMemory] android callbacks to
 * automatically clean cached items when memory gets low.
 *
 * Keys used must properly support equals and hashcode operations.
 *
 * @param cacheDuration Maximum time that entry can be in cache, in milliseconds
 */
@OpenForTesting
class DocumentMemoryCache constructor(private val cacheDuration: Long) {
    private val storage = SimpleArrayMap<Any, CacheEntry>()

    /**
     * Put value into cache.
     */
    @Synchronized
    operator fun set(key: Any, value: Any) {
        val expirationTime = AndroidTimeProvider.elapsedRealtime() + cacheDuration
        storage.put(key, CacheEntry(expirationTime, value))
    }

    /**
     * Get value from cache. If item from that key does not yet exist or if it has gotten stale,
     * this method will return *null*.
     */
    @Synchronized
    operator fun <T> get(key: Any): T? {
        val entry = storage[key] ?: return null
        if (entry.expirationTime < AndroidTimeProvider.elapsedRealtime()) {
            storage.remove(key)
            return null
        }

        @Suppress("UNCHECKED_CAST")
        return entry.value as T
    }

    /**
     * Manually evict single entry from cache
     */
    @Synchronized
    fun evict(key: Any) {
        storage.remove(key)
    }

    /**
     * Get value from cache. If item from that key does not yet exist, if it has gotten stale,
     * or if [forceProduce] is *true*, this method will call [producer], insert its result into
     * cache and return the result.
     */

    final inline fun <T> getOrProduce(
            key: Any,
            forceProduce: Boolean = false,
            producer: () -> T
    ): T {
        val existingValue: T? = if (forceProduce) null else get(key)

        return if (existingValue != null) {
            existingValue
        } else {
            val newValue = producer()
            set(key, newValue as Any)
            newValue
        }
    }

    /**
     * Number of all elements in the cache, including stale ones
     */
    val size: Int
        get() = storage.size()

    /**
     * Manually clean all stale entries from the cache storage
     */
    @Synchronized
    fun evictStaleEntries() {
        val now = AndroidTimeProvider.elapsedRealtime()
        val keysToRemove = (0 until size).filter {
            storage.valueAt(it).expirationTime < now
        }

        keysToRemove.forEach { storage.removeAt(it) }
    }

    /**
     * Remove all entries from cache
     */
    @Synchronized
    fun clear() {
        storage.clear()
    }

    private data class CacheEntry(val expirationTime: Long, val value: Any)
}