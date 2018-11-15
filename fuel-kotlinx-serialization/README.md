### Deserialization using kotlinx.serialzationn

_requires the [kotlinx-serialization extension](#dependency---fuel-kotlinx-serialization)_
_requires [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization#gradlejvm)_


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
