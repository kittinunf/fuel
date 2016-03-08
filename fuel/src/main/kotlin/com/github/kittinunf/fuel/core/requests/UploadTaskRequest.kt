package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Manager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.copyTo
import com.github.kittinunf.fuel.util.toHexString
import com.github.kittinunf.result.Result
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InterruptedIOException
import java.net.URL
import java.net.URLConnection

class UploadTaskRequest(request: Request) : TaskRequest(request) {

    val BUFFER_SIZE = 1024

    val CRLF = "\\r\\n"
    val boundary = System.currentTimeMillis().toHexString()

    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var sourceCallback: ((Request, URL) -> File)

    var dataStream: ByteArrayOutputStream? = null
    var fileInputStream: FileInputStream? = null

    override fun call(): Result<Response, FuelError> {
        try {
            val file = sourceCallback.invoke(request, request.url)
            //file input
            fileInputStream = FileInputStream(file)
            dataStream = ByteArrayOutputStream().apply {
                write(("--" + boundary + CRLF).toByteArray())
                write(("Content-Disposition: form-data; filename=\"" + file.name + "\"").toByteArray())
                write(CRLF.toByteArray())
                write(("Content-Type: " + URLConnection.guessContentTypeFromName(file.name)).toByteArray())
                write(CRLF.toByteArray())
                write(CRLF.toByteArray())
                flush()

                //input file data
                fileInputStream!!.copyTo(this, BUFFER_SIZE) { writtenBytes ->
                    progressCallback?.invoke(writtenBytes, file.length())
                }

                write(CRLF.toByteArray())
                flush()
                write(("--$boundary--").toByteArray())
                write(CRLF.toByteArray())
                flush()
            }

            request.body(dataStream!!.toByteArray())
            return Result.Success(Manager.instance.client.executeRequest(request).apply { dispatchCallback(this) })
        } catch(error: FuelError) {
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            return Result.error(error)
        } finally {
            dataStream?.close()
            fileInputStream?.close()
        }
    }
}