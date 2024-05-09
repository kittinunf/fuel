import fuel.Fuel
import fuel.get
import kotlinx.browser.document
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
