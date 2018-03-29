package si.inova.kotlinova.gms

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import si.inova.kotlinova.data.Resource
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.data.pagination.PaginatedQuery

/**
 * @author Matej Drobnic
 *
 * Wrapper around Firebase Firestore's query that can retrieve data in observable pages.
 */
class ObservableFirebasePaginatedQuery<T>(
    private val baseQuery: Query,
    private val targetClass: Class<T>,
    private val itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
) : EventListener<QuerySnapshot>,
        ObservablePaginatedQuery<Pair<String, T>>,
        MutableLiveData<Resource<List<Pair<String, T>>>>() {

    private var currentQuery: Query? = null
    private var currentListenerRegistration: ListenerRegistration? = null
    private var numItemsFetched = 0

    private var waitingForFirstValue: Boolean = false

    init {
        loadNextPage()
    }

    override fun loadNextPage() {
        if (isAtEnd || waitingForFirstValue) {
            return
        }

        waitingForFirstValue = true
        numItemsFetched += itemsPerPage

        currentListenerRegistration?.remove()
        currentListenerRegistration = null

        val newQuery = baseQuery.limit(numItemsFetched.toLong())
        currentQuery = newQuery

        if (hasActiveObservers()) {
            currentListenerRegistration = newQuery.addSnapshotListener(this)
        }
    }

    override val data: LiveData<Resource<List<Pair<String, T>>>>
        get() = this

    override var isAtEnd = false
        private set

    override fun onEvent(snapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            postValue(Resource.Error(e))
            return
        }

        if (snapshot != null) {
            val data = snapshot.map {
                Pair(it.id, it.toObject(targetClass))
            }

            postValue(Resource.Success(data))

            if (waitingForFirstValue) {
                isAtEnd = data.size < numItemsFetched
                waitingForFirstValue = false
            } else {
                // Data got updated.
                // Force isAtEnd to false to re-check if there was any additional data added.
                isAtEnd = false
            }
        }
    }

    override fun onActive() {
        currentListenerRegistration = currentQuery?.addSnapshotListener(this)
    }

    override fun onInactive() {
        currentListenerRegistration?.remove()
        currentListenerRegistration = null
    }
}

/**
 * Convenience operator that converts regular [Query] into [PaginatedQuery]
 */
inline fun <reified T> Query.paginateObservable(
    itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
): ObservablePaginatedQuery<Pair<String, T>> {
    return ObservableFirebasePaginatedQuery(this, T::class.java, itemsPerPage)
}