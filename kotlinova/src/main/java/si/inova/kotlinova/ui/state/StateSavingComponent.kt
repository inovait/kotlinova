package si.inova.kotlinova.ui.state

/**
 * Interface for Activity/Fragment that offers easy state saving.
 *
 * When implementing interface, you must call [StateSaverManager.saveInstance] and [StateSaverManager.restoreInstance] appropriately.
 *
 * @author Matej Drobnic
 */
interface StateSavingComponent {
    val stateSaverManager: StateSaverManager
}