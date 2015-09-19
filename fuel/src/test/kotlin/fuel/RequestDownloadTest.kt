package fuel

import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import fuel.util.build
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/29/15.
 */

class RequestDownloadTest : BaseTestCase() {

    val manager: Manager by lazy {
        build(Manager()) {
            basePath = "http://httpbin.org"
            callbackExecutor = object : Executor {
                override fun execute(command: Runnable) {
                    command.run()
                }
            }
        }
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpDownloadCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val numberOfBytes = 32768L

        manager.download("/bytes/$numberOfBytes").destination { response, url ->
            val f = File.createTempFile(numberOfBytes.toString(), null)
            println(f.absolutePath)
            f
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

    @Test
    fun httpDownloadWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        val numberOfBytes = 1048576L
        manager.download("/bytes/$numberOfBytes").destination { response, url ->
            val f = File.createTempFile(numberOfBytes.toString(), null)
            println(f.absolutePath)
            f
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

    @Test
    fun httpDownloadWithProgressInvalidEndPointCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val numberOfBytes = 131072
        manager.download("/byte/$numberOfBytes").destination { response, url ->
            val f = File.createTempFile(numberOfBytes.toString(), null)
            println(f.absolutePath)
            f
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

    @Test
    fun httpDownloadWithProgressInvalidFileCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val numberOfBytes = 131072
        manager.download("/bytes/$numberOfBytes").destination { response, url ->
            val dir = System.getProperty("user.dir")
            File.createTempFile("not_found_file", null, File(dir, "not-a-folder"))
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
        assertTrue { error?.exception is IOException }
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode")
    }

}
