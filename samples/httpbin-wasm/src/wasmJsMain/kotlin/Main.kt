import fuel.Fuel
import fuel.get
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.dom.appendElement
import kotlinx.dom.appendText

fun main() {
    document.body?.appendElement("div") {
        CoroutineScope(Dispatchers.Main).launch {
            Fuel.get("http://httpbin.org/get").response?.text()
                ?.then {
                    appendText(it.toString())
                }?.catch {
                    appendText(it.toString())
                }
        }
    }
}
