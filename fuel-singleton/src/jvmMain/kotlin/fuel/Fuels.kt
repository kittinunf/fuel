// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/api/Coils.kt
@file:Suppress("unused")

package fuel

public suspend fun Fuel.get(url: String): Any? = loader().get(Request.Builder().url(url).build())
public suspend fun Fuel.post(
    url: String,
    body: String?
): Any? = loader().post(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.put(
    url: String,
    body: String?
): Any? = loader().put(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.patch(
    url: String,
    body: String?
): Any? = loader().patch(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.delete(
    url: String,
    body: String?
): Any? = loader().delete(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.head(uri: String): Any? = loader().head(Request.Builder().url(uri).build())

public suspend fun Fuel.method(
    url: String,
    method: String?,
    body: String?
): Any? = loader().method(Request.Builder().url(url).method(method).body(body).build())

public suspend fun Fuel.request(convertible: RequestConvertible): Any? = loader().method(convertible.request)
