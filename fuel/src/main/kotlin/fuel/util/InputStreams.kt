package fuel.util

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Kittinun Vantasin on 5/25/15.
 */

public fun InputStream.copyTo(out: OutputStream, bufferSize: Int = defaultBufferSize, progress: ((Long) -> Unit)?): Long {
    var bytesCopied = 0L
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        progress?.invoke(bytesCopied)
        bytes = read(buffer)
    }
    return bytesCopied
}
