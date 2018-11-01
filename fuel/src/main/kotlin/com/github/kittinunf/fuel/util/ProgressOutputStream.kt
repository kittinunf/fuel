package com.github.kittinunf.fuel.util

import java.io.FilterOutputStream
import java.io.OutputStream

typealias WriteProgress = (Long) -> Unit

/**
 * Stream that reports write progress upon efficient writes.
 *
 * @see ProgressInputStream
 *
 * @note the callback is called as many times as the [OutputStream] producer is calling write When this stream is used
 *   to report progress back to a user, you probably want to tween between values, to make it look like there is more
 *   frequent activity, although this actually represents the actual progress.
 *
 * @param stream [OutputStream] the stream that should have progress reporting
 * @param onProgress [WriteProgress] the progress callback
 */
class ProgressOutputStream(stream: OutputStream, val onProgress: WriteProgress) : FilterOutputStream(stream) {
    var position = 0L

    // Report progress if the producer is writing multiple bytes.
    //
    // This means that the amount of times the progress is reported, ties exactly into the number of times the `write`
    // function is called, instead of relying on some arbitrary, but fake, progress.
    //
    override fun write(b: ByteArray?, off: Int, len: Int) {
        super.write(b, off, len)
        position += len
        onProgress.invoke(position)
    }
}