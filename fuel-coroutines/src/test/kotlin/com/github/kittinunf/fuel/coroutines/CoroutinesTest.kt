package com.github.kittinunf.fuel.coroutines

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.ResponseDeserializable
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.UUID
import java.util.regex.Pattern

class CoroutinesTest {

    private val uuidRegex = Pattern.compile("^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-4[A-Fa-f0-9]{3}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$")

    init {
        Fuel.testMode {
            timeout = 30000
        }
    }

    lateinit var mock: MockHelper

    @Before
    fun setup() {
        mock = MockHelper().apply {
            setup()
        }
    }

    @After
    fun tearDown() {
        mock.tearDown()
    }

    @Test
    fun testAsyncResponseSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        try {
            Fuel.get(mock.path("ip")).asyncByteArrayResponse().await().third.fold({ data ->
                assertTrue(data.isNotEmpty())
            }, { error ->
                fail("This test should pass but got an error: ${error.message}")
            })
        } catch (exception: Exception) {
            fail("When using awaitByteArrayResponse errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testAsyncResponseErrorDueToNetwork() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/invalid/url"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        try {
            Fuel.get(mock.path("invalid/url")).asyncByteArrayResponse().await().third.fold({
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
    fun testAsyncStringResponseSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.reflect()
        )

        try {
            Fuel.get(mock.path("uuid")).asyncStringResponse().await().third.fold({ data ->
                assertTrue(data.isNotEmpty())
                assertTrue(data.contains("uuid"))
            }, { error ->
                fail("This test should pass but got an error: ${error.message}")
            })
        } catch (exception: Exception) {
            fail("When using awaitStringResponse errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testAsyncObjectResponse() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        Fuel.get(mock.path("uuid")).asyncObjectResponse(UUIDResponseDeserializer).await().third.fold({ data ->
            assertTrue(data.uuid.isNotEmpty())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAsyncStringResponseDoesNotThrowException() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/not/found/address"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("not/found/address")).asyncStringResponse().await().third.fold({
                fail("This should not be called")
            }, {

            })
        } catch (exception: Exception) {
            fail("This test should fail as exception should be caught")
        }
    }

    @Test
    fun testAsyncForByteArrayResult() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("ip")).asyncByteArrayResponse().await().third.fold({ data ->
            assertTrue(data.isNotEmpty())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAsyncStringResultErrorDueToNetwork() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/not/found/address"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("not/found/address")).asyncStringResponse().await().third.fold({
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
    fun testItCanAsyncStringResult() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        Fuel.get(mock.path("uuid")).asyncStringResponse().await().third.fold({ data ->
            assertTrue(data.isNotEmpty())
            assertTrue(data + ":" + uuidRegex.toRegex().toString(), uuidRegex.matcher(data).find())
        }, { error ->
            fail("This test should pass but got an error: ${error.message}")
        })
    }

    @Test
    fun testAsyncForObjectResultCatchesError() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/error/404"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("error/404")).asyncObjectResponse(UUIDResponseDeserializer).await().third.fold({ _ ->
                fail("This is an error case!")
            }, { error ->
                assertTrue(error.exception is HttpException)
            })
        } catch (exception: Exception) {
            fail("When using awaitSafelyObjectResult errors should be folded instead of thrown.")
        }
    }

    @Test
    fun testAsyncForObjectResultCatchesDeserializeError() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response().withBody(ByteArray(1) { 2 })
        )

        try {
            Fuel.get(mock.path("ip")).asyncObjectResponse(UUIDResponseDeserializer).await().third.fold({ _ ->
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
    fun testItCanAsyncByteArray() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        assertTrue(Fuel.get(mock.path("uuid")).asyncByteArrayResponse().await().third.get().isNotEmpty())
    }

    @Test
    fun testAsyncResponseResultSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        try {
            val data = Fuel.get(mock.path("uuid")).asyncByteArrayResponse().await().third.get()
            assertTrue(data.isNotEmpty())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testItCanAsyncForStringResultCanThrowException() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/error/404"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("error/404")).asyncStringResponse().await().third.get()
            fail("This test should fail due to status code 404")
        } catch (exception: Exception) {
            assertNotNull(exception)
        }
    }

    @Test
    fun testAsyncStringResultSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        try {
            val data = Fuel.get(mock.path("uuid")).asyncStringResponse().await().third.get()
            assertTrue(uuidRegex.matcher(data).find())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testItCanAsyncForObject() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        val uuidResponseDeserializable = Fuel.get(mock.path("uuid")).asyncObjectResponse(UUIDResponseDeserializer)
        assertTrue(uuidResponseDeserializable.await().third.get().uuid.isNotEmpty())
    }

    @Test
    fun testAsyncObjectResultSuccess() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        try {
            val data = Fuel.get(mock.path("uuid")).asyncObjectResponse(UUIDResponseDeserializer).await().third.get()
            assertTrue(data.uuid.isNotEmpty())
        } catch (exception: Exception) {
            fail("This test should pass but got an exception: ${exception.message}")
        }
    }

    @Test
    fun testAsyncObjectResultExceptionDueToNetwork() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/some/invalid/path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        
        try {
            Fuel.get(mock.path("some/invalid/path")).asyncObjectResponse(UUIDResponseDeserializer).await().third.get()
            fail("This test should raise an exception due to invalid URL")
        } catch (exception: Exception) {
            assertTrue(exception.message.orEmpty().contains("404"))
        }
    }

    private data class UUIDResponse(val uuid: String)

    private object UUIDResponseDeserializer : ResponseDeserializable<UUIDResponse> {

        class NoValidFormat(m: String = "Not a UUID"): Exception(m)

        override fun deserialize(content: String): UUIDResponse? {
            if (content.contains("=") || !content.contains("-")) {
                throw FuelError(NoValidFormat())
            }
            return UUIDResponse(content)
        }
    }
}

