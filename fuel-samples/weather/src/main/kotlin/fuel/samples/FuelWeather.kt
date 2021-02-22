package fuel.samples

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.Fuel
import fuel.moshi.toMoshi
import fuel.request
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

@JsonClass(generateAdapter = true)
data class Weather(
        val title: String,
        val latt_long: String,
        val woeid: Int
)

@JsonClass(generateAdapter = true)
data class ConsolidatedWeather(
        val consolidated_weather: List<ConsolidatedWeatherEntry>
)

@JsonClass(generateAdapter = true)
data class ConsolidatedWeatherEntry(
        val applicable_date: String,
        val weather_state_name: String,
        val the_temp: Float
)

fun main() {
    runBlocking {
        val types = Types.newParameterizedType(MutableList::class.java, Weather::class.java)

        val locations = listOf("London", "Tokyo")
        locations.forEach {
            println("Weather for $it : ")
            val location = Fuel.request(WeatherApi.WeatherFor(it)).toMoshi<List<Weather>>(types)!!
            val weathers = Fuel.request(WeatherApi.ConsolidatedWeatherFor(location.first().woeid)).toMoshi(ConsolidatedWeather::class.java)
            println("Date           Weather            Temperature ")
            weathers?.consolidated_weather?.forEach { entry ->
                println(entry.applicable_date + "     " + entry.weather_state_name + "     " + entry.the_temp)
            }
        }
    }
    exitProcess(0)
}
