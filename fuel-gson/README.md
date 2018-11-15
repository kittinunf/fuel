

### Gson Deserialization

* Fuel also provides a built in support for Gson Deserialization. This is possible by including the [Gson](https://github.com/kittinunf/Fuel/tree/master/fuel-gson) module in your dependency block.

```kotlin
data class HttpBinUserAgentModel(var userAgent: String = "")
Fuel.get("/user-agent")
    .responseObject<HttpBinUserAgentModel> { _, _, result -> }
```
