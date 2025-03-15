package fuel.moshi

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.Fuel
import fuel.get
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.ExperimentalOkHttpApi
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalOkHttpApi::class)
class FuelMoshiTest {
    @JsonClass(generateAdapter = true)
    data class HttpBinUserAgentModel(
        var userAgent: String = "",
    )

    @JsonClass(generateAdapter = true)
    data class Card(
        val rank: Char,
        val suit: String,
    )

    @Test
    fun testMoshiResponse(): Unit =
        runBlocking {
            val mockWebServer = startMockServerWithBody("{\"userAgent\": \"Fuel\"}")

            val response = Fuel.get(mockWebServer.url("user-agent").toString())
            val moshi = response.toMoshi(HttpBinUserAgentModel::class.java)
            moshi.fold({
                assertEquals("Fuel", it?.userAgent)
            }, {
                fail(it.localizedMessage)
            })

            mockWebServer.shutdown()
        }

    @Test
    fun testReifiedTypeMoshiResponse(): Unit =
        runBlocking {
            val mockWebServer = startMockServerWithBody("{\"userAgent\": \"Fuel\"}")

            val response = Fuel.get(mockWebServer.url("user-agent").toString())
            val moshi = response.toMoshi<HttpBinUserAgentModel>()
            moshi.fold({
                assertEquals("Fuel", it?.userAgent)
            }, {
                fail(it.localizedMessage)
            })

            mockWebServer.shutdown()
        }

    @Test
    fun testMoshiGenericList() =
        runBlocking {
            val mockWebServer =
                startMockServerWithBody(
                    "[{ " +
                        "    \"rank\": \"4\"," +
                        "    \"suit\": \"CLUBS\"" +
                        "  }, {" +
                        "    \"rank\": \"A\"," +
                        "    \"suit\": \"HEARTS\"" +
                        "  }, {" +
                        "    \"rank\": \"J\"," +
                        "    \"suit\": \"SPADES\"" +
                        "  }" +
                        "]",
                )

            val response = Fuel.get(mockWebServer.url("user-agent").toString())
            val listOfCardsType = Types.newParameterizedType(List::class.java, Card::class.java)
            val cards = response.toMoshi<List<Card>>(listOfCardsType)
            cards.fold({
                assertEquals(3, it?.size)
                assertEquals("CLUBS", it?.get(0)?.suit)
            }, {
                fail(it.localizedMessage)
            })

            mockWebServer.shutdown()
        }

    @Test
    fun customMoshiAdapterWithGenericList() =
        runBlocking {
            val mockWebServer =
                startMockServerWithBody(
                    "[{" +
                        "    \"rank\": \"1\"," +
                        "    \"suit\": \"CLUBS\"" +
                        "  }, {" +
                        "    \"rank\": \"J\"," +
                        "    \"suit\": \"HEARTS\"" +
                        "  }, {" +
                        "    \"rank\": \"K\"," +
                        "    \"suit\": \"SPADES\"" +
                        "  }" +
                        "]",
                )

            val userAgentResponse = Fuel.get(mockWebServer.url("user-agent").toString())
            val listOfCardsType = Types.newParameterizedType(List::class.java, Card::class.java)
            val adapter = defaultMoshi.build().adapter<List<Card>>(listOfCardsType)
            val cards = userAgentResponse.toMoshi<List<Card>>(adapter)
            cards.fold({
                assertEquals(3, it?.size)
                assertEquals("CLUBS", it?.get(0)?.suit)
            }, {
                fail(it.localizedMessage)
            })

            mockWebServer.shutdown()
        }

    private fun startMockServerWithBody(body: String): MockWebServer =
        MockWebServer().apply {
            enqueue(MockResponse(body = body))
            start()
        }
}
