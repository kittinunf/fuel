package fuel.forge

import com.github.kittinunf.forge.core.JSON
import com.github.kittinunf.forge.core.apply
import com.github.kittinunf.forge.core.at
import com.github.kittinunf.forge.core.map
import com.github.kittinunf.forge.util.create
import com.github.kittinunf.result.Result
import fuel.Fuel
import fuel.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class FuelForgeTest {
    data class HttpBinUserAgentModel(var userAgent: String = "", var status: String = "")

    private val httpBinUserDeserializer = { json: JSON ->
        ::HttpBinUserAgentModel.create
            .map(json at "userAgent")
            .apply(json at "status")
    }

    @Test
    fun testForgeResponse() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\", \"status\": \"OK\"}"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val binUserAgentModel = HttpBinUserAgentModel("Fuel", "OK")
        val response = Fuel.get(mockWebServer.url("user-agent"))
        when (val forge = response.toForge(httpBinUserDeserializer)) {
            is Result.Success -> assertEquals(binUserAgentModel, forge.value)
            is Result.Failure -> fail(forge.error.localizedMessage)
        }

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }
}
