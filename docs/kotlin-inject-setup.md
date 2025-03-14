# Setting up Kotlin inject in your Android app

This is a quick and opinionated guide on setting up Kotlin Inject + Anvil in your Android project, meant to be used with
the [kotlinova-navigation](../navigation/README.MD).

1. Add compiler and runtime dependencies of
   the [kotlin-inject](https://github.com/evant/kotlin-inject?tab=readme-ov-file#download)
   and [kotlin-inject-anvil](https://github.com/amzn/kotlin-inject-anvil?tab=readme-ov-file#setup) to your project
2. Add `runtime-optional` dependency of
   the [kotlin-inject-anvil](https://github.com/amzn/kotlin-inject-anvil?tab=readme-ov-file#setup) to your project
3. Create basic `AppComponent` for your app, that will serve as a base component for all your `kotlin-inject` injections:

```kotlin
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
@Component
abstract class AppComponent() : AppComponentMerged
```

All parts of this explained:

* `@MergeComponent` - signals to anvil that it should merge all child components with `AppScope` into this component
* `@SingleIn` - marks this component as an owner of the `AppScope` scope. Every dependency marked with this same annotation will
  be a singleton inside this component (and consequently, app-wide singleton)
* `@Component`- Signals to kotlin-inject to generate code for it
* `AppComponentMerged` - requirement for Anvil. This will be red initially, but should get resolved on the first build

4. Build the project, to allow kotlin-inject to set up all required files

4. Inside your [Application](https://developer.android.com/reference/android/app/Application) class, create our AppComponent
   component and pass the application to it:

```kotlin
class DemoApplication : Application() {
   val component = AppComponent::class.create()
}
```

5. Go through steps 1-5 of the [navigation setup](../navigation/README.MD#setup)
6. Build the project, to allow kotlin-inject to set up all required files
7. Get the navigation component inside your activity by getting the application, casting it to your application class and
   accessing component:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
   val navigationComponent = (application as DemoApplication).component.createNavigationComponent()

   ...
}
```

8. Continue with the steps 6+ of the [navigation setup](../navigation/README.MD#setup)
