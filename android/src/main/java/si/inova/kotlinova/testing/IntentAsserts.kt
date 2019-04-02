package si.inova.kotlinova.testing

import android.content.Intent
import org.assertj.core.api.ObjectAssert
import si.inova.kotlinova.testing.asserts.matches

fun ObjectAssert<Intent>.isEqualToIntent(other: Intent) {
    this.matches("Intent filters should match with other") { it.filterEquals(other) }
    this.matches("Intents should have identical flags") { it.flags == other.flags }
}