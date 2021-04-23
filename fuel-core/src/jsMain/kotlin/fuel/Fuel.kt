package fuel

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response

public object Fuel : HttpLoader {
    override suspend fun get(request: Request): Any? = request(request.url, "GET")
    override suspend fun post(request: Request): Any? = request(request.url, "POST", request.body)
    override suspend fun put(request: Request): Any? = request(request.url, "PUT", request.body)
    override suspend fun patch(request: Request): Any? = request(request.url, "PATCH", request.body)
    override suspend fun delete(request: Request): Any? = request(request.url, "DELETE")
    override suspend fun head(request: Request): Any? = request(request.url, "HEAD")
    override suspend fun method(request: Request): Any? = request(request.url, request.method, request.body)

    //TODO: Headers Support
    private suspend fun request(url: String, method: String?, body: String? = null): Any? {
        val res = window.fetch(url, object : RequestInit {
            override var method: String? = method
            override var body: dynamic = body.asDynamic()
        }).await()
        return if (res.ok) res.json().await() else throw makeError(res)
    }

    private suspend fun makeError(res: Response): FetchError = try {
        val errorResponse: dynamic = res.json().await()
        FetchError("Request failed: $errorResponse")
    } catch (e: Exception) {
        val errorResponse = res.text().await()
        FetchError("Request failed: $errorResponse")
    }
}
