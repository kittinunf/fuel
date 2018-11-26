# fuel-android
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.10-blue.svg)](https://kotlinlang.org)

The android package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-android` with `Maven` and `Gradle`. The android package has the following dependencies:
* `fuel:fuel:<same-version>`
* Kotlin: 1.3.10
* Android SDK: 19+


```groovy
compile 'com.github.kittinunf.fuel:fuel:<latest-version>'
compile 'com.github.kittinunf.fuel:fuel-android:<latest-version>'
```

## Usage

The `fuel` core package automatically uses the `AndroidEnvironment` from the `fuel-android` package to redirect callbacks to the main looper thread.

### Making Requests

It is the same with core package, so refer on core documentation at [here](../fuel/README.md)
