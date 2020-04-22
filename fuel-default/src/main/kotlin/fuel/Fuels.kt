// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/api/Coils.kt
@file:Suppress("unused")

package fuel

import okhttp3.HttpUrl
import okhttp3.RequestBody

suspend inline fun Fuel.get(
    uri: String
) = httpLoader().get(uri)

suspend inline fun Fuel.get(
    url: HttpUrl
) = httpLoader().get(url)

suspend inline fun Fuel.post(
    uri: String,
    requestBody: RequestBody
) = httpLoader().post(uri, requestBody)

suspend inline fun Fuel.post(
    url: HttpUrl,
    requestBody: RequestBody
) = httpLoader().post(url, requestBody)

suspend inline fun Fuel.put(
    uri: String,
    requestBody: RequestBody
) = httpLoader().put(uri, requestBody)

suspend inline fun Fuel.put(
    url: HttpUrl,
    requestBody: RequestBody
) = httpLoader().put(url, requestBody)

suspend inline fun Fuel.patch(
    url: HttpUrl,
    requestBody: RequestBody
) = httpLoader().patch(url, requestBody)

suspend inline fun Fuel.patch(
    uri: String,
    requestBody: RequestBody
) = httpLoader().patch(uri, requestBody)

suspend inline fun Fuel.delete(
    url: HttpUrl,
    requestBody: RequestBody?
) = httpLoader().delete(url, requestBody)

suspend inline fun Fuel.delete(
    uri: String,
    requestBody: RequestBody?
) = httpLoader().delete(uri, requestBody)

suspend inline fun Fuel.head(
    url: HttpUrl
) = httpLoader().head(url)

suspend inline fun Fuel.head(
    uri: String
) = httpLoader().head(uri)

suspend inline fun Fuel.method(
    url: HttpUrl,
    method: String?,
    requestBody: RequestBody?
) = httpLoader().method(url, method, requestBody)

suspend inline fun Fuel.method(
    uri: String,
    method: String?,
    requestBody: RequestBody?
) = httpLoader().method(uri, method, requestBody)

suspend inline fun Fuel.request(convertible: RequestConvertible) =
        httpLoader().method(convertible.request)
