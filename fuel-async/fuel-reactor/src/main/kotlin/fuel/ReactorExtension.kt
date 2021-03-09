package fuel

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import reactor.core.publisher.Mono
import java.io.IOException

public fun Call.toMono(): Mono<Response> =
    Mono.create {
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.error(e)
            }

            override fun onResponse(call: Call, response: Response) {
                it.success(response)
            }
        })
        it.onCancel { cancel() }
    }
