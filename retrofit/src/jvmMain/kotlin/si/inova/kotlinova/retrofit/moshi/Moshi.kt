/**
 * Utilities for Moshi library
 *
 * @author Matej Drobnic
 */
@file:JvmName("MoshiUtils")

package si.inova.kotlinova.retrofit.moshi

import com.squareup.moshi.Moshi
import okio.BufferedSource

inline fun <reified T> Moshi.fromJson(json: String): T {
   return requireNotNull(adapter(T::class.java).nonNull().fromJson(json))
}

inline fun <reified T> Moshi.fromJson(source: BufferedSource): T {
   return requireNotNull(adapter(T::class.java).nonNull().fromJson(source))
}

inline fun <reified T> Moshi.toJson(value: T): String {
   return requireNotNull(adapter(T::class.java).nonNull().toJson(value))
}
