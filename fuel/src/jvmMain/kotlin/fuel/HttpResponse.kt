package fuel

import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

public typealias HttpBody = ResponseBody

public actual class HttpResponse {

    public actual var statusCode: Int = -1

    public actual var body: HttpBody = "".toResponseBody(null)
}
