package fuel

public suspend fun String.httpGet(): HttpResponse = Fuel.get(this)

public suspend fun String.httpPost(body: String?): HttpResponse = Fuel.post(this, body)

public suspend fun String.httpPut(body: String?): HttpResponse = Fuel.put(this, body)

public suspend fun String.httpPatch(body: String?): HttpResponse = Fuel.patch(this, body)

public suspend fun String.httpDelete(body: String?): HttpResponse = Fuel.delete(this, body)

public suspend fun String.httpHead(): HttpResponse = Fuel.head(this)

public suspend fun String.httpMethod(method: String, body: String?): HttpResponse = Fuel.method(this, method, body)