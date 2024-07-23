package fuel

import kotlinx.io.Source

public class HttpResponse {
    public var statusCode: Int = -1
    public lateinit var source: Source
    public var headers: Map<String, String> = emptyMap()
}
