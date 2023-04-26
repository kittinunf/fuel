package fuel

import okio.BufferedSource

public actual class HttpResponse {
    public var statusCode: Int = -1
    public lateinit var source: BufferedSource
    public var body: String = ""
}