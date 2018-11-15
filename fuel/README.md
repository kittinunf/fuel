# Fuel (core)

The core package for [`Fuel`](../).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `Fuel` with `Maven` and `Gradle`. The core package has the following dependencies:
* Kotlin - 1.3.0
* Coroutine - 1.0.0

```groovy
compile 'com.github.kittinunf.fuel:fuel:<latest-version>'
```

## GET request

You can make `GET` requests using `.get(path: String)` on `Fuel`, a `FuelManager` instance or the `httpGet` extension.

```kotlin
Fuel.get("https://httpbin.org/get")
    .response { request, response, result ->
      println(request)
      println(response)
      val (bytes, error) = result
      if (bytes != null) {
        println(bytes)
      }
  }
```

## POST request

You can make `POST` requests using `.post(path: String)` on `Fuel`, a `FuelManager` instance or the `httpPost` extension.

There are several functions to set a `Body` for the request. If you are looking for a `application/form-multipart` upload request, check out the `UploadRequest` feature.

```kotlin
Fuel.post("https://httpbin.org/post")
    .response { request, response, result -> }
```

### Use `application/json`
If you don't want to set the `application/json` header, you can use `.jsonBody(value: String)` extension to automatically do this for you.
```kotlin
Fuel.post("https://httpbin.org/post")
    .jsonBody("{ \"foo\" : \"bar\" }")
    .response { request, response, result -> }
```

### Body
Bodies are formed from generic streams, but there are helpers to set it from values that can be turned into streams. It is important to know that, by default, the streams are **NOT** read into memory until the `Request` is sent.

When you're using the default `Client`, bodies are supported for:
- `POST`
- `PUT`
- `PATCH` (actually a `POST`)
- `DELETE`

#### from `String`
```kotlin
Fuel.post("https://httpbin.org/post")
    .header(Headers.CONTENT_TYPE, "text/plain")
    .body("my body is plain")
    .response { request, response, result -> }
```

#### from a `File`
```kotlin
Fuel.post("https://httpbin.org/post")
    .header(Headers.CONTENT_TYPE, "text/plain")
    .body(File("lipsum.txt"))
    .response { request, response, result -> }
```

#### from a `InputStream`
```kotlin
val stream = ByteArrayInputStream("source-string-from-string".toByteArray())
Fuel.post("https://httpbin.org/post")
    .header(Headers.CONTENT_TYPE, "text/plain")
    .body(stream)
    .response { request, response, result -> }
```

## PUT request

```kotlin
Fuel.put("https://httpbin.org/put")
    .response { request, response, result -> }
```

## DELETE request

```kotlin
Fuel.delete("https://httpbin.org/delete")
    .response { request, response, result -> }
```

## HEAD request

```kotlin
Fuel.head("https://httpbin.org/get")
    .response { request, response, result -> /* request body is empty */ }
```

