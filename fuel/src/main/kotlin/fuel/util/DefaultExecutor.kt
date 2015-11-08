package fuel.util

import java.util.concurrent.Executor

/**
 * Created by Yoav Sternberg on 11/2/15.
 */

class DefaultExecutor : Executor {

    override fun execute(command: Runnable) {
        command.run()
    }

}