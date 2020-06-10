package fuel.moshi

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.HttpLoader
import fuel.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelMoshiTest {
    @JsonClass(generateAdapter = true)
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @JsonClass(generateAdapter = true)
    data class Card(val rank: Char, val suit: String)

    @Test
    fun testMoshiResponse() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\"}"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val response = HttpLoader().get(mockWebServer.url("user-agent"))
        val moshi = response.toMoshi(HttpBinUserAgentModel::class.java)!!
        assertEquals("Fuel", moshi.userAgent)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun testMoshiGenericList() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("[{"
        + "    \"rank\": \"4\","
        + "    \"suit\": \"CLUBS\""
        + "  }, {"
        + "    \"rank\": \"A\","
        + "    \"suit\": \"HEARTS\""
        + "  }, {"
        + "    \"rank\": \"J\","
        + "    \"suit\": \"SPADES\""
        + "  }"
        + "]"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val response = HttpLoader().get(mockWebServer.url("user-agent"))
        val listOfCardsType = Types.newParameterizedType(List::class.java, Card::class.java)
        val cards = response.toMoshi<List<Card>>(listOfCardsType)!!
        assertEquals(3, cards.size)
        assertEquals("CLUBS", cards[0].suit)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun CustomMoshiAdapterWithGenericList() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("[{"
        + "    \"rank\": \"4\","
        + "    \"suit\": \"CLUBS\""
        + "  }, {"
        + "    \"rank\": \"A\","
        + "    \"suit\": \"HEARTS\""
        + "  }, {"
        + "    \"rank\": \"J\","
        + "    \"suit\": \"SPADES\""
        + "  }"
        + "]"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val response = HttpLoader().get(mockWebServer.url("user-agent"))
        val listOfCardsType = Types.newParameterizedType(List::class.java, Card::class.java)
        val adapter = defaultMoshi.build().adapter<List<Card>>(listOfCardsType)
        val cards = response.toMoshi<List<Card>>(adapter)!!
        assertEquals(3, cards.size)
        assertEquals("CLUBS", cards[0].suit)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }
}
