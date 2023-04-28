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

package si.inova.kotlinova.navigation.simplestack

import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.GlobalServices
import java.lang.ref.WeakReference

/**
 * Lookup a service from the scope, and if it's not found, lookup from the parent backstack.
 *
 * This requires that the parent backstack was registered with [registerParentBackstack] when this backstack has been created.
 */
fun <T> Backstack.lookupFromScopeWithParentFallback(scopeTag: String, serviceTag: String): T {
   if (!canFindService(ParentBackstackHolder::class.java.name)) {
      return lookupFromScope(scopeTag, serviceTag)
   }

   val parentBackstackHolder = lookupService<ParentBackstackHolder>(ParentBackstackHolder::class.java.name)
   val parentBackstack = parentBackstackHolder.backstack

   return if (parentBackstack == null || canFindFromScope(scopeTag, serviceTag)) {
      lookupFromScope<T>(scopeTag, serviceTag)
   } else {
      parentBackstack.lookupFromScope(parentBackstackHolder.scopeTag, serviceTag)
   }
}

/**
 * Register a reference to a parent backstack. This allows [lookupFromScopeWithParentFallback] to work.
 */
fun GlobalServices.Builder.registerParentBackstack(backstack: Backstack, scopeTag: String) {
   this.addService(ParentBackstackHolder::class.java.name, ParentBackstackHolder(backstack, scopeTag))
}

class ParentBackstackHolder(backstack: Backstack, val scopeTag: String) {
   private val holder = WeakReference(backstack)

   val backstack: Backstack?
      get() = holder.get()
}
