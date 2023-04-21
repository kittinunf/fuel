package fuel

public object Fuel {
    private var httpLoader: HttpLoader? = null
    private var httpLoaderFactory: HttpLoaderFactory? = null

    public fun loader(): HttpLoader = httpLoader ?: newHttpLoader()

    public fun setHttpLoader(loader: HttpLoader) {
        httpLoaderFactory = null
        httpLoader = loader
    }

    public fun setHttpLoader(factory: HttpLoaderFactory) {
        httpLoaderFactory = factory
        httpLoader = null
    }

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
