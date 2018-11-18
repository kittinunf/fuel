# fuel-kotlinx-serialization
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.10-blue.svg)](https://kotlinlang.org)

The kotlinx-serialization extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-kotlinx-serialization` with `Maven` and `Gradle`. The kotlinx-serialization package has the following dependencies:
* `fuel:fuel:<same-version>`
* Kotlin: 1.3.10
* [KotlinX Serialization](https://github.com/Kotlin/kotlinx.serialization#gradlejvm): 0.9.0

```groovy
compile 'com.github.kittinunf.fuel:fuel:<latest-version>'
compile 'com.github.kittinunf.fuel:fuel-kotlinx-serialization:<latest-version>'
```

## Usage

### Deserialization using kotlinx.serialzationn

```kotlin
@Serializable
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent")
    .responseObject<HttpBinUserAgentModel> { _, _, result -> }
```

This is by default strict and will reject unknown keys, for that you can pass a custom JSOn instance

`JSON(nonstrict = true)`
```kotlin
@Serializable
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent")
    .responseObject<HttpBinUserAgentModel>(json = JSON(nonstrict = true)) { _, _, result -> }
```

`kotlinx.serialization` can not always guess the correct serialzer to use, when generics are involved for example

```kotlin
@Serializable
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/list/user-agent")
    .responseObject<HttpBinUserAgentModel>(loader = HttpBinUserAgentModel.serilaizer().list) { _, _, result -> }
```

It can be used with coroutines by using `kotlinxDeserilaizerOf()` it takes the same `json` and `loader` as parameters

```kotlin
@Serializable
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent")
    .awaitResponseObject<HttpBinUserAgentModel>(kotlinxDeserializerOf()) { _, _, result -> }
```
