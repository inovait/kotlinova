@file:JvmName("MapUtils")

package si.inova.kotlinova.core.containers

/**
 * Method that puts value into map only if said value is not *null*.
 */
fun <K, V> MutableMap<K, V>.putIfNotNull(key: K, value: V?) {
   if (value != null) {
      put(key, value)
   }
}
