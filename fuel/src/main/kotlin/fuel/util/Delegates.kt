package fuel.util

import kotlin.properties.ReadWriteProperty

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

public fun readWriteLazy<T> (initializer: () -> T): ReadWriteProperty<Any?, T> = ReadWriteLazyVal(initializer)

private class ReadWriteLazyVal<T>(private val initializer: () -> T) : ReadWriteProperty<Any?, T> {

    private var value: Any? = null

    public operator override fun get(thisRef: Any?, property: PropertyMetadata): T {
        if (value == null) {
            value = (initializer()) ?: throw IllegalStateException("Initializer block of property ${property.name} return null")
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    operator override fun set(thisRef: Any?, property: PropertyMetadata, value: T) {
        this.value = value
    }

}