package fuel

public expect object Fuel {
    public fun loader(): HttpLoader
    public fun setHttpLoader(loader: HttpLoader)
    public fun setHttpLoader(factory: HttpLoaderFactory)
}