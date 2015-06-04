# Fuel
A simplest HTTP networking library in Kotlin

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE in a fluent interface fashion
- [x] Download File
- [ ] Upload File

## Installation

### Gradle


## Usage

### Get

``` Kotlin
Fuel.get("http://httpbin.org/get").response { request, response, either ->
    println(request)
    println(response)
    val (exception, bytes) = either
    if (bytes != null) {
        println(bytes)
    }
}
```

### Post

``` Kotlin
Fuel.post("http://httpbin.org/post", mapOf("foo" to "bar", "foo2" to "bar2")).responseString { request, response, either ->
    either.fold({ error ->
        //do something with error
    }, { responseStr ->
        //do something with response string
    })
}
```
