# Retrofit

Direct gradle usage:

```kotlin
implementation("si.inova.kotlinova:retrofit:X.Y.Z")
testImplementation("si.inova.kotlinova:retrofit-test:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
kotlinova = "X.Y.Z"
```

```toml
[libraries]
kotlinova-retrofit = { module = "si.inova.kotlinova:retrofit", version.ref = "kotlinova" }
kotlinova-retrofit-test = { module = "si.inova.kotlinova:retrofit-test", version.ref = "kotlinova" }
```

```kotlin
implementation(libs.kotlinova.retrofit)
testImplementation(libs.kotlinova.retrofit.test)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# Features

Library is a collection of utilities for [Retrofit](https://square.github.io/retrofit/). Notable features include:

## Call Adapters

### Error Handler

```kotlin
retrofitBuilder.addCallAdapterFactory(ErrorHandlingAdapterFactory(coroutineScope, errorHandler))
```

Adapter will intercept all failed responses and pass them to provided error handler, which can parse them and
return proper exception.

This allows you to parse standard error responses from your server and return a standardized exception whenever
a request fails.

### Stale while revalidate

```kotlin
retrofitBuilder.addCallAdapterFactory(StaleWhileRevalidateCallAdapterFactory(coroutineScope, errorReporter))
```

This call adapter factory will emit data twice. Immediately upon calling the get method, `Outcome.Progress` will be emitted with
stale cached data,
while new fresh data will be loaded from the server. Afterwards, `Outcome.Success` will be emitted with the
new fresh server data.

This allows you to immediately display data (albeit stale) to the user, while new data is being fetched.

To use the factory, you need enable [OkHttp Cache](https://square.github.io/okhttp/features/caching/) and
use Flow as a return type:

```kotlin
interface MyService {
   @GET("/data")
   fun getData(): Flow<Outcome<MyData>>
}
```

Factory will also properly respect force refresh header, similarly to the Bypass Cache Interceptor (see below).

## Interceptors

### Bypass Cache Interceptor

```kotlin
okHttpBuilder.addInterceptor(BypassCacheInterceptor())
```

This interceptor makes it easier to bypass Okhttp Cache.

With it active, you can add `HEADER_FORCE_REFRESH` header as a boolean to every request:

```kotlin
interface MyService {
   @GET("/data")
   fun getData(
      @Header(SyntheticHeaders.HEADER_FORCE_REFRESH)
      force: Boolean = false
   ): MyData
}
```

Whenever this header set to `true`, call will ignore cache and will always be made on the network. Synthetic
header is purged before it goes out, server should never actually receive it.

## Lazy Retrofit Converter Factory

This factory allows you to initialize a `Converter.Factory` lazily. Instead of being created immediately,
factory will be created on the first request (ideally on the background thread). For example, for Moshi:

```kotlin
retrofitBuilder.addConverterFactory(LazyRetrofitConverterFactory(lazy { MoshiConverterFactory.create(moshi) }))
```

For example, Moshi uses quite a bit of reflection underneath while being created, which can slow down app startup
if initialized eagerly. By deferring initialization, app is opened faster, improving user experience.

## Java Time Moshi adapter

```kotlin
moshiBuilder.add(JavaTimeMoshiAdapter)
```

Moshi adapter for converting java.time's `Instant`, `LocalTime`, `LocalDate`, `LocalDateTime` and `ZonedDateTime` to ISO String
for JSON serialization.

## Enum json fallback

Annotation `@JsonFallback` for adding fallback value to enum.
Enum class can be added via `Moshi.Builder` with extension function `addEnumAdapter`.

```kotlin
@JsonClass(generateAdapter = false)
enum class Level {
   @Json(name = "low")
   LOW,

   @Json(name = "medium")
   MEDIUM,

   @Json(name = "high")
   HIGH,

   @JsonFallback
   OTHER;
}
```

## OKHttp Cache manager

(Only on Android projects)

```kotlin
val manager = GlobalOkHttpDiskCacheManager(context, errorReporter)

...

okHttpBuilder.cache(manager.cache)
```

Class that automatically determines proper OkHttp cache size based on the
[Android system's assigned quota](https://developer.android.com/reference/android/os/storage/StorageManager#getCacheQuotaBytes(java.util.UUID))
.

## Miscellaneous collections of utility methods

* [Moshi](src/jvmCommon/kotlin/si/inova/kotlinova/retrofit/moshi/Moshi.kt)

# Test utilities

## MockWebServer creator

A builder that allows you to easily create OKHttp mock server and perform service tests:

```kotlin
@Test
fun `A MockWebServer test`() {
   mockWebServer {
      mockJsonResponse("/data") {
         setJsonBody("""{"key": "value"}""")
      }

      val service: TestRetrofitService = createRetrofitService(this.baseUrl)

      ...
   }
}
```

## ServiceTestingHelper

Utility that allows easy creation of fake retrofit services. For example:

```kotlin
class FakeMyService(private val helper: ServiceTestingHelper = ServiceTestingHelper()) : MyService, FakeService by helper {
   override suspend fun getData(): Data {
      helper.intercept()

      return ...
    }
}
```

Then tests can easily make the service return loading or error:

```kotlin
val service = FakeMyService()

// Normal call
val data = service.getData()

// Infinite loading
service.interceptNextCallWith(InterceptionStyle.InfiniteLoad)
val data = service.getData()
service.completeInfiniteLoad() // Can resume later with this call


// Error
service.interceptNextCallWith(InterceptionStyle.Error(IOException))
val data = service.getData()
```
