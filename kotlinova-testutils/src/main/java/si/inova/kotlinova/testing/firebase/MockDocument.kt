package si.inova.kotlinova.testing.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

/**
 * @author Matej Drobnic
 */
class MockDocument<T>(val key: String) {
    constructor(key: String, initialValue: T) : this(key) {
        readValue = initialValue
    }

    val subCollections = HashMap<String, MockCollection<*>>()

    var readValue: T? = null
        set(value) {
            field = value
            val newSnapshot = getSnapshot(key, value)
            listeners.forEach {
                it.onEvent(newSnapshot, null)
            }
        }
    var writtenValue: Map<String, Any>? = null

    private val listeners = ArrayList<EventListener<DocumentSnapshot>>()

    fun <C> createSubCollection(name: String): MockCollection<C> {
        val collection = MockCollection<C>()
        subCollections[name] = collection
        return collection
    }

    fun <C> createSubCollection(name: String, data: List<Pair<String, C>>): MockCollection<C> {
        val collection = createSubCollection<C>(name)

        for (item in data) {
            collection.insertObject(item.first, item.second)
        }

        return collection
    }

    private fun getSnapshot(key: String, value: T?): QueryDocumentSnapshot = mock {
        whenever(it.exists()).thenReturn(value != null)

        whenever(it.id).thenReturn(key)

        whenever<T>(it.toObject(any())).thenAnswer {
            if (value == null) {
                throw RuntimeException("Value does not exist")
            }

            value
        }

        whenever(it.exists()).thenReturn(value != null)

        whenever(it.toString()).thenReturn("{$key: $value}")
    }

    fun toDocumentRef(): DocumentReference = mock {

        whenever(it.id).thenReturn(key)

        whenever(it.get()).thenAnswer {
            Tasks.forResult(getSnapshot(key, readValue))
        }

        whenever(it.set(any())).then {
            @Suppress("UNCHECKED_CAST")
            writtenValue = it.arguments[0] as Map<String, Any>?

            Tasks.forResult(null)
        }

        whenever(it.collection(any())).thenAnswer {
            val name = it.arguments[0] as String
            subCollections[name]?.toColRef() ?: MockCollection<Any>().toColRef()
        }

        whenever(it.addSnapshotListener(any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            val listener = it.arguments[0] as EventListener<DocumentSnapshot>
            listeners.add(listener)
            if (readValue != null) {
                listener.onEvent(getSnapshot(key, readValue), null)
            }

            null
        }
    }

    fun toDocumentSnap(): DocumentSnapshot {
        return toDocumentRef().get().result
    }
}