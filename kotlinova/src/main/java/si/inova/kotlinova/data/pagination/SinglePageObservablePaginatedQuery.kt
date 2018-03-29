package si.inova.kotlinova.data.pagination

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import si.inova.kotlinova.data.Resource

/**
 * [PaginatedQuery] that only provides single page (one list of items)
 *
 * @author Matej Drobnic
 */
class SinglePageObservablePaginatedQuery<T>(providedData: List<T>) : ObservablePaginatedQuery<T> {
    override val isAtEnd: Boolean = true

    private val _data = MutableLiveData<Resource<List<T>>>()
    override val data: LiveData<Resource<List<T>>> = _data

    init {
        _data.value = Resource.Success(providedData)
    }

    override fun loadNextPage() = Unit
}