package com.github.kittinunf.fuel.test

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.util.decodeBase64
import org.json.JSONArray
import org.json.JSONObject
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpTemplate
import org.slf4j.event.Level
import java.net.URL

class MockHelper {

    private lateinit var mockServer: ClientAndServer

    fun setup(logLevel: Level = Level.WARN) {
        // This is not placed in a @BeforeClass / @BeforeAll so that the tests may have parallel
        // execution. When there is no port given as first argument, it will grab a free port
        this.mockServer = ClientAndServer.startClientAndServer()

        System.setProperty("mockserver.logLevel", logLevel.name)
    }

    fun tearDown() {
        mockServer.stop()
    }

    /**
     * The mock server for the current test run.
     *
     * Do not store this in a class variable as that will prohibit individual test cases from being
     *   correctly parallelised without copying the class, and you can't / should not rely on
     *   expected requests to be available across tests.
     *
     * @return [ClientAndServer]
     */
    fun server(): ClientAndServer = this.mockServer

    /**
     * Convenience method to request a request to its expected response
     *
     * @see request
     * @see response
     * @see reflect
     *
     * @example When you need different responses for the same request, you can reuse the request
     *   with multiple calls to this method:
     *
     *   val request = mock.request().withPath('/different-each-time')
     *   mock.chain(request, mock.response().withStatusCode(500))
     *   mock.chain(request, mock.response().withStatusCode(502))
     *   mock.chain(request, mock.response().withStatusCode(204))
     *
     *   // fetch('/different-each-time) => 500
     *   // fetch('/different-each-time) => 502
     *   // fetch('/different-each-time) => 204
     *
     * @param request [HttpRequest] the request
     * @param response [HttpResponse] the response
     * @param times [Times] times this can occur, defaults to once
     * @param server [ClientAndServer] the server to register on
     */
    fun chain(
        request: HttpRequest,
        response: HttpResponse,
        times: Times = Times.once(),
        server: ClientAndServer = server()
    ) {
        server.`when`(request, times).respond(response)
    }

    /**
     * @see chain(HttpRequest, HttpResponse, Times, ClientAndServer)
     */
    fun chain(
        request: HttpRequest,
        response: HttpTemplate,
        times: Times = Times.once(),
        server: ClientAndServer = server()
    ) {
        server.`when`(request, times).respond(response)
    }

    /**
     * Creates a new mock request.
     *
     * This method is introduced to keep the import out of test cases and to make it easy to replace
     *   the library for mocking requests.
     *
     * @example mock request for posting on a path
     *
     *   val request = mock.request().withMethod(Method.POST.value).withPath('/post-path')
     *
     * @return [HttpRequest]
     */
    fun request(): HttpRequest = HttpRequest.request()

    /**
     * Creates a new mock response.
     *
     * This method is introduced to keep the import out of test cases and to make it easy to replace
     *   the library for mocking responses.
     */
    fun response(): HttpResponse = HttpResponse.response()

    /**
     * Creates a new mock response template.
     *
     * @see REFLECT_TEMPLATE
     * @see reflect
     *
     * This method is introduced to keep the import out of test cases and to make it easy to replace
     *   the library for mocking requests.
     */
    fun responseTemplate(): HttpTemplate = HttpTemplate.template(HttpTemplate.TemplateType.JAVASCRIPT)

    /**
     * Creates a mock response that reflects what is coming in via the REFLECT_TEMPLATE template
     *
     * @see REFLECT_TEMPLATE
     *
     * This method is introduced to keep the import out of test cases and to make it easy to replace
     *   the library for mocking requests.
     */
    fun reflect(): HttpTemplate = responseTemplate().withTemplate(REFLECT_TEMPLATE)

    /**
     * Generates the full path for a request to the given path
     *
     * @param path [String] the relative path
     * @return [String] the full path
     */
    fun path(path: String): String = URL("http://localhost:${server().localPort}/$path").toString()

    companion object {
        const val REFLECT_TEMPLATE = """
            return {
                'statusCode': 200,
                'headers': {
                    'Date' : [ Date() ],
                    'Content-Type' : [ 'application/json' ],
                    'Cookie' : request.headers['cookie'] || []
                },
                'body': JSON.stringify(
                    {
                        method: request.method,
                        path: request.path,
                        query: request.queryStringParameters,
                        body: request.body,
                        headers: request.headers,
                        reflect: true,
                        userAgent: (request.headers['user-agent'] || request.headers['User-Agent'] || [])[0]                    }
                )
            };
        """
    }
}

data class MockReflected(
    val method: String,
    val path: String,
    val query: Parameters = listOf(),
    val body: MockReflectedBody? = null,
    val headers: Headers = Headers(),
    val reflect: Boolean = true,
    val userAgent: String? = null
) {

    operator fun get(key: String) = headers[key]

    class Deserializer : ResponseDeserializable<MockReflected> {
        override fun deserialize(content: String) = MockReflected.from(JSONObject(content))
    }

    companion object {
        fun from(json: JSONObject): MockReflected {
            val base = MockReflected(
                method = json.getString("method"),
                path = json.getString("path")
            )

            return json.keySet().fold(base) { current, key ->

                if (json.isNull(key)) {
                    current
                } else {
                    when (key) {
                        "query" -> {
                            val queryObject = json.getJSONObject(key)
                            current.copy(
                                query = queryObject.keySet().fold(listOf()) { query, parameter ->
                                    if (queryObject.isNull(parameter)) {
                                        query.plus(Pair(parameter, null))
                                    } else {
                                        val values = queryObject.get(parameter)
                                        when (values) {
                                            is JSONArray -> query.plus(Pair(parameter, values.toList()))
                                            else -> query.plus(Pair(parameter, values.toString()))
                                        }
                                    }
                                }
                            )
                        }
                        "body" -> current.copy(body = MockReflectedBody.from(
                            json.optJSONObject(key) ?: JSONObject().put("type", "STRING").put("string", json.getString(key))
                        ))
                        "headers" -> current.copy(headers = Headers.from(json.getJSONObject(key).toMap()))
                        "reflect" -> current.copy(reflect = json.getBoolean(key))
                        "userAgent" -> current.copy(userAgent = json.getString("userAgent"))
                        else -> current
                    }
                }
            }
        }
    }
}

data class MockReflectedBody(
    val type: String,
    val string: String? = null,
    val binary: ByteArray? = null,
    val contentType: String? = null
) {
    companion object {
        fun from(json: JSONObject): MockReflectedBody {
            val base = MockReflectedBody(
                type = json.getString("type"),
                contentType = json.optString("contentType")
            )

            return when (base.type) {
                "STRING" -> base.copy(string = json.getString("string"))
                "BINARY" -> base.copy(binary = json.getString("binary").decodeBase64())
                else -> base
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockReflectedBody

        if (type != other.type) return false
        if (string != other.string) return false
        if (binary != null) {
            if (other.binary == null) return false
            if (!binary.contentEquals(other.binary)) return false
        } else if (other.binary != null) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (string?.hashCode() ?: 0)
        result = 31 * result + (binary?.contentHashCode() ?: 0)
        result = 31 * result + contentType.hashCode()
        return result
    }
}