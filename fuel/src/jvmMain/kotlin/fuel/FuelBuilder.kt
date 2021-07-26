package fuel

import okhttp3.Call
import okhttp3.OkHttpClient

public actual class FuelBuilder {
    private var callFactory: Call.Factory? = null

    public fun config(callFactory: Call.Factory): FuelBuilder = apply {
        this.callFactory = callFactory
    }

    public fun config(initializer: () -> Call.Factory): FuelBuilder = apply {
        this.callFactory = lazyCallFactory(initializer)
    }

    public actual fun build() : HttpLoader = HttpLoader(callFactory ?: buildDefaultCallFactory())

    private fun buildDefaultCallFactory() = lazyCallFactory {
        OkHttpClient.Builder().build()
    }
}