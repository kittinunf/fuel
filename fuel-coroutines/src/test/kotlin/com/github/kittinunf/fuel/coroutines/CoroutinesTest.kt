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
    fun testItAwaitResponseByteArray() = runBlocking {
        Fuel.get("/ip").awaitResponse().third
                .fold({ data ->
                    assertTrue(data.isNotEmpty())
                }, { error ->
                    fail("This test should pass but got an error: ${error.message}")
                })
    }

    @Test
    fun testAwaitResponseDoesNotThrowException() = runBlocking {
        try {
            Fuel.get("/error/404").awaitResponse().third.fold({
                fail("This should not be called")
            }, {
                assertTrue(it.exception is HttpException)
            })
        } catch (exception: Exception) {
            fail("This test should fail as exception should be caught")
        }
    }

    @Test
    fun testAwaitStringResponseDoesNotThrowException() = runBlocking {
        try {
            Fuel.get("/not/found/address").awaitStringResponse().third.fold({
                fail("This should not be called")
            }, {
                assertNotNull(it.exception is HttpException)
            })
        } catch (exception: Exception) {
            fail("This test should fail as exception should be caught")
        }
    }

    @Test
    fun testAwaitForByteArrayResult() = runBlocking {
        Fuel.get("/ip").awaitForByteArrayResult()
                .fold({ data ->
                    assertTrue(data.isNotEmpty())
                }, { error ->
                    fail("This test should pass but got an error: ${error.message}")
                })
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
    fun testItCanAwaitAnyObject() = runBlocking {
        Fuel.get("/uuid").awaitObjectResponse(UUIDResponseDeserializer).third
                .fold({ data ->
                    assertTrue(data.uuid.isNotEmpty())
                }, { error ->
                    fail("This test should pass but got an error: ${error.message}")
                })
    }

    @Test
    fun testItCanAwaitStringResult() = runBlocking {
        Fuel.get("/uuid").awaitForStringResult()
                .fold({ data ->
                    assertTrue(data.isNotEmpty())
                    assertTrue(data.contains("uuid"))
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
    fun testAwaitForObjectResultPassesObject() = runBlocking {
        Fuel.get("/uuid").awaitForObjectResult(UUIDResponseDeserializer)
                .fold({ data ->
                    assertTrue(data.uuid.isNotEmpty())
                }, { error ->
                    fail("This test should pass but got an error: ${error.message}")
                })
    }

    @Test
    fun testAwaitForObjectResultCatchesError() = runBlocking {
        try {
            Fuel.get("/error/404").awaitForObjectResult(UUIDResponseDeserializer)
                    .fold({ _ ->
                        fail("This is an error case!")
                    }, { error ->
                        assertTrue(error.exception is HttpException)
                    })
        } catch (e: Exception) {
            fail("this should have been caught")
        }
    }

    @Test
    fun testAwaitForObjectResultCatchesDeserializeError() = runBlocking {
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


    private data class UUIDResponse(val uuid: String)

    private object UUIDResponseDeserializer : ResponseDeserializable<UUIDResponse> {
        override fun deserialize(content: String) =
                jacksonObjectMapper().readValue<UUIDResponse>(content)
    }
}
