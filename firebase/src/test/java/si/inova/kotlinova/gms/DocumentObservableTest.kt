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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subscribers.TestSubscriber
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.value
import si.inova.kotlinova.testing.CoroutinesTimeMachine
import si.inova.kotlinova.testing.RxSchedulerRule
import si.inova.kotlinova.testing.firebase.MockDocument

/**
 * @author Matej Drobnic
 */
class DocumentObservableTest {
    @get:Rule
    val rxRule = RxSchedulerRule()

    @get:Rule
    val dispatcher = CoroutinesTimeMachine()

    @Test
    fun dataRetrieval() {
        val document = MockDocument<Int>("Test", 1)
        val testObserver = TestSubscriber.create<Int>()

        val documentObservable = DocumentObservable(document.toDocumentRef())
        documentObservable.flowable.map { it.value!!.toObject(Int::class.java) }
                .subscribe(testObserver)
        dispatcher.triggerActions()

        document.readValue = 2
        document.readValue = 3
        document.readValue = 4
        document.readValue = 5
        document.readValue = 6
        document.readValue = 7
        document.readValue = 8
        document.readValue = 9
        document.readValue = 10

        testObserver.assertValueSequence(1..10)
        testObserver.assertNoErrors()
    }

    @Test
    fun subscribeUnsubscribe() {
        val document: DocumentReference = mock()
        val listenerReference: ListenerRegistration = mock()
        whenever(document.addSnapshotListener(any())).thenReturn(listenerReference)

        val documentObservable = DocumentObservable(document)

        inOrder(document, listenerReference) {
            verifyNoMoreInteractions()

            val subscriber = documentObservable.flowable.subscribe()
            dispatcher.triggerActions()
            verify(document).addSnapshotListener(any())

            subscriber.dispose()
            dispatcher.advanceTime(1000)
            verify(listenerReference).remove()

            verifyNoMoreInteractions()
        }
    }

    @Test
    fun propagateErrorsAsResource() {
        val document: DocumentReference = mock()
        var listener: EventListener<DocumentSnapshot>? = null
        whenever(document.addSnapshotListener(any())).then {
            @Suppress("UNCHECKED_CAST")
            listener = it.arguments[0] as EventListener<DocumentSnapshot>?

            mock<ListenerRegistration>()
        }

        val documentObservable = DocumentObservable(document)

        val testObserver = TestSubscriber.create<Resource<DocumentSnapshot>>()
        documentObservable.flowable.subscribe(testObserver)
        dispatcher.triggerActions()

        val exception = FirebaseFirestoreException("Test", FirebaseFirestoreException.Code.ABORTED)
        listener!!.onEvent(null, exception)

        testObserver.assertValuesOnly(Resource.Error(exception))
        testObserver.assertNoErrors()
    }

    @Test
    fun noErrorsWithLateDelivery() {
        val document = MockDocument<Int>("Test", 1)
        document.readValue = 1

        lateinit var documentListener: EventListener<DocumentSnapshot>
        val mockDocument: DocumentReference = mock() {
            whenever(it.addSnapshotListener(any())).then {
                @Suppress("UNCHECKED_CAST")
                documentListener = it.arguments[0] as EventListener<DocumentSnapshot>

                mock<ListenerRegistration>()
            }
        }

        val documentObservable = DocumentObservable(mockDocument)
        val subscriber = documentObservable.flowable.map { it.value!!.toObject(Int::class.java) }
                .subscribe()

        dispatcher.triggerActions()

        subscriber.dispose()

        dispatcher.advanceTime(1000)

        documentListener.onEvent(document.toDocumentSnap(), null)
    }
}