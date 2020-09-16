// Copied By https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/network/HttpException.kt

package fuel

import okhttp3.Response

/**
 * Exception for an unexpected, non-2xx HTTP response.
 *
 * @see HttpUrlFetcher
 */
public class HttpException(response: Response) : RuntimeException("HTTP ${response.code}: ${response.message}")
