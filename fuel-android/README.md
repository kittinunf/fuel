# fuel-android
The android package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-android` with `Maven` and `Gradle`. The android package has the following dependencies:
* [`Fuel`](../fuel/README.md)
* Android SDK: 19+


```groovy
implementation 'com.github.kittinunf.fuel:fuel:<latest-version>'
implementation 'com.github.kittinunf.fuel:fuel-android:<latest-version>'
```

## Usage

The `fuel` core package automatically uses the `AndroidEnvironment` from the `fuel-android` package to redirect callbacks to the main looper thread.

### Making Requests

It is the same with core package, so refer on core documentation at [here](../fuel/README.md)
