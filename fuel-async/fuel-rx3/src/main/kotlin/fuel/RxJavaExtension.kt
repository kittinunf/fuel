package fuel

import io.reactivex.rxjava3.core.Single
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

public fun Call.toSingle(): Single<Response> =
    Single.create {
        try {
            it.onSuccess(execute())
        } catch (ioe: IOException) {
            it.onError(ioe)
        }
        it.setCancellable { cancel() }
    }
