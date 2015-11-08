package fuel

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

abstract class BaseTestCase {

    val DEFAULT_TIMEOUT = 15L

    var lock: CountDownLatch by Delegates.notNull()

    fun await(seconds: Long = DEFAULT_TIMEOUT) {
        lock.await(seconds, TimeUnit.SECONDS);
    }

}