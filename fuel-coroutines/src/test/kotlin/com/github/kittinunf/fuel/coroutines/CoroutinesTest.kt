package com.github.kittinunf.fuel.coroutines

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.ResponseDeserializable
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.UUID
import java.util.concurrent.Executors
import java.util.regex.Pattern

class CoroutinesTest {

    private val uuidRegex = Pattern.compile("^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-4[A-Fa-f0-9]{3}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$")

    init {
        Fuel.testMode {
            timeout = 30000
        }
    }

    private lateinit var mock: MockHelper

    private val threadPool = Executors.newSingleThreadExecutor()!!
    private val threadPoolDispatcher = threadPool.asCoroutineDispatcher()

    @Before
    fun setup() {
        mock = MockHelper().apply { setup() }
    }

    @After
    fun tearDown() {
        threadPool.shutdown()
        mock.tearDown()
    }

    @Test
    fun testAwaitResponseSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        try {
            Fuel.get(mock.path("ip")).awaitByteArrayResponse().third.fold({ data ->
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
        mock.chain(
            request = mock.request().withPath("/invalid/url"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        try {
            Fuel.get(mock.path("invalid/url")).awaitByteArrayResponse().third.fold({
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
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.reflect()
        )

        try {
            Fuel.get(mock.path("uuid")).awaitStringResponse().third.fold({ data ->
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
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        Fuel.get(mock.path("uuid")).awaitObjectResponse(UUIDResponseDeserializer).third.fold({ data ->
            assertTrue(data.uuid.isNotEmpty())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAwaitStringResponseDoesNotThrowException() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/not/found/address"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("not/found/address")).awaitStringResponse().third.fold({
                fail("This should not be called")
            }, {

            })
        } catch (exception: Exception) {
            fail("This test should fail as exception should be caught")
        }
    }

    @Test
    fun testAwaitForByteArrayResult() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("ip")).awaitByteArrayResult().fold({ data ->
            assertTrue(data.isNotEmpty())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAwaitForByteArrayResultWithDifferentContext() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("ip")).awaitByteArrayResult(threadPoolDispatcher).fold({ data ->
            assertTrue(data.isNotEmpty())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAwaitStringResultErrorDueToNetwork() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/not/found/address"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("not/found/address")).awaitStringResult().fold({
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
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        Fuel.get(mock.path("uuid")).awaitStringResult().fold({ data ->
            assertTrue(data.isNotEmpty())
            assertTrue(data + ":" + uuidRegex.toRegex().toString(), uuidRegex.matcher(data).find())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testItCanAwaitStringResultWithDifferentContext() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        Fuel.get(mock.path("uuid")).awaitStringResult(threadPoolDispatcher).fold({ data ->
            assertTrue(data.isNotEmpty())
            assertTrue(data + ":" + uuidRegex.toRegex().toString(), uuidRegex.matcher(data).find())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAwaitForObjectResultCatchesError() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/error/404"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("error/404")).awaitObjectResult(UUIDResponseDeserializer).fold({ _ ->
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
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response().withBody(ByteArray(1) { 2 })
        )

        try {
            Fuel.get(mock.path("ip")).awaitObjectResult(UUIDResponseDeserializer).fold({ _ ->
                fail("This is an error case!")

            }, { error ->
                assertNotNull(error)
                assertTrue(error.exception is UUIDResponseDeserializer.NoValidFormat)
            })
        } catch (exception: Exception) {
            fail("When using awaitSafelyObjectResult errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testItCanAwaitByteArray() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        assertTrue(Fuel.get(mock.path("uuid")).awaitByteArray().isNotEmpty())
    }

    @Test
    fun testAwaitResponseResultSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        try {
            val data = Fuel.get(mock.path("uuid")).awaitByteArray()
            assertTrue(data.isNotEmpty())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testItCanAwaitForStringResultCanThrowException() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/error/404"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("error/404")).awaitString()
            fail("This test should fail due to status code 404")
        } catch (exception: Exception) {
            assertNotNull(exception)
        }
    }

    @Test
    fun testAwaitStringResultSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        try {
            val data = Fuel.get(mock.path("uuid")).awaitString()
            assertTrue(uuidRegex.matcher(data).find())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testItCanAwaitForObject() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        assertTrue(Fuel.get(mock.path("uuid")).awaitObject(UUIDResponseDeserializer).uuid.isNotEmpty())
    }

    @Test
    fun testAwaitObjectResultSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        try {
            val data = Fuel.get(mock.path("uuid")).awaitObject(UUIDResponseDeserializer)
            assertTrue(data.uuid.isNotEmpty())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testAwaitObjectResultSuccessWithDifferentContext() = runBlocking {
        mock.chain(
                request = mock.request().withPath("/uuid"),
                response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        try {
            val data = Fuel.get(mock.path("uuid")).awaitObject(UUIDResponseDeserializer, threadPoolDispatcher)
            assertTrue(data.uuid.isNotEmpty())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }


    @Test
    fun testAwaitObjectResultExceptionDueToNetwork() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/some/invalid/path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("some/invalid/path")).awaitObject(UUIDResponseDeserializer)
            fail("This test should raise an exception due to invalid URL")
        } catch (exception: Exception) {
            assertTrue(exception.message.orEmpty().contains("404"))
        }
    }

    private data class UUIDResponse(val uuid: String)

    private object UUIDResponseDeserializer : ResponseDeserializable<UUIDResponse> {

        class NoValidFormat(m: String = "Not a UUID"): Exception(m)

        override fun deserialize(content: String): UUIDResponse {
            if (content.contains("=") || !content.contains("-")) {
                throw FuelError(NoValidFormat())
            }
            return UUIDResponse(content)
        }
    }

}

