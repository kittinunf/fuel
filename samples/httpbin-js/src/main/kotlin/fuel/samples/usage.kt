package fuel.samples

import fuel.Fuel
import fuel.get
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() {
    CoroutineScope(Dispatchers.Main).launch {
        val httpBin = JSON.stringify(Fuel.get("http://httpbin.org/get").body)
        document.getElementById("root")?.innerHTML = httpBin
    }
}