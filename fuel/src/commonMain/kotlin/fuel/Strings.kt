package fuel

import kotlinx.coroutines.flow.Flow

public suspend fun String.httpGet(
    parameters: Parameters? = null,
    headers: Map<String, String> = emptyMap(),
): HttpResponse = Fuel.get(this, parameters, headers)

public suspend fun String.httpPost(
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): HttpResponse = Fuel.post(this, parameters, body, headers)

public suspend fun String.httpPut(
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): HttpResponse = Fuel.put(this, parameters, body, headers)

public suspend fun String.httpPatch(
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): HttpResponse = Fuel.patch(this, parameters, body, headers)

public suspend fun String.httpDelete(
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): HttpResponse = Fuel.delete(this, parameters, body, headers)

public suspend fun String.httpHead(parameters: Parameters? = null): HttpResponse = Fuel.head(this, parameters)

public suspend fun String.httpSSE(
    parameters: Parameters?,
    headers: Map<String, String> = emptyMap(),
): Flow<String> = Fuel.sse(this, parameters, headers)

public suspend fun String.httpMethod(
    parameters: Parameters? = null,
    method: String,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): HttpResponse = Fuel.method(this, parameters, method, body, headers)
