# Fuel (core)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.20-blue.svg)](https://kotlinlang.org)

The core package for [`Fuel`](../README.md). The documentation outlined here touches most subjects and functions but is not exhaustive. 

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `Fuel` with `Maven` and `Gradle`. The core package has the following dependencies:
* Kotlin - 1.3.20
* KotlinX Coroutines - 1.0.1

```groovy
compile 'com.github.kittinunf.fuel:fuel:<latest-version>'
```

## Usage

### Making Requests

You can make requests using functions on `Fuel`, a `FuelManager` instance or the string extensions.

```kotlin
Fuel.get("https://httpbin.org/get")
    .response { request, response, result ->
      println(request)
      println(response)
      val (bytes, error) = result
      if (bytes != null) {
        println("[response bytes] ${String(bytes)}")
      }
  }

/*
 * --> GET https://httpbin.org/get
 * "Body : (empty)"
 * "Headers : (0)"
 *

 * <-- 200 (https://httpbin.org/get)
 * Response : OK
 * Length : 268
 * Body : ({
 *   "args": {},
 *   "headers": {
 *     "Accept": "text/html, image/gif, image/jpeg, *; q=.2, *\/*; q=.2",
 *     "Connection": "close",
 *     "Host": "httpbin.org",
 *     "User-Agent": "Java/1.8.0_172"
 *   },
 *   "origin": "123.456.789.123",
 *   "url": "https://httpbin.org/get"
 * })
 * Headers : (8)
 * Connection : keep-alive
 * Date : Thu, 15 Nov 2018 00:47:50 GMT
 * Access-Control-Allow-Origin : *
 * Server : gunicorn/19.9.0
 * Content-Type : application/json
 * Content-Length : 268
 * Access-Control-Allow-Credentials : true
 * Via : 1.1 vegur

 * [response bytes] {
 *   "args": {},
 *   "headers": {
 *     "Accept": "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2",
 *     "Connection": "close",
 *     "Host": "httpbin.org",
 *     "User-Agent": "Java/1.8.0_172"
 *   },
 *   "origin": "123.456.789.123",
 *   "url": "https://httpbin.org/get"
 * }
 */
```

The extensions and functions made available by the core package are listed here:

| Fuel Method | String extension | `Fuel`/`FuelManager` method |
|----|----|----|
| `Method.GET` | `"https://httpbin.org/get".httpGet()` | `Fuel.get("https://httpbin.org/get")` |
| `Method.POST` | `"https://httpbin.org/post".httpPost()` | `Fuel.post("https://httpbin.org/post")` |
| `Method.PUT` | `"https://httpbin.org/put".httpPut()` | `Fuel.put("https://httpbin.org/put")` |
| `Method.PATCH` | `"https://httpbin.org/patch".httpPatch()` | `Fuel.patch("https://httpbin.org/patch")` |
| `Method.HEAD` | `"https://httpbin.org/get".httpHead()` | `Fuel.head("https://httpbin.org/get")` |
| `Method.OPTIONS` | _not supported_ | `Fuel.request(Method.OPTIONS, "https://httpbin.org/anything")` |
| `Method.TRACE` | _not supported_ | `Fuel.request(Method.TRACE, "https://httpbin.org/anything")` |
| `Method.CONNECT` | _not supported_ | _not supported_ |

