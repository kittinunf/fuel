// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/api/Coils.kt
@file:Suppress("unused")

package fuel

import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.RequestBody

public fun Fuel.get(uri: String): Call = loader().get(Request.Builder().data(uri).build())
public fun Fuel.get(url: HttpUrl): Call = loader().get(Request.Builder().data(url).build())
public fun Fuel.post(
    uri: String,
    requestBody: RequestBody
): Call = loader().post(Request.Builder().data(uri).requestBody(requestBody).build())

public fun Fuel.post(
    url: HttpUrl,
    requestBody: RequestBody
): Call = loader().post(Request.Builder().data(url).requestBody(requestBody).build())

public fun Fuel.put(
    uri: String,
    requestBody: RequestBody
): Call = loader().put(Request.Builder().data(uri).requestBody(requestBody).build())

public fun Fuel.put(
    url: HttpUrl,
    requestBody: RequestBody
): Call = loader().put(Request.Builder().data(url).requestBody(requestBody).build())

public fun Fuel.patch(
    url: HttpUrl,
    requestBody: RequestBody
): Call = loader().patch(Request.Builder().data(url).requestBody(requestBody).build())

public fun Fuel.patch(
    uri: String,
    requestBody: RequestBody
): Call = loader().patch(Request.Builder().data(uri).requestBody(requestBody).build())

public fun Fuel.delete(
    url: HttpUrl,
    requestBody: RequestBody?
): Call = loader().delete(Request.Builder().data(url).requestBody(requestBody).build())

public fun Fuel.delete(
    uri: String,
    requestBody: RequestBody?
): Call = loader().delete(Request.Builder().data(uri).requestBody(requestBody).build())

public fun Fuel.head(url: HttpUrl): Call = loader().head(Request.Builder().data(url).build())

public fun Fuel.head(uri: String): Call = loader().head(Request.Builder().data(uri).build())

public fun Fuel.method(
    url: HttpUrl,
    method: String?,
    requestBody: RequestBody?
): Call = loader().method(Request.Builder().data(url).method(method).requestBody(requestBody).build())

public fun Fuel.method(
    uri: String,
    method: String?,
    requestBody: RequestBody?
): Call = loader().method(Request.Builder().data(uri).method(method).requestBody(requestBody).build())

public fun Fuel.request(convertible: RequestConvertible): Call = loader().method(convertible.request)
