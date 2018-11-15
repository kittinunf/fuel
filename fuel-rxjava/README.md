
### RxJava Support

* Fuel supports [RxJava](https://github.com/ReactiveX/RxJava) right off the box.
    ```kotlin
    "https://www.example.com/photos/1".httpGet()
      .toRxObject(Photo.Deserializer())
      .subscribe { /* do something */ }
    ```

* There are 6 extensions over `Request` that provide RxJava 2.x `Single<Result<T, FuelError>>` as return type.
    ```kotlin
    fun Request.toRxResponse(): Single<Pair<Response, Result<ByteArray, FuelError>>>
    fun Request.toRxResponseString(charset: Charset): Single<Pair<Response, Result<String, FuelError>>>
    fun <T : Any> Request.toRxResponseObject(deserializable: Deserializable<T>): Single<Pair<Response, Result<T, FuelError>>>

    fun Request.toRxData(): Single<Result<ByteArray, FuelError>>
    fun Request.toRxString(charset: Charset): Single<Result<String, FuelError>>
    fun <T : Any> Request.toRxObject(deserializable: Deserializable<T>): Single<Result<T, FuelError>>
    ```
