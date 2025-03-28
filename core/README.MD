# Core

Direct gradle usage:

```kotlin
implementation("si.inova.kotlinova:core:X.Y.Z")
testImplementation("si.inova.kotlinova:core-test:X.Y.Z")
```

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

```kotlin
implementation(libs.kotlinova.core)
testImplementation(libs.kotlinova.core.test)
```

Replace `X.Y.Z` with latest version. Check out [releases](https://github.com/inovait/kotlinova/releases) to see what is the latest
version.

# General Features

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

## AccessCallbackDispatcherProvider

Wrapper for a DispatcherProvider from [Dispatch](https://github.com/RBusarow/Dispatch) that triggers `onDispatcherAccess`
callback every time dispatcher is accessed.

You can use it to override `DefaultDispatcherProvider` to ensure it is never actually used
(dispatchers should be passed down via structured concurrency, not using default dispatcher singleton).

For example, you can do this in your Applicaton.onCreate:

```kotlin
DefaultDispatcherProvider.set(
 AccessCallbackDispatcherProvider(DefaultDispatcherProvider.get()) {
    if (BuildConfig.DEBUG) {
       throw IllegalStateException("Dispatchers not provided via coroutine scope.")
    }
 }
)
```

## Debouncer

Debouncer will only execute target task when `executeDebouncing` is not called within some time.

This can be used to, for example, not make any requests on user typing unless user stops typing.

```kotlin
val debouncer = Debouncer(backgroundScope, timeProvider)

...

debouncer.executeDebouncing {
   ...
}
```

## Miscellaneous

* [Math utilities](src/commonMain/kotlin/si/inova/kotlinova/core/math/Math.kt)
* [Moving Averager](src/commonMain/kotlin/si/inova/kotlinova/core/math/MovingAverageUtility.kt)
* [3D Vector](src/commonMain/kotlin/si/inova/kotlinova/core/math/Vector3.kt)
* [Unit conversions](src/commonMain/kotlin/si/inova/kotlinova/core/units/UnitConverters.kt)
* [List Extensions](src/jvmCommon/kotlin/si/inova/kotlinova/core/containers/List.kt)
* [Map Extensions](src/jvmCommon/kotlin/si/inova/kotlinova/core/containers/Map.kt)
* [Weak List](src/jvmCommon/kotlin/si/inova/kotlinova/core/containers/WeakList.kt)
* [Locale metric system check](src/jvmCommon/kotlin/si/inova/kotlinova/core/locale/MeasurementSystems.kt)

# Android-specific utilities

## LocalizedText

Generally, you should only call `context.getString()` on Android from activity context. This ensures that, 
when configuration change happens, text will get re-loaded with the new Locale.

This means that there is no good way to load text anywhere else (for example in ViewModels). 
[LocalizedText](src/androidMain/kotlin/si/inova/kotlinova/core/data/LocalizedText.kt) attempts to fix that by allowing you
to expose a piece of text that uses Android string resources, but still gets resolved on the activity context.

## Miscellaneous

* [Context extensions](src/androidMain/kotlin/si/inova/kotlinova/core/android/Context.kt)
* [Location extensions](src/androidMain/kotlin/si/inova/kotlinova/core/android/Location.kt)
* [Bundle extensions](src/androidMain/kotlin/si/inova/kotlinova/core/state/Bundle.kt)
* [Shared Preferences extensions](src/androidMain/kotlin/si/inova/kotlinova/core/state/SharedPreferences.kt)
* [ColorInt extensions](src/androidMain/kotlin/si/inova/kotlinova/core/ui/Colors.kt)

# Test utilities

## FakeActivity and FakeResources (Android)

You can use them in unit and android tests as a replacements for real Activity and Resources.

To use them in unit tests, you also must have [Unmock](https://github.com/bjoernQ/unmock-plugin) unmock plugin.

## Dispatch utilities

[Several utilites](test/src/commonMain/kotlin/si/inova/kotlinova/core/test/TestDispatchers.kt)
for [Dispatch](https://github.com/RBusarow/Dispatch)
library, for using that library with TestScope, as the core library has not yet been updated for that.
