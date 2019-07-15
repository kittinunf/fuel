# fuel-rxjava

The rxjava extension package for [`Fuel`](../README.md).

## Installation

You can [download](https://bintray.com/kittinunf/maven/Fuel-Android/_latestVersion) and install `fuel-rxjava` with `Maven` and `Gradle`. The rxjava package has the following dependencies:
* [`Fuel`](../fuel/README.md)
* RxJava: 2.2.6

```groovy
compile 'com.github.kittinunf.fuel:fuel:<latest-version>'
compile 'com.github.kittinunf.fuel:fuel-rxjava:<latest-version>'
```

## Usage

See `RxFuel.kt`

### Responses

* Fuel supports [RxJava](https://github.com/ReactiveX/RxJava) right off the box.
    ```kotlin
    "https://www.example.com/photos/1".httpGet()
      .rxObject(Photo.Deserializer())
      .subscribe { /* do something */ }
    ```

* There are extensions over `Request` that provide RxJava 2.x `Single<Result<T, FuelError>>` as return type.

```kotlin    
/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxBytes
 * @return [Single<T>] the [ByteArray] wrapped into a [Pair] and [Result]
 */
fun Request.rxResponse() = rxResponseSingle(ByteArrayDeserializer())
fun Request.rxResponsePair() = rxResponsePair(ByteArrayDeserializer())
fun Request.rxResponseTriple() = rxResponseTriple(ByteArrayDeserializer())

/**
 * Returns a reactive stream for a [Single] value response [String]
 *
 * @see rxString
 *
 * @param charset [Charset] the character set to deserialize with
 * @return [Single<Pair<Response, Result<String, FuelError>>>] the [String] wrapped into a [Pair] and [Result]
 */
fun Request.rxResponseString(charset: Charset = Charsets.UTF_8) = rxResponseSingle(StringDeserializer(charset))
fun Request.rxResponseStringPair(charset: Charset = Charsets.UTF_8) = rxResponsePair(StringDeserializer(charset))
fun Request.rxResponseStringTriple(charset: Charset = Charsets.UTF_8) = rxResponseTriple(StringDeserializer(charset))

/**
 * Returns a reactive stream for a [Single] value response object [T]
 *
 * @see rxObject
 *
 * @param deserializable [Deserializable<T>] something that can deserialize the [Response] to a [T]
 * @return [Single<Pair<Response, Result<T, FuelError>>>] the [T] wrapped into a [Pair] and [Result]
 */
fun <T : Any> Request.rxResponseObject(deserializable: Deserializable<T>) = rxResponseSingle(deserializable)
fun <T : Any> Request.rxResponseObjectPair(deserializable: Deserializable<T>) = rxResponsePair(deserializable)
fun <T : Any> Request.rxResponseObjectTriple(deserializable: Deserializable<T>) = rxResponseTriple(deserializable)

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxResponse
 * @return [Single<Result<ByteArray, FuelError>>] the [ByteArray] wrapped into a [Result]
 */
fun Request.rxBytes() = rxResult(ByteArrayDeserializer())

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxResponseString
 *
 * @param charset [Charset] the character set to deserialize with
 * @return [Single<Result<String, FuelError>>] the [String] wrapped into a [Result]
 */
fun Request.rxString(charset: Charset = Charsets.UTF_8) = rxResult(StringDeserializer(charset))

/**
 * Returns a reactive stream for a [Single] value response [T]
 *
 * @see rxResponseObject
 *
 * @param deserializable [Deserializable<T>] something that can deserialize the [Response] to a [T]
 * @return [Single<Result<T, FuelError>>] the [T] wrapped into a [Result]
 */
fun <T : Any> Request.rxObject(deserializable: Deserializable<T>) = rxResult(deserializable)
```
