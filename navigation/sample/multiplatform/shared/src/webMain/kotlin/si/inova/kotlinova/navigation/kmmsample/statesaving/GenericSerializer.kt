package si.inova.kotlinova.navigation.kmmsample.statesaving

import androidx.savedstate.SavedState
import androidx.savedstate.serialization.serializers.SavedStateSerializer
import kotlinx.serialization.Serializable

/**
 * Handles the mapping between loosely typed `Any?` (from Bundles) and strictly typed
 * [SerializableValue] wrappers (for KXS).
 *
 * Adapted from https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:savedstate/savedstate/src/commonMain/kotlin/androidx/savedstate/serialization/serializers/SavedStateSerializer.kt?q=SavedStateSerializer&ss=androidx%2Fplatform%2Fframeworks%2Fsupport
 */
@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
internal object GenericSerializableValueConverter {

    fun wrapMap(from: Map<String, Any?>): Map<String, SerializableValue> =
        buildMap(from.size) {
            for ((key, value) in from) {
                put(key, wrap(value))
            }
        }

    fun unwrapMap(from: Map<String, SerializableValue>): Map<String, Any?> =
        buildMap(from.size) {
            for ((key, value) in from) {
                put(key, unwrap(value))
            }
        }

    internal fun wrap(value: Any?): SerializableValue =
        when (value) {
            null -> NullValue

            // Primitives
            is Boolean -> BooleanValue(value)
            is Char -> CharValue(value)
            is Double -> DoubleValue(value)
            is Float -> FloatValue(value)
            is Int -> IntValue(value)
            is Long -> LongValue(value)

            // Strings & CharSequences
            is String -> StringValue(value)
            is CharSequence -> CharSequenceValue(value.toString())

            // Primitive Arrays
            is BooleanArray -> BooleanArrayValue(value)
            is CharArray -> CharArrayValue(value)
            is DoubleArray -> DoubleArrayValue(value)
            is FloatArray -> FloatArrayValue(value)
            is IntArray -> IntArrayValue(value)
            is LongArray -> LongArrayValue(value)

            // Object Arrays
            is Array<*> -> wrapArray(value)

            // Lists
            is List<*> -> wrapList(value)

            // Nested SavedState
            is SavedState -> SavedStateValue(value)

            else ->
                throw IllegalArgumentException("Unsupported type in SavedState: ${value::class}")
        }

    internal fun wrapArray(value: Array<*>): SerializableValue {
        if (value.isEmpty()) return StringArrayValue(emptyArray())

        return when (val first = value.first()) {
            is Int -> IntArrayValue(value as IntArray)
            is String -> StringArrayValue(value as Array<String>)
            is CharSequence -> CharSequenceListValue(value.map { it.toString() })
            is SavedState -> SavedStateListValue(value as List<SavedState>)
            else -> throw IllegalArgumentException("Unsupported Array type: ${first!!::class}")
        }
    }

    internal fun wrapList(value: List<*>): SerializableValue {
        if (value.isEmpty()) return StringListValue(emptyList())

        return when (val first = value.first()) {
            is Int -> IntListValue(value as List<Int>)
            is String -> StringListValue(value as List<String>)
            is CharSequence ->
                CharSequenceListValue((value as List<CharSequence>).map { it.toString() })
            is SavedState -> SavedStateListValue(value as List<SavedState>)
            else -> throw IllegalArgumentException("Unsupported List type: ${first!!::class}")
        }
    }

    internal fun unwrap(value: SerializableValue): Any? =
        when (value) {
            is BooleanValue -> value.value
            is CharValue -> value.value
            is DoubleValue -> value.value
            is FloatValue -> value.value
            is IntValue -> value.value
            is LongValue -> value.value
            NullValue -> null

            is StringValue -> value.value
            is CharSequenceValue -> value.value

            is BooleanArrayValue -> value.value
            is CharArrayValue -> value.value
            is DoubleArrayValue -> value.value
            is FloatArrayValue -> value.value
            is IntArrayValue -> value.value
            is LongArrayValue -> value.value

            is StringArrayValue -> value.value
            is CharSequenceArrayValue -> value.value
            is SavedStateArrayValue -> value.value

            is IntListValue -> value.value
            is StringListValue -> value.value
            is CharSequenceListValue -> value.value
            is SavedStateListValue -> value.value

            is SavedStateValue -> value.value
        }
}

@Serializable internal sealed interface SerializableValue

// Primitives
@Serializable internal data class BooleanValue(val value: Boolean) : SerializableValue

@Serializable internal data class CharValue(val value: Char) : SerializableValue

@Serializable internal data class DoubleValue(val value: Double) : SerializableValue

@Serializable internal data class FloatValue(val value: Float) : SerializableValue

@Serializable internal data class IntValue(val value: Int) : SerializableValue

@Serializable internal data class LongValue(val value: Long) : SerializableValue

@Serializable internal data object NullValue : SerializableValue

// Strings & CharSequences
@Serializable internal data class StringValue(val value: String) : SerializableValue

@Serializable internal data class CharSequenceValue(val value: String) : SerializableValue

// Primitive Arrays
@Serializable internal data class BooleanArrayValue(val value: BooleanArray) : SerializableValue

@Serializable internal data class CharArrayValue(val value: CharArray) : SerializableValue

@Serializable internal data class DoubleArrayValue(val value: DoubleArray) : SerializableValue

@Serializable internal data class FloatArrayValue(val value: FloatArray) : SerializableValue

@Serializable internal data class IntArrayValue(val value: IntArray) : SerializableValue

@Serializable internal data class LongArrayValue(val value: LongArray) : SerializableValue

// Object Arrays
@Serializable internal data class StringArrayValue(val value: Array<String>) : SerializableValue

@Serializable
internal data class CharSequenceArrayValue(val value: Array<String>) : SerializableValue

@Serializable
internal data class SavedStateArrayValue(
    val value: Array<@Serializable(with = SavedStateSerializer::class) SavedState>
) : SerializableValue

// Lists
@Serializable internal data class IntListValue(val value: List<Int>) : SerializableValue

@Serializable internal data class StringListValue(val value: List<String>) : SerializableValue

@Serializable internal data class CharSequenceListValue(val value: List<String>) : SerializableValue

@Serializable
internal data class SavedStateListValue(
    val value: List<@Serializable(with = SavedStateSerializer::class) SavedState>
) : SerializableValue

@Serializable
internal data class SavedStateValue(
    @Serializable(with = SavedStateSerializer::class) val value: SavedState
) : SerializableValue
