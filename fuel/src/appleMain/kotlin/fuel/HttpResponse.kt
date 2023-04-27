package fuel

import platform.Foundation.NSData

public actual class HttpResponse {
    public var statusCode: Int = -1
    public var nsData: NSData? = null
    public var body: String = ""
}
