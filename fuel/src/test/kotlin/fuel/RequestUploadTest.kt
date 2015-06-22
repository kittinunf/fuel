package fuel

import android.os.Environment
import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import fuel.toolbox.HttpClient
import fuel.util.build
import java.io.File
import java.net.HttpURLConnection
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 6/22/15.
 */

class RequestUploadTest : BaseTestCase() {

    override val numberOfTestCase = 3

    val manager: Manager by Delegates.lazy {
        build(Manager()) {
            client = HttpClient()
            basePath = "http://httpbin.org"
        }
    }

    val currentDir by Delegates.lazy {
        val sd = Environment.getExternalStorageDirectory();
        val location = File(sd.getAbsolutePath() + "/test")
        location.mkdir()
        return@lazy location
    }

    public fun testHttpUploadCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post").source { request, url ->
            File(currentDir, "download_32768.tmp")
        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            countdownFulfill()
        }

        countdownWait()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code of invalid credential should be $statusCode" )
    }

    public fun testHttpUploadWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        manager.upload("/post").source { request, url ->
            File(currentDir, "downloadWithProgressValid_1048576.tmp")
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            countdownFulfill()
        }

        countdownWait()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        assertTrue(read == total && read != -1L && total != -1L, "read bytes and total bytes should be equal")
        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code of invalid credential should be $statusCode" )
    }

    public fun testHttpUploadWithProgressInvalidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/pos").source { request, url ->
            File(currentDir, "downloadWithProgressInvalid.tmp")
        }.progress { readBytes, totalBytes ->

        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            countdownFulfill()
        }

        countdownWait()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNotNull(error, "error should not be null")
        assertNull(data, "data should be null")

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertTrue(response?.httpStatusCode == statusCode, "http status code of invalid credential should be $statusCode" )
    }

}
