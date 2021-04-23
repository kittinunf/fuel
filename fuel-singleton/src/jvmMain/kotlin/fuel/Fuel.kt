// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/Coil.kt
@file:Suppress("unused")

package fuel

public object Fuel {
    private var httpLoader: HttpLoader? = null
    private var httpLoaderFactory: HttpLoaderFactory? = null

    /**
     * Get the default [HttpLoader]. Creates a new instance if none has been set.
     */
    public fun loader(): HttpLoader = httpLoader ?: newHttpLoader()

    /**
     * Set the default [HttpLoader]. Prefer using `setHttpLoader(HttpLoaderFactory)`
     * to create the [HttpLoader] lazily.
     */
    @Synchronized
    public fun setHttpLoader(loader: HttpLoader) {
        httpLoaderFactory = null
        httpLoader = loader
    }

    /**
     * Set the [HttpLoaderFactory] that will be used to create the default [HttpLoader].
     * The [factory] is guaranteed to be called at most once.
     */
    @Synchronized
    public fun setHttpLoader(factory: HttpLoaderFactory) {
        httpLoaderFactory = factory
        httpLoader = null
    }

    /** Create and set the new default [HttpLoader]. */
    @Synchronized
    private fun newHttpLoader(): HttpLoader {
        // Check again in case httpLoader was just set.
        httpLoader?.let { return it }

        // Create a new HttpLoader.
        val loader = httpLoaderFactory?.newHttpLoader() ?: FuelBuilder().build()
        httpLoaderFactory = null
        httpLoader = loader
        return loader
    }
}
