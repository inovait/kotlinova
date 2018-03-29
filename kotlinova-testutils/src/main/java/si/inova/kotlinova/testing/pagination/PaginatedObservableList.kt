package si.inova.kotlinova.testing.pagination

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import si.inova.kotlinova.data.Resource
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.testing.OpenForTesting

/**
 * [ObservablePaginatedQuery] that provides complete list of pages
 *
 * @author Matej Drobnic
 */
@OpenForTesting
class PaginatedObservableList<T>(initialData: List<List<T>>) : ObservablePaginatedQuery<T> {
    var currentList: List<List<T>> = initialData
        set(value) {
            field = value
            updateObservable()
        }

    private var curPage = 1

    override val isAtEnd: Boolean
        get() = curPage >= currentList.size

    private val _data = MutableLiveData<Resource<List<T>>>()
    override val data: LiveData<Resource<List<T>>>
        get() = _data

    init {
        updateObservable()
    }

    override fun loadNextPage() {
        if (isAtEnd) {
            return
        }

        curPage++
        updateObservable()
    }

    private fun updateObservable() {
        val loadedPages = currentList.subList(0, curPage)
        _data.value = Resource.Success(loadedPages.flatten())
    }
}