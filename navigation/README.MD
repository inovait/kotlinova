# Navigation

Direct gradle usage:

```kotlin
implementation("si.inova.kotlinova:navigation:X.Y.Z")
anvil("si.inova.kotlinova:navigation-compiler:X.Y.Z")
implementation("si.inova.kotlinova:navigation-fragment:X.Y.Z")
```

Version catalogs usage:

```toml
[versions]
kotlinova = "X.Y.Z"
```

```toml
[libraries]
kotlinova-navigation = { module = "si.inova.kotlinova:navigation", version.ref = "kotlinova" }
kotlinova-navigation-compiler = { module = "si.inova.kotlinova:navigation-compiler", version.ref = "kotlinova" }
kotlinova-navigation-fragment = { module = "si.inova.kotlinova:navigation-fragment", version.ref = "kotlinova" }
```

```kotlin
implementation(libs.kotlinova.navigation)
anvil(libs.kotlinova.navigation.compiler)
implementation(libs.kotlinova.navigation.fragment)
```

Replace `X.Y.Z` with latest version. Check out [changelog](../CHANGELOG.MD) to see what is the latest version.

# Features

Kotlinova Navigation is yet another type safe parcelable-keys-based navigation library for Compose, based on the
[Anvil](https://github.com/square/anvil) and [Simple Stack](https://github.com/Zhuinden/simple-stack).

See [sample](sample) project for more practical examples.

## Creating screens

Main selling point of this library is that, for every screen, you only need to create two classes.

First a parcelable key for the screen, which contains screen's arguments and metadata:

```kotlin
@Parcelize
data class MyScreenKey(
   val firstArgument: Int,
   val secondArgument: String
) : ScreenKey()
```

Second, you create a Screen class that contains actual composable:

```kotlin
class MyScreen : Screen<MyScreenKey> {
   @Composable
   override fun Content(key: MyScreenKey) {
      // Screen content here, with direct access to key for getting its arguments.
   }
}
```

And that's it. There is no need to manually put this screen into any xml, when statement, map etc. Anvil compiler does the magic
for you behind the scenes. Even better, **Key and its corresponding screen do not need to be in the same module**. This allows
you to completely decouple your screen modules from each other. Only thing that screen needs to navigate to another screen, is the
key, which can be in some common module. Actual screen, can be in a private module that nobody (except for the main module)
depends on, allowing for very flat module tree.

## Navigating to another screen

Constructor of every screen behaves similarly to the `@Inject` constructor
(but please do not add explicit `@Inject` keyword as it may mess up lifecycles - injection happens automatically).

To navigate to another screen, you need to get access to the `si.inova.kotlinova.navigation.navigator.Navigator`. Since
constructor is an injecting one, simply inject it to the screen. Then you can call `Navigator.navigateTo(anotherScreen)`
to navigate:

```kotlin
class MyScreen(
   private val navigator: Navigator
) : Screen<MyScreenKey> {
   @Composable
   override fun Content(key: MyScreenKey) {
      Button(onClick = { navigator.navigateTo(AnotherScreenKey(...)) }) {
         ...
      }
   }
}
```

## Keys without arguments

If the screen does not require any arguments, you can just create it as an `object` instead of `data class`.
However, this will force you to manually implement `hashCode`, `equals` and `toString`.
To avoid that, you can either:

a) Have your object extend `NoArgsScreenKey`:

```kotlin
@Parcelize
object MyScreenKey : NoArgsScreenKey()
```

