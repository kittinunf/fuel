package fuel.samples

import okhttp3.ResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.Source

class ProgressForwardingSource(
    source: Source,
    private val responseBody: ResponseBody,
    private val progressListener: ProgressListener
) : ForwardingSource(source) {
    private var totalBytesRead = 0L

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = super.read(sink, byteCount)
        // read() returns the number of bytes read, or -1 if this source is exhausted.
        totalBytesRead += if (bytesRead != -1L) bytesRead else 0
        progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
        return bytesRead
    }
}
