package fuel

public fun interface HttpLoaderFactory {
    /**
     * Return a new [HttpLoader]
     */
    public fun newHttpLoader(): HttpLoader
}
