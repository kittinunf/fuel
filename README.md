# Fuel

The easiest HTTP networking library in Kotlin for Android.

[ ![Download](https://api.bintray.com/packages/kittinunf/maven/Fuel/images/download.svg) ](https://bintray.com/kittinunf/maven/Fuel/_latestVersion)

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE in a fluent style interface
- [x] Download File
- [x] Upload File (multipart/form-data)
- [x] Configuration manager

## Installation

### Gradle

``` Groovy
dependencies {
    compile 'fuel:fuel:0.3'
}
```

### Sample

* There are two sample folders, one is in Kotlin and another one in Java.
* Kotlin
``` Kotlin
//simplest get
Fuel.get("http://httpbin.org/get").responseString { request, response, either ->
}
```

* Java
``` Java
//simplest get
Fuel.get("http://httpbin.org/get", params).responseString(new Handler<String>() {
    @Override
    public void failure(Request request, Response response, FuelError error) {
    }

    @Override
    public void success(Request request, Response response, String data) {
    }
});
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
### Either 

* [Either](http://www.ibm.com/developerworks/java/library/j-ft13/index.html) is a functional style data structure that represents data that contains either *left* or *right* but not both. It represents result of action that can be error or success (with result). The common functional convention is the *left* of an Either class contains an exception (if any), and the *right* contains the result.

* Work with either is easy. You could [*fold*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Either.kt#L13) it, [*muliple declare*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/test/kotlin/fuel/RequestAuthenticationTest.kt#L44) it because it is just a [data class](http://kotlinlang.org/docs/reference/data-classes.html) or do a simple ```when``` checking whether it is *left* or *right*.

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

### Download with or without progress handler
``` Kotlin
Fuel.download("http://httpbin.org/bytes/32768").destination { response, url ->
    File.createTempFile("temp", ".tmp");
}.response { req, res, either -> {

}

Fuel.download("http://httpbin.org/bytes/32768").destination { response, url ->
    File.createTempFile("temp", ".tmp");
}.progress { readBytes, totalBytes ->
    val progress = readBytes.toFloat() / totalBytes.toFloat()
}.response { req, res, either -> {

}
```

### Upload with or without progress handler
``` Kotlin
Fuel.upload("/post").source { request, url ->
    File.createTempFile("temp", ".tmp");
}.responseString { request, response, either ->

}
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

## Advanced Configuration

* Use singleton ```Manager.sharedInstance``` to manager global configuration.
* ```basePath``` is to manage common root path. Great usage is for your static API endpoint.

``` Kotlin
Manager.sharedInstance.basePath = "https://httpbin.org
```

``` Kotlin
Fuel.get("/get").response { request, response, either ->
    //make request to https://httpbin.org/get because Fuel.{get|post|put|delete} use Manager.sharedInstance to make HTTP request
}
```

* ```additionalHeaders``` is to manage common HTTP header pairs in format of ``` mapOf<String, String>```.

``` Kotlin
Manager.sharedInstance.additionalHeaders = mapOf("Device" to "Android")
```

``` Kotlin
Fuel.get("/get").response { request, response, either ->
    //make request to https://httpbin.org/get with global device header (Device : Android)
}
```

* ```client``` is a raw HTTP client driver. Generally, it is responsible to make [```Request```](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Request.kt) into [```Response```](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Response.kt). Default is [```HttpClient```](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [```java.net.HttpUrlConnnection```](http://developer.android.com/reference/java/net/HttpURLConnection.html). You could use any httpClient of your choice by conforming to [```client```](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Client.kt) protocol, and set back to ```Manager.sharedInstance``` to kick off the effect.

## Credits

Fuel is brought to you by [contributors](https://github.com/kittinunf/Fuel/wiki/Contributors).

## License

Fuel is released under the [MIT](http://opensource.org/licenses/MIT) license.
>The MIT License (MIT)

>Copyright (c) 2015 by Fuel contributors

>Permission is hereby granted, free of charge, to any person obtaining a copy
>of this software and associated documentation files (the "Software"), to deal
>in the Software without restriction, including without limitation the rights
>to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
>copies of the Software, and to permit persons to whom the Software is
>furnished to do so, subject to the following conditions:

>The above copyright notice and this permission notice shall be included in
>all copies or substantial portions of the Software.

>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
>IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
>FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
>AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
>LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
>OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
>THE SOFTWARE.
