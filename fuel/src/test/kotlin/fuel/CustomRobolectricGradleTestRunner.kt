package fuel

import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.robolectric.manifest.AndroidManifest
import org.robolectric.res.FileFsFile

/**
 * Created by Kittinun Vantasin on 8/7/15.
 */

public class CustomRobolectricGradleTestRunner(clazz: Class<*>) : RobolectricGradleTestRunner(clazz) {

    override fun getAppManifest(config: Config): AndroidManifest {
        val appManifest = super.getAppManifest(config)
        var androidManifestFile = appManifest.androidManifestFile

        if (androidManifestFile.exists()) {
            return appManifest
        } else {
            val moduleRoot = getModuleRootPath(config)
            androidManifestFile = FileFsFile.from(moduleRoot, appManifest.androidManifestFile.path)
            val resDirectory = FileFsFile.from(moduleRoot, appManifest.androidManifestFile.path.replace("AndroidManifest.xml", "res"))
            val assetsDirectory = FileFsFile.from(moduleRoot, appManifest.androidManifestFile.path.replace("AndroidManifest.xml", "assets"))
            return AndroidManifest(androidManifestFile, resDirectory, assetsDirectory)
        }
    }

    private fun getModuleRootPath(config: Config): String {
        val moduleRoot = config.constants.javaClass.getResource("").toString().replace("file:", "").replace("jar:", "")
        return moduleRoot.substring(0, moduleRoot.indexOf("/build"))
    }

}