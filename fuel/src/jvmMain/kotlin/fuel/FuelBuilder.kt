package fuel

import okhttp3.Call
import okhttp3.OkHttpClient

public actual class FuelBuilder {
    private var callFactory: Lazy<Call.Factory>? = null

    public fun config(callFactory: Call.Factory): FuelBuilder =
        apply {
            this.callFactory = lazyOf(callFactory)
        }

    public fun config(initializer: () -> Call.Factory): FuelBuilder =
        apply {
            this.callFactory = lazy(initializer)
        }

    public actual fun build(): HttpLoader = JVMHttpLoader(callFactoryLazy = callFactory ?: lazy { OkHttpClient() })
}
