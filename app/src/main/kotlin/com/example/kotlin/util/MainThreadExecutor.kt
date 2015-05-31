package com.example.kotlin.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

/**
 * Created by Kittinun Vantasin on 5/29/15.
 */

class MainThreadExecutor : Executor {

    val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        handler.post(command)
    }

}
