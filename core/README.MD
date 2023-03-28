# Core

Direct gradle usage:

`implementation("si.inova.kotlinova:core:X.Y.Z")`

`implementation("si.inova.kotlinova:core-test:X.Y.Z")`

Version catalogs usage:

```toml
[versions]
kotlinova = "X.Y.Z"
```

```toml
[libraries]
kotlinova-core = { module = "si.inova.kotlinova:core", version.ref = "kotlinova" }
kotlinova-core-test = { module = "si.inova.kotlinova:core-test", version.ref = "kotlinova" }
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# Features

Library is a collection of core utilities. Notable features include:

## Outcome

[Outcome](src/commonMain/kotlin/si/inova/kotlinova/core/outcome/Outcome.kt) is a standard wrapper for an operation. It can be
either `Outcome.Progress`, `Outcome.Success` or `Outcome.Error`.
All Outcomes contain `data`, allowing you to provide partial data on every outcome. However, data is non-null only on the
`Success`.

`Outcome.Failure` must contain a `CauseException`. This is an exception type that is supposed to be higher level than standard
kotlin exceptions, for example `DataParsingException` or `NoNetworkException`. This allows easier mapping to error messages in the
UI layer. As a fallback for wrapping every other exceptions, you can use `UnknownCauseException`.

Library also includes several utilities for chaining kotlin Flows of Outcomes.
See [Mapping](src/commonMain/kotlin/si/inova/kotlinova/core/outcome/Mapping.kt).

`core-test` artifact also include several assertion calls to make testing with Outcomes easier.

### Blinking prevention

You can use [`Flow<Outcome<T>>.withBlinkingPrevention`](src/commonMain/kotlin/si/inova/kotlinova/core/flow/BlinkingPrevention.kt)
to ensure `Outcome.Progress` is displayed for a minimum
amount of time, ensuring UI does not "blink" to the user, similar to the old
[ContentLoadingProgressBar](https://developer.android.com/reference/androidx/core/widget/ContentLoadingProgressBar).

## CollectInto and UserPresence

Library provides
a [`Flow<T>.collectInto(target: MutableSharedFlow<in T>) `](src/commonMain/kotlin/si/inova/kotlinova/core/flow/CollectInto.kt)
utility method to collect a flow and emit all values
into a provided shared flow (or state flow). This is similar to the `emitAll` method, but it offers an extra feature:
it remembers and supplies information to the upstream on whether target flow is active (being collected) or not.

If the target shared/state flow is collected directly by the UI (fragment/activity/compose), upstream can then call
[`Flow<T>.onlyFlowWhenUserPresent`](src/commonMain/kotlin/si/inova/kotlinova/core/flow/UserPresence.kt) to ensure it will only be
collected when user is present
(for example when Activity with the user is resumed). This allows you to collect the entire flow on the view model scope to ensure
loading does not get interrupted if user briefly leaves the app, but you can add aforementioned `onlyFlowWhenUserPresent` to the
flows that must stop being collected if user leaves the app, for example flows that collect GPS location.

## Logging

Library includes a copy of the square's [Logcat](https://github.com/square/logcat) library,
but in a multiplatform format. This allows pure JVM projects to include this as a dependency and also logging using this library.

## Reporting

There is an [`ErrorReporter`](src/commonMain/kotlin/si/inova/kotlinova/core/reporting/ErrorReporter.kt) interface.  
interface that can be injected anywhere for reporting non-fatal
exceptions (for example, to Firebase).

## Date

### Date provider

To make date mocking easier, there is a [`TimeProvider`](src/jvmMain/kotlin/si/inova/kotlinova/core/time/TimeProvider.kt)
interface.
It can be injected into every class that
needs current time. It supports regular raw `currentTimeMillis` and java.time classes. There is also a concrete implementation
[`DefaultProvider`](src/jvmMain/kotlin/si/inova/kotlinova/core/time/DefaultTimeProvider.kt) and
[`FakeTimeProvider`](test/src/jvmMain/kotlin/si/inova/kotlinova/core/test/time/FakeTimeProvider.kt) for tests.

There is also `AndroidTimeProvider` variant that also provides android-specific times
(from [SystemClock](https://developer.android.com/reference/android/os/SystemClock))

### Date formatter

[`AndroidDateTimeFormatter`](src/androidMain/kotlin/si/inova/kotlinova/core/time/AndroidDateTimeFormatter.kt) is an update of the
[`AndroidDateTimeFormatters`](https://github.com/drewhamilton/AndroidDateTimeFormatters) library.

Class allows easy formatting of the java.time date containers, such as `LocalDateTime`,
with respect to user's locale and time display settings.

There are two implementations: concrete
[`AndroidDateTimeFormatterImpl`](src/androidMain/kotlin/si/inova/kotlinova/core/time/AndroidDateTimeFormatterImpl.kt) and
[`FakeAndroidDateTimeFormatter`](src/androidMain/kotlin/si/inova/kotlinova/core/time/FakeAndroidDateTimeFormatter.kt) for tests
and compose previews.

## Coroutine Resource Manager

[`CoroutineResourceManager`](src/commonMain/kotlin/si/inova/kotlinova/core/outcome/CoroutineResourceManager.kt) is a controller
that manages lifecycle of your coroutines.

Intended usage is pairing with a MutableStateFlow that holds the data:

```kotlin
private val _data = MutableStateFlow<Outcome<MyData>>(Outcome.Progress())
val data: StateFlow<Outcome<MyData>> = _data

fun load() = resourceManager.launchResourceControlTask(_data) {
   // Load data here
   // Use emit() for a single value
   // Or emitAll() for a flow
}
```

It will ensure that only one coroutine for a specified data is being active at a time and will route any thrown exceptions into
data as a `Outcome.Error`.

See docs on this class for more info.
