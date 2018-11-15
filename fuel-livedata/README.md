
### LiveData Support

* Fuel supports [LiveData](https://developer.android.com/topic/libraries/architecture/livedata.html)
```kotlin
Fuel.get("www.example.com/get")
    .liveDataResponse()
    .observe(this) { /* do something */ }
```
