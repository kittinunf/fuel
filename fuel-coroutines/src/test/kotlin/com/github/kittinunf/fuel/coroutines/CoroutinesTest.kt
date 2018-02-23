import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.ResponseDeserializable
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CoroutinesTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 15000
        }
    }

    @Test
    fun testItCanAwaitForAnError() = runBlocking {
        try {
            Fuel.get("/not/found/address").awaitString()
            fail("This test should fail due to status code 404")
        } catch (exception: HttpException) {
            assertNotNull(exception)
            assertEquals("HTTP Exception 404 NOT FOUND", exception.message)
        }
    }

    @Test
    fun testItCanAwaitString() = runBlocking {
        Fuel.get("/uuid").awaitString().third
            .fold({ data ->
                assertTrue(data.isNotEmpty())
                assertTrue(data.contains("uuid"))
            }, { error ->
                fail("This test should pass but got an error: ${error.message}")
            })
    }

    @Test
    fun testItCanAwaitByteArray() = runBlocking {
        Fuel.get("/ip").awaitResponse().third
            .fold({ data ->
                assertTrue(data.isNotEmpty())
            }, { error ->
                fail("This test should pass but got an error: ${error.message}")
            })
    }

    private data class UUIDResponse(val uuid: String)

    private object UUIDResponseDeserializer : ResponseDeserializable<UUIDResponse> {
        override fun deserialize(content: String) =
            jacksonObjectMapper().readValue<UUIDResponse>(content)
    }

    @Test
    fun testItCanAwaitAnyObject() = runBlocking {
        Fuel.get("/uuid").awaitObject(UUIDResponseDeserializer).third
            .fold({ data ->
                assertTrue(data.uuid.isNotEmpty())
            }, { error ->
                fail("This test should pass but got an error: ${error.message}")
            })
    }

    @Test
    fun testItCanAwaitForResultsOnly() = runBlocking {
        with(Fuel.get("/uuid")) {
            assertTrue(awaitResponseResult().isNotEmpty())
            assertTrue(awaitStringResult().isNotEmpty())
            assertTrue(awaitObjectResult(UUIDResponseDeserializer).uuid.isNotEmpty())
        }

        try {
            Fuel.get("/error/404").awaitStringResult()
            fail("This test should fail due to status code 404")
        } catch (exception: HttpException) {
            assertNotNull(exception)
        }
    }
}
