@file:JvmName("GsonUtils")

package si.inova.kotlinova.utils

import com.google.gson.Gson

/**
 * @author Matej Drobnic
 */
inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}