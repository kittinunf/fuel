
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection
import java.util.UUID

class CoroutinesTest : MockHttpTestCase() {

    @Test
    fun awaitByteArray() = runBlocking {
        mock.chain(
                request = mock.request().withPath("/ip"),
                response = mock.reflect()
        )

        try {
            val data = Fuel.get(mock.path("ip")).awaitByteArray()
            assertThat(data.isNotEmpty(), equalTo(true))
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test
    fun awaitByteArrayResponse() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        try {
            val (_, _, data) = Fuel.get(mock.path("ip")).awaitByteArrayResponse()
            assertThat(data.isNotEmpty(), equalTo(true))
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test
    fun awaitByteArrayResult() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        val (data, error) = Fuel.get(mock.path("ip")).awaitByteArrayResult()
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.isNotEmpty(), equalTo(true))
    }

    @Test
    fun awaitByteArrayResponseResult() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.reflect()
        )

        val (_, _, result) = Fuel.get(mock.path("ip")).awaitByteArrayResponseResult()
        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.isNotEmpty(), equalTo(true))
    }

    @Test
    fun awaitByteArrayResultWithNetworkError() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/invalid/url"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (data, error) = Fuel.get(mock.path("invalid/url")).awaitByteArrayResult()
        assertThat("Expected error, actual data $data", error, notNullValue())
        assertTrue(error!!.exception is HttpException)
        assertTrue(error.message!!.contains("HTTP Exception 404"))
    }

    @Test
    fun awaitStringResult() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.reflect()
        )

        val (data, error) = Fuel.get(mock.path("uuid")).awaitStringResult()
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!, containsString("uuid"))
    }

    @Test
    fun awaitObjectResult() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(UUID.randomUUID().toString())
        )

        val (data, error) = Fuel.get(mock.path("uuid")).awaitObjectResult(UUIDResponseDeserializer)
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.uuid.isBlank(), equalTo(false))
    }

    @Test
    fun awaitStringResultWithNetworkError() = runBlocking {
        mock.chain(
            request = mock.request().withPath("/not/found/address"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (data, error) = Fuel.get(mock.path("not/found/address")).awaitStringResult()
        assertThat("Expected error, actual data $data", error, notNullValue())
        assertTrue(error!!.exception is HttpException)
        assertTrue(error.message!!.contains("HTTP Exception 404"))
    }

    private data class UUIDResponse(val uuid: String)

    private object UUIDResponseDeserializer : ResponseDeserializable<UUIDResponse> {

        class NoValidFormat(m: String = "Not a UUID") : Exception(m)

        override fun deserialize(content: String): UUIDResponse {
            if (content.contains("=") || !content.contains("-")) {
                throw FuelError.wrap(NoValidFormat())
            }
            return UUIDResponse(content)
        }
    }

    @Test
    fun handleException() = runBlocking {

        mock.chain(
            request = mock.request().withPath("/fail"),
            response = mock.reflect()
        )

        val (_, res, result) = Fuel.get(mock.path("fail"))
            .awaitObjectResponseResult(object : ResponseDeserializable<Unit> {
                override fun deserialize(content: String): Unit? {
                    throw IllegalStateException("some deserialization exception")
                }
            })

        assertThat(res, notNullValue())
        assertThat(result, notNullValue())
        assertThat(result.component2(), notNullValue())
        val success = when (result) {
            is Result.Success -> true
            is Result.Failure -> false
        }
        assertFalse("should catch exception", success)
        when (result) {
            is Result.Success -> fail("should catch exception")
            is Result.Failure -> {
                assertThat(
                    result.error.exception as? IllegalStateException,
                    isA(IllegalStateException::class.java)
                )
            }
        }
    }
}
