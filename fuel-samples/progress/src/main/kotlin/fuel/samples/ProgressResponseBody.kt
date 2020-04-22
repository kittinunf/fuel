package fuel.samples

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.Source
import okio.buffer

class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressListener: ProgressListener
) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null

    override fun contentLength(): Long = responseBody.contentLength()

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun source(): BufferedSource {
        bufferedSource = bufferedSource ?: source(responseBody.source()).buffer()
        return bufferedSource as BufferedSource
    }

    private fun source(source: Source): Source = ProgressForwardingSource(source, responseBody, progressListener)
}
