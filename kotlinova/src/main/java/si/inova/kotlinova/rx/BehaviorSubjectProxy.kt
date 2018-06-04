package si.inova.kotlinova.rx

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

/**
 * Proxy that runs Observable subscription through [BehaviorSubject]. This ensures that
 * Observable always returns previously emitted value first.
 */
class BehaviorSubjectProxy<T>(private val input: Flowable<T>) {
    private val subject = BehaviorSubject.create<T>()

    val flowable: Flowable<T> = subject
        .doOnSubscribe(this::onSubscribe)
        .doFinally(this::onDispose)
        .share().toFlowable(BackpressureStrategy.LATEST)

    private var disposable: Disposable? = null

    @Synchronized
    private fun onSubscribe(@Suppress("UNUSED_PARAMETER") subjectDisposable: Disposable) {
        check(this.disposable == null) { "There should only be one subscriber" }

        this.disposable = input.subscribe(subject::onNext, subject::onError, subject::onComplete)
    }

    @Synchronized
    private fun onDispose() {
        disposable?.dispose()
        disposable = null
    }
}

/**
 * Transform flowable into another flowable that will remember last emitted value. Whenever
 * new subscriber subscribes, last value will be emitted first, even if there are no existing
 * subscribers
 */
fun <T> Flowable<T>.rememberLastValue(): Flowable<T> {
    return BehaviorSubjectProxy(this).flowable
}