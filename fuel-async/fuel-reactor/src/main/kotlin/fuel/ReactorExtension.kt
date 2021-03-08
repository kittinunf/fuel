package fuel

import okhttp3.Call
import okhttp3.Response
import reactor.core.publisher.Mono
import java.io.IOException

public fun Call.toMono(): Mono<Response> =
    Mono.defer {
        try {
            Mono.just(execute())
        } catch (ioe: IOException) {
            Mono.error(ioe)
        }
    }
