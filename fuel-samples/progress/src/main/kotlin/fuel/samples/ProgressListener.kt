package fuel.samples

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}
