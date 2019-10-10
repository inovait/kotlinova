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
            val newSnapshot = createSnapshot()
            listeners.forEach {
                it.onEvent(newSnapshot, null)
            }
        }
    var readMap: Map<String, Any>? = null
        set(value) {
            field = value
            val newSnapshot = createSnapshot()
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

    fun <C> createMapSubCollection(
        name: String,
        data: List<Pair<String, Map<String, Any>>>
    ): MockCollection<C> {
        val collection = createSubCollection<C>(name)

        for (item in data) {
            collection.insertMap(item.first, item.second)
        }

        return collection
    }

    private fun createSnapshot(): QueryDocumentSnapshot {
        val key = this.key
        val objectValue = this.readValue
        val mapValue = this.readMap

        return mock {
            whenever(it.exists()).thenReturn(objectValue != null || mapValue != null)

            whenever(it.id).thenReturn(key)

            whenever<T>(it.toObject(any())).thenAnswer {
                if (objectValue == null) {
                    throw RuntimeException("Snapshot does not contain concrete object")
                }

                objectValue
            }

            whenever(it.data).thenAnswer {
                if (mapValue == null) {
                    throw RuntimeException("Snapshot does not contain key-value pairs")
                }

                mapValue
            }

            whenever(it[any<String>()]).thenAnswer {
                if (mapValue == null) {
                    throw RuntimeException("Snapshot does not contain key-value pairs")
                }

                val requestedKey = it.arguments[0] as String

                mapValue[requestedKey]
            }

            whenever(it.contains(any<String>())).thenAnswer {
                if (mapValue == null) {
                    throw RuntimeException("Snapshot does not contain key-value pairs")
                }

                val requestedKey = it.arguments[0] as String

                mapValue.containsKey(requestedKey)
            }

            whenever(it.toString()).thenReturn("{$key: $objectValue [or] $mapValue}")
        }
    }

    fun toDocumentRef(): DocumentReference = mock { ref ->
        whenever(ref.id).thenReturn(key)

        whenever(ref.get()).thenAnswer {
            Tasks.forResult(createSnapshot())
        }

        whenever(ref.get(any())).thenAnswer {
            Tasks.forResult(createSnapshot())
        }

        whenever(ref.set(any())).then {
            @Suppress("UNCHECKED_CAST")
            writtenValue = it.arguments[0] as Map<String, Any>?

            Tasks.forResult(null)
        }

        whenever(ref.update(any<Map<String, Any>>())).then {
            if (ref.get().result?.exists() != true) {
                throw IllegalStateException(
                    "DocumentReference.update() can only be called " +
                        "when there is existing value"
                )
            }

            @Suppress("UNCHECKED_CAST")
            writtenValue = it.arguments[0] as Map<String, Any>?

            Tasks.forResult(null)
        }

        whenever(ref.set(any<Map<String, Any>>())).then {
            @Suppress("UNCHECKED_CAST")
            writtenValue = it.arguments[0] as Map<String, Any>?

            Tasks.forResult(null)
        }

        whenever(ref.collection(any())).thenAnswer {
            val name = it.arguments[0] as String
            subCollections[name]?.toColRef() ?: MockCollection<Any>().toColRef()
        }

        whenever(ref.addSnapshotListener(any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            val listener = it.arguments[0] as EventListener<DocumentSnapshot>
            listeners.add(listener)
            if (readValue != null || readMap != null) {
                listener.onEvent(createSnapshot(), null)
            }

            null
        }
    }

    fun toDocumentSnap(): DocumentSnapshot {
        return toDocumentRef().get().result!!
    }
}