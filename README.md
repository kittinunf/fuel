# Fuel

[ ![Kotlin](https://img.shields.io/badge/Kotlin-1.2.50-blue.svg)](http://kotlinlang.org) [ ![jcenter](https://api.bintray.com/packages/kittinunf/maven/Fuel-Android/images/download.svg) ](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) [![Build Status](https://travis-ci.org/kittinunf/Fuel.svg?branch=master)](https://travis-ci.org/kittinunf/Fuel)
[![Codecov](https://codecov.io/github/kittinunf/Fuel/coverage.svg?branch=master)](https://codecov.io/gh/kittinunf/Fuel)

The easiest HTTP networking library for Kotlin/Android.

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE/HEAD/PATCH in a fluent style interface
- [x] Support both asynchronous and blocking requests
- [x] Download file
- [x] Upload file (multipart/form-data)
- [x] Cancel in-flight request
- [x] Request timeout
- [x] Configuration manager by using `FuelManager`
- [x] Debug log / cUrl log
- [x] Support response deserialization into plain old object (both Kotlin & Java)
- [x] Automatically invoke handler on Android Main Thread when using Android Module
- [x] Special test mode for easier testing
- [x] RxJava 2.x support out of the box
- [x] Google Components [LiveData](https://developer.android.com/topic/libraries/architecture/livedata.html) support
- [x] Built-in object serialization module (Gson, Jackson, Moshi, Forge) :sparkles:
- [x] Support Kotlin's [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) module
- [x] API Routing

## Installation

### Dependency - fuel

* [Result](https://github.com/kittinunf/Result) - The modelling for success/failure of operations in Kotlin

### Dependency - fuel-android

* [Android SDK](https://developer.android.com/studio/index.html) - Android SDK

### Dependency - fuel-livedata

* [Live Data](https://developer.android.com/topic/libraries/architecture/livedata.html) - Android Architecture Components - LiveData

### Dependency - fuel-rxjava

* [RxJava](https://github.com/ReactiveX/RxJava) - RxJava – Reactive Extensions for the JVM

### Dependency - fuel-coroutines

* [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) - Kotlin Coroutines - Library support for Kotlin coroutines

### Dependency - fuel-gson

* [Gson](https://github.com/google/gson) - Gson - A Java serialization/deserialization library to convert Java Objects into JSON and back

### Dependency - fuel-jackson

* [Jackson](https://github.com/FasterXML/jackson-module-kotlin) - Jackson - The JSON library for Java

### Dependency - fuel-moshi

* [Moshi](https://github.com/square/moshi) - Moshi - A modern JSON library for Android and Java

### Dependency - fuel-forge

* [Forge](https://github.com/kittinunf/Forge/) - Forge - Functional style JSON parsing written in Kotlin

### Gradle

``` Groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.github.kittinunf.fuel:fuel:<latest-version>' //for JVM
    compile 'com.github.kittinunf.fuel:fuel-android:<latest-version>' //for Android
    compile 'com.github.kittinunf.fuel:fuel-livedata:<latest-version>' //for LiveData support
    compile 'com.github.kittinunf.fuel:fuel-rxjava:<latest-version>' //for RxJava support
    compile 'com.github.kittinunf.fuel:fuel-coroutines:<latest-version>' //for Kotlin Coroutines support
    compile 'com.github.kittinunf.fuel:fuel-gson:<latest-version>' //for Gson support
    compile 'com.github.kittinunf.fuel:fuel-jackson:<latest-version>' //for Jackson support
    compile 'com.github.kittinunf.fuel:fuel-moshi:<latest-version>' //for Moshi support
    compile 'com.github.kittinunf.fuel:fuel-forge:<latest-version>' //for Forge support
}
```

### Sample

* There are two samples, one is in Kotlin and another one in Java.

## Quick Glance Usage

#### Async mode

* Kotlin
``` Kotlin
//an extension over string (support GET, PUT, POST, DELETE with httpGet(), httpPut(), httpPost(), httpDelete())
"http://httpbin.org/get".httpGet().responseString { request, response, result ->
  //do something with response
  when (result) {
    is Result.Failure -> {
      val ex = result.getException()
    }
    is Result.Success -> {
      val data = result.get()
    }
  }
}

//if we set baseURL beforehand, simply use relativePath
FuelManager.instance.basePath = "http://httpbin.org"
"/get".httpGet().responseString { request, response, result ->
    //make a GET to http://httpbin.org/get and do something with response
    val (data, error) = result
    if (error == null) {
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

* Kotlin
``` Kotlin
val (request, response, result) = "http://httpbin.org/get".httpGet().responseString() // result is Result<String, FuelError>
```

* Java
``` Java
try {
    Triple<Request, Response, String> data = Fuel.get("http://www.google.com").responseString();
    Request request = data.getFirst();
    Response response = data.getSecond();
    Result<String,FuelError> text = data.getThird();
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

* Working with result is easy. You could [*fold*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/test/kotlin/com/github/kittinunf/fuel/RequestTest.kt#L324), [*destructure*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/test/kotlin/com/github/kittinunf/fuel/RequestTest.kt#L266) as because it is just a [data class](http://kotlinlang.org/docs/reference/data-classes.html) or do a simple ```when``` checking whether it is *Success* or *Failure*.

### Response
``` Kotlin
fun response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit)
```

### Response in String
``` Kotlin
fun responseString(handler: (Request, Response, Result<String, FuelError>) -> Unit)
```

### Response in Json
_requires the [android extension](#dependency---fuel-android)_
``` Kotlin
fun responseJson(handler: (Request, Response, Result<Json, FuelError>) -> Unit)

val jsonObject = json.obj() //JSONObject
val jsonArray = json.array() //JSONArray
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

### HEAD

``` Kotlin
Fuel.head("http://httpbin.org/get").response { request, response, result ->
   // request body should be empty.
}
```

### PATCH
* The default `client` is [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [`java.net.HttpUrlConnnection`](http://developer.android.com/reference/java/net/HttpURLConnection.html). [`java.net.HttpUrlConnnection`](http://developer.android.com/reference/java/net/HttpURLConnection.html) does not support a [`PATCH`](http://download.java.net/jdk7/archive/b123/docs/api/java/net/HttpURLConnection.html#setRequestMethod(java.lang.String)) method. [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/toolbox/HttpClient.kt) converts `PATCH` requests to a `POST` request and adds a `X-HTTP-Method-Override: PATCH` header. While this is a semi-standard industry practice not all APIs are configured to accept this header by default.

``` Kotlin
Fuel.patch("http://httpbin.org/patch").response { request, response, result ->
   // request body should be empty.
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

### Set request's timeout and read timeout
Default timeout for a request is 15000 milliseconds.
Default read timeout for a request is 15000 milliseconds.

* Kotlin
```kotlin
val timeout = 5000 // 5000 milliseconds = 5 seconds.
val timeoutRead = 60000 // 60000 milliseconds = 1 minute.

"http://httpbin.org/get".httpGet().timeout(timeout).timeoutRead(timeoutRead).responseString { request, response, result -> }
```

* Java
``` Java
int timeout = 5000 // 5000 milliseconds = 5 seconds.
int timeoutRead = 60000 // 60000 milliseconds = 1 minute.
Fuel.get("http://httpbin.org/get", params).timeout(timeout).timeoutRead(timeoutRead).responseString(new Handler<String>() {
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

### Download with or without progress handler
``` Kotlin
Fuel.download("http://httpbin.org/bytes/32768").destination { response, url ->
    File.createTempFile("temp", ".tmp")
}.response { req, res, result ->

}

Fuel.download("http://httpbin.org/bytes/32768").destination { response, url ->
    File.createTempFile("temp", ".tmp")
}.progress { readBytes, totalBytes ->
    val progress = readBytes.toFloat() / totalBytes.toFloat()
}.response { req, res, result ->

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

//upload with multiple files
Fuel.upload("/post").sources { request, url ->
    listOf(
        File.createTempFile("temp1", ".tmp"),
        File.createTempFile("temp2", ".tmp")
    )
}.name {
    "temp"
}.responseString { request, response, result ->

}
```
### Specify custom field names for files
```Kotlin
Fuel.upload("/post").dataParts { request, url -> 
    listOf( 
        //DataPart takes a file, and you can specify the name and/or type
	DataPart(File.createTempFile("temp1", ".tmp"), "image/jpeg"), 
	DataPart(File.createTempFile("temp2", ".tmp"), "file2"), 
	DataPart(File.createTempFile("temp3", ".tmp"), "third-file", "image/jpeg") 
    ) 
}.responseString { request, response, result ->
    ... 
}
```
### Upload a multipart form without a file

``` Kotlin
val formData = listOf("Email" to "mail@example.com", "Name" to "Joe Smith" )
Fuel.upload("/post", param = formData)
    //Upload normally requires a file, but we can give it an empty list of `DataPart`
    .dataParts { request, url -> listOf<DataPart>() } 
    .responseString { request, response, result ->
        ...
    }
```
	
### Upload from an `InputStream`

``` Kotlin
Fuel.upload("/post").blob { request, url ->
    Blob("filename.png", someObject.length, { someObject.getInputStream() })
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

* By default, the valid range for HTTP status code will be (200..299).

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
"http://www.example.com/user/1".httpGet().responseObject(User.Deserializer()) { req, res, result ->
    //result is of type Result<User, Exception>
    val (user, err) = result

    println(user.firstName)
    println(user.lastName)
}

```

### Gson Deserialization

* Fuel also provides a built in support for Gson Deserialization. This is possible by including the [Gson](https://github.com/kittinunf/Fuel/tree/master/fuel-gson) module in your dependency block.

``` Kotlin

data class HttpBinUserAgentModel(var userAgent: String = "")

Fuel.get("/user-agent").responseObject<HttpBinUserAgentModel> { _, _, result ->
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

* Use singleton `FuelManager.instance` to manage global configurations.

* `basePath` is used to manage common root path. Great usage is for your static API endpoint.

``` Kotlin
FuelManager.instance.basePath = "https://httpbin.org"
```

``` Kotlin
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get because Fuel.{get|post|put|delete} use FuelManager.instance to make HTTP request
}
```

* `baseHeaders` is to manage common HTTP header pairs in format of `Map<String, String>>`.

``` Kotlin
FuelManager.instance.baseHeaders = mapOf("Device" to "Android")
```

``` Kotlin
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get with global device header (Device : Android)
}
```

* `baseParams` is used to manage common `key=value` query param, which will be automatically included in all of your subsequent requests in format of ` List<Pair<String, Any?>>` (`Any` is converted to `String` by `toString()` method)

``` Kotlin
FuelManager.instance.baseParams = listOf("api_key" to "1234567890")
```

``` Kotlin
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get?api_key=1234567890
}
```

* `client` is a raw HTTP client driver. Generally, it is responsible to make [`Request`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Request.kt) into [`Response`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Response.kt). Default is [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [`java.net.HttpUrlConnnection`](http://developer.android.com/reference/java/net/HttpURLConnection.html). You could use any httpClient of your choice by conforming to [`client`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/core/Client.kt) protocol, and set back to `FuelManager.instance` to kick off the effect.

* `keyStore` is configurable by user. By default it is `null`.

* `socketFactory` can be supplied by user. If `keyStore` is not null, `socketFactory` will be derived from it.

* `hostnameVerifier` is configurable by user. By default, it uses `HttpsURLConnection.getDefaultHostnameVerifier()`.

* `requestInterceptors` `responseInterceptors` is a side-effect to add to `Request` and/or `Response` objects. For example, one might wanna print cUrlString style for every request that hits server in DEBUG mode.

``` Kotlin
val manager = FuelManager()
if (BUILD_DEBUG) {
    manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())
}
val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").response() //it will print curl -i -H "Accept-Encoding:compress;q=0.5, gzip;q=1.0" "https://httpbin.org/get"
```

* Another example is that you might wanna add data into your Database, you can achieve that with providing `responseInterceptors` such as

``` Kotlin
inline fun <reified T> DbResponseInterceptor() =
        { next: (Request, Response) -> Response ->
            { req: Request, res: Response ->
                val db = DB.getInstance()
                val instance = Parser.getInstance().parse(res.data, T::class)
                db.transaction {
                    it.copyToDB(instance)
                }
                next(req, res)
            }
        }

manager.addResponseInterceptor(DBResponseInterceptor<Dog>)
manager.request(Method.GET, "http://www.example.com/api/dog/1").response() // Db interceptor will be called to intercept data and save into Database of your choice
```

### Test mode

Testing asynchronized calls can be somehow hard without special care. That's why Fuel has a special test mode with make all the requests blocking, for tests.

``` Kotlin
Fuel.testMode {
    timeout = 15000 // Optional feature, set all requests' timeout to this value.
}
```

In order to disable test mode, just call `Fuel.regularMode()`

### RxJava Support

* Fuel supports [RxJava](https://github.com/ReactiveX/RxJava) right off the box.

``` Kotlin
"http://www.example.com/photos/1".httpGet().rx_object(Photo.Deserializer()).subscribe {
	//do something
}
```

* There are 6 extensions over `Request` that provide RxJava 2.x `Single<Result<T, FuelError>>` as return type.

``` Kotlin
fun Request.rx_response(): Single<Pair<Response, Result<ByteArray, FuelError>>>
fun Request.rx_responseString(charset: Charset): Single<Pair<Response, Result<String, FuelError>>>
fun <T : Any> Request.rx_responseObject(deserializable: Deserializable<T>): Single<Pair<Response, Result<T, FuelError>>>

fun Request.rx_data(): Single<Result<ByteArray, FuelError>>
fun Request.rx_string(charset: Charset): Single<Result<String, FuelError>>
fun <T : Any> Request.rx_object(deserializable: Deserializable<T>): Single<Result<T, FuelError>>
```

### LiveData Support

* Fuel supports [LiveData](https://developer.android.com/topic/libraries/architecture/livedata.html)

``` Kotlin
Fuel.get("www.example.com/get").liveDataResponse().observe(this) {
  //do something
}
```

### Routing Support

In order to organize better your network stack FuelRouting interface allows you to easily setup a Router design pattern.

```Kotlin
sealed class WeatherApi: FuelRouting {

    override val basePath = "https://www.metaweather.com"

    class weatherFor(val location: String): WeatherApi() {}

    override val method: Method
        get() {
            when(this) {
                is weatherFor -> return Method.GET
            }
        }

    override val path: String
        get() {
            return when(this) {
                is weatherFor -> "/api/location/search/"
            }
        }

    override val params: List<Pair<String, Any?>>?
        get() {
            return when(this) {
                is weatherFor -> listOf("query" to this.location)
            }
        }

    override val headers: Map<String, String>?
        get() {
            return null
        }

}


// Usage
Fuel.request(WeatherApi.weatherFor("london")).responseJson { request, response, result ->
            result.fold(success = { json ->
                Log.d("qdp success", json.array().toString())
            }, failure = { error ->
                Log.e("qdp error", error.toString())
            })
        }
```

### Coroutines Support

Coroutines module provides extension functions to wrap a response inside a coroutine and handle its result. The coroutines-based API provides equivalent methods to the standard API (e.g: `responseString()` in coroutines is `awaitStringResponse()`).

```kotlin
runBlocking {
    val (request, response, result) = Fuel.get("https://httpbin.org/ip").awaitStringResponse()

    result.fold({ data ->
        println(data) // "{"origin":"127.0.0.1"}"
    }, { error ->
        println("An error of type ${error.exception} happened: ${error.message}")
    })
}
```

There are functions to handle `Result` object directly too.

```kotlin
runBlocking {
    Fuel.get("https://httpbin.org/ip").awaitStringResult()
        .fold({ data ->
            println(data) // "{"origin":"127.0.0.1"}"
        }, { error ->
            println("An error of type ${error.exception} happened: ${error.message}")
        })
}
```

It also provides useful methods to retrieve the `ByteArray`,`String` or `Object` directly. The difference with these implementations is that they throw exception instead of returning it wrapped a `FuelError` instance.

```kotlin
runBlocking {
    try {
        println(Fuel.get("https://httpbin.org/ip").awaitString()) // "{"origin":"127.0.0.1"}"
    } catch(exception: Exception) {
        println("A network request exception was thrown: ${exception.message}")
    }
}
```

Handling objects other than `String` (`awaitStringResponse() `) or `ByteArray` (`awaitByteArrayResponse()`) can be done using `awaitObject`, `awaitObjectResult` or `awaitObjectResponse`.

```kotlin
data class Ip(val origin: String)

object IpDeserializer : ResponseDeserializable<Ip> {
    override fun deserialize(content: String) =
        jacksonObjectMapper().readValue<Ip>(content)
}
```

```kotlin
runBlocking {
    Fuel.get("https://httpbin.org/ip").awaitObjectResult(IpDeserializer)
        .fold({ data ->
            println(data.origin) // 127.0.0.1
        }, { error ->
            println("An error of type ${error.exception} happened: ${error.message}")
        })
}
```

```kotlin
runBlocking {
    try {
        val data = Fuel.get("https://httpbin.org/ip").awaitObject(IpDeserializer)
        println(data.origin) // 127.0.0.1
    } catch (exception: Exception) {
        when (exception){
            is HttpException -> println("A network request exception was thrown: ${exception.message}")
            is JsonMappingException -> println("A serialization/deserialization exception was thrown: ${exception.message}")
            else -> println("An exception [${exception.javaClass.simpleName}\"] was thrown")
        }
    }
}
```

## Other libraries
If you like Fuel, you might also like other libraries of mine;
* [Result](https://github.com/kittinunf/Result) - The modelling for success/failure of operations in Kotlin
* [Fuse](https://github.com/kittinunf/Fuse) - A simple generic LRU memory/disk cache for Android written in Kotlin
* [Forge](https://github.com/kittinunf/Forge) - Functional style JSON parsing written in Kotlin
* [ReactiveAndroid](https://github.com/kittinunf/ReactiveAndroid) - Reactive events and properties with RxJava for Android SDK

## Credits

Fuel is brought to you by [contributors](https://github.com/kittinunf/Fuel/graphs/contributors).

## Licenses

Fuel is released under the [MIT](http://opensource.org/licenses/MIT) license.
