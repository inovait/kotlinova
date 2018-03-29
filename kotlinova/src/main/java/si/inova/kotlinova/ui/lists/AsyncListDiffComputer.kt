package si.inova.kotlinova.ui.lists

import android.support.v7.util.DiffUtil
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