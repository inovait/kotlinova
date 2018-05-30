package si.inova.kotlinova.testing.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.mockito.Answers

/**
 * @author Matej Drobnic
 */
class MockCollection<T>() {
    constructor(data: List<Pair<String, T>>) : this() {
        data.forEach {
            insertObject(it.first, it.second)
        }
    }

    var nextId: String = "NextID"

    private val documents = LinkedHashMap<String, DocumentReference>()
    private val listeners = ArrayList<EventListener<QuerySnapshot>>()

    fun createDocument(name: String): MockDocument<T> {
        val document = MockDocument<T>(name)
        insert(name, document.toDocumentRef())
        return document
    }

    fun insert(name: String, collection: DocumentReference) {
        documents[name] = collection
    }

    fun insertObject(name: String, data: T) {
        createDocument(name)
            .readValue = data

        updateListeners()
    }

    fun insertMap(name: String, data: Map<String, Any>) {
        createDocument(name)
            .readMap = data

        updateListeners()
    }

    fun clear() {
        documents.clear()

        updateListeners()
    }

    fun updateListeners() {
        val newSnapshot = toQuerySnapshot()

        for (listener in listeners) {
            listener.onEvent(newSnapshot, null)
        }
    }

    fun toQuerySnapshot(): QuerySnapshot {
        val documents = LinkedHashMap<String, DocumentReference>().apply {
            putAll(this@MockCollection.documents)
        }

        return mock {
            whenever(it.size()).thenReturn(documents.size)

            whenever(it.documents).then {
                documents
                    .values
                    .toList()
                    .map {
                        it.get().result
                    }
            }

            whenever(it.iterator()).then {
                documents
                    .values
                    .toList()
                    .map {
                        it.get().result
                    }
                    .iterator()
            }
        }
    }

    fun toColRef(): CollectionReference {
        return mock(defaultAnswer = Answers.RETURNS_SELF) {
            whenever(it.document()).then {
                val name = nextId

                documents[name]
                    ?: MockDocument<T>(name).toDocumentRef()
            }

            whenever(it.document(any())).then {
                val name = it.arguments[0] as String

                documents[name]
                    ?: MockDocument<T>(name).toDocumentRef()
            }

            whenever(it.get()).thenAnswer { (Tasks.forResult(toQuerySnapshot())) }

            whenever(it.addSnapshotListener(any())).thenAnswer {
                @Suppress("UNCHECKED_CAST")
                val listener = it.arguments[0] as EventListener<QuerySnapshot>
                listener.onEvent(toQuerySnapshot(), null)

                listeners.add(listener)

                val registration: ListenerRegistration = mock()
                doAnswer { listeners.remove(listener) }.whenever(registration).remove()
                registration
            }
        }
    }
}