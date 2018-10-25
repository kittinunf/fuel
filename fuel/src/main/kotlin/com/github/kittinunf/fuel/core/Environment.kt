package com.github.kittinunf.fuel.core

import java.util.concurrent.Executor

interface Environment {
    var callbackExecutor: Executor
}

fun createEnvironment(): Environment = try {
    Class.forName(AndroidEnvironmentClass).newInstance() as Environment
} catch (exception: ClassNotFoundException) {
    DefaultEnvironment()
}

class DefaultEnvironment : Environment {
    override var callbackExecutor: Executor = Executor { command -> command?.run() }
}

const val AndroidEnvironmentClass = "com.github.kittinunf.fuel.android.util.AndroidEnvironment"
