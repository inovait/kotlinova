package si.inova.kotlinova.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Adds a list of items with dividers between them.
 *
 * @param items the data list
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one.
 * @param contentType a factory of the content types for the item. The item compositions of
 * the same type could be reused more efficiently. Note that null is a valid type and items of such
 * type will be considered compatible.
 * @param itemContent the content displayed by a single item
 * @param [dividerContent] the content displayed between each item
 * @param [modifier] Modifier for the entire combined item. Use for animations.
 */

inline fun <T> LazyListScope.itemsWithDivider(
   items: List<T>,
   crossinline modifier: LazyItemScope.(T) -> Modifier = { Modifier },
   crossinline dividerContent: @Composable () -> Unit = { Divider() },
   noinline key: ((item: T) -> Any)? = null,
   noinline contentType: (item: T) -> Any? = { null },
   crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = items(
   count = items.size,
   key = if (key != null) { index: Int -> key(items[index]) } else null,
   contentType = { index: Int -> contentType(items[index]) }
) {
   val item = items[it]
   Column(modifier(item)) {
      itemContent(item)
      if (it < items.size - 1) {
         dividerContent()
      }
   }
}
