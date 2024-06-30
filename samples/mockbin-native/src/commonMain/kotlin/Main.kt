import fuel.FuelBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.io.readString

fun main() =
    runBlocking {
        val fuel = FuelBuilder().build()
        val response =
            fuel.post(request = {
                url = "http://mockbin.com/request?foo=bar&foo=baz"
                body = "{\"foo\": \"bar\"}"
            })
        println(response.source.readString())
    }
