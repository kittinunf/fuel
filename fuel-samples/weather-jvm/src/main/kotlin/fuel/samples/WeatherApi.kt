package fuel.samples

import fuel.FuelRouting

sealed class WeatherApi : FuelRouting {
    override val basePath = "https://www.metaweather.com"

    class WeatherFor(val location: String) : WeatherApi()
    class ConsolidatedWeatherFor(val woeId: Int) : WeatherApi()

    override val method: String
        get() = when (this) {
            is WeatherFor -> "GET"
            is ConsolidatedWeatherFor -> "GET"
        }

    override val path: String
        get() = when (this) {
            is WeatherFor -> "/api/location/search/?query=$location"
            is ConsolidatedWeatherFor -> "/api/location/$woeId"
        }

    override val headers: Map<String, String>?
        get() = null

    override val body: String?
        get() = null
}