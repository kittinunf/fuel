# fuel-json
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.20-blue.svg)](https://kotlinlang.org)

The Json extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-json` with `Maven` and `Gradle`. The json package has the following dependencies:
* `fuel:fuel:<same-version>`
* Kotlin: 1.3.20
* Json: 20170516

```groovy
compile 'com.github.kittinunf.fuel:fuel:<latest-version>'
compile 'com.github.kittinunf.fuel:fuel-json:<latest-version>'
```
## Usage

#### Response in Json
```kotlin
fun responseJson(handler: (Request, Response, Result<Json, FuelError>) -> Unit)

val jsonObject = json.obj() //JSONObject
val jsonArray = json.array() //JSONArray
```

See `FuelJson.kt`
