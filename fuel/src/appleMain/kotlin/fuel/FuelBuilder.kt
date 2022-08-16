package fuel

import platform.Foundation.NSURLSessionConfiguration

public actual class FuelBuilder {
    private var sessionConfiguration: NSURLSessionConfiguration? = null

    private val defaultSessionConfiguration by lazy { NSURLSessionConfiguration.defaultSessionConfiguration }

    init {
        failOnNativeIfLegacyMemoryManager()
    }

    public fun config(sessionConfiguration: NSURLSessionConfiguration): FuelBuilder = apply {
        this.sessionConfiguration = sessionConfiguration
    }

    public actual fun build(): HttpLoader = HttpLoader(sessionConfiguration ?: defaultSessionConfiguration)
}
