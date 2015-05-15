package fuel.util

import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

public inline fun HttpURLConnection(url: URL, builder: HttpURLConnection.() -> Unit): HttpURLConnection {
    val connection = url.openConnection() as HttpURLConnection
    connection.builder()
    return connection
}
