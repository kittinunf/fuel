package fuel

import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

RunWith(CustomRobolectricGradleTestRunner::class)
Config(constants = BuildConfig::class, sdk = intArrayOf(21))
abstract class BaseTestCase {

    val DEFAULT_TIMEOUT = 15L

    abstract val numberOfTestCase: Int

    val countdown by Delegates.lazy { CountDownLatch(numberOfTestCase) }

    fun countdownWait(timeout: Long = DEFAULT_TIMEOUT) = countdown.await(timeout, TimeUnit.SECONDS)
    fun countdownFulfill() = countdown.countDown()

}