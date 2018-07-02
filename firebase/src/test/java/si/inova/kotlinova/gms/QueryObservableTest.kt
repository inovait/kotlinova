package si.inova.kotlinova.gms

import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.value
import si.inova.kotlinova.testing.RxSchedulerRule
import si.inova.kotlinova.testing.TimedDispatcher
import si.inova.kotlinova.testing.firebase.MockCollection

/**
 * @author Matej Drobnic
 */
class QueryObservableTest {
    @get:Rule
    val rxRule = RxSchedulerRule()
    @get:Rule
    val dispatcher = TimedDispatcher()

    @Test
    fun dataRetrieval() {
        val query = MockCollection(
            listOf(
                "A" to 1,
                "B" to 2
            )
        )
        val testObserver = TestSubscriber.create<QuerySnapshot>()

        val documentObservable = QueryObservable(query.toColRef())
        documentObservable.flowable.map { it.value!! }
            .subscribe(testObserver)

        assertEquals(1, testObserver.valueCount())

        query.insertObject("C", 3)

        assertEquals(2, testObserver.valueCount())
        testObserver.values()[0].let {
            assertEquals(2, it.size())
            assertEquals("A", it.documents[0].id)
            assertEquals(1, it.documents[0].toObject(Int::class.java))
            assertEquals("B", it.documents[1].id)
            assertEquals(2, it.documents[1].toObject(Int::class.java))
        }
        testObserver.values()[1].let {
            assertEquals(3, it.size())
            assertEquals("A", it.documents[0].id)
            assertEquals(1, it.documents[0].toObject(Int::class.java))
            assertEquals("B", it.documents[1].id)
            assertEquals(2, it.documents[1].toObject(Int::class.java))
            assertEquals("C", it.documents[2].id)
            assertEquals(3, it.documents[2].toObject(Int::class.java))
        }

        testObserver.assertNoErrors()
    }

    @Test
    fun subscribeUnsubscribe() {
        val query: Query = mock()
        val listenerReference: ListenerRegistration = mock()
        whenever(query.addSnapshotListener(any())).thenReturn(listenerReference)

        val queryObservable = QueryObservable(query)

        inOrder(query, listenerReference) {
            verifyNoMoreInteractions()

            val subscriber = queryObservable.flowable.subscribe()
            verify(query).addSnapshotListener(any())

            subscriber.dispose()
            dispatcher.advanceTime(1000)
            verify(listenerReference).remove()

            verifyNoMoreInteractions()
        }
    }

    @Test
    fun propagateErrorsAsResource() {
        val query: Query = mock()
        val listener = argumentCaptor<EventListener<QuerySnapshot>>()
        whenever(query.addSnapshotListener(listener.capture()))
            .thenReturn(mock())

        val queryObservable = QueryObservable(query)

        val testObserver = TestSubscriber.create<Resource<QuerySnapshot>>()
        queryObservable.flowable.subscribe(testObserver)

        val exception = FirebaseFirestoreException("Test", FirebaseFirestoreException.Code.ABORTED)
        listener.firstValue.onEvent(null, exception)

        testObserver.assertValuesOnly(Resource.Error(exception))
        testObserver.assertNoErrors()
    }

    @Test
    fun noErrorsWithLateDelivery() {
        val query = MockCollection(
            listOf(
                "A" to 1,
                "B" to 2
            )
        )

        val mockQuery = mock<Query>()

        val listener = argumentCaptor<EventListener<QuerySnapshot>>()
        whenever(mockQuery.addSnapshotListener(listener.capture())).thenReturn(mock())

        val queryObservable = QueryObservable(mockQuery)
        queryObservable.flowable.subscribe().dispose()

        dispatcher.advanceTime(1000)

        queryObservable.onEvent(query.toQuerySnapshot(), null)
    }
}