## PATCH request
The default `client` is [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [`java.net.HttpUrlConnnection`](https://developer.android.com/reference/java/net/HttpURLConnection.html). [`java.net.HttpUrlConnnection`](https://developer.android.com/reference/java/net/HttpURLConnection.html) does not support a [`PATCH`](https://download.java.net/jdk7/archive/b123/docs/api/java/net/HttpURLConnection.html#setRequestMethod(java.lang.String)) method. [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/toolbox/HttpClient.kt) converts `PATCH` requests to a `POST` request and adds a `X-HTTP-Method-Override: PATCH` header. While this is a semi-standard industry practice not all APIs are configured to accept this header by default.

```kotlin
Fuel.patch("https://httpbin.org/patch")
    .response { request, response, result -> }
```

## CONNECT request
Connect is not supported by the Java JVM via the regular HTTP clients, and is therefore not supported.

## OPTIONS request
There are no convenience methods for making an OPTIONS request, but you can still make one directly:

```kotlin
Fuel.request(Method.OPTIONS, "https://httpbin.org/anything")
    .response { request, response, result -> }
```

## TRACE request
There are no convenience methods for making an TRACE request, but you can still make one directly:

```kotlin
Fuel.request(Method.TRACE, "https://httpbin.org/anything")
    .response { request, response, result -> }
```

## Logging
Fuel supports multiple forms of logging out of the box.

### Debug Logging
* Use `toString()` method to inspect requests

```kotlin
val request = Fuel.get("https://httpbin.org/get", parameters = listOf("key" to "value"))
println(request)

// --> GET (https://httpbin.org/get?key=value)
//    Body : (empty)
//    Headers : (2)
//    Accept-Encoding : compress;q=0.5, gzip;q=1.0
//    Device : Android
```

* Use `toString()` method to inspect responses

```kotlin
val (_, response, _) = Fuel.get("https://httpbin.org/get", parameters = listOf("key" to "value")).response()
println(response)
// <-- 200 (https://httpbin.org/get?key=value)
//    Body : (empty)
```

* Also support cUrl string to Log request, make it very easy to cUrl on command line

```kotlin
val request = Fuel.post("https://httpbin.org/post", parameters = listOf("foo" to "foo", "bar" to "bar", "key" to "value"))
println(request.cUrlString())
```

```bash
curl -i -X POST -d "foo=foo&bar=bar&key=value" -H "Accept-Encoding:compress;q=0.5, gzip;q=1.0" -H "Device:Android" -H "Content-Type:application/x-www-form-urlencoded" "https://httpbin.org/post"
```

## Parameter Support

* URL encoded style for GET & DELETE request

```kotlin
Fuel.get("https://httpbin.org/get", listOf("foo" to "foo", "bar" to "bar"))
    .response { request, response, result -> }
// resolve to https://httpbin.org/get?foo=foo&bar=bar

Fuel.delete("https://httpbin.org/delete", listOf("foo" to "foo", "bar" to "bar"))
    .response { request, response, result -> }
// resolve to https://httpbin.org/delete?foo=foo&bar=bar
```

* Array support for GET requests

```kotlin
Fuel.get("https://httpbin.org/get", listOf("foo" to "foo", "dwarf" to  arrayOf("grumpy","happy","sleepy","dopey")))
    .response { request, response, result -> }
// resolve to  https://httpbin.org/get?foo=foo&dwarf[]=grumpy&dwarf[]=happy&dwarf[]=sleepy&dwarf[]=dopey
```

* Support x-www-form-urlencoded for PUT & POST

```kotlin
Fuel.post("https://httpbin.org/post", listOf("foo" to "foo", "bar" to "bar"))
    .response { request, response, result -> }
// Body : "foo=foo&bar=bar"

Fuel.put("https://httpbin.org/put", listOf("foo" to "foo", "bar" to "bar"))
    .response { request, response, result -> }
// Body : "foo=foo&bar=bar"
```

## Set request's timeout and read timeout
Default timeout for a request is 15000 milliseconds.
Default read timeout for a request is 15000 milliseconds.

```kotlin
val timeout = 5000 // 5000 milliseconds = 5 seconds.
val timeoutRead = 60000 // 60000 milliseconds = 1 minute.

Fuel.get("https://httpbin.org/get")
    .timeout(timeout)
    .timeoutRead(timeoutRead)
    .responseString { request, response, result -> }
```


## Download with or without progress handler
```kotlin
Fuel.download("https://httpbin.org/bytes/32768")
    .destination { response, url -> File.createTempFile("temp", ".tmp") }
    .response { req, res, result -> }

Fuel.download("https://httpbin.org/bytes/32768")
    .destination { response, url -> File.createTempFile("temp", ".tmp") }
    .progress { readBytes, totalBytes ->
        val progress = readBytes.toFloat() / totalBytes.toFloat() * 100
        println("Bytes downloaded $readBytes / $totalBytes ($progress %)")
    }
    .response { req, res, result -> }
```

## Upload with or without progress handler
```kotlin
Fuel.upload("/post")
    .source { request, url -> File.createTempFile("temp", ".tmp") }
    .responseString { request, response, result -> }

// By default upload use Method.POST, unless it is specified as something else
Fuel.upload("/put", Method.PUT)
    .source { request, url -> File.createTempFile("temp", ".tmp") }
    .responseString { request, response, result -> }

// Upload with multiple files
Fuel.upload("/post")
    .sources { request, url ->
        listOf(
            File.createTempFile("temp1", ".tmp"),
            File.createTempFile("temp2", ".tmp")
        )
    }
    .name { "temp" }
    .responseString { request, response, result -> }
```

## Specify custom field names for files
```Kotlin
Fuel.upload("/post")
    .dataParts { request, url ->
        listOf(
            //DataPart takes a file, and you can specify the name and/or type
            DataPart(File.createTempFile("temp1", ".tmp"), "image/jpeg"),
            DataPart(File.createTempFile("temp2", ".tmp"), "file2"),
            DataPart(File.createTempFile("temp3", ".tmp"), "third-file", "image/jpeg")
        )
    }
    .responseString { request, response, result -> /* ... */ }
```

## Upload a multipart form without a file
```kotlin
val formData = listOf("Email" to "mail@example.com", "Name" to "Joe Smith" )
Fuel.upload("/post", param = formData)
    // Upload normally requires a file, but we can give it an empty list of `DataPart`
    .dataParts { request, url -> listOf<DataPart>() }
    .responseString { request, response, result -> /* ... */ }
```

## Upload from an `InputStream`

```kotlin
Fuel.upload("/post")
    .blob { request, url -> Blob("filename.png", someObject.length) { someObject.getInputStream() } }
```

## Authentication

* Support *Basic Authentication* right off the box
    ```kotlin
    val username = "username"
    val password = "abcd1234"

    Fuel.get("https://httpbin.org/basic-auth/$user/$password")
        .basicAuthentication(username, password)
        .response { request, response, result -> }
    ```

* Support *Bearer Authentication*
    ```kotlin
    val token = "mytoken"

    Fuel.get("https://httpbin.org/bearer")
        .bearerAuthentication(token)
        .response { request, response, result -> }
    ```

* Support *Any authentication* by header
    ```kotlin
    Fuel.get("https://httpbin.org/anything")
        .header(Headers.AUTHORIZATION, "Custom secret")
        .response { request, response, result -> }
    ```

### Validation

* By default, the valid range for HTTP status code will be (200..299).

### Cancel

* If one wants to cancel on-going request, one could call `cancel` on the request object
    ```kotlin
    val request = Fuel.get("https://httpbin.org/get")
      .response { request, response, result ->
        // if request is cancelled successfully, response callback will not be called.
        // Interrupt callback (if provided) will be called instead
      }

    //later
    request.cancel() //this will cancel on-going request
    ```

* Also, interrupt request can be further processed with interrupt callback
    ```kotlin
    val request = Fuel.get("https://httpbin.org/get")
      .interrupt { request -> println("${request.url} was interrupted and cancelled") }
      .response { request, response, result ->
        // if request is cancelled successfully, response callback will not be called.
        // Interrupt callback (if provided) will be called instead
      }

    request.cancel()
    ```

## Advanced Configuration

### Response Handling
### Result

* [Result](https://github.com/kittinunf/Result) is a functional style data structure that represents data that contains result of *Success* or *Failure* but not both. It represents the result of an action that can be success (with result) or error.

* Working with result is easy. You could [*fold*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/test/kotlin/com/github/kittinunf/fuel/RequestTest.kt#L324), [*destructure*](https://github.com/kittinunf/Fuel/blob/master/fuel/src/test/kotlin/com/github/kittinunf/fuel/RequestTest.kt#L266) as because it is just a [data class](https://kotlinlang.org/docs/reference/data-classes.html) or do a simple ```when``` checking whether it is *Success* or *Failure*.

### Response
```kotlin
fun response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit)
```

### Response in String
```kotlin
fun responseString(handler: (Request, Response, Result<String, FuelError>) -> Unit)
```

### Response in T (object)
```kotlin
fun <T> responseObject(deserializer: ResponseDeserializable<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit)
```

### Response Deserialization

* Fuel provides built-in support for response deserialization. Here is how one might want to use Fuel together with [Gson](https://github.com/google/gson)
```kotlin
//User Model
data class User(val firstName: String = "",
                val lastName: String = "") {

    //User Deserializer
    class Deserializer : ResponseDeserializable<User> {
        override fun deserialize(content: String) = Gson().fromJson(content, User::class.java)
    }

}

//Use httpGet extension
"https://www.example.com/user/1".httpGet().responseObject(User.Deserializer()) { req, res, result ->
    //result is of type Result<User, Exception>
    val (user, err) = result

    println(user.firstName)
    println(user.lastName)
}

```

* There are 4 methods to support response deserialization depending on your needs (also depending on JSON parsing library of your choice), and you are required to implement only one of them.

    ```kotlin
    fun deserialize(bytes: ByteArray): T?

    fun deserialize(inputStream: InputStream): T?

    fun deserialize(reader: Reader): T?

    fun deserialize(content: String): T?
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
    ```kotlin
    FuelManager.instance.basePath = "https://httpbin.org"

    // Later
    Fuel.get("/get").response { request, response, result ->
        //make request to https://httpbin.org/get because Fuel.{get|post|put|delete} use FuelManager.instance to make HTTP request
    }
    ```

* `baseHeaders` is to manage common HTTP header pairs in format of `Map<String, String>`.
    - The base headers are only applied if the request does not have those headers set.
    ```kotlin
    FuelManager.instance.baseHeaders = mapOf("Device" to "Android")
    ```

* `Headers` can be added to a request via various methods including
    - `fun header(name: String, value: Any): Request`: `request.header("foo", "a")`
    - `fun header(pairs: Map<String, Any>): Request`: `request.header(mapOf("foo" to "a"))`
    - `fun header(vararg pairs: Pair<String, Any>): Request`: `request.header("foo" to "a")`
    - `operator fun set(header: String, value: Collection<Any>): Request`: `request["foo"] = listOf("a", "b")`
    - `operator fun set(header: String, value: Any): Request`: `request["foo"] = "a"`
* By default, all subsequent calls overwrite earlier calls, but you may use the `appendHeader` variant to append values to existing values.
    - In earlier versions a `mapOf` overwrote, and `varargs pair` did not, but this was confusing.
* Some of the HTTP headers are defined under `Headers.Companion` and can be used instead of literal strings.
    ```kotlin
    Fuel.post("/my-post-path")
        .header(Headers.ACCEPT, "text/html, */*; q=0.1")
        .header(Headers.CONTENT_TYPE, "image/png")
        .header(Headers.COOKIE to "basic=very")
        .appendHeader(Headers.COOKIE to "value_1=foo", Headers.COOKIE to "value_2=bar", Headers.ACCEPT to "application/json")
        .appendHeader("MyFoo" to "bar", "MyFoo" to "baz")
        .response { /*...*/ }

     // => request with:
     //    Headers:
     //      Accept: "text/html, */*; q=0.1, application/json"
     //      Content-Type: "image/png"
     //      Cookie: "basic=very; value_1=foo; value_2=bar"
     //      MyFoo: "bar, baz"
    ```

* `baseParams` is used to manage common `key=value` query param, which will be automatically included in all of your subsequent requests in format of ` Parameters` (`Any` is converted to `String` by `toString()` method)
    ```kotlin
    FuelManager.instance.baseParams = listOf("api_key" to "1234567890")

    // Later
    Fuel.get("/get").response { request, response, result ->
        //make request to https://httpbin.org/get?api_key=1234567890
    }
    ```

* `client` is a raw HTTP client driver. Generally, it is responsible to make [`Request`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Request.kt) into [`Response`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Response.kt). Default is [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [`java.net.HttpUrlConnnection`](https://developer.android.com/reference/java/net/HttpURLConnection.html). You could use any httpClient of your choice by conforming to [`client`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/core/Client.kt) protocol, and set back to `FuelManager.instance` to kick off the effect.

* `keyStore` is configurable by user. By default it is `null`.

* `socketFactory` can be supplied by user. If `keyStore` is not null, `socketFactory` will be derived from it.

* `hostnameVerifier` is configurable by user. By default, it uses `HttpsURLConnection.getDefaultHostnameVerifier()`.

* `requestInterceptors` `responseInterceptors` is a side-effect to add to `Request` and/or `Response` objects.
    - For example, one might wanna print cUrlString style for every request that hits server in DEBUG mode.
        ```kotlin
        val manager = FuelManager()
        if (BUILD_DEBUG) {
            manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())
        }
        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").response() //it will print curl -i -H "Accept-Encoding:compress;q=0.5, gzip;q=1.0" "https://httpbin.org/get"
        ```

    - Another example is that you might wanna add data into your Database, you can achieve that with providing `responseInterceptors` such as

        ```kotlin
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
        manager.request(Method.GET, "https://www.example.com/api/dog/1").response() // Db interceptor will be called to intercept data and save into Database of your choice
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

    override val params: Parameters?
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
Fuel.request(WeatherApi.weatherFor("london"))
    .responseJson { request, response, result ->
        result.fold(success = { json ->
            Log.d("qdp success", json.array().toString())
        }, failure = { error ->
            Log.e("qdp error", error.toString())
        })
    }
```
