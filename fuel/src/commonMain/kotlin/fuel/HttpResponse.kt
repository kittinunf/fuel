package fuel

public expect class HttpBody

public expect class HttpResponse {

    public var statusCode: Int

    public var body: HttpBody
}
