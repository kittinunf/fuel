package fuel

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.posix.memcpy

public fun NSHTTPURLResponse.readHeaders(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    allHeaderFields.forEach {
        map[it.key as String] = it.value as String
    }
    return map
}

@OptIn(ExperimentalForeignApi::class)
public fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            memcpy(refTo(0), bytes, length)
        }
    }

@BetaInteropApi
public fun String.encode(): NSData = NSString.create(string = this).dataUsingEncoding(NSUTF8StringEncoding)!!
