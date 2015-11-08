package util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

/**
 * Created by Kittinun Vantasin on 11/8/15.
 */

public class AndroidMainThreadExecutor : Executor {

    val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable?) {
        handler.post(command)
    }

}