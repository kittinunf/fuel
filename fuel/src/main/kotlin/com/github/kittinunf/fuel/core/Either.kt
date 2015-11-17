package com.github.kittinunf.fuel.core

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

@Suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
sealed public class Either<out L, out R> {

    public operator abstract fun component1(): L?
    public operator abstract fun component2(): R?

    public inline fun fold(fl: (L) -> Unit, fr: (R) -> Unit) {
        return when (this) {
            is Left<L, R> -> fl(this.left)
            is Right<L, R> -> fr(this.right)
            else -> throw UnsupportedOperationException()
        }
    }

    public inline fun left(fl: (L) -> Unit) {
        if (this is Left<L, R>) fl(this.left)
    }

    public inline fun right(fr: (R) -> Unit) {
        if (this is Right<L, R>) fr(this.right)
    }

    public fun swap(): Either<R, L> {
        return when (this) {
            is Left<L, R> -> Right(this.left)
            is Right<L, R> -> Left(this.right)
            else -> throw UnsupportedOperationException()
        }
    }

    public fun <X> get(): X {
        @Suppress("UNCHECKED_CAST")
        return when (this) {
            is Left<L, R> -> this.left as X
            is Right<L, R> -> this.right as X
            else -> throw UnsupportedOperationException()
        }
    }

    public class Left<L, R>(val left: L) : Either<L, R>() {

        public override fun component1() = left
        public override fun component2() = null

    }

    public class Right<L, R>(val right: R) : Either<L, R>() {

        public override fun component1() = null
        public override fun component2() = right

    }
}

