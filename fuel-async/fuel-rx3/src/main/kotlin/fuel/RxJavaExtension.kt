package fuel

import io.reactivex.rxjava3.core.Single
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

public fun Call.toSingle(): Single<Response> =
    Single.defer {
        try {
            Single.just(execute())
        } catch (ioe: IOException) {
            Single.error(ioe)
        }
    }.doOnDispose {
        cancel()
    }
