# Fuel

[![jcenter](https://api.bintray.com/packages/kittinunf/maven/Fuel-Android/images/download.svg)](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion)
[![mavenCentral](https://maven-badges.herokuapp.com/maven-central/com.github.kittinunf.fuel/fuel/badge.svg)](https://search.maven.org/search?q=g:com.github.kittinunf.fuel)
[![Build Status](https://travis-ci.org/kittinunf/fuel.svg?branch=master)](https://travis-ci.org/kittinunf/fuel)
[![Codecov](https://codecov.io/github/kittinunf/fuel/coverage.svg?branch=master)](https://codecov.io/gh/kittinunf/fuel)

The easiest HTTP networking library for Kotlin/Android.

> You are looking at the documentation for **2.x.y.**. If you are looking for the documentation for **1.x.y**, checkout [the 1.16.0 README.md](https://github.com/kittinunf/Fuel/blob/1.16.0/README.md)

## Features

- [x] HTTP `GET`/`POST`/`PUT`/`DELETE`/`HEAD`/`PATCH` requests in a fluent style interface
- [x] Asynchronous and blocking requests
- [x] Download as a file
- [x] Upload files, `Blob`s, `DataPart`s as `multipart/form-data`
- [x] Cancel asynchronous request
- [x] Debug logging / convert to cUrl call
- [x] Deserialization into POJO / POKO
- [x] Requests as [coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [x] API Routing

## Installation

We offer maven and jitpack installations. Maven via bintray only has stable releases but jitpack can be used to build any branch, commit and version.

### Maven
You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `Fuel` with `Maven` and `Gradle`. The core package has the following dependencies:
* Kotlin - [![Kotlin](https://img.shields.io/badge/Kotlin-1.4.10-blue.svg)](https://kotlinlang.org)
* [ Result ](https://github.com/kittinunf/result/) - 3.1.0

```groovy
  //core
  implementation 'com.github.kittinunf.fuel:fuel:<latest-version>'
  
  //packages
  implementation 'com.github.kittinunf.fuel:<package>:<latest-version>'
```

Make sure to include `jcenter()` or `mavenCentral()` in your repositories 
```groovy
repositories {
  jcenter() //or mavenCentral()
}
```

Each of the extensions / integrations has to be installed separately.

| Package | Description |
|----------|---------|
| [`fuel`](./fuel) | Core package |
| [`fuel-android`](./fuel-android) |  _Android_: Automatically invoke handler on Main Thread when using Android Module |
| [`fuel-coroutines`](./fuel-coroutines) | _KotlinX_: Execution with [coroutines](https://github.com/Kotlin/kotlinx.coroutines) |
| [`fuel-forge`](./fuel-forge) | _Deserialization_: [`Forge`](https://github.com/kittinunf/Forge/) |
| [`fuel-gson`](./fuel-gson) | _(De)serialization_: [`Gson`](https://github.com/google/gson) |
| [`fuel-jackson`](./fuel-jackson) | _Deserialization_: [`Jackson`](https://github.com/FasterXML/jackson-module-kotlin)
| [`fuel-json`](/fuel-json) | _Deserialization_: [`Json`](http://www.json.org/) |
| [`fuel-kotlinx-serialization`](/fuel-kotlinx-serialization) | _(De)serialization_: [`KotlinX Serialization`](https://github.com/Kotlin/kotlinx.serialization) |
| [`fuel-livedata`](./fuel-livedata) | _Android Architectures_: Responses as [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata.html) |
| [`fuel-moshi`](./fuel-moshi) | _Deserialization_: [`Moshi`](https://github.com/square/moshi)  |
| [`fuel-reactor`](./fuel-reactor) | _Reactive Programming_: Responses as [`Mono`](https://projectreactor.io/docs/core/release/reference/#mono) (**Project Reactor 3.x**)
| [`fuel-rxjava`](./fuel-rxjava) | _Reactive Programming_: Responses as [`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html) (**RxJava 2.x**)
| [`fuel-stetho`](./fuel-stetho) | _Utility_: Debug utility for Android on Chrome Developer Tools, [`Stetho`](https://github.com/facebook/stetho)

### Jitpack

If you want a SNAPSHOT distribution, you can use [`Jitpack`](https://jitpack.io/#kittinunf/fuel)

```kotlin
repositories {
  maven(url = "https://www.jitpack.io") {
    name = "jitpack"
  }
}

dependencies {
  //core
  implementation(group = "com.github.kittinunf.fuel", name = "fuel", version = "-SNAPSHOT")
  
  //packages
  // replace <package> with the package name e.g. fuel-coroutines
  implementation(group = "com.github.kittinunf.fuel", name = "<package>", version = "-SNAPSHOT")
}
```

or

```kotlin
dependencies {
  //core and/or packages
  // replace <package> with the package name e.g. fuel-coroutines
  listof("fuel", "<package>").forEach {
    implementation(group = "com.github.kittinunf.fuel", name = it, version = "-SNAPSHOT")
  }
}
```

#### Configuration
- `group` is made up of `com.github` as well as username and project name

- `name` is the subproject, this may be any of the packages listed in the [installation instructions](https://github.com/kittinunf/fuel#installation)
eg. `fuel`, `fuel-coroutines`, `fuel-kotlinx-serialization`, etc
- `version` can be the latest `master-SMAPSHOT` or `-SNAPSHOT` which always points at the HEAD or any other branch, tag or commit hash, e.g. as listed on [jitpack.io](https://jitpack.io/#kittinunf/fuel).

We recommend _not_ using `SNAPSHOT` builds, but a specific commit in a specific branch (like a commit on master), because your build will then be stable.

#### Build time-out
Have patience when updating the version of fuel or building for the first time as jitpack will build it, and this may cause the request to jitpack to time out. Wait a few minutes and try again (or check the status on jitpack).

**NOTE:** do _not_ forget to add the `kotlinx` repository when using `coroutines` or `serialization`

### Forks
Jitpack.io also allows to build from `fuel` forks. If a fork's username is `$yourname`,
- adjust `group` to `com.github.$yourName.fuel`
- and look for `version` on `https://jitpack.io/#$yourName/Fuel`

## Quick start

Fuel requests can be made on the `Fuel` namespace object, any `FuelManager` or using one of the `String` extension methods. If you specify a callback the call is `async`, if you don't it's `blocking`.

#### Async Usage Example

```kotlin
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

fun main(args: Array<String>) {

    val httpAsync = "https://httpbin.org/get"
        .httpGet()
        .responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    println(ex)
                }
                is Result.Success -> {
                    val data = result.get()
                    println(data)
                }
            }
        }

    httpAsync.join()
}
```
#### Blocking Usage Example
```kotlin
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result;

fun main(args: Array<String>) {

    val (request, response, result) = "https://httpbin.org/get"
        .httpGet()
        .responseString()

    when (result) {
        is Result.Failure -> {
            val ex = result.getException()
            println(ex)
        }
        is Result.Success -> {
            val data = result.get()
            println(data)
        }
    }

}

// You can also use Fuel.get("https://httpbin.org/get").responseString { ... }
// You can also use FuelManager.instance.get("...").responseString { ... }
```

`Fuel` and the extension methods use the `FuelManager.instance` under the hood. You can use this FuelManager to change the default behaviour of all requests:

```kotlin
FuelManager.instance.basePath = "https://httpbin.org"

"/get"
  .httpGet()
  .responseString { request, response, result -> /*...*/ }
// This is a GET request to "https://httpbin.org/get"
```

## Detailed usage

Check each of the packages documentations or the Wiki for more features, usages and examples. Are you looking for basic usage on how to set headers, authentication, request bodies and more? [`fuel`: Basic usage](./fuel/README.md) is all you need.

### Basic functionality
- [`fuel`: Basic usage](./fuel/README.md)
- [`fuel-android`: Android usage](./fuel-android/README.md)
- [`fuel-coroutines`: Execution with coroutines](./fuel-coroutines/README.md)

### Responses
- [`fuel-livedata`: Responses as LiveData](./fuel-livedata/README.md)
- [`fuel-reactor`: Responses as Mono](./fuel-reactor/README.md)
- [`fuel-rxjava`: Responses as Single](./fuel-rxjava/README.md)

### (De)serialization
- [`fuel-forge`: Deserialization with Forge](./fuel-forge/README.md)
- [`fuel-gson`: (De)serialization with Gson](./fuel-gson/README.md)
- [`fuel-jackson`: Deserialization with Jackson](./fuel-jackson/README.md)
- [`fuel-json`: Deserialization with Json](./fuel-json/README.md)
- [`fuel-kotlinx-serialization`: (De)serialization with KotlinX Serialization](/fuel-kotlinx-serialization/README.md)
- [`fuel-moshi`: Deserialization with Moshi](./fuel-moshi/README.md)

### Utility
- [`fuel-stetho` : Debugging bridge for Android with Stetho](./fuel-stetho/README.md)


## Other libraries

If you like Fuel, you might also like other libraries of mine;
* [Result](https://github.com/kittinunf/Result) - The modelling for success/failure of operations in Kotlin
* [Fuse](https://github.com/kittinunf/Fuse) - A simple generic LRU memory/disk cache for Android written in Kotlin
* [Forge](https://github.com/kittinunf/Forge) - Functional style JSON parsing written in Kotlin
* [ReactiveAndroid](https://github.com/kittinunf/ReactiveAndroid) - Reactive events and properties with RxJava for Android SDK

## Credits

Fuel is brought to you by [contributors](https://github.com/kittinunf/Fuel/graphs/contributors).

## Licenses

Fuel is released under the [MIT](https://opensource.org/licenses/MIT) license.
