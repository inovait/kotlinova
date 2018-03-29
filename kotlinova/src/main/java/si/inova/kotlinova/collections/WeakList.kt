package si.inova.kotlinova.collections

import java.lang.ref.WeakReference
import java.util.LinkedList

/**
 * Collection that holds weak references to objects.
 * Since references are cleaned up while iterating, only supported reading operation is
 * iterating.
 *
 * @author Matej Drobnic
 */
class WeakList<T> {
    private val storage: MutableList<WeakReference<T>> = LinkedList<WeakReference<T>>()

    fun add(item: T) {
        storage.add(WeakReference(item))
    }

    fun remove(item: T) {
        storage.removeAll { it.get() == item }
    }

    fun clear() {
        storage.clear()
    }

    fun forEach(action: (T) -> Unit) {
        val iterator = storage.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val item = ref.get()

            if (item == null) {
                iterator.remove()
            } else {
                action(item)
            }
        }
    }
}