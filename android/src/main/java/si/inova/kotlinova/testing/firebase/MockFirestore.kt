package si.inova.kotlinova.testing.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

/**
 * @author Matej Drobnic
 */
class MockFirestore {
    private val collections = HashMap<String, CollectionReference>()

    fun <T> createCollection(name: String): MockCollection<T> {
        val collection = MockCollection<T>()
        insert(name, collection.toColRef())
        return collection
    }

    fun insert(name: String, collection: CollectionReference) {
        collections[name] = collection
    }

    fun toFirestore(): FirebaseFirestore {
        return mock {
            whenever(it.collection(any())).then {
                val name = it.arguments[0] as String

                collections[name] ?: MockCollection<Any>().toColRef()
            }

            whenever(it.runTransaction<Any>(any())).thenAnswer {
                val transFunc = it.arguments[0] as Transaction.Function<*>
                val result = transFunc.apply(transaction)

                Tasks.forResult(result)
            }
        }
    }

    private val transaction: Transaction by lazy {
        mock<Transaction> {
            whenever(it.get(any())).then {
                val ref = it.arguments[0] as DocumentReference
                ref.get().result
            }

            whenever(it.delete(any())).then {
                val ref = it.arguments[0] as DocumentReference
                ref.delete()
                it.mock
            }

            whenever(it.set(any(), any<Map<String, Any>>())).then {
                val ref = it.arguments[0] as DocumentReference
                @Suppress("UNCHECKED_CAST")
                ref.set(it.arguments[1] as Map<String, Any>)
                it.mock
            }

            whenever(it.set(any(), any<Any>())).then {
                val ref = it.arguments[0] as DocumentReference
                ref.set(it.arguments[1] as Any)
                it.mock
            }

            whenever(it.update(any(), any<Map<String, Any>>())).then {
                val ref = it.arguments[0] as DocumentReference
                @Suppress("UNCHECKED_CAST")
                ref.update(it.arguments[1] as Map<String, Any>)
                it.mock
            }
        }
    }
}