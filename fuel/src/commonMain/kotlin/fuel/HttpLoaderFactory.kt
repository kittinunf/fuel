package fuel

public interface HttpLoaderFactory {
    /**
     * Return a new [HttpLoader]
     */
    public fun newHttpLoader(): HttpLoader
}