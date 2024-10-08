# 3.2.0

### Features

* **navigation**: enable predictive back animations ([22aa6cbc](https://github.com/inovait/kotlinova/commit/22aa6cbcf6fc6d05e8d8c33b45b65d0564663455))
* **navigation**: enable predictive back animations ([1013bacb](https://github.com/inovait/kotlinova/commit/1013bacb5758237e510115ed4eea53268059aa7b))
* **core**: added better-named Bundle.toMap() ([64160120](https://github.com/inovait/kotlinova/commit/64160120281dd2dad258ae29e62eba3d7aa88a8c))

### Bug Fixes

* **gradle**: disable build cache on reportMerge task ([dca90585](https://github.com/inovait/kotlinova/commit/dca90585126de68146cb6941b75917b2f684faa3))
* **compose**: remove garbage collected callbacks on unregister ([df683b1c](https://github.com/inovait/kotlinova/commit/df683b1c683063e4d64f7816df0226330a18b42d))
* **compose**: FakeCoilLoader crashing on many colors ([831a7cf2](https://github.com/inovait/kotlinova/commit/831a7cf2b138b973b61285cb37948231d13fce55))
* **gradle**: detect pre-commit hook compatibility with project isolation ([8b354a3e](https://github.com/inovait/kotlinova/commit/8b354a3e84fbcecf60ba83debed59c1e157738bf))
* **gradle**: sarif merging compatibility with project isolation ([3316e161](https://github.com/inovait/kotlinova/commit/3316e161551c93ca4274415e221434d0d129c89b))
* **retrofit**: ErrorHandlingAdapterFactory stripping headers from responses ([93f0280a](https://github.com/inovait/kotlinova/commit/93f0280acc6b728cb2df3720e1df565b67a67d4c))

# 3.1.0

### Features

* **core**: add per-execution time customization to Debouncer ([b4a9c485](https://github.com/inovait/kotlinova/commit/b4a9c4857061956a8c21a158e7025d0a7cb5b643))

### Bug Fixes

* **gradle**: create single gitPreCommitFileList ([e6259b2f](https://github.com/inovait/kotlinova/commit/e6259b2fff7152b724ea9a47030c8b0ce3d6fb4d))
* **core**: Debouncer not properly delaying first trigger ([ff301e97](https://github.com/inovait/kotlinova/commit/ff301e97f006a0268a76560cb7f32b8dc231aae1))
* **core**: CancellationException propagation with catchIntoOutcome ([9177d83e](https://github.com/inovait/kotlinova/commit/9177d83e31866be4590e107edb565c33736733e1))
* **retrofit**: CancellationException propagation on stale while revalidate ([43c6283e](https://github.com/inovait/kotlinova/commit/43c6283e3c9dd233ce9ada324ecc256c150ba380))
* **navigation**: compatibility with anvil 2.5.0-beta04 ([1af4a083](https://github.com/inovait/kotlinova/commit/1af4a083bbcf19d5308567af3c51ffc51bd4f037))

# 3.0.2

### Bug Fixes

* **gradle**: pre-commit hook not working when gradle project not in git root ([14c6e415](https://github.com/inovait/kotlinova/commit/14c6e415a0ca1c4f039d76afa4fd0cfdb6f3b1fd))

# 3.0.1

### Bug Fixes

* **navigation**: add support for anvil 2.5.0's incremental compilation ([11c36c81](https://github.com/inovait/kotlinova/commit/11c36c81e22f17e34c416dc342da056988d0e12d))

# 3.0.0

### Bug Fixes

* **gradle**: put runs with fewer errors at the end of sarif ([b621d2c2](https://github.com/inovait/kotlinova/commit/b621d2c23a8f17d5ce285bbd6bcbee4f74a082f0))
* **navigation**: fix compatibility with compose 1.6.0 ([8a54fc85](https://github.com/inovait/kotlinova/commit/8a54fc85962691b6103b2cd4e629791f69aeb4c3))

# 3.0.0-alpha25

### Bug Fixes

* **gradle**: SarifMergeTask crashing on empty sarif collection ([1fc5ef46](https://github.com/inovait/kotlinova/commit/1fc5ef460075992008663a663f77535fe9611bc5))

# 3.0.0-alpha24

### Features

* **gradle**: use our own sarif merging task ([1a1dbc11](https://github.com/inovait/kotlinova/commit/1a1dbc1189456b849ecf962db1bf66d05533d371))

# 3.0.0-alpha23

### Bug Fixes

* **gradle**: compatibility with AGP 8.2.x ([dbe17ef2](https://github.com/inovait/kotlinova/commit/dbe17ef21473519ae094b625637df842d788ad92))

# 3.0.0-alpha22

### Features

* **core-test**: add TestScopeWithDispatcherProvider ([8644fc2e](https://github.com/inovait/kotlinova/commit/8644fc2eb07015b4e177ee18699788ee72150cb6))
* **gradle**: add detekt sarif merge ([8aaa330a](https://github.com/inovait/kotlinova/commit/8aaa330a7c86a5e5fcc50a0d4da263be37b53ec2))
* **gradle**: add android lint sarif merge ([e65080cd](https://github.com/inovait/kotlinova/commit/e65080cddc9d4dc74a9581f47b7513c6ba396aa3))
* **gradle**: add detect pre-commit hook ([b7938c1d](https://github.com/inovait/kotlinova/commit/b7938c1dfda9993fab187dbd507e8e153f738037))
* **gradle**: add android macrobenchmark upload task ([c9e0e1a1](https://github.com/inovait/kotlinova/commit/c9e0e1a1d37f51b8a6d928cb57bd51ab2f1035d5))

### Bug Fixes

* **navigation**: FragmentScreen not showing when activity starts in the background ([241350ff](https://github.com/inovait/kotlinova/commit/241350ff74d3f2daa7249d4519d643deddbaf898))

### Miscellaneous

* **gradle**: move tomlVersionBump into more general kotlinova plugin ([505f797e](https://github.com/inovait/kotlinova/commit/505f797ed3857a71b54f109146702d64bf0e3363))

# 3.0.0-alpha20

### Bug Fixes

* **navigation**: update to compose 1.5.x ([4f6110ff](https://github.com/inovait/kotlinova/commit/4f6110ffb5414b933b657efd3d9bfe8569946af0))

# 3.0.0-alpha19

### Features

* **core-test**: add plurals to FakeResources ([41fc4aa6](https://github.com/inovait/kotlinova/commit/41fc4aa6232b91e7144532c9a19238cba4d836c0))
* **gradle**: skip toml entries without version.ref ([bbdb753e](https://github.com/inovait/kotlinova/commit/bbdb753ec8b05de0e290cda166e0405ef2cc90e3))

# 3.0.0-alpha18

### Bug Fixes

* **navigation-detekt**: navigation detekt not being published by maven ([395ad3f4](https://github.com/inovait/kotlinova/commit/395ad3f4ad0ff8be40410482d8eafd21ff2b621e))

# 3.0.0-alpha17

### Features

* **core-test**: improve outcome assertions error messages ([43d1c57c](https://github.com/inovait/kotlinova/commit/43d1c57c13b37a18b6111fdd1fea597d1a5217ef))
* **navigation**: add detekt plugin to safeguard against enums ([cf6432c4](https://github.com/inovait/kotlinova/commit/cf6432c44155a9b8d0231fd501237c980cfba53c))

# 3.0.0-alpha16

### Bug Fixes

* migrate to kotest 5.6.2 ([02e4b6f9](https://github.com/inovait/kotlinova/commit/02e4b6f94b939528135574c1b95ee9b1fb7cd368))
* **navigation**: @Inherited not recursively fetching more than one level ([a00fd828](https://github.com/inovait/kotlinova/commit/a00fd8286c0115b3a39e3d05e5f4ebc6417f62a8))
* **navigation**: SingleTopKey not saving state after process kill ([8edffa7a](https://github.com/inovait/kotlinova/commit/8edffa7abd36438944ec04264c1b8c397e0480b0))

# 3.0.0-alpha15

### Features

* **core**: add USER_REQUESTED_REFRESH LoadingStyle ([04545f81](https://github.com/inovait/kotlinova/commit/04545f81dead6b04ccda866497e7d6cad8006037))
* **navigation**: add screenWrapper to RootNavigationContainer ([e6ed7378](https://github.com/inovait/kotlinova/commit/e6ed73785f44fc20fc4bbb5ecdf3492caa717055))
* **navigation**: allow boundType = Screen::class in @ContributesScreenBinding ([32ed4ea9](https://github.com/inovait/kotlinova/commit/32ed4ea931029006a71af7f27198755d8ce0bb07))
* **core**: add currentMonotonicTimeMillis to TimeProvider ([4f941d34](https://github.com/inovait/kotlinova/commit/4f941d34f6996d445de94b90215685686aac1dfb))
* **core**: add multiplatform Immutable annotation ([934a03cc](https://github.com/inovait/kotlinova/commit/934a03cce57a8aac876555c5b436ee47438ae683))
* **core**: mark Outcome as Immutable ([d4dc95d2](https://github.com/inovait/kotlinova/commit/d4dc95d2c7857a196629a71fa8dd2a54344b286f))
* **navigation**: improve duplicate screen error message ([5eec5f4c](https://github.com/inovait/kotlinova/commit/5eec5f4ca898bbdca4177a9ae8c6d30499d10646))

### Bug Fixes

* **core-test**: improve outcome assertion error messages ([d9beeba8](https://github.com/inovait/kotlinova/commit/d9beeba8015eabeb5814073630676d5acb3fe34c))
* **navigation-fragment**: onSaveInstanceState fragment crash ([4047e001](https://github.com/inovait/kotlinova/commit/4047e00162f9143cb8b2d121ac0e0de3d56a7f47))
* **navigation**: disable navigation while animating ([bc14a545](https://github.com/inovait/kotlinova/commit/bc14a545c368e102bc9f630b959b72dcc5db63ec))

# 3.0.0-alpha14

### Bug Fixes

* constant download attempts for lifecycle-runtime-compose:2.5.1 ([b8d55eb0](https://github.com/inovait/kotlinova/commit/b8d55eb05ec62d95a5ccc557a968eeba8050bf91))

# 3.0.0-alpha13

### Bug Fixes

* compatibility with coroutines 1.7.0 ([b5ce8aa4](https://github.com/inovait/kotlinova/commit/b5ce8aa4ad3bf56bfafba870cd543eb8c6ef32cc))

# 3.0.0-alpha12

### Features

* **retrofit**: improve failed error parsing error message ([f1c7190d](https://github.com/inovait/kotlinova/commit/f1c7190d2a01a5dedf6e17fe5c2dc3d86de99f54))
* **retrofit-test**: add default response to MockWebServerScope ([96afd0ce](https://github.com/inovait/kotlinova/commit/96afd0ce4965aab30554650987503c29e5ffd48b))

### Bug Fixes

* **compose-android-test**: improve LoadingCountingIdlingResource error message ([d3261eb4](https://github.com/inovait/kotlinova/commit/d3261eb40780da152eab586f84e65166e647e4f4))
* **navigation-fragment**: fragments being created asynchronously after first composition ([968a8f25](https://github.com/inovait/kotlinova/commit/968a8f25deb9fa3569e863ed2eca579b17ce0b98))
* **navigation-fragment**: multiple fragments in single scope not properly destroying ([4633c3f2](https://github.com/inovait/kotlinova/commit/4633c3f26c92dfa0affa1624590c554391102256))

# 3.0.0-alpha11

### BREAKING Bug Fixes

* **core**: remove PureApplicationScope ([4831f8e1](https://github.com/inovait/kotlinova/commit/4831f8e1d88c4aea6f390b215d82db48280d12b4))

### Features

* **navigation-test**: add test util for SaveableScopedService ([a1518af9](https://github.com/inovait/kotlinova/commit/a1518af9f64aa816f09d685d2209bef915b9d4b5))
* **navigation**: SaveableScopedService saved now requires non-null types ([435ba5d7](https://github.com/inovait/kotlinova/commit/435ba5d72eb6cfe9451e95f6b74c7c97345a4549))
* **navigation**: add scoped service inheritance ([fdd610de](https://github.com/inovait/kotlinova/commit/fdd610defed17f03d8b68018cffb0405741725fa))
* **navigation**: add @ContributesScreenBinding ([260a92f2](https://github.com/inovait/kotlinova/commit/260a92f2beac89493f4b98fa87e5f74124d30b6a))
* **navigation-test**: add FakeNavigator ([16f82a89](https://github.com/inovait/kotlinova/commit/16f82a8944c224ff121ab856e028df13bab1c1aa))
* **navigation**: add ReplaceTop instruction ([84d68681](https://github.com/inovait/kotlinova/commit/84d686813bb090614f0675ca5c5791e6ccad1be4))
* **navigation**: allow referencing generic Screen when performing screen composition ([23fb0a9c](https://github.com/inovait/kotlinova/commit/23fb0a9c886758060ed2b93cece9405f22a39e35))

### Bug Fixes

* **gradle**: improve error messages for missing toml entries ([1b61882a](https://github.com/inovait/kotlinova/commit/1b61882a6b2d4c6453894d5cc937a061a4217511))
* **navigation**: equals and hashcode implementation for MultiNavigationInstructions ([6199c5ff](https://github.com/inovait/kotlinova/commit/6199c5ff2e967b987e7cee65dfe24b2a68526a7d))
* **navigation**: scoped services not being initialized for abstract screens ([89f6f361](https://github.com/inovait/kotlinova/commit/89f6f3610287cee635e876c0eb8e0f9d37ecd137))

# 3.0.0-alpha10

### Features

* **core**: add data keeping to launchResourceControlTask ([945d0133](https://github.com/inovait/kotlinova/commit/945d013379bd37717b19b0132e31b38b1843a11f))
* **core**: defer default value init in SaveableScopedService ([fd738fa5](https://github.com/inovait/kotlinova/commit/fd738fa530820170c78249d77e4dabc63c9f709e))
* **core**: add more Outcome mapping functions ([fdab58f6](https://github.com/inovait/kotlinova/commit/fdab58f67473a59565f281de99ba48cee2d91f96))
* **core-test**: improve throwing error reporter ([cd5d78e3](https://github.com/inovait/kotlinova/commit/cd5d78e3ea8fd12dea063d8074988d401d38eed5))

# 3.0.0-alpha9

### Bug Fixes

* **navigation**: scoped services not being per-screen on backing out ([493a319d](https://github.com/inovait/kotlinova/commit/493a319d1692424a7c4e196130c8bd2e007fc33f))

# 3.0.0-alpha8

### Features

* add gradle module ([5b8821f8](https://github.com/inovait/kotlinova/commit/5b8821f8d065cff325cbb3dcc53802ed94f80f3d))
* **kotlinova-gradle**: print failed version checks ([1081657b](https://github.com/inovait/kotlinova/commit/1081657bb80e40f7fcbbb18e48bde752c3b52272))

# 3.0.0-alpha7

### Features

* add navigation ([689c65b3](https://github.com/inovait/kotlinova/commit/689c65b36acaee855259cfe2cd4b90779933a1d2))

* Revert ci: run emulator tests ([15258e7d](https://github.com/inovait/kotlinova/commit/15258e7db6939e083cc9b2b75fb9ea6a1dc879b8))

# 3.0.0-alpha6

### Features

* add compose module ([ed47bfe8](https://github.com/inovait/kotlinova/commit/ed47bfe862f24305e869349fc5dd28f868bc7696))
* add retrofit module ([c5e752c7](https://github.com/inovait/kotlinova/commit/c5e752c78aa06e9a946a3f4041e39e98fbecb95d))
* **core**: add AccessCallbackDispatcherProvider ([9150d253](https://github.com/inovait/kotlinova/commit/9150d253c573f20e53a33f83345851600fbe0a31))
* **core**: remove Flow.testWithExceptions utility ([428ff470](https://github.com/inovait/kotlinova/commit/428ff470a93d81de82fb2cce0b721b1d80b227f2))
* lower min sdk to 23 ([b74fa00b](https://github.com/inovait/kotlinova/commit/b74fa00be03b7d186743d3612b7b751e3fa9986c))
* **core**: add LocalizedText ([d2d8921f](https://github.com/inovait/kotlinova/commit/d2d8921fd6b117400431d437495a27c1e8a6db25))
* **retrofit**: add ServiceTestingHelper and moshi utilities ([3ed29517](https://github.com/inovait/kotlinova/commit/3ed29517e5c2f41205291cf47aa1ca391a74901a))
* **core**: add Debouncer ([5bac35e5](https://github.com/inovait/kotlinova/commit/5bac35e5fb154c6beebf118d1c42df3f375d1694))
* **core**: add several miscellaneous utilities ([5cdfd7a7](https://github.com/inovait/kotlinova/commit/5cdfd7a7cf16ca580003f1ae2a0965e9ca199e04))
* **retrofit-test**: remove the need to use mockWebServer in TestScope ([823e99e0](https://github.com/inovait/kotlinova/commit/823e99e0ecb9ad4be986ed0d5eb7d171ded84711))
* add compose-android-test ([1ebd0ae6](https://github.com/inovait/kotlinova/commit/1ebd0ae6f809204a9b3e7047a93c7db8feb08c6f))

### Bug Fixes

* **core**: Context.findActivity now returns regular Activity ([c744e6b2](https://github.com/inovait/kotlinova/commit/c744e6b263620926cf807489f8ebb5a855b96448))
* **core**: wrong BuildConfig entries ([17cc1bda](https://github.com/inovait/kotlinova/commit/17cc1bdaf5d1e9687cf85cd0b3e7bb52e8fcfa83))
* **core**: AndroidDateTimeFormatterImpl crashing on SDK 23 ([f52e75d1](https://github.com/inovait/kotlinova/commit/f52e75d1d056c22a57bd457715df11e5754d2599))

* localized text amend ([74b158bf](https://github.com/inovait/kotlinova/commit/74b158bf5942d35019d11dade3c8b06d0fbe6831))

# 3.0.0-alpha5

### Bug Fixes

* publish developer name ([f451c88e](https://github.com/inovait/kotlinova/commit/f451c88e288a49e61bb93ea0524595fe0dbcbaec))

# 3.0.0-alpha4

### Bug Fixes

* publish name and description ([59c88086](https://github.com/inovait/kotlinova/commit/59c8808646e94aab672b1093e8eb7ebb316fc7cc))

# 3.0.0-alpha3

### Bug Fixes

* also publish javadoc and SCM URL ([bfcb2a2b](https://github.com/inovait/kotlinova/commit/bfcb2a2be52fca4cae5042067a86834fe33d93f6))

# 3.0.0-alpha2

### Features

* **core**: add more documentation ([bfe0697d](https://github.com/inovait/kotlinova/commit/bfe0697d78a7f2198709be2bd7a6722a2209f832))

# 3.0.0-alpha1

## BREAKING

* Removed most of the old code. It was outdated and not updated in a while. It was time to start with a new slate.
* Added core module. 
