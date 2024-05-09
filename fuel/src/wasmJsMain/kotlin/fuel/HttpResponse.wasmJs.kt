package fuel

import org.w3c.fetch.Response

public actual class HttpResponse {
    public var statusCode: Short = -1
    public var response: Response? = null
}
