// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/Coil.kt
@file:Suppress("MemberVisibilityCanBePrivate")

package fuel

object Fuel {
    private var httpLoader: HttpLoader? = null
    private var httpLoaderFactory: HttpLoaderFactory? = null

    /**
     * Get the default [HttpLoader]. Creates a new instance if none has been set.
     */
    fun httpLoader(): HttpLoader = httpLoader ?: newHttpLoader()

    /**
     * Set the default [HttpLoader]. Shutdown the current instance if there is one.
     */
    fun setHttpLoader(loader: HttpLoader) {
        setHttpLoader { loader }
    }

    /**
     * Set the [HttpLoaderFactory] that will be used to create the default [HttpLoader].
     * Shutdown the current instance if there is one. The [factory] is guaranteed to be called at most once.
     */
    @Synchronized
    fun setHttpLoader(factory: HttpLoaderFactory) {
        httpLoaderFactory = factory
        httpLoader = null
    }

    /** Create and set the new default [HttpLoader]. */
    @Synchronized
    private fun newHttpLoader(): HttpLoader {
        // Check again in case httpLoader was just set.
        httpLoader?.let { return it }

        // Create a new HttpLoader.
        val loader = httpLoaderFactory?.newHttpLoader() ?: HttpLoader()
        httpLoaderFactory = null
        setHttpLoader(loader)
        return loader
    }
}
