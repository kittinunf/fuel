@file:Suppress("unused")

package fuel

public suspend fun Fuel.get(url: String): Any? = get(Request.Builder().url(url).build())

public suspend fun Fuel.post(url: String, body: String?): Any? = post(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.put(url: String, body: String?): Any? = put(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.patch(url: String, body: String?): Any? = patch(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.delete(url: String, body: String?): Any? = delete(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.head(url: String): Any? = head(Request.Builder().url(url).build())

public suspend fun Fuel.method(url: String, body: String?, method: String?): Any? =
    method(Request.Builder().url(url).body(body).method(method).build())

public suspend fun Fuel.request(convertible: RequestConvertible): Any? = method(convertible.request)
