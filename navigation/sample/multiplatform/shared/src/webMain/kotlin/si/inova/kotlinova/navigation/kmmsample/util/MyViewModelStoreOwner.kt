package si.inova.kotlinova.navigation.kmmsample.util

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import si.inova.kotlinova.navigation.backstack.BackstackHolderViewModel
import kotlin.reflect.KClass

class MyViewModelStoreOwner : ViewModelStoreOwner, HasDefaultViewModelProviderFactory, ViewModelProvider.Factory {
    private val store = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = store

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = this

    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras
    ): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            BackstackHolderViewModel::class -> BackstackHolderViewModel()
            else -> super.create(modelClass, extras)
        } as T
    }
}
