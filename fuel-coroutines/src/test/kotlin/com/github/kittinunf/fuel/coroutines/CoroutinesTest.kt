import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.ResponseDeserializable
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CoroutinesTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"
        Fuel.testMode {
            timeout = 30000
        }
    }

    @Test
    fun testAwaitResponseSuccess() = runBlocking {
        try {
            Fuel.get("/ip").awaitByteArrayResponse().third.fold({ data ->
                assertTrue(data.isNotEmpty())
            }, { error ->
                fail("This test should pass but got an error: ${error.message}")
            })
        } catch (exception: Exception) {
            fail("When using awaitByteArrayResponse errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testAwaitResponseErrorDueToNetwork() = runBlocking {
        try {
            Fuel.get("/invalid/url").awaitByteArrayResponse().third.fold({
                fail("This test should fail due to HTTP status code.")
            }, { error ->
                assertTrue(error.exception is HttpException)
                assertTrue(error.message!!.contains("HTTP Exception 404"))
            })
        } catch (exception: HttpException) {
            fail("When using awaitByteArrayResponse errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testAwaitStringResponseSuccess() = runBlocking {
        try {
            Fuel.get("/uuid").awaitStringResponse().third.fold({ data ->
                assertTrue(data.isNotEmpty())
                assertTrue(data.contains("uuid"))
            }, { error ->
                fail("This test should pass but got an error: ${error.message}")
            })
        } catch (exception: Exception) {
            fail("When using awaitString errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testAwaitObjectResponse() = runBlocking {
        Fuel.get("/uuid").awaitObjectResponse(UUIDResponseDeserializer).third.fold({ data ->
            assertTrue(data.uuid.isNotEmpty())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAwaitStringResponseDoesNotThrowException() = runBlocking {
        try {
            Fuel.get("/not/found/address").awaitStringResponse().third.fold({
                fail("This should not be called")
            }, {

            })
        } catch (exception: Exception) {
            fail("This test should fail as exception should be caught")
        }
    }

    @Test
    fun testAwaitForByteArrayResult() = runBlocking {
        Fuel.get("/ip").awaitByteArrayResult().fold({ data ->
            assertTrue(data.isNotEmpty())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAwaitStringResultErrorDueToNetwork() = runBlocking {
        try {
            Fuel.get("/not/found/address").awaitStringResult().fold({
                fail("This test should fail due to HTTP status code.")
            }, { error ->
                assertTrue(error.exception is HttpException)
                assertTrue(error.message.orEmpty().contains("HTTP Exception 404"))
            })
        } catch (exception: HttpException) {
            fail("When using awaitString errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testItCanAwaitStringResult() = runBlocking {
        Fuel.get("/uuid").awaitStringResult().fold({ data ->
            assertTrue(data.isNotEmpty())
            assertTrue(data.contains("uuid"))
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAwaitForObjectResultCatchesError() = runBlocking {
        try {
            Fuel.get("/error/404").awaitObjectResult(UUIDResponseDeserializer).fold({ _ ->
                fail("This is an error case!")
            }, { error ->
                assertTrue(error.exception is HttpException)
            })
        } catch (exception: Exception) {
            fail("When using awaitSafelyObjectResult errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testAwaitForObjectResultCatchesDeserializeError() = runBlocking {
        try {
            Fuel.get("/ip").awaitObjectResult(UUIDResponseDeserializer).fold({ _ ->
                fail("This is an error case!")

            }, { error ->
                assertNotNull(error)
                assertTrue(error.exception is JsonMappingException)
            })
        } catch (exception: Exception) {
            fail("When using awaitSafelyObjectResult errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testItCanAwaitByteArray() = runBlocking {
        assertTrue(Fuel.get("/uuid").awaitByteArray().isNotEmpty())
    }

    @Test
    fun testAwaitResponseResultSuccess() = runBlocking {
        try {
            val data = Fuel.get("/uuid").awaitByteArray()
            assertTrue(data.isNotEmpty())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testItCanAwaitForStringResultCanThrowException() = runBlocking {
        try {
            Fuel.get("/error/404").awaitString()
            fail("This test should fail due to status code 404")
        } catch (exception: Exception) {
            assertNotNull(exception)
        }
    }

    @Test
    fun testAwaitStringResultSuccess() = runBlocking {
        try {
            val data = Fuel.get("/uuid").awaitString()
            assertTrue(data.contains("uuid"))
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testItCanAwaitForObject() = runBlocking {
        assertTrue(Fuel.get("/uuid").awaitObject(UUIDResponseDeserializer).uuid.isNotEmpty())
    }

    @Test
    fun testAwaitObjectResultSuccess() = runBlocking {
        try {
            val data = Fuel.get("/uuid").awaitObject(UUIDResponseDeserializer)
            assertTrue(data.uuid.isNotEmpty())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testAwaitObjectResultExceptionDueToNetwork() = runBlocking {
        try {
            Fuel.get("/some/invalid/path").awaitObject(UUIDResponseDeserializer)
            fail("This test should raise an exception due to invalid URL")
        } catch (exception: Exception) {
            assertTrue(exception.message.orEmpty().contains("404"))
        }
    }

    private data class UUIDResponse(val uuid: String)

    private object UUIDResponseDeserializer : ResponseDeserializable<UUIDResponse> {
        override fun deserialize(content: String) =
                jacksonObjectMapper().readValue<UUIDResponse>(content)
    }
}

