package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.test.MockReflected
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

class ReadmeIntegrityTest : MockHttpTestCase() {

    /*
    // This silences the printing so it doesn't pollute the log
    private val outContent = ByteArrayOutputStream()
    private val originalOut = System.out

    @Before
    fun prepareStream() {
        System.setOut(PrintStream(outContent))
    }

    @After
    fun teardownStream() {
        System.setOut(originalOut)
    }*/

    @Test
    fun makingRequestsExample() {
        reflectedRequest(Method.GET, "get")
            .response { request, response, result ->
                println("[request] $request")
                println("[response] $response")
                val (bytes, error) = result
                if (bytes != null) {
                    println("[response bytes] ${String(bytes)}")
                }

                assertThat("Expected bytes, actual error $error", bytes, notNullValue())
            }
            .join()
    }

    @Test
    fun makingRequestsAboutPatchRequests() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value),
            response = mock.reflect()
        )

        Fuel.patch(mock.path("/post"))
            .also { println("[request] $it") }
            .response()
    }

    @Test
    fun makingRequestsAddingParametersGet() {
        val parameters = listOf("foo" to "foo", "bar" to "bar")
        val endpoint = "https://httpbin.org/get"

        val request = Fuel.get(endpoint, parameters = parameters)
        assertThat(request.url.toString(), equalTo("$endpoint?foo=foo&bar=bar"))
        val request2 = endpoint.httpGet(parameters = parameters)
        assertThat(request2.url.toString(), equalTo("$endpoint?foo=foo&bar=bar"))
    }

    @Test
    fun makingRequestsAddingParametersDelete() {
        val parameters = listOf("foo" to "foo", "bar" to "bar")
        val endpoint = "https://httpbin.org/delete"

        val request = Fuel.delete(endpoint, parameters = parameters)
        assertThat(request.url.toString(), equalTo("$endpoint?foo=foo&bar=bar"))
        val request2 = endpoint.httpDelete(parameters = parameters)
        assertThat(request2.url.toString(), equalTo("$endpoint?foo=foo&bar=bar"))
    }

    @Test
    fun makingRequestsAddingParametersGetArray() {
        val parameters = listOf("foo" to "foo", "dwarf" to arrayOf("grumpy", "happy", "sleepy", "dopey"))
        val endpoint = "https://httpbin.org/get"

        val request = Fuel.get(endpoint, parameters = parameters)
        assertThat(request.url.toString(), equalTo("$endpoint?foo=foo&dwarf%5B%5D=grumpy&dwarf%5B%5D=happy&dwarf%5B%5D=sleepy&dwarf%5B%5D=dopey"))
        val request2 = endpoint.httpGet(parameters = parameters)
        assertThat(request2.url.toString(), equalTo("$endpoint?foo=foo&dwarf%5B%5D=grumpy&dwarf%5B%5D=happy&dwarf%5B%5D=sleepy&dwarf%5B%5D=dopey"))
    }

    @Test
    fun makingRequestsAddingParametersPost() {
        val parameters = listOf("foo" to "foo", "bar" to "bar")
        val expectedBody = parameters.joinToString("&") { it -> "${it.first}=${it.second}" }
        val endpoint = "https://httpbin.org/post"

        val request = Fuel.post(endpoint, parameters = parameters)
        assertThat(request.url.toString(), equalTo(endpoint))
        assertThat(String(request.body.toByteArray()), equalTo(expectedBody))
        val request2 = endpoint.httpPost(parameters = parameters)
        assertThat(request2.url.toString(), equalTo(endpoint))
        assertThat(String(request2.body.toByteArray()), equalTo(expectedBody))
    }

    @Test
    fun makingRequestsAddingParametersPut() {
        val parameters = listOf("foo" to "foo", "bar" to "bar")
        val expectedBody = parameters.joinToString("&") { it -> "${it.first}=${it.second}" }
        val endpoint = "https://httpbin.org/put"

        val request = Fuel.put(endpoint, parameters = parameters)
        assertThat(request.url.toString(), equalTo(endpoint))
        assertThat(String(request.body.toByteArray()), equalTo(expectedBody))
        val request2 = endpoint.httpPut(parameters = parameters)
        assertThat(request2.url.toString(), equalTo(endpoint))
        assertThat(String(request2.body.toByteArray()), equalTo(expectedBody))
    }

    @Test
    fun makingRequestsAddingParametersPatch() {
        val parameters = listOf("foo" to "foo", "bar" to "bar")
        val expectedBody = parameters.joinToString("&") { it -> "${it.first}=${it.second}" }
        val endpoint = "https://httpbin.org/patch"

        val request = Fuel.patch(endpoint, parameters = parameters)
        assertThat(request.url.toString(), equalTo(endpoint))
        assertThat(String(request.body.toByteArray()), equalTo(expectedBody))
        val request2 = endpoint.httpPatch(parameters = parameters)
        assertThat(request2.url.toString(), equalTo(endpoint))
        assertThat(String(request2.body.toByteArray()), equalTo(expectedBody))
    }

    @Test
    fun makingRequestsAddingRequestBody() {
        val body = "My Post Body"

        reflectedRequest(Method.POST, "post")
            .body(body)
            .also { println(it) }
            .responseObject(MockReflected.Deserializer()) { result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected body to be set", data!!.body?.string, notNullValue())
                assertThat(data.body!!.string, equalTo(body))
            }
            .join()
    }

    @Test
    fun makingRequestsAddingRequestBodyUseApplicationJson() {
        val body = "{ \"foo\" : \"bar\" }"

        reflectedRequest(Method.POST, "post")
            .jsonBody(body)
            .also { println(it) }
            .also { request -> assertThat(request.headers[Headers.CONTENT_TYPE].lastOrNull(), equalTo("application/json")) }
            .responseObject(MockReflected.Deserializer()) { result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected body to be set", data!!.body?.string, notNullValue())
                assertThat(data.body!!.string, equalTo(body))
            }
            .join()
    }

    @Test
    fun makingRequestsAddingRequestBodyFromString() {
        val body = "my body is plain"

        reflectedRequest(Method.POST, "post")
            .header(Headers.CONTENT_TYPE, "text/plain")
            .body(body)
            .also { println(it) }
            .responseObject(MockReflected.Deserializer()) { result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected body to be set", data!!.body?.string, notNullValue())
                assertThat(data.body!!.string, equalTo(body))
            }
            .join()
    }

    @Test
    fun makingRequestsAddingRequestBodyFromFile() {
        val contents = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."

        val file = File.createTempFile("lipsum", ".txt")
        file.writeText(contents)

        reflectedRequest(Method.POST, "post")
            .header(Headers.CONTENT_TYPE, "text/plain")
            .body(file)
            .also { println(it) }
            .responseObject(MockReflected.Deserializer()) { result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected body to be set", data!!.body?.string, notNullValue())
                assertThat(data.body!!.string, equalTo(contents))
            }
            .join()
    }

    @Test
    fun makingRequestsAddingRequestBodyFromInputStream() {
        val contents = "source-string-from-string"
        val stream = ByteArrayInputStream(contents.toByteArray())

        reflectedRequest(Method.POST, "post")
            .header(Headers.CONTENT_TYPE, "text/plain")
            .body(stream)
            .also { println(it) }
            .responseObject(MockReflected.Deserializer()) { result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected body to be set", data!!.body?.string, notNullValue())
                assertThat(data.body!!.string, equalTo(contents))
            }
            .join()
    }

    @Test
    fun makingRequestsAddingRequestBodyFromLazySource() {
        val contents = "source-string-from-string"
        val produceStream = { ByteArrayInputStream(contents.toByteArray()) }

        reflectedRequest(Method.POST, "post")
            .header(Headers.CONTENT_TYPE, "text/plain")
            .body(produceStream)
            .also { println(it) }
            .responseObject(MockReflected.Deserializer()) { result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected body to be set", data!!.body?.string, notNullValue())
                assertThat(data.body!!.string, equalTo(contents))
            }
            .join()
    }
}
