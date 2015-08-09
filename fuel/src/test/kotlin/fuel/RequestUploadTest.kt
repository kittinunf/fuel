package fuel

import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import fuel.toolbox.HttpClient
import fuel.util.build
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 6/22/15.
 */

class RequestUploadTest : BaseTestCase() {

    val manager: Manager by Delegates.lazy {
        build(Manager()) {
            client = HttpClient()
            basePath = "http://httpbin.org"
            callbackExecutor = object : Executor {
                override fun execute(command: Runnable) {
                    command.run()
                }
            }
        }
    }

    val currentDir: File by Delegates.lazy {
        val dir = System.getProperty("user.dir")
        File(dir, "src/test/assets")
    }

    Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    Test
    fun httpUploadCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post").source { request, url ->
            File(currentDir, "lorem_ipsum_short.tmp")
        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode")
    }

    Test
    fun httpUploadWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        manager.upload("/post").source { request, url ->
            File(currentDir, "lorem_ipsum_long.tmp")
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            println("read: $read, total: $total")
        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        assertTrue(read == total && read != -1L && total != -1L, "read bytes and total bytes should be equal")
        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode")
    }

    Test
    fun httpUploadWithProgressInvalidEndPointCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/pos").source { request, url ->
            File(currentDir, "lorem_ipsum_short.tmp")
        }.progress { readBytes, totalBytes ->

        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNotNull(error, "error should not be null")
        assertNull(data, "data should be null")

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode")
    }

    Test
    fun httpUploadWithProgressInvalidFileCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post").source { request, url ->
            File(currentDir, "not_found_file.tmp")
        }.progress { readBytes, totalBytes ->

        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNotNull(error, "error should not be null")
        assertNull(data, "data should be null")

        val statusCode = -1
        assertTrue { error?.exception is FileNotFoundException }
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode")
    }

}
