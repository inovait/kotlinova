package si.inova.kotlinova.rx

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.JobCancellationException
import kotlinx.coroutines.experimental.cancelChildren
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import si.inova.kotlinova.coroutines.CommonPool
import si.inova.kotlinova.utils.awaitCancellation
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.coroutineContext

/**
 * Data provider that activates when anyone subscribes to its observable and provides a stream
 * of items. After activated, implementors must call [send] method to pass new item into target
 * observable.
 *
 * Items can be consumed by subscribing to [flowable] property.
 *
 * Class also automatically handles debouncing (when there are no subscribers left, provider
 * will stay active for a short time in case subscribers arrive back). Implementors can control
 * this behavior using [debounceTimeout] parameter.
 *
 * @author Matej Drobnic
 */
abstract class OnDemandProvider<T>(
    private val launchingContext: CoroutineContext = CommonPool,
    rxScheduler: Scheduler = Schedulers.computation()
) {
    /**
     * Minimum time that needs to pass after everyone unsubscribes (and nobody resubscribes)
     * for this service to go inactive
     */
    protected var debounceTimeout = DEFAULT_DEBOUNCE_TIMEOUT

    /**
     * Flowable that relays stream of items transmitted from that provider
     */
    @Suppress("LeakingThis")
    val flowable: Flowable<T> =
        Flowable.create<T>(this::onSubscribe, BackpressureStrategy.LATEST)
            .doFinally(this::onDispose)
            .replay(1)
            .refCount()
            .subscribeOn(rxScheduler)

    @Volatile
    private var emitter: FlowableEmitter<T>? = null

    private val inDebounce = AtomicBoolean(false)
    private val lastValue = AtomicReference<T?>()

    private val parentActivationJob = Job()

    private var currentCleanupJob: Job? = null

    val isActive: Boolean
        get() {
            return emitter != null || inDebounce.get()
        }

    @Synchronized
    private fun onSubscribe(emitter: FlowableEmitter<T>) {
        this.emitter = emitter

        currentCleanupJob?.cancel()

        if (inDebounce.get()) {
            val curValue = lastValue.get()

            if (curValue != null) {
                emitter.onNext(curValue)
            }

            inDebounce.set(false)
            return
        }

        runOnActive(emitter)
    }

    protected fun runOnActive(newEmitter: FlowableEmitter<T>? = null) {
        val targetEmitter = newEmitter ?: emitter ?: throw IllegalStateException("No emitter")

        parentActivationJob.cancelChildren()

        launch(launchingContext, parent = parentActivationJob) {
            val thisJob = coroutineContext[Job]
            parentActivationJob.children
                .filter { it != thisJob }
                .forEach { it.join() }

            this@OnDemandProvider.emitter = targetEmitter

            try {
                onActive()
                awaitCancellation()
            } catch (e: Exception) {
                if (e !is JobCancellationException) {
                    if (isActive) {
                        sendCrash(e)
                    } else {
                        //Coroutines eat all exceptions when cancelled. Log manually.
                        Timber.e(e)
                    }
                }
            } finally {
                lastValue.set(null)
                inDebounce.set(false)
                this@OnDemandProvider.emitter = null

                onInactive()
            }
        }
    }

    private fun onDispose() {
        inDebounce.set(true)

        currentCleanupJob?.cancel()
        currentCleanupJob = launch(launchingContext) {
            delay(debounceTimeout)

            parentActivationJob.cancelChildren()
            inDebounce.set(false)
        }
    }

    protected fun send(item: T) {
        lastValue.set(item)

        if (inDebounce.get()) {
            return
        }

        val emitter =
            emitter ?: throw IllegalStateException("Cannot send data when provider is inactive")

        emitter.onNext(item)
    }

    private fun sendCrash(exception: Exception) {
        val emitter = emitter ?: throw exception
        emitter.onError(exception)
    }

    /**
     * Method that gets triggered when someone starts observing your service
     */
    protected abstract suspend fun CoroutineScope.onActive()

    /**
     * Method that gets triggered when everyone stops observing your service
     */
    protected open fun onInactive() = Unit
}

private const val DEFAULT_DEBOUNCE_TIMEOUT = 500