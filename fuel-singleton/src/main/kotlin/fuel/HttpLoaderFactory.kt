// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/ImageLoaderFactory.kt

package fuel

public fun interface HttpLoaderFactory {
    /**
     * Return a new [HttpLoader]
     */
    public fun newHttpLoader(): HttpLoader
}
