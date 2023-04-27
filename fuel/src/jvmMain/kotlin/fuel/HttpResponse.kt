package fuel

import okhttp3.ResponseBody

public actual class HttpResponse {
    public var statusCode: Int = -1
    public lateinit var body: ResponseBody
}
