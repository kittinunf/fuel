package com.github.kittinunf.fuel.android.util

import android.os.Handler
import android.os.Looper
import com.github.kittinunf.fuel.core.Environment
import java.util.concurrent.Executor

internal class AndroidEnvironment : Environment {
    val handler = Handler(Looper.getMainLooper())

    override var callbackExecutor: Executor = Executor { command -> handler.post(command) }

}