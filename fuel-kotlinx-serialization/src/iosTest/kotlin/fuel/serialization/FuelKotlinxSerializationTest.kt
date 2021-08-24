package fuel.serialization

import fuel.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelKotlinxSerializationTest {
    @Serializable
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Serializable
    data class RocketLaunch(var rocket: String, var success: Boolean, var details: String)

    @Test
    fun testSerializableResponse() {
        val httpResponse = HttpResponse().apply {
            statusCode = 200
            body = "{\"userAgent\": \"Fuel\"}"
        }
        val json = httpResponse.toJson(Json.Default, HttpBinUserAgentModel.serializer())
        json.fold({
            assertEquals("Fuel", it?.userAgent)
        }, {
            fail(it.message)
        })
    }

    @Test
    fun testSpaceXDetail() {
        val httpResponse = HttpResponse().apply {
            statusCode = 200
            body = "{\"rocket\":\"5e9d0d95eda69973a809d1ec\", \"success\":true,\"details\":\"Second GTO launch for Falcon 9. The USAF evaluated launch data from this flight as part of a separate certification program for SpaceX to qualify to fly U.S. military payloads and found that the Thaicom 6 launch had \\\"unacceptable fuel reserves at engine cutoff of the stage 2 second burnoff\\\"\"}"
        }
        val json = httpResponse.toJson(Json.Default, RocketLaunch.serializer())
        json.fold({
            assertEquals("Second GTO launch for Falcon 9. The USAF evaluated launch data from this flight as part of a separate certification program for SpaceX to qualify to fly U.S. military payloads and found that the Thaicom 6 launch had \"unacceptable fuel reserves at engine cutoff of the stage 2 second burnoff\"", it?.details)
        }, {
            fail(it.message)
        })
    }
}
