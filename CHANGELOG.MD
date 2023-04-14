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
