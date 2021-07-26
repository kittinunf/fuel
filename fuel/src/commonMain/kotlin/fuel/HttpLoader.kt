package fuel

public expect class HttpLoader {
    public suspend fun get(request: Request): HttpResponse
    public suspend fun post(request: Request): HttpResponse
    public suspend fun put(request: Request): HttpResponse
    public suspend fun patch(request: Request): HttpResponse
    public suspend fun delete(request: Request): HttpResponse
    public suspend fun head(request: Request): HttpResponse
    public suspend fun method(request: Request): HttpResponse
}