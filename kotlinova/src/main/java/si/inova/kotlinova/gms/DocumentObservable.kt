package si.inova.kotlinova.gms

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.experimental.CoroutineScope
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
            send(Resource.Error(e))
            return
        }

        if (newDocument != null) {
            if (!newDocument.exists()) {
                send(Resource.Error(NoSuchElementException("${newDocument.reference.parent} is missing")))
                return
            }

            send(Resource.Success(newDocument))
        }
    }
}