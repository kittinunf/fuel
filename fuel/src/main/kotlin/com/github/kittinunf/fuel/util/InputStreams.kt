package com.github.kittinunf.fuel.util

import java.io.InputStream
import java.io.OutputStream

typealias CopyProgress = (Long) -> Unit
internal fun InputStream.copyTo(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    onProgress: CopyProgress?,
    onComplete: CopyProgress? = onProgress
): Long {
    var bytesCopied = 0L
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)

    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        onProgress?.invoke(bytesCopied)
        bytes = read(buffer)
    }

    onComplete?.invoke(bytesCopied)
    return bytesCopied
}
