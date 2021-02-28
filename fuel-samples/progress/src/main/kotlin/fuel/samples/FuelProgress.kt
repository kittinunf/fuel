package fuel.samples

import fuel.FuelBuilder
import fuel.Request
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import kotlin.system.exitProcess

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

fun main() {
    runBlocking {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor {
                val originalResponse = it.proceed(it.request())
                originalResponse.newBuilder()
                    .body(ProgressResponseBody(originalResponse.body!!, progressListener))
                    .build()
            }.build()
        val httpLoader = FuelBuilder().okHttpClient(client).build()
        val request = Request.Builder().data("https://publicobject.com/helloworld.txt").build()

        val string = httpLoader.get(request).body!!.string()
        println(string)
    }
    exitProcess(0)
}
