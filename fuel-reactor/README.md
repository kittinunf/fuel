
### Project Reactor

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
