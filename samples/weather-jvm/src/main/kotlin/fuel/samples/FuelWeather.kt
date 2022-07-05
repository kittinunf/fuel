package fuel.samples

import com.github.kittinunf.result.onSuccess
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.Fuel
import fuel.moshi.toMoshi
import fuel.request
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

@JsonClass(generateAdapter = true)
data class Location(
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
        val cityLocation = listOf("London", "Tokyo")
        cityLocation.forEach { city ->

            val locationList = Fuel.request(WeatherApi.WeatherFor(city))
                .toMoshi<List<Location>>(Types.newParameterizedType(List::class.java, Location::class.java))
            locationList.fold({ locations ->
                val firstLocation = locations?.first()
                println("Weather for ${firstLocation?.title} : ${firstLocation?.latt_long}")

                val weathers = firstLocation?.woeid?.let {
                    WeatherApi.ConsolidatedWeatherFor(it)
                }?.let {
                    Fuel.request(it).toMoshi<ConsolidatedWeather>()
                }
                println("Date           Weather         Temperature(°C) ")
                println("-----------------------------------------------")
                weathers?.onSuccess {
                    it?.consolidated_weather?.forEach { weather ->
                        println(weather.applicable_date + "     " + weather.weather_state_name + "     " + weather.the_temp)
                    }
                }
                println("-----------------------------------------------")
            }, {
                println(it.localizedMessage)
            })
        }
    }
    exitProcess(0)
}
