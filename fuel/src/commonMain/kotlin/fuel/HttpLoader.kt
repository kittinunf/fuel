package fuel

public interface HttpLoader {
    public suspend fun get(request: Request.Builder.() -> Unit): HttpResponse

    public suspend fun post(request: Request.Builder.() -> Unit): HttpResponse

    public suspend fun put(request: Request.Builder.() -> Unit): HttpResponse

    public suspend fun patch(request: Request.Builder.() -> Unit): HttpResponse

    public suspend fun delete(request: Request.Builder.() -> Unit): HttpResponse

    public suspend fun head(request: Request.Builder.() -> Unit): HttpResponse

    public suspend fun method(request: Request.Builder.() -> Unit): HttpResponse
}
