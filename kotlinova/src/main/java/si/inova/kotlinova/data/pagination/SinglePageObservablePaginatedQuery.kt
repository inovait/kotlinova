package si.inova.kotlinova.data.pagination

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import si.inova.kotlinova.data.Resource

/**
 * [PaginatedQuery] that only provides single page (one list of items)
 *
 * @author Matej Drobnic
 */

class SinglePageObservablePaginatedQuery<T>(private val providedData: List<T>) :
    ObservablePaginatedQuery<T> {

    override val isAtEnd: Boolean = true

    private val _data = BehaviorSubject.create<Resource<List<T>>>()
    override val data: Flowable<Resource<List<T>>> = _data.toFlowable(BackpressureStrategy.LATEST)

    override suspend fun loadFirstPage() {
        _data.onNext(Resource.Success(providedData))
    }

    override suspend fun loadNextPage() = Unit
}