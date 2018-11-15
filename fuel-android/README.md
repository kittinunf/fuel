

### Response in Json
_requires the [android extension](#dependency---fuel-android)_
```kotlin
fun responseJson(handler: (Request, Response, Result<Json, FuelError>) -> Unit)

val jsonObject = json.obj() //JSONObject
val jsonArray = json.array() //JSONArray
```
