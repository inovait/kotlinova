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

package si.inova.kotlinova.gms

import com.google.firebase.firestore.*
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.rx.OnDemandProvider
import si.inova.kotlinova.utils.awaitUnlockIfLocked
import si.inova.kotlinova.utils.use

/**
 * @author Matej Drobnic
 *
 * Wrapper around Firebase Firestore's query that can retrieve data in observable pages.
 */
class ObservableFirebasePaginatedQuery(
        private val baseQuery: Query,
        private val itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
) : OnDemandProvider<Resource<List<DocumentSnapshot>>>(Dispatchers.Main),
        EventListener<QuerySnapshot>,
        ObservablePaginatedQuery<DocumentSnapshot> {

    private var currentQuery: Query? = null
    private var currentListenerRegistration: ListenerRegistration? = null
    private var numItemsFetched = 0

    private var waitingForFirstValue: Boolean = false
    private var waitingForFirstNextPageValue: Boolean = false
    private var loadingLatch = Mutex(false)

    override suspend fun loadFirstPage() {
        numItemsFetched = 0
        loadNextPage()
    }

    override suspend fun loadNextPage() = data.use {
        if (isAtEnd || waitingForFirstNextPageValue) {
            return@use
        }

        waitingForFirstValue = true
        waitingForFirstNextPageValue = true
        numItemsFetched += itemsPerPage

        currentListenerRegistration?.remove()
        currentListenerRegistration = null

        val newQuery = baseQuery.limit(numItemsFetched.toLong())
        currentQuery = newQuery

        loadingLatch.lock()
        currentListenerRegistration = newQuery.addSnapshotListener(this)
        loadingLatch.awaitUnlockIfLocked()
    }

    override val data: Flowable<Resource<List<DocumentSnapshot>>>
        get() = flowable

    override var isAtEnd = false
        private set

    override fun onEvent(snapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (!isActive) {
            // Event was received after we went inactive
            // ignore it

            return
        }

        if (e != null) {
            offer(Resource.Error(e))
            return
        }

        if (snapshot != null) {
            val data = snapshot.documents

            offer(Resource.Success(data))

            if (waitingForFirstValue) {
                if (waitingForFirstNextPageValue) {
                    loadingLatch.unlock()
                }

                isAtEnd = data.size < numItemsFetched
                waitingForFirstValue = false
            } else {
                // Data got updated.
                // Force isAtEnd to false to re-check if there was any additional data added.
                isAtEnd = false
            }

            waitingForFirstNextPageValue = false
        }
    }

    override suspend fun CoroutineScope.onActive() {
        currentQuery?.let {
            waitingForFirstValue = true
            currentListenerRegistration =
                    it.addSnapshotListener(this@ObservableFirebasePaginatedQuery)
        }
    }

    override fun onInactive() {
        currentListenerRegistration?.remove()
        currentListenerRegistration = null
    }
}

/**
 * Convenience operator that converts regular [Query] into [ObservablePaginatedQuery]
 */
fun Query.paginateObservable(
        itemsPerPage: Int = DEFAULT_PAGINATION_LIMIT
): ObservablePaginatedQuery<DocumentSnapshot> {
    return ObservableFirebasePaginatedQuery(this, itemsPerPage)
}