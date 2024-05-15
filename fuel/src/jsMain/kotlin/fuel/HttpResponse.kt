package fuel

import org.w3c.fetch.Response

public actual class HttpResponse {
    public var statusCode: Int = -1
    public var response: Response? = null
    public var headers: Map<String, String> = emptyMap()
}
