package si.inova.kotlinova.room

import androidx.room.Room
import androidx.test.espresso.Espresso
import androidx.test.espresso.idling.concurrent.IdlingThreadPoolExecutor
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.room.db.TestDatabase
import si.inova.kotlinova.room.db.TextEntry
import si.inova.kotlinova.testing.UncaughtExceptionThrowRule
import si.inova.kotlinova.testing.asserts.assertThatValue
import si.inova.kotlinova.testing.asserts.isError
import si.inova.kotlinova.testing.asserts.isLoadingWithValue
import si.inova.kotlinova.testing.asserts.isSuccess
import si.inova.kotlinova.testing.asserts.isSuccessWithValue
import si.inova.kotlinova.testing.espresso.IdlingResourceRule
import si.inova.kotlinova.testing.espresso.IgnoreIdlingResource
import si.inova.kotlinova.testing.espresso.coroutines.DispatchersIdlingResourceRule
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class RoomLoadableResourceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val roomExecutor = IdlingThreadPoolExecutor(
        "TestRoomExecutor",
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors(),
        0,
        TimeUnit.SECONDS,
        LinkedBlockingDeque(),
        Executors.defaultThreadFactory()
    )

    private val db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java)
        .setQueryExecutor(roomExecutor)
        .build()

    private val dao = db.testDao()

    @get:Rule
    val dispatcherIdlingResource = DispatchersIdlingResourceRule()
    @get:Rule
    val dispatcherIdlingResourceInjector = IdlingResourceRule(dispatcherIdlingResource)
    @get:Rule
    val executolrIdlingResourceInjector = IdlingResourceRule(roomExecutor)

    @get:Rule
    val errorRule = UncaughtExceptionThrowRule()

    @Before
    fun setUp() {
        dao.insertEntry(TextEntry("A"))
        dao.insertEntry(TextEntry("B"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun receiveExistingDataFromDatabase() = runBlocking<Unit> {
        val loadableResource = TestRoomLoadableResource { }
        val subscriber = loadableResource.data.test()

        loadableResource.load()

        Espresso.onIdle()

        val receivedValues = subscriber.values().toList()

        assertThat(receivedValues).first().isInstanceOf(Resource.Loading::class.java)
        assertThat(receivedValues).last().isSuccess()

        assertThat(receivedValues)
            .filteredOn { it is Resource.Success }
            .allSatisfy {
                assertThat(it).isSuccessWithValue(listOf(TextEntry("A"), TextEntry("B")))
            }
    }

    @Test
    fun loadDataAndReceiveItFromDatabase() = runBlocking<Unit> {
        val loadableResource = TestRoomLoadableResource { dao.insertEntry(TextEntry("C")) }
        val subscriber = loadableResource.data.test()

        loadableResource.load()

        Espresso.onIdle()

        val receivedValues = subscriber.values().toList()

        assertThat(receivedValues).first().isInstanceOf(Resource.Loading::class.java)
        assertThat(receivedValues).last().isSuccess()

        assertThat(receivedValues)
            .describedAs("All successes should only contain latest data")
            .filteredOn { it is Resource.Success }
            .allSatisfy {
                assertThat(it)
                    .assertThatValue()
                    .asList()
                    .contains(TextEntry("C"))
            }
    }

    @Test
    @IgnoreIdlingResource
    fun forwardStaleDataFromDbAsLoadingWhileLoadingFreshData() = runBlocking<Unit> {
        val loadableResource = TestRoomLoadableResource { delay(Int.MAX_VALUE.toLong()) }
        val subscriber = loadableResource.data.test()

        val loadJob = GlobalScope.launch(Dispatchers.Default) {
            loadableResource.load()
        }

        Espresso.onIdle()

        val receivedValues = subscriber.values().toList()

        assertThat(receivedValues).noneSatisfy { assertThat(it).isSuccess() }
        assertThat(receivedValues).last().isLoadingWithValue(
            listOf(TextEntry("A"), TextEntry("B"))
        )

        loadJob.cancel()
    }

    @Test
    fun doNotFetchFromDatabaseWhenNotSubscribed() = runBlocking<Unit> {
        val loadableResource = TestRoomLoadableResource { dao.insertEntry(TextEntry("C")) }

        loadableResource.load()
        Espresso.onIdle()

        assertThat(loadableResource.numDatabaseCalls).isEqualTo(0)
    }

    @Test
    fun refreshStaleDataAfterSubscription() = runBlocking<Unit> {
        val loadableResource = TestRoomLoadableResource { dao.insertEntry(TextEntry("C")) }
        loadableResource.load()
        Espresso.onIdle()

        val subscriber = loadableResource.data.test()
        Espresso.onIdle()

        subscriber.assertValues(
            Resource.Loading<List<TextEntry>>(),
            Resource.Success(listOf(TextEntry("A"), TextEntry("B"), TextEntry("C")))
        )
    }

    @Test
    fun reportDatabaseCrashesAsErrorResources() = runBlocking<Unit> {
        val loadableResource = CrashingRoomLoadableResource()
        val subscriber = loadableResource.data.test()

        loadableResource.load()
        Espresso.onIdle()

        val values = subscriber.values().toList()

        assertThat(values).last().isError(CloneNotSupportedException::class.java)
    }

    @Test
    fun reportStaleDataAsLoadingBeforeFetchingNewOne() = runBlocking<Unit> {
        val loadableResource = TestRoomLoadableResource { dao.insertEntry(TextEntry("C")) }
        var subscriber = loadableResource.data.test()

        loadableResource.load()
        Espresso.onIdle()
        subscriber.dispose()

        dao.insertEntry(TextEntry("D"))
        Espresso.onIdle()

        subscriber = loadableResource.data.test()
        Espresso.onIdle()

        subscriber.assertValues(
            Resource.Loading(
                listOf(
                    TextEntry("A"),
                    TextEntry("B"),
                    TextEntry("C")
                )
            ),
            Resource.Success(
                listOf(
                    TextEntry("A"),
                    TextEntry("B"),
                    TextEntry("C"),
                    TextEntry("D")
                )
            )
        )
    }

    @Test
    fun reportPartialLoadingValues() = runBlocking<Unit> {
        val loadableResource = TestRoomLoadableResource {
            dao.insertEntry(TextEntry("C"))
            Espresso.onIdle()
            dao.insertEntry(TextEntry("D"))
            Espresso.onIdle()
        }

        val subscriber = loadableResource.data.test()
        Espresso.onIdle()

        loadableResource.load()
        Espresso.onIdle()

        val receivedValues = subscriber.values().toList()

        assertThat(receivedValues).first().isInstanceOf(Resource.Loading::class.java)

        assertThat(receivedValues).containsSubsequence(
            listOf(
                Resource.Loading(
                    listOf(
                        TextEntry("A"),
                        TextEntry("B")
                    )
                ),
                Resource.Loading(
                    listOf(
                        TextEntry("A"),
                        TextEntry("B"),
                        TextEntry("C")
                    )
                ),
                Resource.Loading(
                    listOf(
                        TextEntry("A"),
                        TextEntry("B"),
                        TextEntry("C"),
                        TextEntry("D")
                    )
                ),
                Resource.Success(
                    listOf(
                        TextEntry("A"),
                        TextEntry("B"),
                        TextEntry("C"),
                        TextEntry("D")
                    )
                )
            )
        )

        assertThat(receivedValues).last().isSuccessWithValue(
            listOf(
                TextEntry("A"),
                TextEntry("B"),
                TextEntry("C"),
                TextEntry("D")
            )
        )
    }

    private inner class TestRoomLoadableResource(private val loader: suspend () -> Unit) :
        RoomLoadableResource<TextEntry>(
            db,
            "entries"
        ) {

        @Volatile
        var numDatabaseCalls: Int = 0

        override suspend fun loadDataFromServerAndSaveToDb() {
            loader()
        }

        override fun getDataFromDatabase(): List<TextEntry> {
            numDatabaseCalls++
            return dao.getAllEntries()
        }
    }

    private inner class CrashingRoomLoadableResource() :
        RoomLoadableResource<TextEntry>(
            db,
            "entries"
        ) {

        override suspend fun loadDataFromServerAndSaveToDb() {}

        override fun getDataFromDatabase(): List<TextEntry> {
            throw CloneNotSupportedException()
        }
    }
}