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

package si.inova.kotlinova.navigation.fragment.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Find an activity that owns this context or *null* if there is no such activity
 */
internal fun Context.findActivity(): Activity? {
   var currentContext: Context? = this

   while (currentContext != null) {
      when (currentContext) {
         is Activity -> {
            return currentContext
         }

         is ContextWrapper -> {
            currentContext = currentContext.baseContext
         }

         else -> {
            return null
         }
      }
   }

   return null
}

/**
 * Find an activity that owns this context or throw [IllegalStateException] if there is no such activity
 */
internal fun Context.requireActivity(): Activity {
   return findActivity() ?: error("$this is not an activity context")
}
