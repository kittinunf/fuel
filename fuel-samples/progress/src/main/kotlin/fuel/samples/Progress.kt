package fuel.samples

import fuel.HttpLoader
import fuel.Request
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient

val progressListener = object : ProgressListener {
    var firstUpdate = true

    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        if (done) {
            println("completed")
        } else {
            if (firstUpdate) {
                firstUpdate = false
                if (contentLength == -1L) {
                    println("content-length: unknown")
                } else {
                    println("content-length: $contentLength")
                }
            }
            println(bytesRead)

            if (contentLength != -1L) {
                System.out.format("%d%% done\n", (100 * bytesRead) / contentLength)
            }
        }
    }
}

fun main() = runBlocking {
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor {
            val originalResponse = it.proceed(it.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body!!, progressListener))
                .build()
        }.build()
    val httpLoader = HttpLoader.Builder().okHttpClient(client).build()
    val url = "https://publicobject.com/helloworld.txt".toHttpUrlOrNull()
    val request = Request.Builder().data(url).build()
    val string = httpLoader.get(request).body!!.string()
    println(string)
}
