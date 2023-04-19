package fuel

public suspend fun Fuel.get(
    url: String,
    parameters: Parameters? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse = loader().get(
    Request.Builder()
        .url(url)
        .parameters(parameters)
        .headers(headers)
        .build()
)

public suspend fun Fuel.post(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse = loader().post(
    Request.Builder()
        .url(url)
        .parameters(parameters)
        .headers(headers)
        .body(body)
        .build()
)

public suspend fun Fuel.put(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse = loader().put(
    Request.Builder()
        .url(url)
        .parameters(parameters)
        .headers(headers)
        .body(body)
        .build()
)

public suspend fun Fuel.patch(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse = loader().patch(
    Request.Builder()
        .url(url)
        .parameters(parameters)
        .headers(headers)
        .body(body)
        .build()
)

public suspend fun Fuel.delete(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse = loader().delete(
    Request.Builder()
        .url(url)
        .parameters(parameters)
        .headers(headers)
        .body(body)
        .build()
)

public suspend fun Fuel.head(
    url: String,
    parameters: Parameters? = null
): HttpResponse = loader().head(
    Request.Builder()
        .url(url)
        .parameters(parameters)
        .build()
)

public suspend fun Fuel.method(
    url: String,
    parameters: Parameters? = null,
    method: String? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse = loader().method(
    Request.Builder()
        .url(url)
        .parameters(parameters)
        .method(method)
        .headers(headers)
        .body(body)
        .build()
)

public suspend fun Fuel.request(convertible: RequestConvertible): HttpResponse =
    loader().method(convertible.request)
