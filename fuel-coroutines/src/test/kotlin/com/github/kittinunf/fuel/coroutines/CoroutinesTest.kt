import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.ResponseDeserializable
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CoroutinesTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 30000
        }
    }

    @Test
    fun testItCanAwaitForAnError() = runBlocking {
        try {
            Fuel.get("/not/found/address").awaitStringResponse()
            fail("This test should fail due to status code 404")
        } catch (exception: HttpException) {
            assertNotNull(exception)
            assertTrue(exception.message!!.contains("HTTP Exception 404"))
        }
    }

    @Test
    fun testItCanAwaitString() = runBlocking {
        Fuel.get("/uuid").awaitStringResponse().third
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
        Fuel.get("/uuid").awaitObjectResponse(UUIDResponseDeserializer).third
                .fold({ data ->
                    assertTrue(data.uuid.isNotEmpty())
                }, { error ->
                    fail("This test should pass but got an error: ${error.message}")
                })
    }

    @Test
    fun testItCanAwaitForObjectResult() = runBlocking {
        assertTrue(Fuel.get("/uuid").awaitForObject(UUIDResponseDeserializer).uuid.isNotEmpty())
    }

    @Test
    fun testItCanAwaitResponseResult() = runBlocking {
        assertTrue(Fuel.get("/uuid").awaitForByteArray().isNotEmpty())
    }

    @Test
    fun testItCanAwaitForStringResult() = runBlocking {
        assertTrue(Fuel.get("/uuid").awaitForString().isNotEmpty())
    }

    @Test
    fun testItCanAwaitForStringResultCanThrowException() = runBlocking {
        try {
            Fuel.get("/error/404").awaitForString()
            fail("This test should fail due to status code 404")
        } catch (exception: HttpException) {
            assertNotNull(exception)
        }
    }

    @Test
    fun testAwaitSafelyPassesObject() = runBlocking {
        Fuel.get("/uuid").awaitForObjectResult(UUIDResponseDeserializer)
                .fold({ data ->
                    assertTrue(data.uuid.isNotEmpty())
                }, { error ->
                    fail("This test should pass but got an error: ${error.message}")
                })
    }

    @Test
    fun testAwaitSafelyCatchesError() = runBlocking {
        try {
            Fuel.get("/error/404").awaitForObjectResult(UUIDResponseDeserializer)
                    .fold({ _ ->
                        fail("This is an error case!")
                    }, { error ->
                        assertTrue( error.exception is HttpException)
                    })
        } catch (e: Exception) {
            fail("this should have been caught")
        }
    }

    @Test
    fun testAwaitSafelyCatchesDeserializeError() = runBlocking {
        try {
            Fuel.get("/ip").awaitForObjectResult(UUIDResponseDeserializer)
                    .fold({ _ ->
                        fail("This is an error case!")
                    }, { error ->
                        assertNotNull(error)
                    })
        } catch (e: Exception) {
            fail("this should have been caught")
        }
    }
}
