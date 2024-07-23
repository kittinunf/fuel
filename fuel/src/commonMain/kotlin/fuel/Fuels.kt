package fuel

public suspend fun Fuel.get(
    url: String,
    parameters: Parameters? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse =
    loader().get {
        this.url = url
        this.parameters = parameters
        this.headers = headers
    }

public suspend fun Fuel.post(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse =
    loader().post {
        this.url = url
        this.parameters = parameters
        this.body = body
        this.headers = headers
    }

public suspend fun Fuel.put(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse =
    loader().put {
        this.url = url
        this.parameters = parameters
        this.headers = headers
        this.body = body
    }

public suspend fun Fuel.patch(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse =
    loader().patch {
        this.url = url
        this.parameters = parameters
        this.body = body
        this.headers = headers
    }

public suspend fun Fuel.delete(
    url: String,
    parameters: Parameters? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse =
    loader().delete {
        this.url = url
        this.parameters = parameters
        this.body = body
        this.headers = headers
    }

public suspend fun Fuel.head(
    url: String,
    parameters: Parameters? = null
): HttpResponse =
    loader().head {
        this.url = url
        this.parameters = parameters
    }

public suspend fun Fuel.method(
    url: String,
    parameters: Parameters? = null,
    method: String? = null,
    body: String? = null,
    headers: Map<String, String> = emptyMap()
): HttpResponse =
    loader().method {
        this.url = url
        this.parameters = parameters
        this.method = method
        this.body = body
        this.headers = headers
    }

public suspend fun Fuel.request(convertible: RequestConvertible): HttpResponse {
    val request = convertible.request
    return loader().method {
        url = request.url
        parameters = request.parameters
        method = request.method
        body = request.body
        headers = request.headers
    }
}
