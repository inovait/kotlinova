package si.inova.kotlinova.coroutines

/**
 * Convenience parent for [CoroutineViewModel] with common error handling enabled by default
 */
abstract class CommonErrorCoroutineViewModel : CoroutineViewModel() {
    override val routeErrorsToCommonObservableByDefault: Boolean
        get() = true
}