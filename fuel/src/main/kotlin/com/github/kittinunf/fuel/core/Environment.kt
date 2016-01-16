package com.github.kittinunf.fuel.core

import java.util.concurrent.Executor

/**
 * Created by Kittinun Vantasin on 11/8/15.
 */

interface Environment {
    var callbackExecutor: Executor
}

public fun createEnvironment(): Environment {
    try {
        return Class.forName(AndroidEnvironmentClass).newInstance() as Environment
    } catch(exception: ClassNotFoundException) {
        return DefaultEnvironment()
    }
}

public class DefaultEnvironment : Environment {
    override var callbackExecutor: Executor = Executor { command -> command?.run() }
}

const val AndroidEnvironmentClass = "com.github.kittinunf.fuel.android.util.AndroidEnvironment"

