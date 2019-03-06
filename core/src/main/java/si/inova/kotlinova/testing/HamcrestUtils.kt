@file:JvmName("HamcrestUtils")

package si.inova.kotlinova.testing

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

inline fun <I, O> that(
    descriptionText: String? = null,
    crossinline check: (O) -> Boolean
): Matcher<I> where O : I {
    @Suppress("UNCHECKED_CAST")
    return object : BaseMatcher<I>() {
        override fun describeTo(description: Description) {
            descriptionText?.let { description.appendText(it) }
        }

        override fun describeMismatch(item: Any?, description: Description) {
            description.appendText("Failed: $descriptionText")
        }

        override fun matches(item: Any): Boolean {
            return check(item as O)
        }
    }
}