package fuel

import okhttp3.HttpUrl
import okhttp3.RequestBody
import okhttp3.Response

public fun Fuel.getBlocking(uri: String): Response = loader().get(Request.Builder().data(uri).build()).execute()

public fun Fuel.getBlocking(url: HttpUrl): Response = loader().get(Request.Builder().data(url).build()).execute()

public fun Fuel.postBlocking(uri: String, requestBody: RequestBody): Response = loader().post(Request.Builder().data(uri).requestBody(requestBody).build()).execute()

public fun Fuel.postBlocking(url: HttpUrl, requestBody: RequestBody): Response = loader().post(Request.Builder().data(url).requestBody(requestBody).build()).execute()

public fun Fuel.putBlocking(uri: String, requestBody: RequestBody): Response = loader().put(Request.Builder().data(uri).requestBody(requestBody).build()).execute()

public fun Fuel.putBlocking(url: HttpUrl, requestBody: RequestBody): Response = loader().put(Request.Builder().data(url).requestBody(requestBody).build()).execute()

public fun Fuel.patchBlocking(uri: String, requestBody: RequestBody): Response = loader().patch(Request.Builder().data(uri).requestBody(requestBody).build()).execute()

public fun Fuel.patchBlocking(url: HttpUrl, requestBody: RequestBody): Response = loader().patch(Request.Builder().data(url).requestBody(requestBody).build()).execute()

public fun Fuel.deleteBlocking(url: HttpUrl, requestBody: RequestBody?): Response = loader().delete(Request.Builder().data(url).requestBody(requestBody).build()).execute()

public fun Fuel.deleteBlocking(uri: String, requestBody: RequestBody?): Response = loader().delete(Request.Builder().data(uri).requestBody(requestBody).build()).execute()
