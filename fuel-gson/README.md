# fuel-gson
The gson extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-gson` with `Maven` and `Gradle`. The gson package has the following dependencies:
* [`Fuel`](../fuel/README.md)
* Gson: 2.8.5

```groovy
implementation 'com.github.kittinunf.fuel:fuel:<latest-version>'
implementation 'com.github.kittinunf.fuel:fuel-gson:<latest-version>'
```

## Usage

See `FuelGson.kt`

### Gson Deserialization

* Fuel also provides a built in support for Gson Deserialization. This is possible by including the [Gson](https://github.com/kittinunf/Fuel/tree/master/fuel-gson) module in your dependency block.

```kotlin
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent").responseObject<HttpBinUserAgentModel> { _, _, result -> 
  // handle result similarly as it is shown by the core module
}
```
