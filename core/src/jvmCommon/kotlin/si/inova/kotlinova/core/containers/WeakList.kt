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

package si.inova.kotlinova.core.containers

import java.lang.ref.WeakReference
import java.util.LinkedList

/**
 * Collection that holds weak references to objects.
 * Since references are cleaned up while iterating, only supported reading operation is
 * iterating.
 */
class WeakList<T> : Iterable<T> {
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

   override fun iterator(): Iterator<T> {
      return iterator {
         val storageIterator = storage.iterator()
         while (storageIterator.hasNext()) {
            val ref = storageIterator.next()
            val item = ref.get()

            if (item == null) {
               storageIterator.remove()
            } else {
               yield(item)
            }
         }
      }
   }
}
