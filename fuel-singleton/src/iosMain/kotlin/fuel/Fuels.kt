@file:Suppress("unused")

package fuel

import kotlinx.coroutines.flow.Flow

public suspend fun Fuel.get(url: String): Flow<Any?> = get(Request.Builder().url(url).build())

public suspend fun Fuel.post(url: String, body: String?): Flow<Any?> = post(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.put(url: String, body: String?): Flow<Any?> = put(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.patch(url: String, body: String?): Flow<Any?> = patch(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.delete(url: String, body: String?): Flow<Any?> = delete(Request.Builder().url(url).body(body).build())

public suspend fun Fuel.head(url: String): Flow<Any?> = head(Request.Builder().url(url).build())

public suspend fun Fuel.method(url: String, body: String?, method: String?): Flow<Any?> =
    method(Request.Builder().url(url).body(body).method(method).build())

public suspend fun Fuel.request(convertible: RequestConvertible): Flow<Any?> = method(convertible.request)
