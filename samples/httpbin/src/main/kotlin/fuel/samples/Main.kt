package fuel.samples

import fuel.Fuel
import fuel.get
import fuel.httpGet
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val string = Fuel.get("https://httpbin.org/get").body.string()
    println(string)

    val cookieString = "https://httpbin.org/cookies".httpGet().body.string()
    println(cookieString)
}
