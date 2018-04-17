package si.inova.kotlinova.gms

import android.arch.lifecycle.LiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import si.inova.kotlinova.data.Resource

/**
 * LiveData that wraps [DocumentReference]
 * @author Matej Drobnic
 */
class DocumentLiveData<T>(
    private val documentReference: DocumentReference,
    private val clazz: Class<T>
) :
    LiveData<Resource<T>>(), EventListener<DocumentSnapshot> {
    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        listenerRegistration = documentReference.addSnapshotListener(this)
    }

    override fun onInactive() {
        listenerRegistration?.remove()
    }

    override fun onEvent(newDocument: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            postValue(Resource.Error(e))
            return
        }

        if (newDocument != null) {
            if (!newDocument.exists()) {
                postValue(Resource.Error(NoSuchElementException()))
                return
            }

            postValue(Resource.Success(newDocument.toObject(clazz)!!))
        }
    }
}