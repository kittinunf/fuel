package fuel

import okhttp3.HttpUrl
import okhttp3.Response

public fun Fuel.getBlocking(uri: String): Response = httpLoader().getCall(Request.Builder().data(uri).build()).execute()

public fun Fuel.getBlocking(url: HttpUrl): Response = httpLoader().getCall(Request.Builder().data(url).build()).execute()
