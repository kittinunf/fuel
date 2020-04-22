// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/api/ImageLoaders.kt

package fuel

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody

suspend inline fun HttpLoader.get(
    uri: String
) = get(Request.Builder().data(uri.toHttpUrlOrNull()).build())

suspend inline fun HttpLoader.get(
    url: HttpUrl
) = get(Request.Builder().data(url).build())

suspend inline fun HttpLoader.post(
    uri: String,
    requestBody: RequestBody
) = post(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun HttpLoader.post(
    url: HttpUrl,
    requestBody: RequestBody
) = post(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun HttpLoader.put(
    uri: String,
    requestBody: RequestBody
) = put(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun HttpLoader.put(
    url: HttpUrl,
    requestBody: RequestBody
) = put(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun HttpLoader.patch(
    uri: String,
    requestBody: RequestBody
) = patch(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun HttpLoader.patch(
    url: HttpUrl,
    requestBody: RequestBody
) = patch(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun HttpLoader.delete(
    uri: String,
    requestBody: RequestBody?
) = delete(Request.Builder().data(uri.toHttpUrlOrNull()).requestBody(requestBody).build())

suspend inline fun HttpLoader.delete(
    url: HttpUrl,
    requestBody: RequestBody?
) = delete(Request.Builder().data(url).requestBody(requestBody).build())

suspend inline fun HttpLoader.head(
    uri: String
) = head(Request.Builder().data(uri.toHttpUrlOrNull()).build())

suspend inline fun HttpLoader.head(
    url: HttpUrl
) = head(Request.Builder().data(url).build())

suspend inline fun HttpLoader.method(
    uri: String,
    method: String?,
    requestBody: RequestBody?
) = method(Request.Builder().data(uri.toHttpUrlOrNull()).method(method).requestBody(requestBody).build())

suspend inline fun HttpLoader.method(
    url: HttpUrl,
    method: String?,
    requestBody: RequestBody?
) = method(Request.Builder().data(url).method(method).requestBody(requestBody).build())
