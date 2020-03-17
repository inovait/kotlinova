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

import androidx.recyclerview.widget.DiffUtil
import si.inova.kotlinova.data.LastResultAsyncItemProcessor
import si.inova.kotlinova.data.process

/**
 * Class that computes [DiffUtil.DiffResult] between two lists asynchronously and provides callback
 * when finished
 *
 * @author Matej Drobnic
 */
class AsyncListDiffComputer {
    private val asyncProcessor = LastResultAsyncItemProcessor<Unit, DiffUtil.DiffResult>()

    fun <T> computeDiffs(
        diffProvider: ListDiffProvider<T>,
        oldList: List<T>?,
        newList: List<T>,
        callback: (DiffUtil.DiffResult) -> Unit
    ) {

        val diffUtilCallback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffProvider
                    .areItemsTheSame(oldList!![oldItemPosition], newList[newItemPosition])
            }

            override fun getOldListSize(): Int {
                return oldList?.size ?: 0
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffProvider
                    .areContentsSame(oldList!![oldItemPosition], newList[newItemPosition])
            }
        }

        asyncProcessor.process(
            {
                callback(it)
            },
            {
                DiffUtil.calculateDiff(diffUtilCallback)
            })
    }
}

interface ListDiffProvider<in T> {
    /**
     * @return Whether *first* and *second* items are exact same items,
     * but not do not necessarily have same content (for example data in one could be outdated)
     */
    fun areItemsTheSame(first: T, second: T): Boolean

    /**
     * @return Whether *first* and *second* items contain exact same data
     */
    fun areContentsSame(first: T, second: T): Boolean
}