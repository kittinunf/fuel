package fuel.core

import java.util.concurrent.Executor

/**
 * Created by Kittinun Vantasin on 11/8/15.
 */

interface Environment {

    var callbackExecutor: Executor

}

public fun createEnvironment(): Environment {
    try {
        return Class.forName("util.AndroidEnvironment").newInstance() as Environment
    } catch(exception: ClassNotFoundException) {
        return DefaultEnvironment()
    }
}

public class DefaultEnvironment : Environment {

    override var callbackExecutor: Executor = object : Executor {

        override fun execute(command: Runnable?) {
            command?.run()
        }

    }

}

