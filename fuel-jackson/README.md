# fuel-jackson

The jackson extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-jackson` with `Maven` and `Gradle`. The jackson package has the following dependencies:
* [`Fuel`](../fuel/README.md)
* Jackson: 2.9.8

```groovy
implementation 'com.github.kittinunf.fuel:fuel:<latest-version>'
implementation 'com.github.kittinunf.fuel:fuel-jackson:<latest-version>'
```

## Usage

The Fuel-Jackson module provides a built in support for Jackson deserialization.
This is done by adding the `responseObject` extension function into Fuel `Request` interface.

By default, the `responseObject` call will use the `defaultMapper` property defined in `FuelJackson.kt`.

```kotlin
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent")
    .responseObject<HttpBinUserAgentModel>()
```

Alternatively, you can provide your own `ObjectMapper` as a parameter to it.

```kotlin
data class HttpBinUserAgentModel(var userAgent: String = "")

val mapper = ObjectMapper().registerKotlinModule()
                           .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

Fuel.get("/user-agent")
    .responseObject<HttpBinUserAgentModel>(mapper)
```

Also, the `responseObject` overloads allows you to pass `Response Handlers` as lambda functions

```kotlin
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent").responseObject<HttpBinUserAgentModel> { request, response, result ->
    //handle here
}
```

or `ResponseHandler<T>` instances.

```kotlin
data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent")
    .responseObject(object : ResponseHandler<HttpBinUserAgentModel> {
           override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
              //handle success
           }
    
           override fun failure(request: Request, response: Response, error: FuelError) {
              //handle failure
           }
     })
```

Both overloads allows you to provide custom `ObjectMapper` if needed.
