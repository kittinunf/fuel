package fuel

public suspend fun Fuel.get(url: String): HttpResponse = loader().get(Request.Builder().url(url).build())
public suspend fun Fuel.post(
    url: String,
    body: String?
): HttpResponse = loader().post(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.put(
    url: String,
    body: String?
): HttpResponse = loader().put(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.patch(
    url: String,
    body: String?
): HttpResponse = loader().patch(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.delete(
    url: String,
    body: String?
): HttpResponse = loader().delete(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.head(uri: String): HttpResponse = loader().head(Request.Builder().url(uri).build())

public suspend fun Fuel.method(
    url: String,
    method: String?,
    body: String?
): HttpResponse = loader().method(Request.Builder().url(url).method(method).body(body).build())

public suspend fun Fuel.request(convertible: RequestConvertible): HttpResponse = loader().method(convertible.request)
