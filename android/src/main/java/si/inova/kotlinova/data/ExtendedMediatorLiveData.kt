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

package si.inova.kotlinova.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import java.lang.ref.WeakReference

/**
 * Version of [MediatorLiveData] that can remember list of all assigned sources and can remove all them if needed
 *
 * @author Matej Drobnic
 */
open class ExtendedMediatorLiveData<T> : MediatorLiveData<T>() {
    private val sources = ArrayList<WeakReference<LiveData<*>>>()

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        sources.add(WeakReference(source))
        super.addSource(source, onChanged)
    }

    override fun <S : Any?> removeSource(source: LiveData<S>) {
        super.removeSource(source)

        sources.removeAll { it.get() == source }
    }

    fun <S : Any?> addSourceIfNotPresent(source: LiveData<S>, onChanged: (S?) -> Unit) {
        if (!isSourceAdded(source)) {
            addSource(source, onChanged)
        }
    }

    fun <S : Any?> isSourceAdded(source: LiveData<S>): Boolean {
        return sources.any { it.get() == source }
    }

    fun removeAllSources() {
        for (source in sources) {
            source.get()?.let {
                super.removeSource(it)
            }
        }

        sources.clear()
    }

    fun hasAnySources(): Boolean {
        return sources.any { it.get() != null }
    }
}