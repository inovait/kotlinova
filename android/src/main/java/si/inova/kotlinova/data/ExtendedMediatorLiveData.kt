package si.inova.kotlinova.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import java.lang.ref.WeakReference

/**
 * Version of [MediatorLiveData] that can remember list of all assigned sources and can remove all them if needed
 *
 * @author Matej Drobnic
 */
open class ExtendedMediatorLiveData<T> : MediatorLiveData<T>() {
    private val sources = ArrayList<WeakReference<LiveData<*>>>()

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<S>) {
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