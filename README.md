# Fuel

The easiest HTTP networking library in Kotlin

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE in a fluent style interface
- [x] Download File
- [ ] Upload File
- [x] Configuration manager

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

Fuel.delete("http://httpbin.org/delete", mapOf("foo" to "foo", "bar" to "bar")).response { request, response, either ->
    //resolve to http://httpbin.org/get?foo=foo&bar=bar
}
```

* Support x-www-form-urlencoded for PUT & POST

``` Kotlin
Fuel.post("http://httpbin.org/post", mapOf("foo" to "foo", "bar" to "bar")).response { request, response, either ->
    //http body includes foo=foo&bar=bar
}

Fuel.put("http://httpbin.org/put", mapOf("foo" to "foo", "bar" to "bar")).response { request, response, either ->
    //http body includes foo=foo&bar=bar
}
```

### Download file with/without progress handler
```
```

### Authentication

* Support Basic Authentication right off the box
``` Kotlin
val username = "username"
val password = "abcd1234"

Fuel.get("http://httpbin.org/basic-auth/$user/$password").authenticate(username, password).response { request, response, either -> 
}
```

### Validation

* By default, (200..299) status code will be valid range for HTTP status code. However, it can be configurable
``` Kotlin
Fuel.get("http://httpbin.org/status/418").response { request, response, either -> 
    //either contains Error
    
}

//418 will pass validator
Fuel.get("http://httpbin.org/status/418").validate(400..499).response { request, response, either -> 
    //either contains data
}
```

## Configuration

* Use singleton ```Manager.sharedInsance``` to manager global config
