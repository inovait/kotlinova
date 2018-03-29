package si.inova.kotlinova.ui.state

/**
 * Base class for all classes that automatically save their state through [StateSaverManager].
 *
 * Implementors must call [register] as soon as they have access to their key and to [StateSaverManager].
 *
 * @author Matej Drobnic
 */
abstract class StateSaver<T> {
    private var key: String? = null
    private var stateSaverManager: StateSaverManager? = null

    /**
     *  Register this StateSaver with StateSaverManager. This method executes only once,
     *  but can be called multiple times (all subsequent calls do nothing).
     */
    protected fun register(stateSaverManager: StateSaverManager, key: String) {
        if (this.stateSaverManager != null) {
            return
        }

        stateSaverManager.registerStateSaver(key, this)
        this.stateSaverManager = stateSaverManager
        this.key = key
    }

    protected val lastSavedState: T?
        get() {
            val stateSaver = stateSaverManager
                    ?: throw IllegalStateException("StateSaver not registered")
            return stateSaver.getLastLoadedValue(key!!)
        }

    /**
     * Method that gets automatically called when state gets saved.
     * You must return current state or *null*.
     */
    internal abstract fun saveState(): T?

    /**
     * Method that gets automatically called when state gets loaded. Override.
     */
    internal open fun loadState(savedValue: T?) = Unit
}
