package si.inova.kotlinova.room

import androidx.annotation.WorkerThread
import androidx.room.RoomDatabase
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import si.inova.kotlinova.coroutines.TestableDispatchers
import si.inova.kotlinova.data.LastResultAsyncItemProcessor
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.value
import si.inova.kotlinova.resources.LoadableResource

/**
 * Loadable Resource backed by Room Database that follows Stale-While-Revalidate caching pattern.
 *
 *
 * [data] stream returns Loading resource of existing data until [load] method completes. After that
 * it returns Success resource of new data. Any updates of the data in database that will
 * automatically be pushed with new Success object (or Loading object if [load] has not completed
 * yet).
 *
 * @param database room database used by this resource
 * @param tables list of tables that [getDataFromDatabase] will fetch data from
 *
 */
abstract class RoomLoadableResource<T : Any>(
    private val database: RoomDatabase,
    vararg tables: String
) :
    LoadableResource<List<T>> {
    private val dataSubject = BehaviorProcessor.create<Resource<List<T>>>()
    private val dbFetcher = LastResultAsyncItemProcessor<Boolean, DataFromDatabase<List<T>>?>(
        callbackContext = TestableDispatchers.Default
    )

    @Volatile
    private var loading: Boolean = true

    @Volatile
    private var waitingForRefresh: Boolean = false

    override suspend fun load() {
        loading = true
        convertCurrentDataToLoading()

        try {
            loadDataFromServerAndSaveToDb()
        } finally {
            loading = false
        }

        invalidateData()
    }

    override val data: Flowable<Resource<List<T>>> = dataSubject
        .doOnSubscribe { onSubscribe() }

    private fun onSubscribe() {
        if (waitingForRefresh) {
            waitingForRefresh = false
            triggerDatabaseRefresh()
        }
    }

    // This needs to be a lambda value instead of a function to not be garbage collected
    // when used in WeakInvalidationObserver
    private val invalidateData = invalidateData@{
        if (!dataSubject.hasSubscribers()) {
            waitingForRefresh = true
            convertCurrentDataToLoading()

            return@invalidateData
        }

        triggerDatabaseRefresh()
    }

    private fun triggerDatabaseRefresh() {
        dbFetcher.process(loading, this::updateWithDatabaseData) { wasLoading ->
            try {
                DataFromDatabase(wasLoading, getDataFromDatabase())
            } catch (e: Exception) {
                dataSubject.onNext(Resource.Error(e))
                null
            }
        }
    }

    private fun updateWithDatabaseData(dataFromDatabase: DataFromDatabase<List<T>>?) {
        if (dataFromDatabase == null) {
            return
        }

        val newValue = if (dataFromDatabase.wasLoading) {
            Resource.Loading(dataFromDatabase.data)
        } else {
            Resource.Success(dataFromDatabase.data)
        }

        dataSubject.onNext(newValue)
    }

    private fun convertCurrentDataToLoading() {
        val currentValue = dataSubject.value?.value
        dataSubject.onNext(Resource.Loading(currentValue))
    }

    init {
        val tracker = database.invalidationTracker
        tracker.addObserver(WeakInvalidationObserver(tables, tracker, invalidateData))

        invalidateData()
    }

    /**
     * Within this function you can load data from the server (using non-blocking suspending APIs)
     * and then save data to the DB.
     *
     * After this method, your DB implementation must automatically trigger update
     * in the flowable returned by [getDatabaseConnection()][getDatabaseConnection].
     */
    @WorkerThread
    abstract suspend fun loadDataFromServerAndSaveToDb()

    /**
     * @return Automatically updated flowable that points to desired data
     */
    @WorkerThread
    abstract fun getDataFromDatabase(): List<T>

    private data class DataFromDatabase<T : Any>(val wasLoading: Boolean, val data: T)
}