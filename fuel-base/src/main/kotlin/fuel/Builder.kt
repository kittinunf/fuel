package fuel

import okhttp3.Call
import okhttp3.OkHttpClient

public class Builder {

    private var callFactory: Call.Factory? = null

    /**
     * Set the [OkHttpClient] used for network requests.
     *
     * This is a convenience function for calling `callFactory(Call.Factory)`.
     */
    public fun okHttpClient(okHttpClient: OkHttpClient): Builder = callFactory(okHttpClient)

    /**
     * Set a lazy callback to create the [OkHttpClient] used for network requests.
     *
     * This is a convenience function for calling `callFactory(() -> Call.Factory)`.
     */
    public fun okHttpClient(initializer: () -> OkHttpClient): Builder = callFactory(initializer)

    /**
     * Set the [Call.Factory] used for network requests.
     *
     * Calling [okHttpClient] automatically sets this value.
     */
    public fun callFactory(callFactory: Call.Factory): Builder = apply {
        this.callFactory = callFactory
    }

    /**
     * Set a lazy callback to create the [Call.Factory] used for network requests.
     *
     * This allows lazy creation of the [Call.Factory] on a background thread.
     * [initializer] is guaranteed to be called at most once.
     *
     * Prefer using this instead of `callFactory(Call.Factory)`.
     *
     * Calling [okHttpClient] automatically sets this value.
     */
    public fun callFactory(initializer: () -> Call.Factory): Builder = apply {
        this.callFactory = lazyCallFactory(initializer)
    }

    /**
     * Create a new [HttpLoader] instance with suspend capability.
     */
    public fun build(): SuspendHttpLoader = RealSuspendHttpLoader(callFactory ?: buildDefaultCallFactory())

    /**
     * Create a new [HttpLoader] instance.
     */
    public fun buildBlocking(): HttpLoader = RealHttpLoader(callFactory ?: buildDefaultCallFactory())

    private fun buildDefaultCallFactory() = lazyCallFactory {
        OkHttpClient.Builder().build()
    }
}
