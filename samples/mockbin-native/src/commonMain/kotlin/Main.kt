import fuel.FuelBuilder
import fuel.Request
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val fuel = FuelBuilder().build()
    val request = Request.Builder()
        .body("{\"foo\": \"bar\"}")
        .url("http://mockbin.com/request?foo=bar&foo=baz")
        .build()
    val response = fuel.post(request)
    println(response.body)
}