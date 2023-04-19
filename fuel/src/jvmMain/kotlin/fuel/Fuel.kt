package fuel

public actual object Fuel {
    private var httpLoader: HttpLoader? = null
    private var httpLoaderFactory: HttpLoaderFactory? = null

    /**
     * Get the default [HttpLoader]. Creates a new instance if none has been set.
     */
    public actual fun loader(): HttpLoader = httpLoader ?: newHttpLoader()

    /**
     * Set the default [HttpLoader]. Prefer using `setHttpLoader(HttpLoaderFactory)`
     * to create the [HttpLoader] lazily.
     */
    @Synchronized
    public actual fun setHttpLoader(loader: HttpLoader) {
        httpLoaderFactory = null
        httpLoader = loader
    }

    @Synchronized
    public actual fun setHttpLoader(factory: HttpLoaderFactory) {
        httpLoaderFactory = factory
        httpLoader = null
    }

    /** Create and set the new default [HttpLoader]. */
    @Synchronized
    private fun newHttpLoader(): HttpLoader {
        // Check again in case httpLoader was just set.
        httpLoader?.let { return it }

        // Create a new HttpLoader.
        val loader = httpLoaderFactory?.newHttpLoader() ?: JVMHttpLoader()
        httpLoaderFactory = null
        httpLoader = loader
        return loader
    }
}
