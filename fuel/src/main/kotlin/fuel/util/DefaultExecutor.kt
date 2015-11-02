package fuel.util

import java.util.concurrent.Executor

class DefaultExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}