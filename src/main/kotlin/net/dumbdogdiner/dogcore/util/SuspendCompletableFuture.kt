package net.dumbdogdiner.dogcore.util

import java.util.concurrent.CompletableFuture
import kotlin.coroutines.suspendCoroutine

suspend fun <T> CompletableFuture<T>.await(): T {
    return suspendCoroutine { continuation ->
        Thread {
            continuation.resumeWith(runCatching { this.get() })
        }.start()
    }
}
