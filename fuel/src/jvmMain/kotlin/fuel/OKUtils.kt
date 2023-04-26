package fuel

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun Call.await(): HttpResponse = suspendCancellableCoroutine {
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            it.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            val httpResponse = HttpResponse().apply {
                statusCode = response.code
                source = response.body.source()
                body = response.body.string()
            }
            it.resume(httpResponse)
        }
    })

    it.invokeOnCancellation {
        cancel()
    }
}
