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

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import si.inova.kotlinova.coroutines.TestableDispatchers
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.testing.DispatcherReplacementRule
import si.inova.kotlinova.testing.RxSchedulerRule
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import si.inova.kotlinova.testing.assertDocumentsEqual
import si.inova.kotlinova.testing.assertIs
import si.inova.kotlinova.testing.firebase.MockCollection
import si.inova.kotlinova.testing.firebase.MockDocument

/**
 * @author Matej Drobnic
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class ObservableFirebasePaginatedQueryTest {
    @Mock(answer = Answers.RETURNS_SELF)
    private lateinit var query: Query

    @Mock
    private lateinit var listenerRegistration: ListenerRegistration

    private lateinit var observableFirebasePaginatedQuery:
        ObservablePaginatedQuery<DocumentSnapshot>
    private var listener: EventListener<QuerySnapshot>? = null

    val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val dispatcherReplacement = DispatcherReplacementRule(dispatcher)

    @get:Rule
    val rxJavaSchedulerRule = RxSchedulerRule()

    @get:Rule
    val uncaughtExceptionThrowRule = UncaughtExceptionThrowRule()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(query.addSnapshotListener(any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            listener = it.arguments[0] as EventListener<QuerySnapshot>
            listenerRegistration
        }

        observableFirebasePaginatedQuery = query.paginateObservable(TEST_ITEMS_PER_PAGE)
    }

    @Test
    fun registerUnregisterListenerOnLifecycle() = runBlocking<Unit> {
        async(Dispatchers.Unconfined) {
            observableFirebasePaginatedQuery.loadFirstPage()
        }

        listener!!.onEvent(MockCollection(emptyList<Pair<String, Int>>()).toQuerySnapshot(), null)

        inOrder(listenerRegistration, query) {
            var subscription = observableFirebasePaginatedQuery.data.subscribe()
            assertNotNull(listener)
            verify(query).addSnapshotListener(any())

            subscription.dispose()
            dispatcher.advanceTimeBy(600)
            verify(listenerRegistration).remove()

            subscription = observableFirebasePaginatedQuery.data.subscribe()
            verify(query).addSnapshotListener(any())

            subscription.dispose()
            dispatcher.advanceTimeBy(600)
            verify(listenerRegistration).remove()
        }
    }

    @Test
    fun singlePage() = runBlocking {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "30" to 30
        )

        var valuePlaceholder: Resource<List<DocumentSnapshot>>? = null
        observableFirebasePaginatedQuery.data.subscribe {
            valuePlaceholder = it
        }

        async(TestableDispatchers.Default) { observableFirebasePaginatedQuery.loadFirstPage() }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val result = valuePlaceholder
        assertIs(result, Resource.Success::class.java)
        result as Resource.Success
        assertDocumentsEqual(
            testData.map { MockDocument(it.first, it.second).toDocumentSnap() },
            result.data
        )
    }

    @Test
    fun multiPage() = runBlocking {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "40" to 40,
            "50" to 50
        )

        var valuePlaceholder: Resource<List<DocumentSnapshot>>? = null
        observableFirebasePaginatedQuery.data.subscribe {
            valuePlaceholder = it
        }

        async(TestableDispatchers.Default) { observableFirebasePaginatedQuery.loadFirstPage() }

        assertNotNull(listener)

        // We do not test actual page separation (Firebase SDK does that behind the scenes).
        // Just make sure right limits were called

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        async(TestableDispatchers.Default) { observableFirebasePaginatedQuery.loadNextPage() }

        inOrder(query) {
            verify(query).limit(2L)
            verify(query).limit(4L)
        }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val result = valuePlaceholder
        assertIs(result, Resource.Success::class.java)
        result as Resource.Success
        assertDocumentsEqual(
            testData.map { MockDocument(it.first, it.second).toDocumentSnap() },
            result.data
        )
    }

    @Test
    fun isAtEnd() {
        val firstPage = listOf(
            "10" to 10,
            "20" to 20
        )

        val firstAndSecondPage = listOf(
            "10" to 10,
            "20" to 20,
            "40" to 40
        )

        val newFirstAndSecondPage = listOf(
            "11" to 11,
            "20" to 20,
            "40" to 40
        )

        GlobalScope.async(
            TestableDispatchers.Default,
            CoroutineStart.DEFAULT,
            { observableFirebasePaginatedQuery.loadFirstPage() })
        observableFirebasePaginatedQuery.data.subscribe()

        listener!!.onEvent(MockCollection(firstPage).toQuerySnapshot(), null)
        assertFalse(observableFirebasePaginatedQuery.isAtEnd)

        GlobalScope.async(
            TestableDispatchers.Default,
            CoroutineStart.DEFAULT,
            { observableFirebasePaginatedQuery.loadNextPage() })
        listener!!.onEvent(MockCollection(firstAndSecondPage).toQuerySnapshot(), null)
        assertTrue(observableFirebasePaginatedQuery.isAtEnd)

        // Data got updated - there could be more out there
        listener!!.onEvent(MockCollection(newFirstAndSecondPage).toQuerySnapshot(), null)
        assertFalse(observableFirebasePaginatedQuery.isAtEnd)
    }

    @Test
    fun fetchOnePageAtATime() {
        GlobalScope.async(
            TestableDispatchers.Default,
            CoroutineStart.DEFAULT,
            { observableFirebasePaginatedQuery.loadFirstPage() })
        observableFirebasePaginatedQuery.data.subscribe()

        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "30" to 30,
            "40" to 40,
            "50" to 50,
            "60" to 60
        )

        inOrder(query) {
            verify(query).limit(2L)

            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

            verify(query, never()).limit(any())
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            verify(query).limit(4L)

            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

            verify(query, never()).limit(any())
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })
            verify(query).limit(6L)
        }
    }

    @Test
    fun doNotRefetchAfterEnd() {
        val singlePage = listOf(
            "10" to 10
        )
        GlobalScope.async(
            TestableDispatchers.Default,
            CoroutineStart.DEFAULT,
            { observableFirebasePaginatedQuery.loadFirstPage() })
        observableFirebasePaginatedQuery.data.subscribe()

        inOrder(query) {
            verify(query).limit(2L)
            listener!!.onEvent(MockCollection(singlePage).toQuerySnapshot(), null)

            runBlocking {
                observableFirebasePaginatedQuery.loadNextPage()
                observableFirebasePaginatedQuery.loadNextPage()
                observableFirebasePaginatedQuery.loadNextPage()
                observableFirebasePaginatedQuery.loadNextPage()
                observableFirebasePaginatedQuery.loadNextPage()
                observableFirebasePaginatedQuery.loadNextPage()
            }

            verify(query, never()).limit(any())
        }
    }

    @Test
    fun restartListenersAfterFetchingNewPage() {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "40" to 40,
            "50" to 50
        )

        GlobalScope.async(
            TestableDispatchers.Default,
            CoroutineStart.DEFAULT,
            { observableFirebasePaginatedQuery.loadFirstPage() })
        observableFirebasePaginatedQuery.data.subscribe()

        inOrder(query, listenerRegistration) {
            verify(query).addSnapshotListener(any())

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)
            GlobalScope.async(
                TestableDispatchers.Default,
                CoroutineStart.DEFAULT,
                { observableFirebasePaginatedQuery.loadNextPage() })

            verify(listenerRegistration).remove()
            verify(query).addSnapshotListener(any())
        }
    }

    @Test
    fun loadFirstPageDoesNotReturnUntilPageLoaded() {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "30" to 30
        )

        observableFirebasePaginatedQuery.data.subscribe()

        val loadFirstPageTask =
            GlobalScope.async(TestableDispatchers.Default, CoroutineStart.DEFAULT, {
                observableFirebasePaginatedQuery.loadFirstPage()
            })

        assertFalse(loadFirstPageTask.isCompleted)

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)
        assertTrue(loadFirstPageTask.isCompleted)
    }

    @Test
    fun loadNextPageDoesNotReturnUntilPageLoaded() {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "30" to 30
        )

        observableFirebasePaginatedQuery.data.subscribe()

        GlobalScope.async(
            TestableDispatchers.Default,
            CoroutineStart.DEFAULT,
            { observableFirebasePaginatedQuery.loadFirstPage() })
        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val loadNextPageTask =
            GlobalScope.async(TestableDispatchers.Default, CoroutineStart.DEFAULT, {
                observableFirebasePaginatedQuery.loadNextPage()
            })

        assertFalse(loadNextPageTask.isCompleted)

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)
        assertTrue(loadNextPageTask.isCompleted)
    }

    @Test
    fun synchronousDataArrival() {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "30" to 30
        )

        var valuePlaceholder: Resource<List<DocumentSnapshot>>? = null
        observableFirebasePaginatedQuery.data.subscribe {
            valuePlaceholder = it
        }

        whenever(query.addSnapshotListener(any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            listener = it.arguments[0] as EventListener<QuerySnapshot>
            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

            listenerRegistration
        }

        runBlocking { observableFirebasePaginatedQuery.loadFirstPage() }

        val result = valuePlaceholder
        assertIs(result, Resource.Success::class.java)
        result as Resource.Success
        assertDocumentsEqual(
            testData.map { MockDocument(it.first, it.second).toDocumentSnap() },
            result.data
        )
    }

    @Test
    fun keepIsAtEndAfterObserverChange() = runBlocking {
        val singlePage = listOf(
            "10" to 10
        )

        val subscriber = observableFirebasePaginatedQuery.data.subscribe()
        async(TestableDispatchers.Default) { observableFirebasePaginatedQuery.loadFirstPage() }

        inOrder(query) {
            verify(query).limit(2L)
            listener!!.onEvent(MockCollection(singlePage).toQuerySnapshot(), null)
            assertTrue(observableFirebasePaginatedQuery.isAtEnd)

            subscriber.dispose()
            dispatcher.advanceTimeBy(600)
            observableFirebasePaginatedQuery.data.subscribe()
            listener!!.onEvent(MockCollection(singlePage).toQuerySnapshot(), null)
            assertTrue(observableFirebasePaginatedQuery.isAtEnd)
        }
    }

    @Test
    fun resetObserverBeforeLoadingNextPage() = runBlocking {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "40" to 40,
            "50" to 50
        )

        var valuePlaceholder: Resource<List<DocumentSnapshot>>? = null
        val subscriber = observableFirebasePaginatedQuery.data.subscribe {
            valuePlaceholder = it
        }

        async(TestableDispatchers.Default) { observableFirebasePaginatedQuery.loadFirstPage() }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        subscriber.dispose()
        dispatcher.advanceTimeBy(600)
        observableFirebasePaginatedQuery.data.subscribe {
            valuePlaceholder = it
        }

        async(TestableDispatchers.Default) { observableFirebasePaginatedQuery.loadNextPage() }

        inOrder(query) {
            verify(query).limit(2L)
            verify(query).limit(4L)
        }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val result = valuePlaceholder
        assertIs(result, Resource.Success::class.java)
        result as Resource.Success
        assertDocumentsEqual(
            testData.map { MockDocument(it.first, it.second).toDocumentSnap() },
            result.data
        )
    }
}

private const val TEST_ITEMS_PER_PAGE = 2