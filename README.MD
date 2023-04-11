# KOTLINOVA

An opinionated set of common utility classes for Kotlin projects, primarily based on Kotlin coroutines.

## Modules

### • [Core](core/README.MD)

### • [Compose](compose/README.MD)

### • [Navigation](navigation/README.MD)

### • [Retrofit](retrofit/README.MD)

## Versioning

This library uses [Semantic Versioning](https://semver.org/). Version number is updated automatically whenever new release is
built.

When updating, it is recommended that you check out [changelog](CHANGELOG.MD) first to see the changes, especially if there was
major version change.

## Contributing

To contribute to this set of libraries:

1. Checkout `master`
2. Create new branch for your contribution
3. Commit your work. While commiting, use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/). Scope tag should
   be the name of the module you are updating.
4. Try to avoid breaking changes, but if it cannot be avoided, you must put `BREAKING CHANGE` in the footer of the commit mesasage
   and explain the change.
5. Manually run instrumented tests (`./gradlew connectedCheck`) - we do not run those on CI at the moment.
5. Create merge request
6. When your PR is merged, new release will be generated automatically
