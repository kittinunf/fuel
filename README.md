# Fuel

[ ![Kotlin](https://img.shields.io/badge/Kotlin-1.0.0--beta--4583-blue.svg)](http://kotlinlang.org) [ ![jcenter](https://api.bintray.com/packages/kittinunf/maven/Fuel-Android/images/download.svg) ](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) [![Build Status](https://travis-ci.org/kittinunf/Fuel.svg?branch=master)](https://travis-ci.org/kittinunf/Fuel)

The easiest HTTP networking library for Kotlin/Android.

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE in a fluent style interface
- [X] Support both asynchronous and blocking requests
- [x] Download file
- [x] Upload file (multipart/form-data)
- [x] Cancel in-flight request
- [x] Configuration manager
- [x] Debug log / cUrl log
- [x] Support response deserialization into plain old object (both Kotlin & Java)
- [x] Automatically invoke handler on Android Main Thread

## Installation

### Gradle

``` Groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.github.kittinunf.fuel:fuel:0.67' //for JVM
    compile 'com.github.kittinunf.fuel:fuel-android:0.67' //for Android
}
```

### Sample

* There are two samples, one is in Kotlin and another one in Java.

### Quick Glance Usage

#### Async mode

* Kotlin
``` Kotlin
//an extension over string (support GET, PUT, POST, DELETE with httpGet(), httpPut(), httpPost(), httpDelete())
"http://httpbin.org/get".httpGet().responseString { request, response, result ->
	//do something with response
	when (result) {
        is Result.Failure -> {
            error = result.getAs()
        }
        is Result.Success -> {
            data = result.getAs()
        }
    }
}

//if we set baseURL beforehand, simply use relativePath
Manager.instance.basePath = "http://httpbin.org"
"/get".httpGet().responseString { request, response, result ->    
    //make a GET to http://httpbin.org/get and do something with response
    val (data, error) = result
    if (error != null) {
        //do something when success
    } else {
        //error handling
    }
}

//if you prefer this a little longer way, you can always do
//get
Fuel.get("http://httpbin.org/get").responseString { request, response, result ->
	//do something with response
	result.fold({ d ->
	    //do something with data
	}, { err ->
	    //do something with error
	})
}

```

* Java
``` Java
//get
Fuel.get("http://httpbin.org/get", params).responseString(new Handler<String>() {
    @Override
    public void failure(Request request, Response response, FuelError error) {
    	//do something when it is failure
    }

    @Override
    public void success(Request request, Response response, String data) {
    	//do something when it is successful
    }
});
```

#### Blocking mode
You can also wait for the response. It returns the same parameters as the async version, but it blocks the thread. It supports all the features of the async version.

**Note:** Instead of returning `Result` object, it will throw the exception.

* Kotlin
``` Kotlin
val (request, response, data) = "http://httpbin.org/get".httpGet().responseString()
```

* Java
``` Java
try {
    Triple<Request, Response, String> data = Fuel.get("http://www.google.com").responseString();
    Request request = data.getFirst();
    Response response = data.getSecond();
    String text = data.getThird();
} catch (Exception networkError) {

}

```

## Detail Usage

### GET

``` Kotlin
Fuel.get("http://httpbin.org/get").response { request, response, result ->
    println(request)
    println(response)
    val (bytes, error) = result
    if (bytes != null) {
        println(bytes)
    }
}
```

### Response Handling
### Result

* [Result](https://github.com/kittinunf/Result) is a functional style data structure that represents data that contains result of *Success* or *Failure* but not both. It represents the result of an action that can be success (with result) or error.

* Working with result is easy. You could [*fold*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/test/kotlin/com/github/kittinunf/fuel/RequestTest.kt#L314), [*muliple declare*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/test/kotlin/com/github/kittinunf/fuel/RequestAuthenticationTest.kt#L47) as because it is just a [data class](http://kotlinlang.org/docs/reference/data-classes.html) or do a simple ```when``` checking whether it is *Success* or *Failure*.

### Response
``` Kotlin
fun response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit)
```

### Response in String
``` Kotlin
fun responseString(handler: (Request, Response, Result<String, FuelError>) -> Unit)
```

### Response in [JSONObject](http://www.json.org/javadoc/org/json/JSONObject.html)
``` Kotlin
fun responseJson(handler: (Request, Response, Result<JSONObject, FuelError>) -> Unit)
```

### Response in T (object)
``` Kotlin
fun <T> responseObject(deserializer: ResponseDeserializable<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit)
```

### POST

``` Kotlin
Fuel.post("http://httpbin.org/post").response { request, response, result ->
}

//if you have body to post it manually
Fuel.post("http://httpbin.org/post").body("{ \"foo\" : \"bar\" }").response { request, response, result ->
}
```

### PUT

``` Kotlin
Fuel.put("http://httpbin.org/put").response { request, response, result ->
}
```

### DELETE

``` Kotlin
Fuel.delete("http://httpbin.org/delete").response { request, response, result ->
}
```

### Debug Logging
* Use `toString()` method to Log (request|response)

``` Kotlin
Log.d("log", request.toString())
```

```
//print and header detail
//request
--> GET (http://httpbin.org/get?key=value)
    Body : (empty)
    Headers : (2)
    Accept-Encoding : compress;q=0.5, gzip;q=1.0
    Device : Android

//response
<-- 200 (http://httpbin.org/get?key=value)
```

* Also support cUrl string to Log request, make it very easy to cUrl on command line

``` Kotlin
Log.d("cUrl log", request.cUrlString())
```

``` Bash
//print
curl -i -X POST -d "foo=foo&bar=bar&key=value" -H "Accept-Encoding:compress;q=0.5, gzip;q=1.0" -H "Device:Android" -H "Content-Type:application/x-www-form-urlencoded" "http://httpbin.org/post"
```

### Parameter Support

* URL encoded style for GET & DELETE request

``` Kotlin
Fuel.get("http://httpbin.org/get", listOf("foo" to "foo", "bar" to "bar")).response { request, response, result -> {
    //resolve to http://httpbin.org/get?foo=foo&bar=bar
}

Fuel.delete("http://httpbin.org/delete", listOf("foo" to "foo", "bar" to "bar")).response { request, response, result ->
    //resolve to http://httpbin.org/get?foo=foo&bar=bar
}
```

* Support x-www-form-urlencoded for PUT & POST

``` Kotlin
Fuel.post("http://httpbin.org/post", listOf("foo" to "foo", "bar" to "bar")).response { request, response, result ->
    //http body includes foo=foo&bar=bar
}

Fuel.put("http://httpbin.org/put", listOf("foo" to "foo", "bar" to "bar")).response { request, response, result ->
    //http body includes foo=foo&bar=bar
}
```

### Download with or without progress handler
``` Kotlin
Fuel.download("http://httpbin.org/bytes/32768").destination { response, url ->
    File.createTempFile("temp", ".tmp")
}.response { req, res, result -> {

}

Fuel.download("http://httpbin.org/bytes/32768").destination { response, url ->
    File.createTempFile("temp", ".tmp")
}.progress { readBytes, totalBytes ->
    val progress = readBytes.toFloat() / totalBytes.toFloat()
}.response { req, res, result -> {

}
```

### Upload with or without progress handler
``` Kotlin
Fuel.upload("/post").source { request, url ->
    File.createTempFile("temp", ".tmp");
}.responseString { request, response, result ->

}

//by default upload use Method.POST, unless it is specified as something else
Fuel.upload("/put", Method.PUT).source { request, url ->
    File.createTempFile("temp", ".tmp");
}.responseString { request, response, result ->
    // calls to http://example.com/api/put with PUT

}
```

### Authentication

* Support Basic Authentication right off the box
``` Kotlin
val username = "username"
val password = "abcd1234"

Fuel.get("http://httpbin.org/basic-auth/$user/$password").authenticate(username, password).response { request, response, result ->
}
```

### Validation

* By default, the valid range for HTTP status code will be (200..299). However, it can be configurable
``` Kotlin
Fuel.get("http://httpbin.org/status/418").response { request, response, result ->
    //result contains Error

}

//418 will pass the validator
Fuel.get("http://httpbin.org/status/418").validate(400..499).response { request, response, result ->
    //result contains data
}
```

### Cancel

* If one wants to cancel on-going request, one could call `cancel` on the request object
``` Kotlin
val request = Fuel.get("http://httpbin.org/get").response { request, response, result ->
    // if request is cancelled successfully, response callback will not be called. Interrupt callback (if provided) will be called instead
}

//later
request.cancel() //this will cancel on-going request
```

* Also, interrupt request can be further processed with interrupt callback
``` Kotlin
val request = Fuel.get("http://httpbin.org/get").interrupt { request ->
    println("${request.url} was interrupted and cancelled")
}.response { request, response, result ->
    // if request is cancelled successfully, response callback will not be called. Interrupt callback (if provided) will be called instead
}

request.cancel()
```

### Synchronous Call 
* Fuel supports synchronous call by calling `sync` before `response`. 
``` Kotlin
var data: String? = null
//the call will block until the http call finished
Fuel.get("http://httpbin.org/get").sync().responseString { req, res, result ->
    val (d, e) = result
    data = d
}

//do something with data
```

## Advanced Configuration

### Response Deserialization

* Fuel provides built-in support for response deserialization. Here is how one might want to use Fuel together with [Gson](https://github.com/google/gson)

``` Kotlin

//User Model
data class User(val firstName: String = "",
                val lastName: String = "") {

    //User Deserializer
    class Deserializer : ResponseDeserializable<User> {
        override fun deserialize(content: String) = Gson().fromJson(content, User::class.java)
    }

}

//Use httpGet extension
"http://www.example.com/user/1".httpGet().responseObject(User.Deserializer()) { req, res, result
    //result is of type Result<User, Exception>
    val (user, err) = result

    println(user.firstName)
    println(user.lastName)
}

```

* There are 4 methods to support response deserialization depending on your needs (also depending on JSON parsing library of your choice), and you are required to implement only one of them.

``` Kotlin
public fun deserialize(bytes: ByteArray): T?

public fun deserialize(inputStream: InputStream): T?

public fun deserialize(reader: Reader): T?

public fun deserialize(content: String): T?
```

* Another example may be parsing a website that is not UTF-8. By default, Fuel serializes text as UTF-8, we need to define our deserializer as such

```kotlin
object Windows1255StringDeserializer : ResponseDeserializable<String> {
        override fun deserialize(bytes: ByteArray): String {
            return String(bytes, "windows-1255")
        }
    }
```

### Configuration

* Use singleton `Manager.instance` to manager global configuration.

* `basePath` is used to manage common root path. Great usage is for your static API endpoint.

``` Kotlin
Manager.instance.basePath = "https://httpbin.org"
```

``` Kotlin
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get because Fuel.{get|post|put|delete} use Manager.instance to make HTTP request
}
```

* `baseHeaders` is to manage common HTTP header pairs in format of `Map<String, String>>`.

``` Kotlin
Manager.instance.baseHeaders = mapOf("Device" to "Android")
```

``` Kotlin
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get with global device header (Device : Android)
}
```

* `baseParams` is used to manage common `key=value` query param, which will be automatically included in all of your subsequent requests in format of ` List<Pair<String, Any?>>` (`Any` is converted to `String` by `toString()` method)

``` Kotlin
Manager.instance.baseParams = listOf("api_key" to "1234567890")
```

``` Kotlin
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get?api_key=1234567890
}
```

* `client` is a raw HTTP client driver. Generally, it is responsible to make [`Request`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Request.kt) into [`Response`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Response.kt). Default is [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [`java.net.HttpUrlConnnection`](http://developer.android.com/reference/java/net/HttpURLConnection.html). You could use any httpClient of your choice by conforming to [`client`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/core/Client.kt) protocol, and set back to `Manager.instance` to kick off the effect.

* `keyStore` is configurable by user. By default it is `null`.

* `socketFactory` can be supplied by user. If `keyStore` is not null, `socketFactory` will be derived from it.

* `hostnameVerifier` is configurable by user. By default, it is just ignore it by returning `true` to all hostnames. If this is not what you want, please consider provide it.

## Other libraries
If you like Fuel, you might also like other libraries;
* [Result](https://github.com/kittinunf/Result) - The modelling for success/failure of operations in Kotlin
* [Kovenant](https://github.com/mplatvoet/kovenant) - Kovenant. Promises for Kotlin.

## Credits

Fuel is brought to you by [contributors](https://github.com/kittinunf/Fuel/graphs/contributors).

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
