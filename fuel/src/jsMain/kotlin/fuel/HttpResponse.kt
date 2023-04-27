package fuel

import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob

public actual class HttpResponse {
    public var statusCode: Int = -1
    public lateinit var array: ArrayBuffer
    public var body: String = ""
    public lateinit var blob: Blob
}
