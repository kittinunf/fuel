# Fuel

[ ![Kotlin](https://img.shields.io/badge/Kotlin-1.0.2-blue.svg)](http://kotlinlang.org) [ ![jcenter](https://api.bintray.com/packages/kittinunf/maven/Fuel-Android/images/download.svg) ](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) [![Build Status](https://travis-ci.org/kittinunf/Fuel.svg?branch=master)](https://travis-ci.org/kittinunf/Fuel)

The easiest HTTP networking library for Kotlin/Android.

## Features

- [x] Support basic HTTP GET/POST/PUT/DELETE/HEAD in a fluent style interface
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
- [x] RxJava support out of the box

## Installation

### Dependency - fuel

* [Result](https://github.com/kittinunf/Result) - The modelling for success/failure of operations in Kotlin

### Dependency - fuel-rxjava

* [RxJava](https://github.com/ReactiveX/RxJava) - RxJava – Reactive Extensions for the JVM

### Gradle

``` Groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.github.kittinunf.fuel:fuel:1.3.1' //for JVM
    compile 'com.github.kittinunf.fuel:fuel-android:1.3.1' //for Android
    compile 'com.github.kittinunf.fuel:fuel-rxjava:1.3.1' //for RxJava support
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
            error = result.getAs()
        }
        is Result.Success -> {
            data = result.getAs()
        }
    }
}

//if we set baseURL beforehand, simply use relativePath
FuelManager.instance.basePath = "http://httpbin.org"
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
val readTimeout = 60000 // 60000 milliseconds = 1 minute.

"http://httpbin.org/get".httpGet().timeout(timeout).readTimeout(readTimeout).responseString { request, response, result -> }
```

* Java
``` Java
int timeout = 5000 // 5000 milliseconds = 5 seconds.
int readTimeout = 60000 // 60000 milliseconds = 1 minute.
Fuel.get("http://httpbin.org/get", params).timeout(timeout).readTimeout(readTimeout).responseString(new Handler<String>() {
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

* There are 6 extensions over `Request` that provide RxJava `Observable<T>` as return type.

``` Kotlin
fun Request.rx_response(): Observable<Pair<Response, ByteArray>>
fun Request.rx_responseString(charset: Charset): Observable<Pair<Response, String>>
fun <T : Any> Request.rx_responseObject(deserializable: Deserializable<T>): Observable<Pair<Response, T>>

fun Request.rx_data(): Observable<ByteArray>
fun Request.rx_string(charset: Charset): Observable<String>
fun <T : Any> Request.rx_object(deserializable: Deserializable<T>): Observable<T>
```

## Other libraries
If you like Fuel, you might also like other libraries;
* [Result](https://github.com/kittinunf/Result) - The modelling for success/failure of operations in Kotlin
* [Kovenant](https://github.com/mplatvoet/kovenant) - Kovenant. Promises for Kotlin.
* [Fuse](https://github.com/kittinunf/Fuse) - A simple generic LRU memory/disk cache for Android written in Kotlin

## Credits

Fuel is brought to you by [contributors](https://github.com/kittinunf/Fuel/graphs/contributors).

## Licenses

Fuel is released under the [MIT](http://opensource.org/licenses/MIT) license.
