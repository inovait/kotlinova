/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.ui.lists

import android.content.Context
import android.os.Handler
import android.view.View
import com.google.android.material.snackbar.Snackbar
import si.inova.kotlinova.android.R

/**
 * Class that handles undo for deleted items in lists
 *
 * On start init all callbacks for deleting
 * To enable undo, use [performVisualDelete()][performVisualDelete] method.
 *
 * On method [onStop()] call [commitDeletionsAll()[commitDeletionsAll]
 *
 * @author Kristjan Kotnik
 */
class UndoHelper<T>(
        private val context: Context,
        private val rootView: View,
        private val deleteText: String,
        private val restoreText: String
) {
    var deleteCallback: ((T) -> Unit)? = null
    var restoreCallBack: (() -> Unit)? = null
    var notifyItemRemovedCallback: (() -> Unit)? = null
    private val itemsToRemove = mutableListOf<T>()
    private val handler = Handler()
    private val runnable = Runnable { commitDeletionsAll() }

    fun performVisualDelete(itemId: T) {
        removeCallbacks()
        itemsToRemove.add(itemId)
        Snackbar
                .make(rootView, deleteText, Snackbar.LENGTH_LONG)
                .setAction(context.getString(R.string.undo)) {
                    commitDeletionsAllButLast()
                    restoreCallBack?.invoke()
                    Snackbar.make(rootView, restoreText, Snackbar.LENGTH_SHORT).show()
                }.show()
        notifyItemRemovedCallback?.invoke()
        handler.postDelayed(runnable, SNACKBAR_DISPLAY_DELAY)
    }

    private fun removeCallbacks() {
        handler.removeCallbacks(runnable)
    }

    fun commitDeletionsAll() {
        for (i in 0 until itemsToRemove.count()) {
            deleteCallback?.invoke(itemsToRemove[i])
        }
        itemsToRemove.clear()
    }

    private fun commitDeletionsAllButLast() {
        for (i in 0 until itemsToRemove.count() - 1) {
            deleteCallback?.invoke(itemsToRemove[i])
        }
        removeCallbacks()
        itemsToRemove.clear()
    }
}

private const val SNACKBAR_DISPLAY_DELAY = 3500L