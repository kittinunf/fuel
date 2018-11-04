package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.util.TestConfiguration

class Fuel {
    interface PathStringConvertible {
        val path: String
    }

    interface RequestConvertible {
        val request: Request
    }

    companion object {
        internal var testConfiguration = TestConfiguration(timeout = null, timeoutRead = null)

        @JvmStatic
        @JvmOverloads
        fun testMode(configuration: TestConfiguration.() -> Unit = {}) {
            testConfiguration = TestConfiguration().apply(configuration)
        }

        @JvmStatic
        fun regularMode() = testMode { timeout = null; timeoutRead = null}

        // convenience methods
        // get
        @JvmStatic
        @JvmOverloads
        fun get(path: String, parameters: Parameters? = null): Request =
                request(Method.GET, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun get(convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                request(Method.GET, convertible, parameters)

        // post
        @JvmStatic
        @JvmOverloads
        fun post(path: String, parameters: Parameters? = null): Request =
                request(Method.POST, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun post(convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                request(Method.POST, convertible, parameters)

        // put
        @JvmStatic
        @JvmOverloads
        fun put(path: String, parameters: Parameters? = null): Request =
                request(Method.PUT, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun put(convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                request(Method.PUT, convertible, parameters)

        // patch
        @JvmStatic
        @JvmOverloads
        fun patch(path: String, parameters: Parameters? = null): Request =
                request(Method.PATCH, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun patch(convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                request(Method.PATCH, convertible, parameters)

        // delete
        @JvmStatic
        @JvmOverloads
        fun delete(path: String, parameters: Parameters? = null): Request =
                request(Method.DELETE, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun delete(convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                request(Method.DELETE, convertible, parameters)

        // download
        @JvmStatic
        @JvmOverloads
        fun download(path: String, parameters: Parameters? = null): Request =
                FuelManager.instance.download(path, parameters)

        @JvmStatic
        @JvmOverloads
        fun download(convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                download(convertible.path, parameters)

        // upload
        @JvmStatic
        @JvmOverloads
        fun upload(path: String, method: Method = Method.POST, parameters: Parameters? = null): Request =
                FuelManager.instance.upload(path, method, parameters)

        @JvmStatic
        @JvmOverloads
        fun upload(convertible: PathStringConvertible, method: Method = Method.POST, parameters: Parameters? = null): Request =
                upload(convertible.path, method, parameters)

        // head
        @JvmStatic
        @JvmOverloads
        fun head(path: String, parameters: Parameters? = null): Request =
                request(Method.HEAD, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun head(convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                request(Method.HEAD, convertible, parameters)

        /**
         * Convenience method to make a request
         *
         * @see Method
         *
         * @param method [Method] the HTTP method to make the request with
         * @param path [String] the absolute url or relative path (to FuelManager.instance.basePath)
         * @param parameters [Parameters?] list of parameters
         */
        @JvmStatic
        @JvmOverloads
        fun request(method: Method, path: String, parameters: Parameters? = null): Request =
                FuelManager.instance.request(method, path, parameters)

        /**
         * Convenience method to make a request from a [com.github.kittinunf.fuel.Fuel.PathStringConvertible}
         *
         * @see Method
         * @see Fuel.request(Method, String, Parameters?)
         *
         * @param convertible [PathStringConvertible]
         */
        @JvmStatic
        @JvmOverloads
        fun request(method: Method, convertible: PathStringConvertible, parameters: Parameters? = null): Request =
                request(method, convertible.path, parameters)

        /**
         * Convenience method to make a request from a [com.github.kittinunf.fuel.Fuel.RequestConvertible}
         */
        @JvmStatic
        fun request(convertible: RequestConvertible): Request = FuelManager.instance.request(convertible)
    }
}

@JvmOverloads
fun String.httpGet(parameters: Parameters? = null): Request = Fuel.get(this, parameters?.flatMap { pair ->
    (pair.second as? Iterable<*>)?.map {
        "${pair.first}[]" to it
    }?.toList() ?: (pair.second as? Array<*>)?.map {
        "${pair.first}[]" to it
    }?.toList() ?: listOf(pair)
})

@JvmOverloads
fun Fuel.PathStringConvertible.httpGet(parameter: Parameters? = null): Request = Fuel.get(this, parameter)

@JvmOverloads
fun String.httpPost(parameters: Parameters? = null): Request = Fuel.post(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpPost(parameter: Parameters? = null): Request =
        Fuel.post(this, parameter)

@JvmOverloads
fun String.httpPut(parameters: Parameters? = null): Request = Fuel.put(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpPut(parameter: Parameters? = null): Request = Fuel.put(this, parameter)

@JvmOverloads
fun String.httpPatch(parameters: Parameters? = null): Request = Fuel.patch(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpPatch(parameter: Parameters? = null): Request =
        Fuel.patch(this, parameter)

@JvmOverloads
fun String.httpDelete(parameters: Parameters? = null): Request = Fuel.delete(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpDelete(parameter: Parameters? = null): Request =
        Fuel.delete(this, parameter)

@JvmOverloads
fun String.httpDownload(parameter: Parameters? = null): Request = Fuel.download(this, parameter)

@JvmOverloads
fun Fuel.PathStringConvertible.httpDownload(parameter: Parameters? = null): Request =
        Fuel.download(this, parameter)

@JvmOverloads
fun String.httpUpload(method: Method = Method.POST, parameters: Parameters? = null): Request =
        Fuel.upload(this, method, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpUpload(method: Method = Method.POST, parameters: Parameters? = null): Request =
        Fuel.upload(this, method, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpHead(parameter: Parameters? = null): Request =
        Fuel.head(this, parameter)

@JvmOverloads
fun String.httpHead(parameters: Parameters? = null): Request = Fuel.head(this, parameters)