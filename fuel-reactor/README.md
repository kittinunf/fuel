# fuel-reactor
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.0-blue.svg)](https://kotlinlang.org)

The reactor extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-reactor` with `Maven` and `Gradle`. The reactor package has the following dependencies:
* `fuel:fuel:<same-version>`
* Kotlin: 1.3.0
* Project Reactor: 3.2.0.M4

```groovy
compile 'com.github.kittinunf.fuel:fuel:<latest-version>'
compile 'com.github.kittinunf.fuel:fuel-reactor:<latest-version>'
```

## Usage

See `FuelReactor.kt`

### Responses

The Reactor module API provides functions starting with the prefix `mono` to handle instances of `Response`, `Result<T, FuelError>` and values directly (`String`, `ByteArray`, `Any`). All functions expose exceptions as `FuelError` instance.

**Data handling example**

```kotlin
Fuel.get("https://icanhazdadjoke.com")
    .header(Headers.ACCEPT to "text/plain")
    .monoString()
    .subscribe(::println)
```

**Error handling example**

```kotlin
data class Guest(val name: String)

object GuestMapper : ResponseDeserializable<Guest> {
    override fun deserialize(content: String) =
        jacksonObjectMapper().readValue<Guest>(content)
}

Fuel.get("/guestName").monoResultObject(GuestMapper)
    .map(Result<Guest, FuelError>::get)
    .map { (name) -> "Welcome to the party, $name!" }
    .onErrorReturn("I'm sorry, your name is not on the list.")
    .subscribe(::println)
```

**Response handling example**

```kotlin
FuelManager.instance.basePath = "https://httpbin.org"

Fuel.get("/status/404").monoResponse()
    .filter(Response::isSuccessful)
    .switchIfEmpty(Fuel.get("/status/200").monoResponse())
    .map(Response::statusCode)
    .subscribe(::println)
```
