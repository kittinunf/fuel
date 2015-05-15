package fuel.core

import fuel.Fuel
import fuel.toolbox.HttpClient
import fuel.util.readWriteLazy
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/14/15.
 */

public class Manager {

    companion object Singleton {
        var sharedInstance by Delegates.readWriteLazy {
            val manager = Manager()
            manager.client = HttpClient()
            manager
        }
    }

    var client: Client by Delegates.notNull()

    public fun request(m: Method, path: String): Request {
        return Request {
            method = m
            urlString = path
        }
    }

    public fun request(m: Method, convertible: Fuel.StringConvertible): Request {
        return Request {
            method = m
            urlString = convertible.path
        }
    }

    public fun request(convertible: Fuel.RequestConvertible): Request {
        return convertible.request
    }

}

public inline fun Manager(builder: Manager.() -> Unit): Manager {
    val manager = Manager()
    manager.builder()
    return manager
}

object Executor {

    private var executor: ExecutorService = Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

    fun execute(f: Runnable) {
        executor.execute(f)
    }

}
