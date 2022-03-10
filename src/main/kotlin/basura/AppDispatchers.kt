package basura

import basura.concurrent.NamedThreadFactory
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object AppDispatchers {
    private val ioExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        NamedThreadFactory("basura-io-dispatcher-worker")
    )

    val IO = ioExecutor.asCoroutineDispatcher()
}
