package com.github.kittinunf.fuel.util

import java.io.BufferedOutputStream
import java.io.OutputStream

typealias WriteProgress = (Long) -> Unit
class ProgressOutputStream(
    stream: OutputStream,
    val onProgress: WriteProgress
) : BufferedOutputStream(stream) {
    var position = 0L

    // Report progress if the producer is efficient
    override fun write(b: ByteArray?, off: Int, len: Int) {
        super.write(b, off, len)
        position += len
        onProgress.invoke(position)
    }
}
