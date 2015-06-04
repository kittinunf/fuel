# Fuel
A simplest HTTP networking library in Kotlin

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE in a fluent style interface
- [x] Download File
- [ ] Upload File

## Installation

### Gradle



## Usage

### Get Request

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
``` fun response(handler: (Request, Response, Either<FuelError, ByteArray>) -> Unit) ```

### Response in String
``` fun responseString(handler: (Request, Response, Either<FuelError, String>) -> Unit) ```

### Post Request

``` Kotlin
Fuel.post("http://httpbin.org/post", mapOf("foo" to "bar", "foo2" to "bar2")).response { request, response, either ->
    
}
```

### Put Request

``` Kotlin
Fuel.put("/put", mapOf("foo" to "foo", "bar" to "bar")).response { request, response, either ->

}
```

### Delete Request

``` Kotlin
Fuel.delete("/delete", mapOf("foo" to "foo", "bar" to "bar")).response { request, response, either ->

}
```




