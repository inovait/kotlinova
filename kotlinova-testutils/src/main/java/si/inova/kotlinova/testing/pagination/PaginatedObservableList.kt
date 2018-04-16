package si.inova.kotlinova.testing.pagination

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.sync.Mutex
import si.inova.kotlinova.data.Resource
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
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

    private var curPage = 1

    override val isAtEnd: Boolean
        get() = curPage >= currentList.size

    private val _data = BehaviorSubject.create<Resource<List<T>>>()
    override val data: Flowable<Resource<List<T>>>
        get() = _data.toFlowable(BackpressureStrategy.DROP)

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