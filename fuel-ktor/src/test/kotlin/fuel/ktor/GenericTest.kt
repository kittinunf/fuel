package fuel.ktor

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.lang.IllegalArgumentException
import java.net.SocketTimeoutException

class GenericTest {
    @Test
    fun `HttpClient without parameter for not null`() {
        val httpClient = HttpClient()
        assertNotNull(httpClient)
    }

    @Test
    fun `get container name`() {
        val fuelEngineContainer = FuelEngineContainer()
        assertEquals("Fuel", fuelEngineContainer.toString())
    }

    @Test
    fun `setting connect timeout for not null`() {
        val client = HttpClient {
            install(HttpTimeout) {
                connectTimeoutMillis = 3000
            }
        }
        assertNotNull(client)
    }

    @Test
    fun `setting request timeout for not null`() = runBlocking {
        val client = HttpClient {
            install(HttpTimeout)
        }
        val response = client.get<String>("https://www.google.com") {
            timeout {
                requestTimeoutMillis = 1300
            }
        }
        assertNotNull(response)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws error for HttpTimeout Feature not installed`() = runBlocking {
        val client = HttpClient()
        client.get<String>("https://www.google.com") {
            timeout {}
        }
        assertNotNull(client)
    }

    @Test
    fun `setting socket timeout for not null`() = runBlocking {
        val client = HttpClient {
            install(HttpTimeout)
        }
        val response = client.get<String>("https://www.google.com") {
            parameter("delay", "5000")
            timeout { socketTimeoutMillis = 1000 }
        }
        assertNotNull(response)
    }

    @Test(expected = SocketTimeoutException::class)
    fun testConnectTimeoutPerRequestAttributes() = runBlocking {
        val client = HttpClient {
            install(HttpTimeout)
        }
        val response = client.get<String>("http://www.google.com:81") {
            timeout { connectTimeoutMillis = 1000 }
        }
        assertNotNull(response)
    }
}
