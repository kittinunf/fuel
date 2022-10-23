package fuel

public suspend fun String.httpGet(): HttpResponse = Fuel.get(this)

public suspend fun String.httpPost(body: String?, headers: Map<String, String>): HttpResponse =
    Fuel.post(this, body, headers)

public suspend fun String.httpPut(body: String?, headers: Map<String, String>): HttpResponse =
    Fuel.put(this, body, headers)

public suspend fun String.httpPatch(body: String?, headers: Map<String, String>): HttpResponse =
    Fuel.patch(this, body, headers)

public suspend fun String.httpDelete(body: String?, headers: Map<String, String>): HttpResponse =
    Fuel.delete(this, body, headers)

public suspend fun String.httpHead(): HttpResponse = Fuel.head(this)

public suspend fun String.httpMethod(method: String, body: String?, headers: Map<String, String>): HttpResponse =
    Fuel.method(this, method, body, headers)
