package com.github.kittinunf.fuel.util

import java.io.BufferedInputStream
import java.io.InputStream

typealias ReadProgress = (Long) -> Unit
class ProgressInputStream(stream: InputStream, val onProgress: ReadProgress) : BufferedInputStream(stream) {
    var position = 0L

    override fun reset() {
        super.reset()
        // Going back at most current buffer position - marked buffer position
        position -= (pos - markpos)
    }

    override fun skip(n: Long): Long {
        return super.skip(n).apply {
            position + n
        }
    }

    // Report progress if the consumer is efficient
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        return super.read(b, off, len).apply {
            position += Math.max(this, 0)
            onProgress.invoke(position)
        }
    }
}
