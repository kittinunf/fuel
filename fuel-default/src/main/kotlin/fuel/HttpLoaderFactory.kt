// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/ImageLoaderFactory.kt
package fuel

interface HttpLoaderFactory {
    /**
     * Return a new [HttpLoader]
     */
    fun newHttpLoader(): HttpLoader
}
