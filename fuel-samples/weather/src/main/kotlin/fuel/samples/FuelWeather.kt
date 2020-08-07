package fuel.samples

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.Fuel
import fuel.moshi.toMoshi
import fuel.request
import kotlinx.coroutines.runBlocking

@JsonClass(generateAdapter = true)
data class Weather(
    val title: String,
    val latt_long: String
)

fun main() {
    runBlocking {
        val types = Types.newParameterizedType(MutableList::class.java, Weather::class.java)
        val weathers = Fuel.request(WeatherApi.WeatherFor("london")).toMoshi<List<Weather>>(types)
        weathers?.forEach {
            println("${it.title} - ${it.latt_long}")
        }
    }
}
