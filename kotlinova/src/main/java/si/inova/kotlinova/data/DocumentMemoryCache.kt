package si.inova.kotlinova.data

import android.support.v4.util.SimpleArrayMap
import si.inova.kotlinova.time.TimeProvider

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
class DocumentMemoryCache constructor(private val cacheDuration: Long) {
    private val storage = SimpleArrayMap<Any, CacheEntry>()

    /**
     * Put value into cache.
     */
    @Synchronized
    operator fun set(key: Any, value: Any) {
        val expirationTime = TimeProvider.elapsedRealtime() + cacheDuration
        storage.put(key, CacheEntry(expirationTime, value))
    }

    /**
     * Get value from cache. If item from that key does not yet exist or if it has gotten stale,
     * this method will return *null*.
     */
    @Synchronized
    operator fun <T> get(key: Any): T? {
        val entry = storage[key] ?: return null
        if (entry.expirationTime < TimeProvider.elapsedRealtime()) {
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

    inline fun <T> getOrProduce(
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
        val now = TimeProvider.elapsedRealtime()
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