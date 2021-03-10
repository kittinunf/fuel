package fuel

import io.reactivex.rxjava3.core.Single
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

public fun Call.toSingle(): Single<Response> =
    Single.create {
        val newCall = clone()
        try {
            if (it.isDisposed.not()) {
                it.onSuccess(newCall.execute())
            }
        } catch (ioe: IOException) {
            it.onError(ioe)
        }

        it.setCancellable {
            newCall.cancel()
        }
    }