b) Enable and use [data objects](https://kotlinlang.org/docs/object-declarations.html#data-objects):

```kotlin
@Parcelize
data object MyScreenKey : ScreenKey()
```

## Scoped Services

To persist data between activity configuration changes and/or share data between screens, you can create
[Scoped Services](https://github.com/Zhuinden/simple-stack/wiki/5.-Scoping,-data-or-state-sharing-between-screens#service-lifecycle)
.

Simply put, a Scoped Service is very similar to the AndroidX ViewModel - It is created when screen first added to the
navigation backstack and is kept alive until screen is removed from the backstack, including through Activity's configuration
change restarts.

To create a scoped service, simply create your class and have that class extend `ScopedService`.
Class also needs to have an `@Inject` constructor, since it is created by the framework (of course, you can
use this constructor to inject any dependencies that the service needs):

```kotlin
class MyService @Inject constructor() : ScopedService {

}
```

To use the service, simply add it to the constructor of your screen:

```kotlin
class MyScreen(
   private val myService: MyService
) : Screen<MyScreenKey> {
   ...
}
```

### Coroutine Scoped Service

If you need to create coroutines inside your services, your service can extend `CoroutineScopedService`.

You have to provide new `CoroutineScope` to its constructor, allowing for customization of various scope parameters,
such as dispatcher. We recommend that you also pass construction of the scope to the injection and then
create a scope within Dagger module. That way, you can easily create fake version of the service inside unit tests with test
dispatcher.

Passed coroutine scope will be cancelled automatically when service is destroyed.

```kotlin
class MyService @Inject constructor(coroutineScope: CoroutineScope) : CoroutineScopedService(coroutineScope) {
   // You can use coroutineScope variable inside.
   // Scope will automatically get cancelled when service is destroyed.
}
```

### Saveable scoped service

Scoped services only survive activity configuration change, but they do not survive process kills, when Android system kills
your app to reclaim memory.

If your service keeps important data (such as entered form data, before user confirms the form), that data should also survive p
process death. To do this, your service can
implement [Bundleable](https://github.com/Zhuinden/simple-stack/blob/master/simple-stack/src/main/java/com/zhuinden/simplestack/Bundleable.java)
.

Navigation library already includes a convenience implementation of `Bundleable` that allows you to create variables inside your
services that will survive process deaths, using `by saved` and `by savedFlow` syntax:

```kotlin
class MySaveableService @Inject constructor(coroutineScope: CoroutineScope) : SaveableScopedService(coroutineScope) {
   // Contents of those two variables will be saved and restored across process kills
   val savedVariable by saved("Hello")
   val savedFlow by savedFlow("World")

   override fun onServiceRegistered() {
      // Data is guaranteed to be restored at this point
   }
}
```

### Single Screen View Model

If your service is scoped only to a single screen, it can extend `SingleScreenViewModel`. This will
allow you to access `key` property, which will contain a key that was used to create that screen. You can use it,
for example, to automatically load data on that screen, without the need for manual load calls.

```kotlin
class MyScreenViewModel @Inject constructor(coroutineScope: CoroutineScope) : SingleScreenViewModel<MyScreenKey>(coroutineScope) {
   override fun onServiceRegistered() {
      // Key is available at this point
      val key: MyScreenKey = key
   }
}
```

(Note that key in this ViewModel is static, it will not update even if screen key updates, for example in SingleTopKey scenario.
In this case, you need to pass updates from screen to the ViewModel manually).

### Sharing scoped services between screens

By default, scoped services will be only scoped to a single screen (e.g. every screen will have its own instance of the services).

You can override this behavior by overriding `getScopeTag` on the keys of the screens that you want to share
services:

```kotlin
@Parcelize
data class FirstScreenKey(...) : ScreenKey() {
   override fun getScopeTag(): String {
      return "SharedScope"
   }
}

@Parcelize
data class SecondScreenKey(...) : ScreenKey() {
   override fun getScopeTag(): String {
      return "SharedScope"
   }
}
```

All screens with the same scope tag will share the same service instances. For this to work properly,
you also must inject all shared services into all screens.

## Custom animations

To customize animations, you can override `forwardAnimation` and `backwardAnimation` methods of the screen's key.
See [AnimatedContent's transitionSpec](https://developer.android.com/jetpack/compose/animation/composables-modifiers#animatedcontent)
for more info on the contents of these methods.

See [SlideAnimationScreenKey](sample/keys/src/main/java/si/inova/kotlinova/navigation/sample/keys/SlideAnimationScreenKey.kt)
for example.

## Single top screens

You can have any key extend `SingleTopKey`. When you navigate to that screen and the same type
of screen is already displayed, navigation part will be skipped and only `Content` composable will
be re-called with the new key. For example:

```kotlin
@Parcelize
data class TabScreenKey(val selectedTab: SelectedTab = SelectedTab.A) : SingleTopKey() {
   enum class SelectedTab {
      A,
      B,
      C
   }
}
```

## Screen composition

In addition to injecting services, screens can also inject other screens in their constructor.
This allows easy composition of screens inside another screens.

For example:

```kotlin
class TabScreen(
   private val firstTabSubscreen: FirstTabScreen,
   private val secondTabSubscreen: SecondTabScreen,
   private val thirdTabSubscreen: ThirdTabScreen,
) : Screen<TabScreenKey>() {
   @Composable
   override fun Content(key: MyScreenKey) {
      TabBar()
      val displayedScreen = ...

      when (displayedScreen) {
         0 -> firstTabSubscreen.Content(key)
         1 -> secondTabSubscreen.Content(key)
         2 -> thirdTabSubscreen.Content(key)
      }
   }
}
```

Note that injected screens share same scope, so scoped services of sub-screens will have
the same lifecycle as the parent screen.

## Advanced Navigation

You might have noticed that `Navigator.navigateTo` is an extension method and the only method on the actual
`Navigator` is `Navigator.navigate(NavigationInstructions)`.

`NavigationInstructions` is a composable, parcelable class that tells navigation what do to.
For example, let's take a look at `GoBack` NavigationInstruction:

```kotlin
@Parcelize
object GoBack : NavigationInstruction() {
   override fun performNavigation(backstack: List<ScreenKey>, context: NavigationContext): NavigationResult {
      return if (backstack.size > 1) {
         NavigationResult(backstack.dropLast(1), StateChange.BACKWARD)
      } else {
         NavigationResult(backstack, StateChange.REPLACE)
      }
   }
}
```

As you can see, NavigationInstruction receives a backstack and returns a new backstack. This allows very flexible navigation,
as developer can be in a full control of the backstack. In addition to the backstack, navigation instruction also
returns state change type (either `FORWARD`, `BACKWARD` or `REPLACE`). This determines what kind of animation is played
when navigation is performed.

Navigation instructions are classes instead of just extension methods to allow composition of them. For example,
you can use `MultiNavigationInstructions` to perform multiple navigation actions at once:

```kotlin
navigator.navigate(
   MultiNavigationInstructions(
      OpenScreen(FirstScreen),
      OpenScreen(SecondScreen)
   )
)
```

In addition, since instructions are parcelable, you can pass them around, which has several useful properties,
for example for conditional navigation (see below).

You can use one of the several [pre-written navigation instructions](src/main/kotlin/si/inova/kotlinova/navigation/instructions)
or feel free to write your own.

## Nested navigation

You can use pre-defined `NestedBackstackScreen` to host nested navigation backstack inside a screen. You can either
compose that screen inside your screen (see "Screen composition" above) or navigate to it directly
via `NestedNavigationScreenKey`.

Inside nested screen, regular `private val navigator: Navigator` will navigate the nested backstack. But you can also
inject `@MainNavigation private val mainNavigator: Navigator` which will navigate outer (root) backstack.

See [NestedScreen in sample](sample/nested/src/main/java/si/inova/kotlinova/navigation/sample/nested/NestedScreen.kt)
for example.

Note that main and nested screen do not share scoped services - nested screens have their own scope.

## Conditional navigation

Library has a conditional navigation system, where any screen can define a condition that has to be resolved, before
a screen can be navigated to (for example, user has to be logged in before opening profile screen).

To enable this, you first have to create a condition. This is a simple object or a data class
(if condition has arguments, like login type):

```kotlin
@Parcelize
object LoginCondition : NavigationCondition
```

Then, you need to define a condition navigation handler. This class will be called whenever library wants to navigate to a
condition.
Called method is an interception method and user will be navigate to whatever you return from that method. If condition is
already met, you can just pass along `navigateToIfConditionMet`. Otherwise, you have to navigate to a screen that allows user to
meet that condition (or an error screen if user cannot meet it).

To register the handler with the navigation library, you must put `@ContributesMultibinding` and `@ClassKey` annotations on it.

For example:

```kotlin
@ClassKey(LoginCondition::class)
@ContributesMultibinding(OuterNavigationScope::class)
class LoginConditionalNavigationHandler @Inject constructor(
   // You can inject dependencies, such as login repository here
) : ConditionalNavigationHandler {
   override fun getNavigationRedirect(
      condition: NavigationCondition,
      navigateToIfConditionMet: NavigationInstruction
   ): NavigationInstruction {
      // If your Condition has arguments, you can cast `condition` to your condition type
      // and get the arguments here

      val isUserLoggedIn = ...
      return if (isUserLoggedIn) {
         // No need to do anything, user is already logged in
         navigateToIfConditionMet
      } else {
         // User is not logged in. Return a navigation instruction instead that will
         // navigate user to the login screen and pass successful navigation instruction along.
         // If login succeeds, login screen must call Navigator.navigate() with the passed 
         // navigation instruction
         val loginScreenKey = MyLoginScreenKey(navigateToIfConditionMet)
         OpenScreen(loginScreenKey)
      }
   }
}
```

See [login example](sample/conditional/src/main/java/si/inova/kotlinova/navigation/sample/conditional)
for more information.

## Navigating to fragments

Navigation also has an optional `navigation-fragment` library that allows navigation to fragments instead of to 100% compose
screens, while
attempting to conform to the usual fragment lifecycle as closely as possible.

This is mostly meant as a migration tool: You can use this library to navigate to all of your all fragments first and then you can
migrate your screens one-by-one to compose.

To navigate to a fragment, you first have to create a key for your fragment. Key must implement `FragmentScreenKey`.
To conform to that interface, you need to provide a unique `tag` for your key that persists over configuration changes and
process death. Easiest way to do so is to put the tag into key's constructor and give it a default random value:

```kotlin
@Parcelize
data class MyFragmentScreenKey(
   ...,
   override val tag: String = UUID.randomUUID().toString()
) : ScreenKey(), FragmentScreenKey

```

Then you have to create a screen for that key. Screen should extend `FragmentScreen` and pass all its constructor arguments.
Inside
that screen, you implement `createFragment` method where you create a `Fragment` instance (and pass all arguments, if applicable):

```kotlin
class DemoFragmentScreen(scopeExitListener: ScopeExitListener) : FragmentScreen<MyFragmentScreenKey>(scopeExitListener) {
   override fun createFragment(key: MyFragmentScreenKey, fragmentManager: FragmentManager): Fragment {
      return ...
   }
}
```

## Deep link handling

Navigation also has optional system to route deep links to the navigation screens.

System works by creating `DeepLinkHandler` classes. You can create as many of them as you want, in as many
modules as you want (so you can logically group them with features in the same module). Use
`@ContributesMultibinding(OuterNavigationScope::class)` to register them with Anvil to ensure they will be called.

Every `DeepLinkHandler` will receive `Uri`. You need to then parse that Uri. If it matches your screen,
you have to return `NavigationInstruction` that opens your screen. Oherwise, return null. `startup` argument is true if your
app was started using this deep link.

You can use `matchDeepLink` helper to make deep link parsing easier:

```kotlin
@ContributesMultibinding(OuterNavigationScope::class)
class MyScreenDeepLinkHandler @Inject constructor() : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return uri.matchDeepLink("myapp://screenA/{argument}") {
         val argument = it.getValue("argument")

         // Use OpenScreenOrMoveToTop to ensure user cannot open same screen multiple times
         // by opening the same deep link multiple times in succession
         return OpenScreenOrMoveToTop(MyScreenKey(argument))
      }
   }
}
```

If your handler needs to handle multiple deep links, you can use `handleMultipledeepLinks`:

```kotlin
@ContributesMultibinding(OuterNavigationScope::class)
class MyDeepLinkHandler @Inject constructor() : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean): NavigationInstruction? {
      return handleMultipleDeepLinks(uri, startup) {
         matchDeepLink("myapp://screenA") { args, startup ->
            OpenScreenOrMoveToTop(...)
         }
         matchDeepLink("myapp://screenB") { args, startup ->
            OpenScreenOrMoveToTop(...)
         }
      }
   }
}
```

# Setup

To add the library to your app:

1. Add navigation library as implementation dependency and navigation compiler as anvil dependency
   (see the top of this README)
2. Create Key and Screen for your root screen
3. Set up [Anvil](https://github.com/square/anvil) injection in your app (easiest way to do this is via
   [Whetstone](https://github.com/deliveryhero/whetstone), with your own application component)
4. Add `@MergeComponent(OuterNaviationScope::class)` to your application component:

```kotlin
@MergeComponent(OuterNavigationScope::class)
@...
interface MyApplicationComponent : ApplicationComponent {
   ...
}
```

5. In your main activity (or whatever activity you want to use navigation in), inject `NavigationInjection.Factory`:

```kotlin
class MainActivity : Activity() {
   @Inject
   lateinit var navigationInjectionFactory: NavigationInjection.Factory

   ...
}
```

6. Inside a composable inside main activity (for example, inside `setContent` lambda), call `RootNavigationContainer` and pass in
   your initial
   backstack, usually just `History.of` with your root screen:

```kotlin
setContent {
   navigationInjectionFactory.RootNavigationContainer { History.of(FirstScreenKey) }
}
```

7. That's it. When you run your app, it should display the first screen.

## Deep links

If you want to use deep link integration feature, you will need some extra setup steps:

1. Set your activity's `launchMode` to `singleTask` - this will ensure proper value of the `startup` parameter in the
   deep link handlers
1. Inject `MainDeepLinkHandler` and `NavigationContext` to your activity:

```kotlin
@Inject
lateinit var mainDeepLinkHandler: MainDeepLinkHandler

@Inject
lateinit var navigationContext: NavigationContext
```

2. Inside Activity's `onCreate` method, query deep link handlers for received deep link, if any. If `savedInstanceState` is not
   null, it means that activity was recreated, so we need to ignore the uri, otherwise we will open it twice. Note that
   we call the handler with `startup` = `true`, because this happens on the app's startup.

```kotlin
val deepLinkTarget = if (savedInstanceState == null) {
   intent?.data?.let { mainDeepLinkHandler.handleDeepLink(it, startup = true) }
} else {
   null
}
```

3. When you call `RootNavigationContainer`, navigate to the deep link instead of to the default screen, if applicable:

```kotlin
navigationInjectionFactory.RootNavigationContainer {
   val initialBackstack = History.of(FirstScreenKey)

   if (deepLinkTarget != null) {
      deepLinkTarget.performNavigation(initialBackstack, navigationContext).newBackstack
   } else {
      initialBackstack
   }
}
```

4. Get `backstack` from the `RootNavigationContainer` and then call `handleNewIntentDeepLinks`. This method will
   automatically handle all deep links that arrive while the app is open.

```kotlin
val backstack = navigationInjectionFactory.RootNavigationContainer { ... }
mainDeepLinkHandler.HandleNewIntentDeepLinks(this@MainActivity, backstack)
```
