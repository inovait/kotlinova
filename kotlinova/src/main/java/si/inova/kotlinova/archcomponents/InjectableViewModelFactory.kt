package si.inova.kotlinova.archcomponents

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Lazy
import si.inova.kotlinova.testing.OpenForTesting
import javax.inject.Inject

/**
 * [ViewModelProvider.Factory] for ViewModels that can be injected into dagger.
 *
 * @author Matej Drobnic
 */
@OpenForTesting
class InjectableViewModelFactory<VM : ViewModel> @Inject constructor(
    private val viewModel: Lazy<VM>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return viewModel.get() as T
    }
}