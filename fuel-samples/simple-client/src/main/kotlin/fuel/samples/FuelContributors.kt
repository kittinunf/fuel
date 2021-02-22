package fuel.samples

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.Fuel
import fuel.get
import fuel.moshi.toMoshi
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

private const val ENDPOINT = "https://api.github.com/repos/kittinunf/fuel/contributors"

@JsonClass(generateAdapter = true)
data class Contributor(
    val login: String,
    val contributions: Int
)

fun main() {
    runBlocking {
        val types = Types.newParameterizedType(MutableList::class.java, Contributor::class.java)
        val contributors = Fuel.get(ENDPOINT).toMoshi<List<Contributor>>(types)

        val newContributor = contributors?.sortedByDescending { contributor ->
            contributor.contributions
        }

        newContributor?.forEach { contributor ->
            println("${contributor.login} : ${contributor.contributions}")
        }
    }
    exitProcess(0)
}
