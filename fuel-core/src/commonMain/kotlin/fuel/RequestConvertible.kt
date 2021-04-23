package fuel

/**
 * Anything that is [RequestConvertible] can be used as [request]
 */
public interface RequestConvertible {
    public val request: Request
}