#### About `PATCH` requests
The default `client` is [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [`java.net.HttpUrlConnection`](https://developer.android.com/reference/java/net/HttpURLConnection.html). [`java.net.HttpUrlConnection`](https://developer.android.com/reference/java/net/HttpURLConnection.html) does not support a [`PATCH`](https://download.java.net/jdk7/archive/b123/docs/api/java/net/HttpURLConnection.html#setRequestMethod(java.lang.String)) method. [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/toolbox/HttpClient.kt) converts `PATCH` requests to a `POST` request and adds a `X-HTTP-Method-Override: PATCH` header. While this is a semi-standard industry practice not all APIs are configured to accept this header by default.

```kotlin
Fuel.patch("https://httpbin.org/patch")
    .also { println(it) }
    .response { result -> }

/* --> PATCH https://httpbin.org/post
 * "Body : (empty)"
 * "Headers : (1)"
 * Content-Type : application/x-www-form-urlencoded
 */

// What is actually sent to the server

/* --> POST (https://httpbin.org/post)
 * "Body" : (empty)
 * "Headers : (3)"
 * Accept-Encoding : compress;q=0.5, gzip;q=1.0
 * Content-Type : application/x-www-form-urlencoded
 * X-HTTP-Method-Override : PATCH
 */
```

#### About `CONNECT` request
Connect is not supported by the Java JVM via the regular HTTP clients, and is therefore not supported.

### Adding `Parameters`
All the `String` extensions listed above, as well as the `Fuel` and `FuelManager` calls accept a parameter `parameters: Parameters`.

* URL encoded style for `GET` and `DELETE` request

    ```kotlin
    Fuel.get("https://httpbin.org/get", listOf("foo" to "foo", "bar" to "bar"))
        .url
    // https://httpbin.org/get?foo=foo&bar=bar
    ```

    ```kotlin
    Fuel.delete("https://httpbin.org/delete", listOf("foo" to "foo", "bar" to "bar"))
        .url
    // https://httpbin.org/delete?foo=foo&bar=bar
    ```

* Support `x-www-form-urlencoded` for `PUT`, `POST` and `PATCH`

    ```kotlin
    Fuel.post("https://httpbin.org/post", listOf("foo" to "foo", "bar" to "bar"))
        .also { println(it.url) }
        .also { println(String(it.body().toByteArray())) }

    // https://httpbin.org/post
    // "foo=foo&bar=bar"
    ```

    ```kotlin
    Fuel.put("https://httpbin.org/put", listOf("foo" to "foo", "bar" to "bar"))
        .also { println(it.url) }
        .also { println(String(it.body().toByteArray())) }

    // https://httpbin.org/post
    // "foo=foo&bar=bar"
    ```
    
#### `Parameters` with `Body`
If a request already has a body, the parameters are url-encoded instead. You can remove the handling of parameter encoding by removing the default `ParameterEncoder` request interceptor from your `FuelManager`.

#### `Parameters` with `multipart/form-data`
The `UploadRequest` handles encoding parameters in the body. Therefore by default, parameter encoding is ignored by `ParameterEncoder` if the content type is `multipart/form-data`.

#### `Parameters` with empty, array, list or null values
All requests can have parameters, regardless of the method.
- a list is encoded as `key[]=value1&key[]=value2&...`
- an array is encoded as `key[]=value1&key[]=value2&...`
- an empty string value is encoded as `key`
- a null value is removed

### Adding `Request` body
Bodies are formed from generic streams, but there are helpers to set it from values that can be turned into streams. It is important to know that, by default, the streams are **NOT** read into memory until the `Request` is sent. However, if you pass in an in-memory value such as a `ByteArray` or `String`, `Fuel` uses `RepeatableBody`, which are kept into memory until the `Request` is dereferenced.

When you're using the default `Client`, bodies are supported for:
- `POST`
- `PUT`
- `PATCH` (actually a `POST`, as noted above)
- `DELETE`

There are several functions to set a `Body` for the request. If you are looking for a `multipart/form-data` upload request, checkout the `UploadRequest` feature.

```kotlin
Fuel.post("https://httpbin.org/post")
    .body("My Post Body")
    .also { println(it) }
    .response { result -> }

/* --> POST https://httpbin.org/post
 * "Body : My Post Body"
 * "Headers : (1)"
 * Content-Type : application/x-www-form-urlencoded
 */
```

#### Use `application/json`
If you don't want to set the `application/json` header, you can use `.jsonBody(value: String)` extension to automatically do this for you.
```kotlin
Fuel.post("https://httpbin.org/post")
    .jsonBody("{ \"foo\" : \"bar\" }")
    .also { println(it) }
    .response { result -> }

/* --> POST https://httpbin.org/post
 * "Body : { "foo" : "bar" }"
 * "Headers : (1)"
 * Content-Type : application/json
 */
```

#### from `String`
```kotlin
Fuel.post("https://httpbin.org/post")
    .header(Headers.CONTENT_TYPE, "text/plain")
    .body("my body is plain")
    .also { println(it) }
    .response { result -> }

/* --> POST https://httpbin.org/post
 * "Body : my body is plain"
 * "Headers : (1)"
 * Content-Type : text/plain
 */
```

#### from a `File`
```kotlin
Fuel.post("https://httpbin.org/post")
    .header(Headers.CONTENT_TYPE, "text/plain")
    .body(File("lipsum.txt"))
    .also { println(it) }
    .response { result -> }

/* --> POST https://httpbin.org/post
 * "Body : Lorem ipsum dolor sit amet, consectetur adipiscing elit."
 * "Headers : (1)"
 * Content-Type : text/plain
 */
```

#### from a `InputStream`
```kotlin
val stream = ByteArrayInputStream("source-string-from-string".toByteArray())

Fuel.post("https://httpbin.org/post")
    .header(Headers.CONTENT_TYPE, "text/plain")
    .body(stream)
    .also { println(it) }
    .response { result -> }

/* --> POST https://httpbin.org/post
 * "Body : source-string-from-string"
 * "Headers : (1)"
 * Content-Type : text/plain
 */
```

#### from a `lazy` source (`InputStream`)
Fuel always reads the body lazily, which means you can also provide a callback that will return a stream. This is also known as a `BodyCallback`:

```kotlin
val produceStream = { ByteArrayInputStream("source-string-from-string".toByteArray()) }

Fuel.post("https://httpbin.org/post")
    .header(Headers.CONTENT_TYPE, "text/plain")
    .body(produceStream)
    .also { println(it) }
    .response { result -> }

/* --> POST https://httpbin.org/post
 * "Body : source-string-from-string"
 * "Headers : (1)"
 * Content-Type : text/plain
 */
```

#### Using automatic body redirection
The default redirection interceptor only forwards `RepeatableBody`, and only if the status code is 307 or 308, as per the RFCs. In order to use a `RepeatableBody`, pass in a `String` or `ByteArray` as body, or explicitely set `repeatable = true` for the `fun body(...)` call.

**NOTE** this loads the _entire_ body into memory, and therefore is not suited for large bodies.

### Adding `Headers`
There are many ways to set, overwrite, remove and append headers. For your convenience, internally used and common header names are attached to the `Headers` companion and can be accessed (e.g. `Headers.CONTENT_TYPE`, `Headers.ACCEPT`, ...).

The most common ones are mentioned here:

#### Reading `HeaderValues`

| call | arguments | action |
|----|----|----|
| `request[header]` | `header: String` | Get the current values of the header, after normalisation of the header |
| `request.header(header)` | `header: String` | Get the current values |

#### (Over)writing `HeaderValues`

| call | arguments | action |
|----|----|----|
| `request[header] = values` | `header: String`, `values: Collection<*>` | Set the values of the header, overriding what's there, after normalisation of the header |
| `request[header] = value` | `header: String`, `value: Any` | Set the value of the header, overriding what's there, after normalisation of the header |
| `request.header(map)` | `map: Map<String, Any>` | Replace the headers with the map provided |
| `request.header(pair, pair, ...)` | `vararg pairs: Pair<String, Any>` | Replace the headers with the pairs provided |
| `request.header(header, values)` | `header: String, values: Collection<*>` | Replace the header with the provided values |
| `request.header(header, value)` | `header: String, value: Any` | Replace the header with the provided value |
| `request.header(header, value, value, ...)` | `header: String, vararg values: Any` | Replace the header with the provided values |

#### Appending `HeaderValues`

| call | arguments | action |
|----|----|----|
| `request.appendHeader(pair, pair, ...)` | `vararg pairs: Pair<String, Any>` | Append each pair, using the key as header name and value as header content |
| `request.appendHeader(header, value)` | `header: String, value: Any` |  Appends the value to the header or sets it if there was none yet |
| `request.appendHeader(header, value, value, ...)` | `header: String, vararg values: Any` | Appends the value to the header or sets it if there was none yet |

Note that headers which by the RFC may only have one value are always overwritten, such as `Content-Type`.

#### `FuelManager` base headers vs. `Request` headers
The `baseHeaders` set through a `FuelManager` are only applied to a `Request` if that request does **not** have that specific header set yet. There is no appending logic. If you set a header it will overwrite the base value.

#### `Client` headers vs. `Request` headers
Any `Client` can add, remove or transform `HeaderValues` before it sends the `Request` or after it receives the `Response`. The default `Client` for example sets `TE` values.

#### `HeaderValues` values are `List`
Even though some headers can only be set once (and will overwrite even when you try to append), the internal structure is always a list. Before a `Request` is made, the default `Client` collapses the multiple values, if allowed by the RFCs, into a single header value delimited by a separator for that header. Headers that can only be set once will use the last value by default and ignore earlier set values.

### Adding Authentication
Authentication can be added to a `Request` using the `.authentication()` feature.
By default, `authentication` is passed on when using the default `redirectResponseInterceptor` (which is enabled by default), unless it is redirecting to a different host. You can remove this behaviour by implementing your own redirection logic.

> When you call `.authentication()`, a few extra functions are available. If you call a regular function (e.g. `.header()`) the extra functions are no longer available, but you can safely call `.authentication()` again without losing any previous calls.

*  *Basic* authentication

```kotlin
val username = "username"
val password = "abcd1234"

Fuel.get("https://httpbin.org/basic-auth/$user/$password")
    .authentication()
    .basic(username, password)
    .response { result -> }
```

* *Bearer* authentication

```kotlin
val token = "mytoken"

Fuel.get("https://httpbin.org/bearer")
    .authentication()
    .bearer(token)
    .response { result -> }
```

* Any authentication using a header

```kotlin
Fuel.get("https://httpbin.org/anything")
    .header(Headers.AUTHORIZATION, "Custom secret")
    .response { result -> }
```

### Adding Progress callbacks

Any request supports `Progress` callbacks when uploading or downloading a body; the `Connection` header does not support progress (which is the only thing that is sent if there are no bodies).
You can have as many progress handlers of each type as you like.

#### Request progress

```kotlin
Fuel.post("/post")
    .body(/*...*/)
    .requestProgress { readBytes, totalBytes ->
      val progress = readBytes.toFloat() / totalBytes.toFloat() * 100
      println("Bytes uploaded $readBytes / $totalBytes ($progress %)")
    }
    .response { result -> }
```

#### Response progress

```kotlin
Fuel.get("/get")
    .responseProgress { readBytes, totalBytes ->
      val progress = readBytes.toFloat() / totalBytes.toFloat() * 100
      println("Bytes downloaded $readBytes / $totalBytes ($progress %)")
    }
    .response { result -> }
```

#### Why does totalBytes increase?

Not all source `Body` or `Response` `Body` report their total size. If the size is not known, the current size will be reported. This means that you will constantly get an increasing amount of totalBytes that equals readBytes.

### Using `multipart/form-data` (`UploadRequest`)

Fuel supports multipart uploads using the `.upload()` feature. You can turn _any_ `Request` into a upload request by calling `.upload()` or call `.upload(method = Method.POST)` directly onto `Fuel` / `FuelManager`.

> When you call `.upload()`, a few extra functions are available. If you call a regular function (e.g. `.header()`) the extra functions are no longer available, but you can safely call `.upload()` again without losing any previous calls.

| method | arguments | action |
|----|----|----|
| `request.add { }` | `varargs dataparts: (Request) -> DataPart` | Add one or multiple DataParts lazily |
| `request.add()` | `varargs dataparts: DataPart` | Add one or multiple DataParts |
| `request.progress(handler)` | `hander: ProgressCallback` | Add a `requestProgress` handler |

```kotlin
Fuel.upload("/post")
    .add { FileDataPart(File("myfile.json"), name = "fieldname", filename="contents.json") }
    .response { result -> }
```

#### `DataPart` from `File`
In order to add `DataPart`s that are sources from a `File`, you can use `FileDataPart`, which takes a `file: File`. There are some sane defaults for the field name `name: String`, and remote file name `filename: String`, as well as the `Content-Type` and `Content-Disposition` fields, but you can override them.

In order to receive a list of files, for example in the field `files`, use the array notation:
```kotlin
Fuel.upload("/post")
    .add(
        FileDataPart(File("myfile.json"), name = "files[]", filename="contents.json"),
        FileDataPart(File("myfile2.json"), name = "files[]", filename="contents2.json"),
        FileDataPart(File("myfile3.json"), name = "files[]", filename="contents3.json")
    )
    .response { result -> }
```

Sending multiple files in a single datapart is _not_ supported as it's deprecated by the multipart/form-data RFCs, but to simulate this behaviour, give the same `name` to multiple parts.

You can use the convenience constructors `FileDataPart.from(directory: , filename: , ...args)` to create a `FileDataPart` from `String` arguments.

#### `DataPart` from inline content
Sometimes you have some content inline that you want to turn into a `DataPart`. You can do this with `InlineDataPart`:

```kotlin
Fuel.upload("/post")
    .add(
        FileDataPart(File("myfile.json"), name = "file", filename="contents.json"),
        InlineDataPart(myInlineContent, name = "metadata", filename="metadata.json", contentType = "application/json")
    )
    .response { result -> }
```

A `filename` is not mandatory and is empty by default; the `contentType` is `text/plain` by default.

#### `DataPart` from `InputStream` (formely `Blob`)
You can also add dataparts from arbitrary `InputStream`s, which you can do using `BlobDataPart`:

 ```kotlin
 Fuel.upload("/post")
     .add(
         FileDataPart(File("myfile.json"), name = "file", filename="contents.json"),
         BlobDataPart(someInputStream, name = "metadata", filename="metadata.json", contentType = "application/json", contentLength = 555)
     )
     .response { result -> }
 ```
If you don't set the `contentLength` to a positive integer, your entire `Request` `Content-Length` will be undeterminable and the default `HttpClient` will switch to chunked streaming mode with an arbitrary stream buffer size.

#### Multipart request without a file

Simply don't call `add`. The parameters are encoded as parts!

```kotlin
val formData = listOf("Email" to "mail@example.com", "Name" to "Joe Smith" )

Fuel.upload("/post", param = formData)
    .response { result -> }
```

### Getting a Response

As mentioned before, you can use `Fuel` both synchronously and a-synchronously, with support for coroutines.

#### Blocking responses

By default, there are three response functions to get a request synchronously:

| function | arguments | result |
|---|---|---|
| `response()` | _none_ | `ResponseResultOf<ByteArray>` |
| `responseString(charset)` | `charset: Charset` | `ResponseResultOf<String>` |
| `responseObject(deserializer)` | `deserializer: Deserializer<U>` | `ResponseResultOf<U>` |

The default charset is `UTF-8`. If you want to implement your own deserializers, scroll down to advanced usage.

#### Async responses

Add a handler to a blocking function, to make it asynchronous:

| function | arguments | result |
|---|---|---|
| `response() { handler }` | `handler: Handler` | `CancellableRequest` |
| `responseString(charset) { handler }` | `charset: Charset, handler: Handler` | `CancellableRequest` |
| `responseObject(deserializer) { handler }` | `deserializer: Deserializer, handler: Handler` | `CancellableRequest` |

The default charset is `UTF-8`. If you want to implement your own deserializers, scroll down to advanced usage.

#### Suspended responses

The core package has limited support for coroutines:

| function | arguments | result |
|---|---|---|
| `await(deserializer)` | `deserializer: Deserializer<U>` | `U` |
| `awaitResult(deserializer)` | `deserializer: Deserializer<U>` | `Result<U, FuelError>` |
| `awaitResponse(deserializer)` | `deserializer: Deserializer<U>` | `ResponseOf<U>` |
| `awaitResponseResult(deserializer)` | `deserializer: Deserializer<U>` | `ResponseResultOf<U>` |

When using other packages such as `fuel-coroutines`, more response/await functions are available.

#### Response types

- The `ResponseResultOf<U>` type is a `Triple` of the `Request`, `Response` and a `Result<U, FuelError>`
- The `ResponseOf<U>` type is a `Triple` of the `Request`, `Response` and a `U`; errors are thrown
- The `Result<U, FuelError>` type is a non-throwing wrapper around `U`
- The `U` type doesn't wrap anything; errors are thrown

#### Handler types

When defining a handler, you can use one of the following for all `responseXXX` functions that accept a `Handler`:

| type | handler fns | arguments | description |
|---|---|---|---|
|`Handler<T>`| 2 | 1 | calls `success` with an instance of `T` or `failure` on errors |
|`ResponseHandler<T>` | 2 | 3 | calls `success` with `Request`, `Response` and an instance of `T`, or `failure` or errors |
|`ResultHandler<T>` | 1 | 1 | invokes the function with `Result<T, FuelError>` |
|`ResponseResultHandler<T>` | 1 | 3 | invokes the function with `Request` `Response` and `Result<T, FuelError>` |

This means that you can either choose to unwrap the `Result` yourself using a `ResultHandler` or `ResponseResultHandler`, or define dedicated callbacks in case of success or failure.

#### Dealing with `Result<T, FuelError>`

[Result](https://github.com/kittinunf/Result) is a functional style data structure that represents data that contains result of *Success* or *Failure* but not both. It represents the result of an action that can be success (with result) or error.

Working with result is easy: 
- You can call [`fold`] and define a tranformation function for both cases that results in the same return type,
- [`destructure`] as `(data, error) = result` because it is just a [data class](https://kotlinlang.org/docs/reference/data-classes.html) or 
- use `when` checking whether it is `Result.Success` or `Result.Failure`

#### Download response to output (`File` or `OutputStream`)

Fuel supports downloading the request `Body` to a file using the `.download()` feature. You can turn _any_ `Request` into a download request by calling `.download()` or call `.download(method = Method.GET)` directly onto `Fuel` / `FuelManager`.

> When you call `.download()`, a few extra functions are available. If you call a regular function (e.g. `.header()`) the extra functions are no longer available, but you can safely call `.download()` again without losing any previous calls.

| method | arguments | action |
|----|----|----|
| `request.fileDestination { }` | `(Response, Request) -> File` | Set the destination file callback where to store the data |
| `request.streamDestination { }` | `(Response, Request) -> Pair<OutputStream, () -> InputStream>` | Set the destination file callback where to store the data |
| `request.progress(handler)` | `hander: ProgressCallback` | Add a `responseProgress` handler |

```kotlin
Fuel.download("https://httpbin.org/bytes/32768")
    .fileDestination { response, url -> File.createTempFile("temp", ".tmp") }
    .progress { readBytes, totalBytes ->
        val progress = readBytes.toFloat() / totalBytes.toFloat() * 100
        println("Bytes downloaded $readBytes / $totalBytes ($progress %)")
    }
    .response { result -> }
```

The `stream` variant expects your callback to provide a `Pair` with both the `OutputStream` to write too, as well as a callback that gives an `InputStream`, or raises an error.
- The `OutputStream` is _always_ closed after the body has been written. Make sure you wrap whatever functionality you need on top of the stream and don't rely on the stream to remain open.
- The `() -> InputStream` replaces the body after the current body has been written to the `OutputStream`. It is used to make sure you can _also_ retrieve the body via the `response` / `await` method results. If you don't want the body to be readable after downloading it, you have to do two things:
  - use an `EmptyDeserializer` with `await(deserializer)` or one of the `response(deserializer)` variants
  - provide an `InputStream` callback that `throws` or returns an empty `InputStream`.

### Cancel an async `Request`

The `response` functions called with a `handler` are async and return a `CancellableRequest`. These requests expose a few extra functions that can be used to control the `Future` that should resolve a response:

```kotlin
val request = Fuel.get("https://httpbin.org/get")
  .interrupt { request -> println("${request.url} was interrupted and cancelled") }
  .response { result ->
    // if request is cancelled successfully, response callback will not be called.
    // Interrupt callback (if provided) will be called instead
  }

request.cancel() // this will cancel on-going request
```

If you can't get hold of the `CancellableRequest` because, for example, you are adding this logic in an `Interceptor`, a generic `Queue`, or a `ProgressCallback`, you can call `tryCancel()` which returns true if it was cancelled and false otherwise.
At this moment `blocking` requests *can not* be cancelled.

## Advanced Configuration

### Request Configuration

* `baseHeaders` is to manage common HTTP header pairs in format of `Map<String, String>`.
  - The base headers are only applied if the request does not have those headers set.
    
```kotlin
FuelManager.instance.baseHeaders = mapOf("Device" to "Android")
```

* `Headers` can be added to a request via various methods including

```kotlin
fun header(name: String, value: Any): Request = request.header("foo", "a")
fun header(pairs: Map<String, Any>): Request = request.header(mapOf("foo" to "a"))
fun header(vararg pairs: Pair<String, Any>): Request = request.header("foo" to "a")

operator fun set(header: String, value: Collection<Any>): Request = request["foo"] = listOf("a", "b")
operator fun set(header: String, value: Any): Request = request["foo"] = "a"
```
    
* By default, all subsequent calls overwrite earlier calls, but you may use the `appendHeader` variant to append values to existing values.
  - In earlier versions (1.x.y), a `mapOf` overwrote, and `varargs pair` did not, but this was confusing. In 2.0, this issue has been fixed and improved so it works as expected.
  
```kotlin
fun appendHeader(header: String, value: Any): Request
fun appendHeader(header: String, vararg values: Any): Request
fun appendHeader(vararg pairs: Pair<String, Any>): Request
```

* Some of the HTTP headers are defined under `Headers.Companion` and can be used instead of literal strings. This is an encouraged way to configure your header in 2.x.y.

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

### Detail Configuration

* Use singleton `FuelManager.instance` to manage global configurations.

* Create separate managers using `FuelManager()`

* `basePath` is used to manage common root path. Great usage is for your static API endpoint.

```kotlin
FuelManager.instance.basePath = "https://httpbin.org"

// Later
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get because Fuel.{get|post|put|delete} use FuelManager.instance to make HTTP request
}
```

* `baseParams` is used to manage common `key=value` query param, which will be automatically included in all of your subsequent requests in format of ` Parameters` (`Any` is converted to `String` by `toString()` method)

```kotlin
FuelManager.instance.baseParams = listOf("api_key" to "1234567890")

// Later
Fuel.get("/get").response { request, response, result ->
    //make request to https://httpbin.org/get?api_key=1234567890
}
```

* `client` is a raw HTTP client driver. Generally, it is responsible to make [`Request`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Request.kt) into [`Response`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/core/Response.kt). Default is [`HttpClient`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/fuel/toolbox/HttpClient.kt) which is a thin wrapper over [`java.net.HttpUrlConnection`](https://developer.android.com/reference/java/net/HttpURLConnection.html). You could use any httpClient of your choice by conforming to [`client`](https://github.com/kittinunf/Fuel/blob/master/fuel/src/main/kotlin/com/github/kittinunf/fuel/core/Client.kt) protocol, and set back to `FuelManager.instance` to kick off the effect.

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

// Usage
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

```kotlin
// Router Definition
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
            Log.d("Success", json.array().toString())
        }, failure = { error ->
            Log.e("Failure", error.toString())
        })
    }
```
