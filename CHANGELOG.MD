## [2.2.1](http://hydra/matejd/kotlinova/v2.2.1..v2.2.0) (2019-04-01)

### Bug Fixes

* Call invoke on a copied version of listeners to allow modification while iteration ([da89929a](http://hydra/matejd/kotlinova/commit/da89929af775f2ea44d4c8b3bfc5cc58d5f944d2))

## [2.2.0](http://hydra/matejd/kotlinova/v2.2.0..v2.1.1) (2019-03-06)

### Features

* **core:** add several test utilities ([6acb2b77](http://hydra/matejd/kotlinova/commit/6acb2b77e622a314653ffaf7be1b8e774512c3b2))
* **android:** add PicassoIdlingResource ([dc83ba84](http://hydra/matejd/kotlinova/commit/dc83ba8459114dbebf2a15ff11a04f32c34d8b13))
* **android:** add several espresso testing utilities ([68a3a2c9](http://hydra/matejd/kotlinova/commit/68a3a2c95b636ae5788b7f661fcd6138fe95f124))

### Bug Fixes

* **rx:** deprecate Single and Completable awaits ([6db67168](http://hydra/matejd/kotlinova/commit/6db67168501ce00e874f86fa648a6c1ebbf514c1))

## [2.1.1](http://hydra/matejd/kotlinova/compare/v2.1.1...v2.1.0) (2019-02-06)


### Bug Fixes

* **android:** extend placeholder blending over empty sections ([39c623ce](http://hydra/matejd/kotlinova/commit/39c623ce))
* **android:** remove forced adjustViewBounds from AspectRatioImageview ([069b01b4](http://hydra/matejd/kotlinova/commit/069b01b4))
* **android:** fix StateSaverFragment crash without activity ([c9be48f9](http://hydra/matejd/kotlinova/commit/c9be48f9))

### Features

* **android:** add ListSection.clearList() ([7b9b7c22](http://hydra/matejd/kotlinova/commit/7b9b7c22))
* **android:** add exclusiveObserveEvent ([989f3925](http://hydra/matejd/kotlinova/commit/989f3925))
* report exceptions from CoroutinesTimeMachines ([502e7856](http://hydra/matejd/kotlinova/commit/502e7856))
* **android:** add common errors to CoroutineViewModel ([493d34c5](http://hydra/matejd/kotlinova/commit/493d34c5))
* **android:** add utilities for resource observing ([3cb7fccc](http://hydra/matejd/kotlinova/commit/3cb7fccc))

## [2.0.1](http://hydra/matejd/kotlinova/compare/v2.0.0...v2.0.1) (2018-12-13)


### Bug Fixes

* **rx:** expose RxCoroutinesTimeMachine ([18d0a9d6](http://hydra/matejd/kotlinova/commit/18d0a9d6))
* **retrofit:** coroutine call adapter error handling ([76294147](http://hydra/matejd/kotlinova/commit/76294147))


## [2.0.0](http://hydra/matejd/kotlinova/compare/v1.5.3...v2.0.0) (2018-11-20)

### BREAKING Bug fixes

* TimeProvider timezone was always UTC ([a7625ec2](http://hydra/matejd/kotlinova/commit/a7625ec2))

### BREAKING Features

* update coroutines to 0.30.2 ([6d12a715](http://hydra/matejd/kotlinova/commit/6d12a715))
* update coroutines to 0.30.2-eap13 and kotlin to 1.3 ([2c42e53e](http://hydra/matejd/kotlinova/commit/2c42e53e))
* update coroutines to 1.0.0 ([9abca5d3](http://hydra/matejd/kotlinova/commit/9abca5d3))
* migate to AndroidX ([6453b08c](http://hydra/matejd/kotlinova/commit/6453b08c))
* **core:** change dispatcher override to return CoroutineContext ([fcacdec1](http://hydra/matejd/kotlinova/commit/fcacdec1))
* **android:** android dispatcher returns CoroutineContext ([41d59884](http://hydra/matejd/kotlinova/commit/41d59884))
* **rx:** rewrite OnDemandProvider with new coroutines ([ec1c05fe](http://hydra/matejd/kotlinova/commit/ec1c05fe))
* move dispatchers proxy to common class ([e85afaa4](http://hydra/matejd/kotlinova/commit/e85afaa4))
* **android:** add CoroutineScope to CoroutineViewModel ([dff95f36](http://hydra/matejd/kotlinova/commit/dff95f36))
* **android:** delete unused CenterFirstDecoration ([5b10ff53](http://hydra/matejd/kotlinova/commit/5b10ff53))
* **rx:** remove PaginatedQuery.reset ([2721cd88](http://hydra/matejd/kotlinova/commit/2721cd88))
* **android:** remove SectionRecyclerAdapter detach ([08d81e6c](http://hydra/matejd/kotlinova/commit/08d81e6c))

### Bug Fixes

* **android:** PreferenceProperty null default crash ([817668bf](http://hydra/matejd/kotlinova/commit/817668bf))
* **core:** use SupervisorJob on ViewModel ([962b3eb5](http://hydra/matejd/kotlinova/commit/962b3eb5))
* **rx:** lock OnDemandProvider into mutex ([abd0ae70](http://hydra/matejd/kotlinova/commit/abd0ae70))

### Features

* add RxCoroutinesTimeMachine ([c5062ace](http://hydra/matejd/kotlinova/commit/c5062ace))
* add CoroutinesTimeMachine ([33a28b0f](http://hydra/matejd/kotlinova/commit/33a28b0f))
* **android:** remove ResourceLiveData.setValue deprecation ([1ef779cd](http://hydra/matejd/kotlinova/commit/1ef779cd))
* **android:** make LiveData observing utils inline ([340762cc](http://hydra/matejd/kotlinova/commit/340762cc))

### Documentation

* **rx:** add experimental suppress explanation ([a1a2f8b7](http://hydra/matejd/kotlinova/commit/a1a2f8b7))
* **android:** add SectionRecyclerAdapter.getInnerPosition docs ([017ffa99](http://hydra/matejd/kotlinova/commit/017ffa99))


## [1.5.3](http://hydra/matejd/kotlinova/compare/v1.5.2...v1.5.3) (2018-09-19)


### Bug Fixes

* **android:** RecyclerStateSaver not restoring ([d017fb2](http://hydra/matejd/kotlinova/commit/d017fb2))




## [1.5.2](http://hydra/matejd/kotlinova/compare/v1.5.1...v1.5.2) (2018-09-17)


### Bug Fixes

* LiveDataChannel race condition crash ([1e62414](http://hydra/matejd/kotlinova/commit/1e62414))
* ViewModel crash on addSource errors ([5e93f11](http://hydra/matejd/kotlinova/commit/5e93f11))




## [1.5.1](http://hydra/matejd/kotlinova/compare/v1.5.0...v1.5.1) (2018-09-12)


### Bug Fixes

* **android:** createdForTheFirstTime false negative ([c4ff8bb](http://hydra/matejd/kotlinova/commit/c4ff8bb))




# [1.5.0](http://hydra/matejd/kotlinova/compare/v1.4.0...v1.5.0) (2018-09-11)


### Bug Fixes

* **android:** createdForTheFirstTime false positive ([8a93be5](http://hydra/matejd/kotlinova/commit/8a93be5))
* **retrofit:** return Unit from empty response ([66db477](http://hydra/matejd/kotlinova/commit/66db477))


### Features

* **android:** remove ResourceLiveData sources after error ([114e32d](http://hydra/matejd/kotlinova/commit/114e32d))
* **android:** use old resource value as new Loading ([a7112ff](http://hydra/matejd/kotlinova/commit/a7112ff))




# [1.4.0](http://hydra/matejd/kotlinova/compare/v1.3.0...v1.4.0) (2018-08-22)


### Bug Fixes

* **android:** PreferenceProperty default null crash ([18462a6](http://hydra/matejd/kotlinova/commit/18462a6))


### Features

* **rx:** add coroutine adapters for Single and Completable ([7ce2848](http://hydra/matejd/kotlinova/commit/7ce2848))




# [1.3.0](http://hydra/matejd/kotlinova/compare/v1.2.0...v1.3.0) (2018-07-24)


### Features

* **retrofit:** add UnitConverterFactory ([f1f33bc](http://hydra/matejd/kotlinova/commit/f1f33bc))




# [1.2.0](http://hydra/matejd/kotlinova/compare/v1.1.2...v1.2.0) (2018-07-16)


### Bug Fixes

* **retrofit:** deprecate removeMoshiClassJsonAdapter ([e84313f](http://hydra/matejd/kotlinova/commit/e84313f))


### Features

* **android:** add AndroidTimeProvider ([02f96df](http://hydra/matejd/kotlinova/commit/02f96df))
* **android:** deprecate merged TimeProvider ([9d94237](http://hydra/matejd/kotlinova/commit/9d94237))
* **core:** add JavaTimeProvider ([df75629](http://hydra/matejd/kotlinova/commit/df75629))




## [1.1.2](http://hydra/matejd/kotlinova/compare/v1.1.1...v1.1.2) (2018-07-04)


### Bug Fixes

* **retrofit:** remove leftover android dependencies ([8830799](http://hydra/matejd/kotlinova/commit/8830799))




## [1.1.1](http://hydra/matejd/kotlinova/compare/v1.1.0...v1.1.1) (2018-07-03)


### Bug Fixes

* **android:** Fix proguard errors with library ([e4910eb](http://hydra/matejd/kotlinova/commit/e4910eb))




# [1.1.0](http://hydra/matejd/kotlinova/compare/v1.0.0...v1.1.0) (2018-07-03)


### Bug Fixes

* exclude kotlin_module from gms package ([392e41b](http://hydra/matejd/kotlinova/commit/392e41b))
* improve PreferenceProperty null handling ([ee21457](http://hydra/matejd/kotlinova/commit/ee21457))
* non-android project mavenLocal publish ([42cd060](http://hydra/matejd/kotlinova/commit/42cd060))
* proguard build ([0fc6a45](http://hydra/matejd/kotlinova/commit/0fc6a45))
* remove android import from TimedDispatcher ([d2d2c80](http://hydra/matejd/kotlinova/commit/d2d2c80))
* remove duplicate TestUtils ([177ea6f](http://hydra/matejd/kotlinova/commit/177ea6f))
* remove main.kotlin_module from all modules ([3cb9d9a](http://hydra/matejd/kotlinova/commit/3cb9d9a))
* sources generation for non-android projects ([3b9ca8f](http://hydra/matejd/kotlinova/commit/3b9ca8f))
* support multiple observers in SingleLiveEvent ([127dfbc](http://hydra/matejd/kotlinova/commit/127dfbc))


### Features

* add BehaviorSubjectProxy ([c2bef51](http://hydra/matejd/kotlinova/commit/c2bef51))
* add makeNonEditable() method to view ([8288283](http://hydra/matejd/kotlinova/commit/8288283))
* add mapAsync method for Rx ([67b1ef9](http://hydra/matejd/kotlinova/commit/67b1ef9))
* add MoshiAcessors for removing ClassJsonAdapter ([a9b973d](http://hydra/matejd/kotlinova/commit/a9b973d))
* add QueryObservable ([0d0d0e3](http://hydra/matejd/kotlinova/commit/0d0d0e3))
* add reset() method to PaginatedQuery ([0b1ec61](http://hydra/matejd/kotlinova/commit/0b1ec61))
* add several view utils ([cb42920](http://hydra/matejd/kotlinova/commit/cb42920))
* added DocumentMemoryCache ([9fae7cc](http://hydra/matejd/kotlinova/commit/9fae7cc))
* allow DocumentMemoryCache mocking ([cbc0150](http://hydra/matejd/kotlinova/commit/cbc0150))
* allow smooth reset on swipe menu view ([486c1e6](http://hydra/matejd/kotlinova/commit/486c1e6))
* make hours optional on TimeFormat ([dde0dc0](http://hydra/matejd/kotlinova/commit/dde0dc0))
* support suspending lambdas on ViewImageGenerator ([1895b99](http://hydra/matejd/kotlinova/commit/1895b99))



