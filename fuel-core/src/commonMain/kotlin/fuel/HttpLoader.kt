package fuel

public interface HttpLoader {
    public suspend fun get(request: Request): Any?
    public suspend fun post(request: Request): Any?
    public suspend fun put(request: Request): Any?
    public suspend fun patch(request: Request): Any?
    public suspend fun delete(request: Request): Any?
    public suspend fun head(request: Request): Any?
    public suspend fun method(request: Request): Any?
}
