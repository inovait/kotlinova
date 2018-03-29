package si.inova.kotlinova.gms

import android.arch.lifecycle.Observer
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.whenever
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
import si.inova.kotlinova.testing.firebase.MockCollection
import si.inova.kotlinova.data.Resource
import si.inova.kotlinova.data.pagination.ObservablePaginatedQuery
import si.inova.kotlinova.testing.ImmediateDispatcherRule
import si.inova.kotlinova.testing.assertIs

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
    val rule = ImmediateDispatcherRule()

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
        val observer = Observer<Resource<List<Pair<String, Int>>>> {}
        inOrder(listenerRegistration, query) {
            observableFirebasePaginatedQuery.data.observeForever(observer)
            assertNotNull(listener)
            verify(query).addSnapshotListener(any())

            observableFirebasePaginatedQuery.data.removeObserver(observer)
            verify(listenerRegistration).remove()

            observableFirebasePaginatedQuery.data.observeForever(observer)
            verify(query).addSnapshotListener(any())

            observableFirebasePaginatedQuery.data.removeObserver(observer)
            verify(listenerRegistration).remove()

            verify(query, never()).addSnapshotListener(any())
        }
    }

    @Test
    fun singlePage() {
        val testData = listOf(
                "10" to 10,
                "20" to 20,
                "30" to 30
        )

        observableFirebasePaginatedQuery.data.observeForever { }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val result = observableFirebasePaginatedQuery.data.value
        assertIs(result, Resource.Success::class.java)
        result as Resource.Success
        assertEquals(testData, result.data)
    }

    @Test
    fun multiPage() {
        val testData = listOf(
                "10" to 10,
                "20" to 20,
                "40" to 40,
                "50" to 50
        )

        observableFirebasePaginatedQuery.data.observeForever { }
        assertNotNull(listener)

        // We do not test actual page separation (Firebase SDK does that behind the scenes).
        // Just make sure right limits were called

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        observableFirebasePaginatedQuery.loadNextPage()

        inOrder(query) {
            verify(query).limit(2L)
            verify(query).limit(4L)
        }

        listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

        val result = observableFirebasePaginatedQuery.data.value
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

        observableFirebasePaginatedQuery.data.observeForever { }

        listener!!.onEvent(MockCollection(firstPage).toQuerySnapshot(), null)
        assertFalse(observableFirebasePaginatedQuery.isAtEnd)

        observableFirebasePaginatedQuery.loadNextPage()
        listener!!.onEvent(MockCollection(firstAndSecondPage).toQuerySnapshot(), null)
        assertTrue(observableFirebasePaginatedQuery.isAtEnd)

        // Data got updated - there could be more out there
        listener!!.onEvent(MockCollection(newFirstAndSecondPage).toQuerySnapshot(), null)
        assertFalse(observableFirebasePaginatedQuery.isAtEnd)
    }

    @Test
    fun fetchOnePageAtATime() {
        observableFirebasePaginatedQuery.data.observeForever { }
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

            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

            verify(query, never()).limit(any())
            observableFirebasePaginatedQuery.loadNextPage()
            verify(query).limit(4L)

            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)

            verify(query, never()).limit(any())
            observableFirebasePaginatedQuery.loadNextPage()
            verify(query).limit(6L)
        }
    }

    @Test
    fun doNotRefetchAfterEnd() {
        val singlePage = listOf(
                "10" to 10
        )

        observableFirebasePaginatedQuery.data.observeForever { }

        inOrder(query) {
            verify(query).limit(2L)
            listener!!.onEvent(MockCollection(singlePage).toQuerySnapshot(), null)

            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()
            observableFirebasePaginatedQuery.loadNextPage()

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

        observableFirebasePaginatedQuery.data.observeForever { }

        inOrder(query, listenerRegistration) {
            verify(query).addSnapshotListener(any())

            listener!!.onEvent(MockCollection(testData).toQuerySnapshot(), null)
            observableFirebasePaginatedQuery.loadNextPage()

            verify(listenerRegistration).remove()
            verify(query).addSnapshotListener(any())
        }
    }
}

private const val TEST_ITEMS_PER_PAGE = 2