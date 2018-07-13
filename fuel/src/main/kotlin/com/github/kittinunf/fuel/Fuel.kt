package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
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
        internal var testConfiguration = TestConfiguration(timeout = null, blocking = false)

        @JvmStatic
        @JvmOverloads
        fun testMode(configuration: TestConfiguration.() -> Unit = {}) {
            testConfiguration = TestConfiguration().apply(configuration)
        }

        @JvmStatic
        fun regularMode() = testMode { timeout = null; blocking = false }

        //convenience methods
        //get
        @JvmStatic
        @JvmOverloads
        fun get(path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.GET, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun get(convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.GET, convertible, parameters)

        //post
        @JvmStatic
        @JvmOverloads
        fun post(path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.POST, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun post(convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.POST, convertible, parameters)

        //put
        @JvmStatic
        @JvmOverloads
        fun put(path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.PUT, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun put(convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.PUT, convertible, parameters)

        //patch
        @JvmStatic
        @JvmOverloads
        fun patch(path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.PATCH, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun patch(convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.PATCH, convertible, parameters)

        //delete
        @JvmStatic
        @JvmOverloads
        fun delete(path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.DELETE, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun delete(convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.DELETE, convertible, parameters)

        //download
        @JvmStatic
        @JvmOverloads
        fun download(path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                FuelManager.instance.download(path, parameters)

        @JvmStatic
        @JvmOverloads
        fun download(convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                download(convertible.path, parameters)

        //upload
        @JvmStatic
        @JvmOverloads
        fun upload(path: String, method: Method = Method.POST, parameters: List<Pair<String, Any?>>? = null): Request =
                FuelManager.instance.upload(path, method, parameters)

        @JvmStatic
        @JvmOverloads
        fun upload(convertible: PathStringConvertible, method: Method = Method.POST, parameters: List<Pair<String, Any?>>? = null): Request =
                upload(convertible.path, method, parameters)

        //head
        @JvmStatic
        @JvmOverloads
        fun head(path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.HEAD, path, parameters)

        @JvmStatic
        @JvmOverloads
        fun head(convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                request(Method.HEAD, convertible, parameters)

        //request
        private fun request(method: Method, path: String, parameters: List<Pair<String, Any?>>? = null): Request =
                FuelManager.instance.request(method, path, parameters)

        private fun request(method: Method, convertible: PathStringConvertible, parameters: List<Pair<String, Any?>>? = null): Request =
                request(method, convertible.path, parameters)

        @JvmStatic
        fun request(convertible: RequestConvertible): Request = FuelManager.instance.request(convertible)
    }
}

@JvmOverloads
fun String.httpGet(parameters: List<Pair<String, Any?>>? = null): Request = Fuel.get(this, parameters?.flatMap { pair ->
    (pair.second as? Iterable<*>)?.let {
        it.map { "${pair.first}[]" to it }.toList()
    } ?: listOf(pair)
})

@JvmOverloads
fun Fuel.PathStringConvertible.httpGet(parameter: List<Pair<String, Any?>>? = null): Request = Fuel.get(this, parameter)

@JvmOverloads
fun String.httpPost(parameters: List<Pair<String, Any?>>? = null): Request = Fuel.post(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpPost(parameter: List<Pair<String, Any?>>? = null): Request =
        Fuel.post(this, parameter)

@JvmOverloads
fun String.httpPut(parameters: List<Pair<String, Any?>>? = null): Request = Fuel.put(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpPut(parameter: List<Pair<String, Any?>>? = null): Request = Fuel.put(this, parameter)

@JvmOverloads
fun String.httpPatch(parameters: List<Pair<String, Any?>>? = null): Request = Fuel.patch(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpPatch(parameter: List<Pair<String, Any?>>? = null): Request =
        Fuel.patch(this, parameter)

@JvmOverloads
fun String.httpDelete(parameters: List<Pair<String, Any?>>? = null): Request = Fuel.delete(this, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpDelete(parameter: List<Pair<String, Any?>>? = null): Request =
        Fuel.delete(this, parameter)

@JvmOverloads
fun String.httpDownload(parameter: List<Pair<String, Any?>>? = null): Request = Fuel.download(this, parameter)

@JvmOverloads
fun Fuel.PathStringConvertible.httpDownload(parameter: List<Pair<String, Any?>>? = null): Request =
        Fuel.download(this, parameter)

@JvmOverloads
fun String.httpUpload(method: Method = Method.POST, parameters: List<Pair<String, Any?>>? = null): Request =
        Fuel.upload(this, method, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpUpload(method: Method = Method.POST, parameters: List<Pair<String, Any?>>? = null): Request =
        Fuel.upload(this, method, parameters)

@JvmOverloads
fun Fuel.PathStringConvertible.httpHead(parameter: List<Pair<String, Any?>>? = null): Request =
        Fuel.head(this, parameter)

@JvmOverloads
fun String.httpHead(parameters: List<Pair<String, Any?>>? = null): Request = Fuel.head(this, parameters)