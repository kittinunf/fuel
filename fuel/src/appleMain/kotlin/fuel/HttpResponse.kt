package fuel

import okio.ByteString

public typealias HttpBody = ByteString

public actual class HttpResponse {

    public actual var statusCode: Int = -1

    public actual var body: HttpBody = ByteString.EMPTY
}
