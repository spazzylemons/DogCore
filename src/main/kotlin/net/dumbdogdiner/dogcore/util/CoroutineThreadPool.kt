package net.dumbdogdiner.dogcore.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

/**
 * Allocates some coroutines in global scope, and runs tasks asynchronously on the threads.
 */
object CoroutineThreadPool {
    private val channels = run {
        val channels = arrayOfNulls<SendChannel<suspend CoroutineScope.() -> Unit>>(Runtime.getRuntime().availableProcessors())

        for (i in channels.indices) {
            val channel = Channel<suspend CoroutineScope.() -> Unit>()
            channels[i] = channel

            // set up a thread to consume callbacks
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                channel.consumeEach {
                    runCatching { it() }.onFailure { e ->
                        e.printStackTrace()
                    }
                }
            }
        }

        @Suppress("unchecked_cast")
        channels as Array<SendChannel<suspend CoroutineScope.() -> Unit>>
    }

    /**
     * Blocks until a thread becomes available, and then sends the callback to that thread.
     */
    fun launch(f: suspend CoroutineScope.() -> Unit) {
        runBlocking {
            select {
                for (channel in channels) {
                    channel.onSend(f) {}
                }
            }
        }
    }
}
