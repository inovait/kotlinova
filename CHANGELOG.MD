## [2.6.1](http://hydra/utilities/kotlinova/compare/v2.6.0...v2.6.1) (2019-10-30)

### Bug Fixes

* updated mockitoKotlin version, which fixes NoSuchMethodError exception on MockitoKt.withSettings method ([e8808448](http://hydra/utilities/kotlinova/commit/e8808448c42eddadb23070858e3dad0844b33dd8))

## [2.6.0](http://hydra/utilities/kotlinova/compare/v2.5.1...v2.6.0) (2019-10-28)

### Features

* **rx:** added offer field to OnDemandProvider ([1152ab0a](http://hydra/utilities/kotlinova/commit/1152ab0a75650df9c26ad90de4db0cb1254b0a76))

### Bug Fixes

* **firebase:** Firebase observables stalling UI thread ([14c31181](http://hydra/utilities/kotlinova/commit/14c31181b27f4207baa397c6143c20d947a9b552))

## [2.5.1](http://hydra/utilities/kotlinova/compare/v2.5.0...v2.5.1) (2019-10-22)

### Bug Fixes

* added gradle properties ([569b2c3b](http://hydra/utilities/kotlinova/commit/569b2c3bbe5006a98dadee9aca42106e165a2f1e))
* return job on launch ([282faefc](http://hydra/utilities/kotlinova/commit/282faefc077352503e284d63f7a97181c8a8272c))

## [2.5.0](http://hydra/matejd/kotlinova/compare/v2.4.0...v2.5.0) (2019-10-10)

### Features

* **android:** horizontal recycler list section ([feed1478](http://hydra/matejd/kotlinova/commit/feed14784df8454a5622a222b6f98a7726e2c009))
* **android:** add a way to launch coroutines in Lifecycle scope ([d6791606](http://hydra/matejd/kotlinova/commit/d67916068ea139e8f52bb73d7eaa447cabc3151d))
* **android:** add AlwaysActiveLifecycleOwner ([63bc4469](http://hydra/matejd/kotlinova/commit/63bc446962e2cb4116ba1dccaef86e38c0b15224))
* **android:** add LiveData delegate utils ([d1091902](http://hydra/matejd/kotlinova/commit/d1091902f53d6f043026523be27f4f0cb3c03432))
* **rx:** allow scheduler declaring on RxSchedulerRule ([b3e10ecd](http://hydra/matejd/kotlinova/commit/b3e10ecd11766a30826ad34f59152e636a4c75a5))
* **android:** bypass ListSection's async processing on empty lists ([96a93abf](http://hydra/matejd/kotlinova/commit/96a93abfd62edf40b5b3b50848f08cacce6c9862))

### Bug Fixes

* fix nullability issues to allow build ([2a5c6ff0](http://hydra/matejd/kotlinova/commit/2a5c6ff01deda1f59bf10747b7930169c37cbbba))
* set fragment argument value immediately ([51c1d869](http://hydra/matejd/kotlinova/commit/51c1d869a35a7aca3cb5828c9e53cb1592891d12))
* **android:** add missing proguard rules ([5004b8dd](http://hydra/matejd/kotlinova/commit/5004b8ddc5c0a2da37c3f1bc7dc9e15a93971c5d))
* **core:** isLoadingWithValue error message ([59bacd9e](http://hydra/matejd/kotlinova/commit/59bacd9e56a481c83f31a398e807f8c482c26b02))

## [2.4.0](http://hydra/matejd/kotlinova/compare/v2.3.0...v2.4.0) (2019-04-29)

### Features

* offset paginated query ([ec4d19e5](http://hydra/matejd/kotlinova/commit/ec4d19e5d3bc0a7ef487b2a69970813ecd47e036))

## [2.3.0](http://hydra/utilities/kotlinova/compare/v2.3.0..v2.2.1) (2019-04-08)

### Features

* **room:** add RoomLoadableResource ([194fadad](http://hydra/utilities/kotlinova/commit/194fadad2c4a8655c41d39e3bd5437fb5d6dc65b))
* build project with Java 8 desugaring ([e9602712](http://hydra/utilities/kotlinova/commit/e9602712384007acab52cc8ea0173f9512509180))
* **room:** add WeakInvalidationObserver ([6553f6c4](http://hydra/utilities/kotlinova/commit/6553f6c49cc4df7177fdf5867f10d0d1ec5113e9))
* **rx:** add LoadableResource ([7b38a307](http://hydra/utilities/kotlinova/commit/7b38a3072eb1ecbcc1f661662513e68ac5e4f871))
* **core:** automatically cast resource utilities, add more resource utilities ([2c964996](http://hydra/utilities/kotlinova/commit/2c96499662871ac78aee0bd777d984c8e4a32b2c))
* **core:** add more AssertJ utilities ([1e1b9921](http://hydra/utilities/kotlinova/commit/1e1b9921d4f7870b1d0784a393dc8c4ac0b33db3))
* **android:** add IdlingResource.waitUntilIdle utility ([1d8ce2b0](http://hydra/utilities/kotlinova/commit/1d8ce2b016b5fe165e9d699284888d3e89ed458b))
* **android:** add per-test ignore notification to IdlingResourceRule ([e820cc55](http://hydra/utilities/kotlinova/commit/e820cc55db265408d396138eafe6565cb85e1420))
* **android:** add dispatcher selection to LastResultAsyncItemProcessor ([76d5731f](http://hydra/utilities/kotlinova/commit/76d5731ff3a6ad34af7ef4c7eefcb29d9c0a9f78))
* add room module ([5469bef4](http://hydra/utilities/kotlinova/commit/5469bef4ee7b599866bea54f133c0cf6a3566fb3))
* add AssertJ assertion helpers ([b4c35f48](http://hydra/utilities/kotlinova/commit/b4c35f48bb25e03cb8179a5df127c6ad791a651f))

### Bug Fixes

* **android:** thread races in JobCheckingDispatcherWrapper ([023d4248](http://hydra/utilities/kotlinova/commit/023d424819ccdfba3d7bb45248acba99013428fc))

## [2.2.1](http://hydra/utilities/kotlinova/v2.2.1..v2.2.0) (2019-04-01)

### Bug Fixes

* Call invoke on a copied version of listeners to allow modification while iteration ([da89929a](http://hydra/utilities/kotlinova/commit/da89929af775f2ea44d4c8b3bfc5cc58d5f944d2))

## [2.2.0](http://hydra/utilities/kotlinova/compare/v2.1.1...v2.2.0) (2019-03-06)

### Features

* **core:** add several test utilities ([6acb2b77](http://hydra/utilities/kotlinova/commit/6acb2b77e622a314653ffaf7be1b8e774512c3b2))
* **android:** add PicassoIdlingResource ([dc83ba84](http://hydra/utilities/kotlinova/commit/dc83ba8459114dbebf2a15ff11a04f32c34d8b13))
* **android:** add several espresso testing utilities ([68a3a2c9](http://hydra/utilities/kotlinova/commit/68a3a2c95b636ae5788b7f661fcd6138fe95f124))

### Bug Fixes

* **rx:** deprecate Single and Completable awaits ([6db67168](http://hydra/utilities/kotlinova/commit/6db67168501ce00e874f86fa648a6c1ebbf514c1))

## [2.1.1](http://hydra/utilities/kotlinova/compare/v2.0.1...v2.1.1) (2019-02-06)


### Bug Fixes

* **android:** extend placeholder blending over empty sections ([39c623ce](http://hydra/utilities/kotlinova/commit/39c623ce))
* **android:** remove forced adjustViewBounds from AspectRatioImageview ([069b01b4](http://hydra/utilities/kotlinova/commit/069b01b4))
* **android:** fix StateSaverFragment crash without activity ([c9be48f9](http://hydra/utilities/kotlinova/commit/c9be48f9))

### Features

* **android:** add ListSection.clearList() ([7b9b7c22](http://hydra/utilities/kotlinova/commit/7b9b7c22))
* **android:** add exclusiveObserveEvent ([989f3925](http://hydra/utilities/kotlinova/commit/989f3925))
* report exceptions from CoroutinesTimeMachines ([502e7856](http://hydra/utilities/kotlinova/commit/502e7856))
* **android:** add common errors to CoroutineViewModel ([493d34c5](http://hydra/utilities/kotlinova/commit/493d34c5))
* **android:** add utilities for resource observing ([3cb7fccc](http://hydra/utilities/kotlinova/commit/3cb7fccc))

## [2.0.1](http://hydra/utilities/kotlinova/compare/v2.0.0...v2.0.1) (2018-12-13)


### Bug Fixes

* **rx:** expose RxCoroutinesTimeMachine ([18d0a9d6](http://hydra/utilities/kotlinova/commit/18d0a9d6))
* **retrofit:** coroutine call adapter error handling ([76294147](http://hydra/utilities/kotlinova/commit/76294147))


## [2.0.0](http://hydra/utilities/kotlinova/compare/v1.5.3...v2.0.0) (2018-11-20)

### BREAKING Bug fixes

* TimeProvider timezone was always UTC ([a7625ec2](http://hydra/utilities/kotlinova/commit/a7625ec2))

### BREAKING Features

* update coroutines to 0.30.2 ([6d12a715](http://hydra/utilities/kotlinova/commit/6d12a715))
* update coroutines to 0.30.2-eap13 and kotlin to 1.3 ([2c42e53e](http://hydra/utilities/kotlinova/commit/2c42e53e))
* update coroutines to 1.0.0 ([9abca5d3](http://hydra/utilities/kotlinova/commit/9abca5d3))
* migate to AndroidX ([6453b08c](http://hydra/utilities/kotlinova/commit/6453b08c))
* **core:** change dispatcher override to return CoroutineContext ([fcacdec1](http://hydra/utilities/kotlinova/commit/fcacdec1))
* **android:** android dispatcher returns CoroutineContext ([41d59884](http://hydra/utilities/kotlinova/commit/41d59884))
* **rx:** rewrite OnDemandProvider with new coroutines ([ec1c05fe](http://hydra/utilities/kotlinova/commit/ec1c05fe))
* move dispatchers proxy to common class ([e85afaa4](http://hydra/utilities/kotlinova/commit/e85afaa4))
* **android:** add CoroutineScope to CoroutineViewModel ([dff95f36](http://hydra/utilities/kotlinova/commit/dff95f36))
* **android:** delete unused CenterFirstDecoration ([5b10ff53](http://hydra/utilities/kotlinova/commit/5b10ff53))
* **rx:** remove PaginatedQuery.reset ([2721cd88](http://hydra/utilities/kotlinova/commit/2721cd88))
* **android:** remove SectionRecyclerAdapter detach ([08d81e6c](http://hydra/utilities/kotlinova/commit/08d81e6c))

### Bug Fixes

* **android:** PreferenceProperty null default crash ([817668bf](http://hydra/utilities/kotlinova/commit/817668bf))
* **core:** use SupervisorJob on ViewModel ([962b3eb5](http://hydra/utilities/kotlinova/commit/962b3eb5))
* **rx:** lock OnDemandProvider into mutex ([abd0ae70](http://hydra/utilities/kotlinova/commit/abd0ae70))

### Features

* add RxCoroutinesTimeMachine ([c5062ace](http://hydra/utilities/kotlinova/commit/c5062ace))
* add CoroutinesTimeMachine ([33a28b0f](http://hydra/utilities/kotlinova/commit/33a28b0f))
* **android:** remove ResourceLiveData.setValue deprecation ([1ef779cd](http://hydra/utilities/kotlinova/commit/1ef779cd))
* **android:** make LiveData observing utils inline ([340762cc](http://hydra/utilities/kotlinova/commit/340762cc))

### Documentation

* **rx:** add experimental suppress explanation ([a1a2f8b7](http://hydra/utilities/kotlinova/commit/a1a2f8b7))
* **android:** add SectionRecyclerAdapter.getInnerPosition docs ([017ffa99](http://hydra/utilities/kotlinova/commit/017ffa99))


## [1.5.3](http://hydra/utilities/kotlinova/compare/v1.5.2...v1.5.3) (2018-09-19)


### Bug Fixes

* **android:** RecyclerStateSaver not restoring ([d017fb2](http://hydra/utilities/kotlinova/commit/d017fb2))




## [1.5.2](http://hydra/utilities/kotlinova/compare/v1.5.1...v1.5.2) (2018-09-17)


### Bug Fixes

* LiveDataChannel race condition crash ([1e62414](http://hydra/utilities/kotlinova/commit/1e62414))
* ViewModel crash on addSource errors ([5e93f11](http://hydra/utilities/kotlinova/commit/5e93f11))




## [1.5.1](http://hydra/utilities/kotlinova/compare/v1.5.0...v1.5.1) (2018-09-12)


### Bug Fixes

* **android:** createdForTheFirstTime false negative ([c4ff8bb](http://hydra/utilities/kotlinova/commit/c4ff8bb))




# [1.5.0](http://hydra/utilities/kotlinova/compare/v1.4.0...v1.5.0) (2018-09-11)


### Bug Fixes

* **android:** createdForTheFirstTime false positive ([8a93be5](http://hydra/utilities/kotlinova/commit/8a93be5))
* **retrofit:** return Unit from empty response ([66db477](http://hydra/utilities/kotlinova/commit/66db477))


### Features

* **android:** remove ResourceLiveData sources after error ([114e32d](http://hydra/utilities/kotlinova/commit/114e32d))
* **android:** use old resource value as new Loading ([a7112ff](http://hydra/utilities/kotlinova/commit/a7112ff))




# [1.4.0](http://hydra/utilities/kotlinova/compare/v1.3.0...v1.4.0) (2018-08-22)


### Bug Fixes

* **android:** PreferenceProperty default null crash ([18462a6](http://hydra/utilities/kotlinova/commit/18462a6))


### Features

* **rx:** add coroutine adapters for Single and Completable ([7ce2848](http://hydra/utilities/kotlinova/commit/7ce2848))




# [1.3.0](http://hydra/utilities/kotlinova/compare/v1.2.0...v1.3.0) (2018-07-24)


### Features

* **retrofit:** add UnitConverterFactory ([f1f33bc](http://hydra/utilities/kotlinova/commit/f1f33bc))




# [1.2.0](http://hydra/utilities/kotlinova/compare/v1.1.2...v1.2.0) (2018-07-16)


### Bug Fixes

* **retrofit:** deprecate removeMoshiClassJsonAdapter ([e84313f](http://hydra/utilities/kotlinova/commit/e84313f))


### Features

* **android:** add AndroidTimeProvider ([02f96df](http://hydra/utilities/kotlinova/commit/02f96df))
* **android:** deprecate merged TimeProvider ([9d94237](http://hydra/utilities/kotlinova/commit/9d94237))
* **core:** add JavaTimeProvider ([df75629](http://hydra/utilities/kotlinova/commit/df75629))




## [1.1.2](http://hydra/utilities/kotlinova/compare/v1.1.1...v1.1.2) (2018-07-04)


### Bug Fixes

* **retrofit:** remove leftover android dependencies ([8830799](http://hydra/utilities/kotlinova/commit/8830799))




## [1.1.1](http://hydra/utilities/kotlinova/compare/v1.1.0...v1.1.1) (2018-07-03)


### Bug Fixes

* **android:** Fix proguard errors with library ([e4910eb](http://hydra/utilities/kotlinova/commit/e4910eb))




# [1.1.0](http://hydra/utilities/kotlinova/compare/v1.0.0...v1.1.0) (2018-07-03)


### Bug Fixes

* exclude kotlin_module from gms package ([392e41b](http://hydra/utilities/kotlinova/commit/392e41b))
* improve PreferenceProperty null handling ([ee21457](http://hydra/utilities/kotlinova/commit/ee21457))
* non-android project mavenLocal publish ([42cd060](http://hydra/utilities/kotlinova/commit/42cd060))
* proguard build ([0fc6a45](http://hydra/utilities/kotlinova/commit/0fc6a45))
* remove android import from TimedDispatcher ([d2d2c80](http://hydra/utilities/kotlinova/commit/d2d2c80))
* remove duplicate TestUtils ([177ea6f](http://hydra/utilities/kotlinova/commit/177ea6f))
* remove main.kotlin_module from all modules ([3cb9d9a](http://hydra/utilities/kotlinova/commit/3cb9d9a))
* sources generation for non-android projects ([3b9ca8f](http://hydra/utilities/kotlinova/commit/3b9ca8f))
* support multiple observers in SingleLiveEvent ([127dfbc](http://hydra/utilities/kotlinova/commit/127dfbc))


### Features

* add BehaviorSubjectProxy ([c2bef51](http://hydra/utilities/kotlinova/commit/c2bef51))
* add makeNonEditable() method to view ([8288283](http://hydra/utilities/kotlinova/commit/8288283))
* add mapAsync method for Rx ([67b1ef9](http://hydra/utilities/kotlinova/commit/67b1ef9))
* add MoshiAcessors for removing ClassJsonAdapter ([a9b973d](http://hydra/utilities/kotlinova/commit/a9b973d))
* add QueryObservable ([0d0d0e3](http://hydra/utilities/kotlinova/commit/0d0d0e3))
* add reset() method to PaginatedQuery ([0b1ec61](http://hydra/utilities/kotlinova/commit/0b1ec61))
* add several view utils ([cb42920](http://hydra/utilities/kotlinova/commit/cb42920))
* added DocumentMemoryCache ([9fae7cc](http://hydra/utilities/kotlinova/commit/9fae7cc))
* allow DocumentMemoryCache mocking ([cbc0150](http://hydra/utilities/kotlinova/commit/cbc0150))
* allow smooth reset on swipe menu view ([486c1e6](http://hydra/utilities/kotlinova/commit/486c1e6))
* make hours optional on TimeFormat ([dde0dc0](http://hydra/utilities/kotlinova/commit/dde0dc0))
* support suspending lambdas on ViewImageGenerator ([1895b99](http://hydra/utilities/kotlinova/commit/1895b99))



