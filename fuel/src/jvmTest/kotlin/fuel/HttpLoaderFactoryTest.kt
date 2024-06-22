package fuel

import org.junit.Assert.assertFalse
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

class HttpLoaderFactoryTest {
    @Test
    fun test_checkHttpLoaderInvokedOnce() {
        val httpLoader = FuelBuilder().build()

        val isInitialized = AtomicBoolean(false)
        Fuel.setHttpLoader {
            check(!isInitialized.getAndSet(true)) { "newHttpLoader was invoked more than once." }
            httpLoader
        }

        assertFalse(isInitialized.get())
    }
}
