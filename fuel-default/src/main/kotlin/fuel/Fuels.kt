// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/api/Coils.kt
@file:Suppress("unused")

package fuel

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody

suspend inline fun Fuel.get(
    uri: String
) = httpLoader().get(Request.Builder().data(uri.toHttpUrlOrNull()).build())

suspend inline fun Fuel.get(
    url: HttpUrl
) = httpLoader().get(Request.Builder().data(url).build())

suspend inline fun Fuel.post(
    uri: String,
    requestBody: RequestBody
) = httpLoader().post(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun Fuel.post(
    url: HttpUrl,
    requestBody: RequestBody
) = httpLoader().post(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun Fuel.put(
    uri: String,
    requestBody: RequestBody
) = httpLoader().put(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun Fuel.put(
    url: HttpUrl,
    requestBody: RequestBody
) = httpLoader().put(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun Fuel.patch(
    url: HttpUrl,
    requestBody: RequestBody
) = httpLoader().patch(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun Fuel.patch(
    uri: String,
    requestBody: RequestBody
) = httpLoader().patch(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun Fuel.delete(
    url: HttpUrl,
    requestBody: RequestBody?
) = httpLoader().delete(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun Fuel.delete(
    uri: String,
    requestBody: RequestBody?
) = httpLoader().delete(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun Fuel.head(
    url: HttpUrl
) = httpLoader().head(Request.Builder().data(url).build())

suspend inline fun Fuel.head(
    uri: String
) = httpLoader().head(Request.Builder().data(uri.toHttpUrlOrNull()).build())

suspend inline fun Fuel.method(
    url: HttpUrl,
    method: String?,
    requestBody: RequestBody?
) = httpLoader().method(Request.Builder().data(url).method(method).requestBody(requestBody).build())

suspend inline fun Fuel.method(
    uri: String,
    method: String?,
    requestBody: RequestBody?
) = httpLoader().method(Request.Builder().data(uri.toHttpUrlOrNull()).method(method).requestBody(requestBody).build())

suspend inline fun Fuel.request(convertible: RequestConvertible) =
    httpLoader().method(convertible.request)
