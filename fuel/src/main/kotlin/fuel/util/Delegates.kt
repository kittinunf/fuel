package fuel.util

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

public fun Delegates.readWriteLazy<T> (initializer: () -> T): ReadWriteProperty<Any?, T> = ReadWriteLazyVal(initializer)

private class ReadWriteLazyVal<T>(private val initializer: () -> T) : ReadWriteProperty<Any?, T> {

    private var value: Any? = null

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        if (value == null) {
            value = (initializer()) ?: throw IllegalStateException("Initializer block of property ${desc.name} return null")
        }
        [suppress("UNCHECKED_CAST")]
        return value as T
    }

    override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        this.value = value
    }

}