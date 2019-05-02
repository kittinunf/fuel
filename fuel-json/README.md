# fuel-json

The Json extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-json` with `Maven` and `Gradle`. The json package has the following dependencies:
* [`Fuel`](../fuel/README.md)
* Json: 20170516

```groovy
implementation 'com.github.kittinunf.fuel:fuel:<latest-version>'
implementation 'com.github.kittinunf.fuel:fuel-json:<latest-version>'
```
## Usage

```kotlin
fun responseJson(handler: (Request, Response, Result<Json, FuelError>) -> Unit)

val jsonObject = json.obj() //JSONObject
val jsonArray = json.array() //JSONArray
```

See `FuelJson.kt`
