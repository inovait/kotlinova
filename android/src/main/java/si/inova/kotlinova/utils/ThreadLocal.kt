/**
 * @author Matej Drobnic
 */
@file:JvmName("ThreadLocalUtils")

package si.inova.kotlinova.utils

/**
 * Convenience method to create [ThreadLocal] in Kotlin style
 */
fun <T> threadLocal(creator: () -> T): ThreadLocal<T> {
    return object : ThreadLocal<T>() {
        override fun initialValue(): T {
            return creator()
        }
    }
}