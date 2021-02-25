package fuel

import okhttp3.Response

public interface SuspendHttpLoader {

    /**
     * Load the [request]'s data and suspend until the operation is complete. Return the loaded [Response].
     *
     * @param request The request to execute.
     * @return The [Response] result.
     */
    public suspend fun get(request: Request): Response
    public suspend fun post(request: Request): Response
    public suspend fun put(request: Request): Response
    public suspend fun patch(request: Request): Response
    public suspend fun delete(request: Request): Response
    public suspend fun head(request: Request): Response
    public suspend fun method(request: Request): Response

    public companion object {
        public operator fun invoke(): SuspendHttpLoader = Builder().build()
    }
}
