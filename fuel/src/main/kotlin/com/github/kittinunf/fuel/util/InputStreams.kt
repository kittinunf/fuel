package com.github.kittinunf.fuel.util

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

internal fun InputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, progress: ((Long) -> Unit)?, onComplete: ((ByteArray) -> Unit)? = null): Long {
    var bytesCopied = 0L
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    val byteBuffer = ByteArrayOutputStream(bufferSize)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        byteBuffer.write(buffer, 0, bytes)
        bytesCopied += bytes
        progress?.invoke(bytesCopied)
        bytes = read(buffer)
    }
    onComplete?.invoke(byteBuffer.toByteArray())
    return bytesCopied
}
