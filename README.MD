# KOTLINOVA

Set of common utility classes for Kotlin projects, primarily based on Kotlin coroutines.

## Installation

1. Add Inova's nexus to your `build.gradle`'s maven repositories

    ```
    maven { url 'http://nexus/content/repositories/releases' }
    ```

2. Add modules to your `build.gradle`'s dependencies.

## Modules

Replace `X.Y.Z` with latest version. Check out [changelog](http://hydra/utilities/kotlinova/blob/master/CHANGELOG.MD) to see what is the latest version. For simplicity's sake, every module has the same version number.

Some Kotlinova modules are either native JVM (can be used in all JVM applications),
while others are Android-only.

### Core ![Java Module](docs/java_module.png)

`implementation "si.inova.kotlinova:core:X.Y.Z"`

Core module is the main dependency all other modules depend on. It mostly contains unit-testing helpers for Kotlin's coroutines and some utility classes.

### Rx ![Java Module](docs/java_module.png)

`implementation "si.inova.kotlinova:rx:X.Y.Z"`

[RxJava](https://github.com/ReactiveX/RxJava) Utilities.

### Retrofit ![Java Module](docs/java_module.png)

`implementation "si.inova.kotlinova:retrofit:X.Y.Z"`

Utilities for consuimg REST APIs via [Retrofit](https://github.com/square/retrofit) and [Moshi](https://github.com/square/moshi)

### Android ![Android Module](docs/android_module.png)

`implementation "si.inova.kotlinova:android:X.Y.Z"`

Utilities for integrating Android architecture components (ViewModel, LiveData) with Kotlin's coroutines and common UI components (such as Loading spinners, recycler adapters etc.)

### Gms ![Android Module](docs/android_module.png)

`implementation "si.inova.kotlinova:gms:X.Y.Z"`

Utilities for integrating Google Play Services with Kotlin's coroutines

### Firebase ![Android Module](docs/android_module.png)

`implementation "si.inova.kotlinova:gms:X.Y.Z"`

Utilities for integrating Firebase with Kotlin's coroutines, Firestore mocks for testing and helpers for routing Firestore data into standard Observables.

## Versioning

This library uses [Semantic Versioning](https://semver.org/). Version number is updated automatically whenever new release is built.

When updating, it is recommended that you check out [changelog](http://hydra/utilities/kotlinova/blob/master/CHANGELOG.MD) first to see the changes, especially if there was major version change.

## Contributing

To contribute to this set of libraries:

1. Checkout `master`
2. Create new branch for your contribution
3. Commit your work. While commiting use [semantic commit messages](https://github.com/angular/angular/blob/master/CONTRIBUTING.md#commit). Scope tag should be the name of the module you are updating.
4. Try to avoid breaking changes, but if it cannot be avoided, you must put `BREAKING CHANGE` in the footer of the commit mesasage and explain the change.
5. Create merge request
6. When your MR is merged, new release will be generated automatically