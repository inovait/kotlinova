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

package si.inova.kotlinova.testing.pagination

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.sync.Mutex
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.testing.OpenForTesting
import si.inova.kotlinova.utils.awaitUnlockIfLocked

/**
 * [ObservablePaginatedQuery] that provides complete list of pages.
 *
 * You can send pages in asynchronously with [freeze()][freeze] and [unfreeze()][unfreeze] methods.
 *
 * @author Matej Drobnic
 */
@OpenForTesting
class PaginatedObservableList<T>(initialData: List<List<T>>) : ObservablePaginatedQuery<T> {
    private val loadingLatch = Mutex()

    var currentList: List<List<T>> = initialData
        set(value) {
            field = value
            updateObservable()
        }

    private var curPage = 0

    override val isAtEnd: Boolean
        get() = curPage >= currentList.size

    private val _data = BehaviorSubject.create<Resource<List<T>>>()
    override val data: Flowable<Resource<List<T>>>
        get() = _data.toFlowable(BackpressureStrategy.LATEST)

    /**
     * Freeze data transmission for this query. [loadFirstPage()][loadFirstPage] and
     * [loadNextPage()][loadNextPage] methods
     * will not return until [unfreeze()][unfreeze] is called
     */
    fun freeze() {
        if (!loadingLatch.tryLock()) {
            throw IllegalStateException("Already frozen")
        }
    }

    fun unfreeze() {
        loadingLatch.unlock()
    }

    override suspend fun loadFirstPage() {
        loadingLatch.awaitUnlockIfLocked()

        curPage = 1
        updateObservable()
    }

    override suspend fun loadNextPage() {
        loadingLatch.awaitUnlockIfLocked()

        if (isAtEnd) {
            return
        }

        curPage++
        updateObservable()
    }

    private fun updateObservable() {
        val loadedPages = currentList.subList(0, curPage)
        _data.onNext(Resource.Success(loadedPages.flatten()))
    }
}