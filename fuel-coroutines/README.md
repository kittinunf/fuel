# fuel-coroutines
The coroutines extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-coroutines` with `Maven` and `Gradle`. The coroutines package has the following dependencies:
* [`Fuel`](../fuel/README.md)
* KotlinX Coroutines: 1.1.1

### Gradle

```groovy
implementation 'com.github.kittinunf.fuel:fuel:<latest-version>'
implementation 'com.github.kittinunf.fuel:fuel-coroutines:<latest-version>'
```

### Maven

```xml
<dependency>
    <groupId>com.github.kittinunf.fuel</groupId>
    <artifactId>fuel</artifactId>
    <version>[LATEST_VERSION]</version>
</dependency>

<dependency>
    <groupId>com.github.kittinunf.fuel</groupId>
    <artifactId>fuel-coroutines</artifactId>
    <version>[LATEST_VERSION]</version>
</dependency>

```

## Usage

Coroutines module provides extension functions to wrap a response inside a coroutine and handle its result. The coroutines-based API provides equivalent methods to the standard API (e.g: `responseString()` in coroutines is `awaitStringResponseResult()`).

```kotlin
runBlocking {
    val (request, response, result) = Fuel.get("https://httpbin.org/ip").awaitStringResponseResult()

    result.fold(
        { data -> println(data) /* "{"origin":"127.0.0.1"}" */ },
        { error -> println("An error of type ${error.exception} happened: ${error.message}") }
    )
}
```

There are functions to handle `Result` object directly too.

```kotlin
runBlocking {
    Fuel.get("https://httpbin.org/ip")
        .awaitStringResponseResult()
        .fold(
            { data -> println(data) /* "{"origin":"127.0.0.1"}" */ },
            { error -> println("An error of type ${error.exception} happened: ${error.message}") }
        )
}
```

It also provides useful methods to retrieve the `ByteArray`,`String` or `Object` directly. The difference with these implementations is that they throw exception instead of returning it wrapped a `FuelError` instance.

```kotlin
runBlocking {
    try {
        println(Fuel.get("https://httpbin.org/ip").awaitString()) // "{"origin":"127.0.0.1"}"
    } catch(exception: Exception) {
        println("A network request exception was thrown: ${exception.message}")
    }
}
```

Handling objects other than `String` (`awaitStringResponseResult() `) or `ByteArray` (`awaitByteArrayResponseResult()`) can be done using `awaitObject`, `awaitObjectResult` or `awaitObjectResponseResult`.

```kotlin
data class Ip(val origin: String)

object IpDeserializer : ResponseDeserializable<Ip> {
    override fun deserialize(content: String) =
        jacksonObjectMapper().readValue<Ip>(content)
}
```

```kotlin
runBlocking {
    Fuel.get("https://httpbin.org/ip")
        .awaitObjectResult(IpDeserializer)
        .fold(
            { data -> println(data.origin) /* 127.0.0.1 */ },
            { error -> println("An error of type ${error.exception} happened: ${error.message}") }
        )
}
```

```kotlin
runBlocking {
    try {
        val data = Fuel.get("https://httpbin.org/ip").awaitObject(IpDeserializer)
        println(data.origin) // 127.0.0.1
    } catch (exception: Exception) {
        when (exception){
            is HttpException -> println("A network request exception was thrown: ${exception.message}")
            is JsonMappingException -> println("A serialization/deserialization exception was thrown: ${exception.message}")
            else -> println("An exception [${exception.javaClass.simpleName}\"] was thrown")
        }
    }
}
```
