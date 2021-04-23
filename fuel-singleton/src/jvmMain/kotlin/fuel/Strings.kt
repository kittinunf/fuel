@file:Suppress("unused")

package fuel

public suspend fun String.httpGet(): Any? = Fuel.get(this)

public suspend fun String.httpPost(body: String?): Any? = Fuel.post(this, body)

public suspend fun String.httpPut(body: String?): Any? = Fuel.put(this, body)

public suspend fun String.httpPatch(body: String?): Any? = Fuel.patch(this, body)

public suspend fun String.httpDelete(body: String?): Any? = Fuel.delete(this, body)

public suspend fun String.httpHead(): Any? = Fuel.head(this)

public suspend fun String.httpMethod(method: String, body: String?): Any? = Fuel.method(this, method, body)