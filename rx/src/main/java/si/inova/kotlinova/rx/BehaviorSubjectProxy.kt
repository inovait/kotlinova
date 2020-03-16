/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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