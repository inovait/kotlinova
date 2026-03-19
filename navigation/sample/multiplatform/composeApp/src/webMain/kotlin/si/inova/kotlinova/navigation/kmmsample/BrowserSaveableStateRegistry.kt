package si.inova.kotlinova.navigation.kmmsample

import androidx.compose.runtime.saveable.SaveableStateRegistry

private const val SAVE_KEY = "COMPOSE_SAVED_STATE"

internal class BrowserSaveableStateRegistry() : SaveableStateRegistry by SaveableStateRegistry(
    restoredValues = run {
        val json = DependencyInjectionHolder.appGraph.getNavigationSavedStateJsonSerializer()
        sessionStorage.getItem(SAVE_KEY)?.let {
            json.decodeFromString<Map<String, List<SerializableValue>>>(it)
                .mapValues { (_, value) -> value.map { GenericSerializableValueConverter.unwrap(it) } }
        } ?: emptyMap()
    },
    canBeSaved = { true }
) {
    fun save() {
        val map = performSave().mapValues { (_, list) -> list.map { GenericSerializableValueConverter.wrap(it) } }
        sessionStorage.setItem(
            SAVE_KEY,
            DependencyInjectionHolder.appGraph.getNavigationSavedStateJsonSerializer().encodeToString(map)
        )
    }
}
