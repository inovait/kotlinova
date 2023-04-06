# Compose

Direct gradle usage:

```kotlin
implementation("si.inova.kotlinova:compose:X.Y.Z")
implementation("si.inova.kotlinova:compose-android-test:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
kotlinova = "X.Y.Z"
```

```toml
[libraries]
kotlinova-compose = { module = "si.inova.kotlinova:compose", version.ref = "kotlinova" }
kotlinova-androidTest = { module = "si.inova.kotlinova:compose-android-test", version.ref = "kotlinova" }
```

```kotlin
implementation(libs.kotlinova.compose)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# Features

Library is a collection of utilities for Jetpack Compose. Notable features include:

## Result Passing

Compose native variant of the [Fragment Result API](https://developer.android.com/guide/fragments/communicate#fragment-result).
It allows type-safe passing of data between different Composable screens.

To use, you must first create a result passing store on root composable (for example, in your main activity):

```kotlin
val resultPassingStore = rememberSaveable { ResultPassingStore() }
CompositionLocalProvider(LocalResultPassingStore provides resultPassingStore) {
   // Your screens...
}
```

Then a screen can create a result receiver:

```kotlin
val resultKey = registerResultReceiver<String> {
   // Receive callback
}
```

`registerResultReceiver` will return a type safe parcelable key that sender must use to send data back.
This key can be sent to the sender, for example, through parcelable screen arguments.

Sender can use `SendResult` method to send data back to original screen:

```kotlin
resultKey.SendResult("Hello from second screen")
```

Alternatively, if sender wants to send from non-composable function, it can do so by getting result passing store manually:

```kotlin
val store = LocalResultPassingStore.current

Button(onClick = { store.sendResult(resultKey, "Hello from second screen") })
```

If receiver is in the active composition at the time of sending, it will receive the result immediately. Otherwise it will receive
as soon as it becomes active again.

## Blinking prevention

You can use `Flow.collectAsStateWithLifecycleAndBlinkingPrevention()` as an easier entry to the
[kotlinova core's blinking prevention](../core/README.MD#blinking-prevention)

## Date Formatter

You can use `ComposeAndroidDateTimeFormatter` as a composable wrapper for the
[kotlinova core's date formatter](../core/README.MD#date-formatter). This wrapper will automatically recompose when user's
locale changes, causing dates to auto-update.

```kotlin
val formatter = AndroidDateTimeFormatterImpl(...)
CompositionLocalProvider(LocalDateFormatter provides ComposeAndroidDateTimeFormatter(dateFormatter)) {
   // Your screens...
}
```

```kotlin
val formattedTime = LocalDateFormatter.current.ofLocalizedDateTime(FormatStyle.FULL).format(date)
```

You can pass `FakeAndroidDateTimeFormatter` to the compose formatter's constructor to get predictable date formatting
in Compose previews and tests.

## Fake Coil Loader

You can use `FakeCoilLoader` to display placeholders instead of images
in settings where you can't actually load images (for example in compose previews).

```kotlin
Coil.setImageLoader(FakeCoilLoader())

...

// This will display a static color placeholder instead of attempting to load an actual image
AsyncImage("http://image.com")
```

## Lazy List items with divider

Utility that displays list items plus divider between all items:

```kotlin
LazyColumn {
   itemsWithDivider(items, dividerContent = { Divider() }) {
      ...
   }
}
```

# Instrumented test utils

## Idling Resources

### FixedIdlingDispatcher

A copy of
the [Dispatch's IdlingDispatcher](https://github.com/RBusarow/Dispatch/blob/main/dispatch-android-espresso/src/main/java/dispatch/android/espresso/IdlingDispatcher.kt)
with [Dispatch#619](https://github.com/RBusarow/Dispatch/pull/619) fix applied.

### Easier idling dispatcher registration

```kotlin
composeTestRule.registerIdlingDispatchers(DefaultDispatcherProvider.get())
```

### LoadingCountingIdlingResource

This idling resource serves as an universal idling resource, keeping busy state until any flow in your app
is still in Outcome.Progress state.

1. Update your DI to inject `RegisteringCoroutineResourceManager` in place of regular `CoroutineResourceManager` everywhere
   in your app in instrumented tests
2. `composeTestRule.registerIdlingResource(LoadingCountingIdlingResource)`

## VirtualTime

You can use `composeTestRule.virtualTimeProvider()` to get `AndroidTimeProvider` that is in sync with virtual time managed
by this compose rule.
