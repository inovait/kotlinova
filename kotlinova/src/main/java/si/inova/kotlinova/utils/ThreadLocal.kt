/**
 * @author Matej Drobnic
 */
@file:JvmName("ThreadLocalUtils")

package si.inova.kotlinova.utils

fun <T> threadLocal(creator: () -> T): ThreadLocal<T> {
    return object : ThreadLocal<T>() {
        override fun initialValue(): T {
            return creator()
        }
    }
}