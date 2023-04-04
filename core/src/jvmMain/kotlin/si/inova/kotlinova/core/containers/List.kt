package si.inova.kotlinova.core.containers

/**
 * @return A shallow copy of the list. This is equivalent to just creating
 * a new list based on the previous one (`ArrayList(oldList)`), but calling copy function is more explicit.
 */
fun <T> List<T>.copy(): List<T> {
   return ArrayList<T>(this)
}
