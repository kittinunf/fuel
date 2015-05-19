package fuel.util

/**
 * Created by Kittinun Vantasin on 5/18/15.
 */

public inline fun <T> build(instance: T, builder: T.() -> Unit): T {
    instance.builder()
    return instance
}
