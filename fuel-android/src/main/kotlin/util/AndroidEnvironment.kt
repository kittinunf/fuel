package util

import android.os.Handler
import android.os.Looper
import fuel.core.Environment
import java.util.concurrent.Executor

/**
 * Created by Kittinun Vantasin on 11/9/15.
 */

internal class AndroidEnvironment : Environment {

    val handler = Handler(Looper.getMainLooper())

    override var callbackExecutor: Executor = object : Executor {

        override fun execute(command: Runnable?) {
            handler.post(command)
        }

    }

}