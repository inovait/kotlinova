# Gradle Utilities

Direct gradle usage:

```kotlin
classpath("si.inova.kotlinova:gradle:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
kotlinova = "X.Y.Z"
```

```toml
[libraries]
kotlinova-gradle = { module = "si.inova.kotlinova:gradle", version.ref = "kotlinova" }
```

```kotlin
classpath(libs.kotlinova.gradle)
```

Replace `X.Y.Z` with latest version. Check out [releases](https://github.com/inovait/kotlinova/releases) to see what is the latest
version.

# Features

## Adding kotlinova plugin

All features above require "kotlinova" plugin to be added to the project. Then you can use `kotlinova` extension:

```kotlin
plugins {
   id("kotlinova")
}

kotlinova {
   ...
}
```

##Sarif merging

Sarif merging feature allows you to simply enable merging of all sarif reports of all static checks in sub-modules into
one big top level `merge.sarif`.

## Detekt

To include detekt's sarif output, enable this on all sub-modules that produce detekt reports:

```kotlin
kotlinova {
   mergeDetektSarif = true
}
```

## Android Lint

To include detekt's sarif output, enable this on all sub-modules that produce lint reports:

```kotlin
kotlinova {
   mergeAndroidLintSarif = true
}
```

Note, that you still need to enable sarif manually in lint configuration:

```kotlin
android {
   sarifReport = true
}
```

## Top Level

Lastly, you need to apply `kotlinova` plugin to your top-level project.
Afterwards, you can call `reportMerge` to merge all sarif tasks. Note that, for this to work properly, you MUST call
merging at the same time as detekt reporting (for example `./gradlew detekt reportMerge`).

# Detekt's pre-commit hook

This allows one-line enabling of the
[Detekt's pre-commit hook](https://detekt.dev/docs/gettingstarted/git-pre-commit-hook/) that will setup
detekt to only scan for staged git files whenever `-precommit` flag is added to the gradle's command line.

```kotlin
kotlinova {
   enableDetektPreCommitHook = true
}
```

To use, add `-Pprecommit=true` flag to your pre-commit hook. For example:

```bash
./gradlew -Pprecommit=true detekt
```

# Android benchmark result upload to Google Cloud metrics

Plugin also contains a task that will upload results
from [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)
to [Google Cloud Monitoring](https://cloud.google.com/monitoring) where you can put results into charts and/or set up
alerts when results change.

This is similar
to [Google's Macrobenchmarking sample](https://github.com/android/performance-samples/tree/main/MacrobenchmarkSample/ftl),
except that no Firebase functions are required (everything happens locally in Gradle).

Benchmark results are uploaded as custom metrics under, `custom.googleapis.com/android/{TEST_NAME}/{METRIC_NAME}/{PART}`,
where `TEST_NAME` is the name of the test function that generated the result, converted to camel_case, `METRIC_NAME` is the
remapped
name of the defined metric (see below for mapping info) and `PART` is the name of the result part, such as `median` or `P99`.

To authenticate with Google Cloud, you need to set `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
See [their documentation](https://cloud.google.com/docs/authentication/application-default-credentials#GAC) for more info.

You enable the upload by registering a new monitoring export task:

```kotlin
val benchmarkResultsUpload by tasks.registering(GoogleCloudBenchmarkUpload::class) {
   // Project ID of the target google cloud project
   googleCloudProjectId = "id-of-the-target-google-cloud-project"

   // Set of JSON benchmark result JSON files
   benchmarkResultFiles.from(
      fileTree("/path/to/benchmark/json/results/") {
         include("*-portrait/artifacts/sdcard/Download/*-benchmarkData.json")
      }
   )

   // Map that converts metric name (which can contain invalid characters like spaces) into
   // simple camel_case name that can be used in Google Cloud's custom metric path
   metricMap.putAll(
      mapOf(
         "JIT compiling %Count" to "jit_count",
         "timeToInitialDisplayMs" to "time_to_initial_display",
      )
   )
}
```

then you call the task (`./gradlew benchmarkResultsUpload`) to initiate the upload.
