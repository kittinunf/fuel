package fuel

import okhttp3.HttpUrl
import okhttp3.Response

public fun Fuel.getBlocking(uri: String): Response = httpLoader().getBlocking(Request.Builder().data(uri).build())

public fun Fuel.getBlocking(url: HttpUrl): Response = httpLoader().getBlocking(Request.Builder().data(url).build())
