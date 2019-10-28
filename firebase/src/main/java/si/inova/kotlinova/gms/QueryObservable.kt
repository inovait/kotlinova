package si.inova.kotlinova.gms

import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.rx.OnDemandProvider

/**
 * Observable that sends new data every time passed [query] has been updated
 *
 * @author Matej Drobnic
 */
class QueryObservable(private val query: Query) :
    OnDemandProvider<Resource<QuerySnapshot>>(), EventListener<QuerySnapshot> {

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun CoroutineScope.onActive() {
        listenerRegistration = query.addSnapshotListener(this@QueryObservable)
    }

    override fun onInactive() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onEvent(snapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (!isActive) {
            return
        }

        if (e != null) {
            offer(Resource.Error(e))
            return
        }

        if (snapshot != null) {
            offer(Resource.Success(snapshot))
        }
    }
}