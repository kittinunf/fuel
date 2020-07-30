/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
// Inspired By https://github.com/ktorio/ktor/blob/master/ktor-client/ktor-client-okhttp/jvm/src/io/ktor/client/engine/okhttp/OkHttp.kt

package fuel.ktor

import io.ktor.client.HttpClientEngineContainer
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * [HttpClientEngineFactory] using a [Fuel] based backend implementation
 * with the the associated configuration [FuelConfig].
 */
object Fuel : HttpClientEngineFactory<FuelConfig> {
    @ExperimentalCoroutinesApi
    @KtorExperimentalAPI
    @InternalAPI
    override fun create(block: FuelConfig.() -> Unit): HttpClientEngine =
        FuelEngine(FuelConfig().apply(block))
}

@Suppress("KDocMissingDocumentation")
class FuelEngineContainer : HttpClientEngineContainer {
    override val factory: HttpClientEngineFactory<*> = Fuel

    override fun toString(): String = "Fuel"
}