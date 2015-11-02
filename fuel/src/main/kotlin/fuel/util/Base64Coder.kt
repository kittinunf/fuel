package fuel.util

import java.lang.reflect.Method

/**
 * Credit to https://github.com/simia-tech/epd-kotlin for this reflection work.
 */
internal object Base64Coder {
    private val classLoader = ClassLoader.getSystemClassLoader()

    fun encode(value: ByteArray): String =
            when {
                jdkEncodeMethod != null -> jdkEncodeMethod.invoke(jdkClass, value) as String
                androidEncodeMethod != null -> (androidEncodeMethod.invoke(androidClass, value, 2) as String)
                else -> throw Exception("no base64 coder class found")
            }

    fun decode(encoded: String): ByteArray =
            when {
                jdkDecodeMethod != null -> jdkDecodeMethod.invoke(jdkClass, encoded) as ByteArray
                androidDecodeMethod != null -> androidDecodeMethod.invoke(androidClass, encoded, 2) as ByteArray
                else -> throw Exception("no base64 coder class found")
            }

    private val jdkClass: Class<*>? = run {
        try {
            classLoader.loadClass("javax.xml.bind.DatatypeConverter")
        } catch (exception: ClassNotFoundException) {
            null
        }
    }

    private val jdkEncodeMethod: Method? =
            jdkClass?.getMethod("printBase64Binary", ByteArray::class.java)

    private val jdkDecodeMethod: Method? =
            jdkClass?.getMethod("parseBase64Binary", String::class.java)

    private val androidClass: Class<*>? = run {
        try {
            classLoader.loadClass("android.util.Base64")
        } catch (exception: ClassNotFoundException) {
            null
        }
    }

    private val androidEncodeMethod: Method? =
            androidClass?.getMethod("encodeToString", ByteArray::class.java, Int::class.java)

    private val androidDecodeMethod: Method? =
            androidClass?.getMethod("decode", String::class.java, Int::class.java)

}