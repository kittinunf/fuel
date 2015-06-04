# Fuel

The easiest HTTP networking library in Kotlin

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE in a fluent style interface
- [x] Download File
- [ ] Upload File

## Installation

### Gradle

```

```

## Usage

### GET

``` Kotlin
Fuel.get("http://httpbin.org/get").response { request, response, either ->
    println(request)
    println(response)
    val (error, bytes) = either
    if (bytes != null) {
        println(bytes)
    }
}
```

### Response Handling

### Response
``` response(handler: (Request, Response, Either<FuelError, ByteArray>) -> Unit) ```

### Response in String
``` responseString(handler: (Request, Response, Either<FuelError, String>) -> Unit) ```

### POST

``` Kotlin
Fuel.post("http://httpbin.org/post").response { request, response, either ->
    
}
```

### PUT

``` Kotlin
Fuel.put("http://httpbin.org/put").response { request, response, either ->

}
```

### DELETE

``` Kotlin
Fuel.delete("http://httpbin.org/delete").response { request, response, either ->

}
```

### Parameter Support

* URL encoded style for GET & DELETE request

``` Kotlin
Fuel.get("http://httpbin.org/get", mapOf("foo" to "foo", "bar" to "bar")).response { request, response, either -> {
    //resolve to http://httpbin.org/get?foo=foo&bar=bar
}

Fuel.delete("http://httpbin.org/delete", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
    //resolve to http://httpbin.org/get?foo=foo&bar=bar
}
```

* Support x-www-form-urlencoded for PUT & POST

``` Kotlin
Fuel.post("/post", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
    //http body includes foo=foo&bar=bar
}

Fuel.put("/put", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
    //http body includes foo=foo&bar=bar
}
```



