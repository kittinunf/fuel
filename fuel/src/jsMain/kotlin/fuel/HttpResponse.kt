package fuel

public typealias HttpBody = String

public actual class HttpResponse {

    public actual var statusCode: Int = -1

    public actual var body: HttpBody = ""
}
