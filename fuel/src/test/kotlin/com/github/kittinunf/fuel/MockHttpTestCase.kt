package com.github.kittinunf.fuel

import org.junit.After
import org.junit.Before
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.HttpTemplate
import org.mockserver.model.HttpTemplate.template

@Suppress("MemberVisibilityCanBePrivate")
abstract class MockHttpTestCase : BaseTestCase() {

    private lateinit var mockServer: ClientAndServer

    @Before
    fun setup(){
        // This is not placed in a @BeforeClass / @BeforeAll so that the tests may have parallel
        // execution. When there is no port given as first argument, it will grab a free port
        this.mockServer = startClientAndServer()
    }

    @After
    fun tearDown(){
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
    protected fun mockServer(): ClientAndServer = this.mockServer

    /**
     * Convenience method to request a request to its expected response
     *
     * @see mockRequest
     * @see mockResponse
     * @see mockReflect
     *
     * @example When you need different responses for the same request, you can reuse the request
     *   with multiple calls to this method:
     *
     *   val request = mockRequest().withPath('/different-each-time')
     *   mockChain(request, mockResponse().withStatusCode(500))
     *   mockChain(request, mockResponse().withStatusCode(502))
     *   mockChain(request, mockResponse().withStatusCode(204))
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
    protected fun mockChain(
            request: HttpRequest, response: HttpResponse,
            times: Times = Times.once(),
            server: ClientAndServer = mockServer()) {
        server.`when`(request, times).respond(response)
    }

    /**
     * @see mockChain(HttpRequest, HttpResponse, Times, ClientAndServer)
     */
    protected fun mockChain(
            request: HttpRequest,
            response: HttpTemplate,
            times: Times = Times.once(),
            server: ClientAndServer = mockServer()) {
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
     *   val request = mockRequest().withMethod(Method.POST.value).withPath('/post-path')
     *
     * @return [HttpRequest]
     */
    protected fun mockRequest(): HttpRequest = request()

    /**
     * Creates a new mock response.
     *
     * This method is introduced to keep the import out of test cases and to make it easy to replace
     *   the library for mocking responses.
     */
    protected fun mockResponse(): HttpResponse = response()

    /**
     * Creates a new mock response template.
     *
     * @see REFLECT_TEMPLATE
     * @see mockReflect
     *
     * This method is introduced to keep the import out of test cases and to make it easy to replace
     *   the library for mocking requests.
     */
    protected fun mockResponseTemplate(): HttpTemplate = template(HttpTemplate.TemplateType.JAVASCRIPT)

    /**
     * Creates a mock response that reflects what is coming in via the REFLECT_TEMPLATE template
     *
     * @see REFLECT_TEMPLATE
     *
     * This method is introduced to keep the import out of test cases and to make it easy to replace
     *   the library for mocking requests.
     */
    protected fun mockReflect(): HttpTemplate = mockResponseTemplate().withTemplate(REFLECT_TEMPLATE)

    /**
     * Generates the full path for a request to the given path
     *
     * @param path [String] the relative path
     * @return [String] the full path
     */
    protected fun mockPath(path: String): String =  "http://localhost:${mockServer().localPort}/$path"

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
                        userAgent: request.headers['user-agent']
                    }
                )
            };
        """
    }
}