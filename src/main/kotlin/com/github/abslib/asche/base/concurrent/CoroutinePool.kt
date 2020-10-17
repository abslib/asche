/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.abslib.asche.base.concurrent

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

internal val log = KotlinLogging.logger {}
internal val terminalTask = FutureTask { false }

@ObsoleteCoroutinesApi
internal val defaultWorkerContext by lazy {
    createThreadPool(32, 32, 4096, "defaultWorkerPool").asCoroutineDispatcher()
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal fun coroutineWorker(pool: CoroutinePool, context: CoroutineContext) = object : Worker {
    val isRunning = AtomicBoolean(false)
    val ch = Channel<FutureTask<out Any>>()

    override suspend fun run() {
        val w = this
        if (isRunning.compareAndSet(false, true)) {
            GlobalScope.launch(context) {
                try {
                    pool.incRunning()
                    while (!ch.isClosedForReceive) {
                        try {
                            val task = ch.receive()
                            if (task == terminalTask) {
                                ch.close()
                                break
                            }
                            task.run()
                        } catch (e: Throwable) {
                            pool.handleException(this.coroutineContext, e)
                        } finally {
                            if (!pool.revertWorker(w)) {
                                break
                            }
                        }
                    }
                } finally {
                    pool.decRunning()
                }
            }
        }
    }

    override suspend fun send(futureTask: FutureTask<out Any>) {
        ch.send(futureTask)
    }

    override suspend fun close() {
        if (isRunning.compareAndSet(true, false)) {
            ch.send(terminalTask)
        }
    }
}

internal interface Worker {
    suspend fun run()
    suspend fun send(futureTask: FutureTask<out Any>)
    suspend fun close()
}

class FutureTask<T>(private val func: suspend () -> T?) {
    private val resultCh = Channel<T>(1)
    private var result: T? = null
    private val state = AtomicInteger(0)

    @ExperimentalCoroutinesApi
    internal suspend fun run() {
        if (state.compareAndSet(0, 1)) {
            try {
                func()?.also { resultCh.send(it) }
            } catch (e: Throwable) {
                resultCh.close(e)
                throw e
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun get(): T? {
        try {
            result = resultCh.receive()
            if (!resultCh.isClosedForReceive) {
                resultCh.close()
            }
        } catch (e: ClosedReceiveChannelException) {
            result = null
        }
        return result
    }

    @ExperimentalCoroutinesApi
    suspend fun get(timeout: Long, unit: TimeUnit): T? {
        withTimeout(unit.toMillis(timeout)) {
            get()
        }
        return result
    }
}

class CoroutinePool(
    private val capacity: Int,
    private val context: CoroutineContext,
    private val exceptionHandler: CoroutineExceptionHandler
) {
    private val running = AtomicInteger(0)
    private val mutex = Mutex()
    private val workers = Channel<Worker>(capacity)
    private val stopped = AtomicBoolean(false)

    @ObsoleteCoroutinesApi
    constructor(capacity: Int) : this(capacity, defaultWorkerContext)
    constructor(capacity: Int, context: CoroutineContext) : this(
        capacity,
        context,
        CoroutineExceptionHandler { _, e -> log.error { e } })

    internal suspend fun revertWorker(worker: Worker): Boolean {
        return try {
            workers.send(worker)
            true
        } catch (e: Exception) {
            false
        }
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    private suspend fun retrieveWorker(): Worker {
        val pool = this
        var w: Worker? = null
        if (running.get() < capacity) {
            w = coroutineWorker(pool, context).also {
                it.run()
            }
        }
        if (w == null) {
            yield()
            w = workers.receive()
        }
        return w
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    suspend fun <T : Any> submit(func: suspend () -> T): FutureTask<T> {
        mutex.withLock {
            if (stopped.get()) {
                throw IllegalStateException("CoroutinePool stopped")
            }
            return FutureTask(func).also { retrieveWorker().send(it) }
        }
    }

    suspend fun close(timeout: Long, timeUnit: TimeUnit) {
        val dt = timeUnit.toMillis(timeout)
        val st = System.currentTimeMillis()
        mutex.withLock {
            if (stopped.compareAndSet(false, true)) {
                workers.close()
                for (w in workers) {
                    w.close()
                }
                while (running.get() > 0) {
                    delay(10)
                    if (System.currentTimeMillis() - st > dt) {
                        break
                    }
                }
            }
        }
    }

    fun handleException(context: CoroutineContext, exception: Throwable) {
        exceptionHandler.handleException(context, exception)
    }

    internal fun incRunning() {
        running.incrementAndGet()
    }

    internal fun decRunning() {
        running.decrementAndGet()
    }
}