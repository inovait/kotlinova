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

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.rx.OnDemandProvider

/**
 * Observable that sends new data every time passed [documentReference] has been updated
 *
 * @author Matej Drobnic
 */
class DocumentObservable(private val documentReference: DocumentReference) :
    OnDemandProvider<Resource<DocumentSnapshot>>(), EventListener<DocumentSnapshot> {

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun CoroutineScope.onActive() {
        listenerRegistration = documentReference.addSnapshotListener(this@DocumentObservable)
    }

    override fun onInactive() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onEvent(newDocument: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (!isActive) {
            return
        }

        if (e != null) {
            offer(Resource.Error(e))
            return
        }

        if (newDocument != null) {
            if (!newDocument.exists()) {
                offer(
                    Resource.Error(
                        NoSuchElementException("${newDocument.reference.path} is missing")
                    )
                )
                return
            }

            offer(Resource.Success(newDocument))
        }
    }
}