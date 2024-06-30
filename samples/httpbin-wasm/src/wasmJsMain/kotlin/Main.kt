import fuel.Fuel
import fuel.get
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import kotlinx.io.readString

fun main() {
    document.body?.appendElement("div") {
        CoroutineScope(Dispatchers.Main).launch {
            val string = Fuel.get("http://httpbin.org/get").source.readString()
            appendText(string)
        }
    }
}
