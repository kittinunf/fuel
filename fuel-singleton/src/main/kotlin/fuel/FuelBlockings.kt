package fuel

import okhttp3.HttpUrl
import okhttp3.Response

public fun Fuel.getBlocking(uri: String): Response = loader().get(Request.Builder().data(uri).build()).response()

public fun Fuel.getBlocking(url: HttpUrl): Response = loader().get(Request.Builder().data(url).build()).response()
