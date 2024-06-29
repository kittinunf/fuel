package fuel

public interface FuelRouting : RequestConvertible {
    /**
     * Base path handler for the remote call.
     */
    public val basePath: String

    /**
     * Method handler for the remote requests.
     */
    public val method: String

    /**
     * Path handler for the request.
     */
    public val path: String

    /**
     * Parameters for the remote call.
     * It uses a pair with String, String.
     */
    public val parameters: Parameters?

    /**
     * Headers for remote call.
     */
    public val headers: Map<String, String>?

    /**
     * Body to handle other type of request (e.g. application/json )
     */
    public val body: String?

    override val request: Request
        get() =
            Request(
                "$basePath/$path",
                parameters,
                headers.orEmpty(),
                body,
                method
            )
}
