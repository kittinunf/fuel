package fuel.ktor

import io.ktor.client.HttpClient
import org.junit.Assert
import org.junit.Test

class GenericTest {
    @Test
    fun `HttpClient without parameter for not null`() {
        val httpClient = HttpClient()
        Assert.assertNotNull(httpClient)
    }

    @Test
    fun `get container name`() {
        val fuelEngineContainer = FuelEngineContainer()
        Assert.assertEquals("Fuel", fuelEngineContainer.toString())
    }
}