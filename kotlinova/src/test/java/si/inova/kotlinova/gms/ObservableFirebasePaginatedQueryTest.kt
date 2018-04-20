package si.inova.kotlinova.gms

import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.whenever
import io.github.plastix.rxschedulerrule.RxSchedulerRule
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import si.inova.kotlinova.coroutines.CommonPool
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.testing.TimedDispatcher
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import si.inova.kotlinova.testing.assertIs
import si.inova.kotlinova.testing.firebase.MockCollection

/**
 * @author Matej Drobnic
 */
class ObservableFirebasePaginatedQueryTest {
    @Mock(answer = Answers.RETURNS_SELF)
    private lateinit var query: Query
    @Mock
    private lateinit var listenerRegistration: ListenerRegistration

    private lateinit var observableFirebasePaginatedQuery:
        ObservablePaginatedQuery<Pair<String, Int>>
    private var listener: EventListener<QuerySnapshot>? = null

    @get:Rule
    val dispatcher = TimedDispatcher()
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

        observableFirebasePaginatedQuery = query.paginateObservable<Int>(TEST_ITEMS_PER_PAGE)
    }

    @Test
    fun registerUnregisterListenerOnLifecycle() {
        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }
        listener!!.onEvent(MockCollection(emptyList<Pair<String, Int>>()).toQuerySnapshot(), null)

        inOrder(listenerRegistration, query) {
            var subscription = observableFirebasePaginatedQuery.data.subscribe()
            assertNotNull(listener)
            verify(query).addSnapshotListener(any())

            subscription.dispose()
            dispatcher.advanceTime(600)
            verify(listenerRegistration).remove()

            subscription = observableFirebasePaginatedQuery.data.subscribe()
            verify(query).addSnapshotListener(any())

            subscription.dispose()
            dispatcher.advanceTime(600)
            verify(listenerRegistration).remove()

            verify(query, never()).addSnapshotListener(any())
        }
    }

    @Test
    fun singlePage() = runBlocking {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "30" to 30
        )

        var valuePlaceholder: Resource<List<Pair<String, Int>>>? = null
        observableFirebasePaginatedQuery.data.subscribe {
            valuePlaceholder = it
        }

        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val result = valuePlaceholder
        assertIs(result, Resource.Success::class.java)
        result as Resource.Success
        assertEquals(testData, result.data)
    }

    @Test
    fun multiPage() = runBlocking {
        val testData = listOf(
            "10" to 10,
            "20" to 20,
            "40" to 40,
            "50" to 50
        )

        var valuePlaceholder: Resource<List<Pair<String, Int>>>? = null
        observableFirebasePaginatedQuery.data.subscribe {
            valuePlaceholder = it
        }

        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }

        assertNotNull(listener)

        // We do not test actual page separation (Firebase SDK does that behind the scenes).
        // Just make sure right limits were called

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }

        inOrder(query) {
            verify(query).limit(2L)
            verify(query).limit(4L)
        }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val result = valuePlaceholder
        assertIs(result, Resource.Success::class.java)
        result as Resource.Success
        assertEquals(testData, result.data)
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

        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }
        observableFirebasePaginatedQuery.data.subscribe()

        listener!!.onEvent(MockCollection(firstPage).toQuerySnapshot(), null)
        assertFalse(observableFirebasePaginatedQuery.isAtEnd)

        async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
        listener!!.onEvent(MockCollection(firstAndSecondPage).toQuerySnapshot(), null)
        assertTrue(observableFirebasePaginatedQuery.isAtEnd)

        // Data got updated - there could be more out there
        listener!!.onEvent(MockCollection(newFirstAndSecondPage).toQuerySnapshot(), null)
        assertFalse(observableFirebasePaginatedQuery.isAtEnd)
    }

    @Test
    fun fetchOnePageAtATime() {
        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }
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

            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

            verify(query, never()).limit(any())
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            verify(query).limit(4L)

            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

            verify(query, never()).limit(any())
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }
            verify(query).limit(6L)
        }
    }

    @Test
    fun doNotRefetchAfterEnd() {
        val singlePage = listOf(
            "10" to 10
        )
        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }
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

        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }
        observableFirebasePaginatedQuery.data.subscribe()

        inOrder(query, listenerRegistration) {
            verify(query).addSnapshotListener(any())

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)
            async(CommonPool) { observableFirebasePaginatedQuery.loadNextPage() }

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

        val loadFirstPageTask = async(CommonPool) {
            observableFirebasePaginatedQuery.loadFirstPage()
        }

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

        async(CommonPool) { observableFirebasePaginatedQuery.loadFirstPage() }
        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val loadNextPageTask = async(CommonPool) {
            observableFirebasePaginatedQuery.loadNextPage()
        }

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

        var valuePlaceholder: Resource<List<Pair<String, Int>>>? = null
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
        assertEquals(testData, result.data)
    }
}

private const val TEST_ITEMS_PER_PAGE = 2