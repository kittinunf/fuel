package fuel

/**
 * Anything that is [RequestConvertible] can be used as [request]
 */
interface RequestConvertible {
    val request: Request
}
