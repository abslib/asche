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

package com.github.abslib.asche.domain.actuator

import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean

internal val log = KotlinLogging.logger {}

@ObsoleteCoroutinesApi
internal val flowActuator = TaskProcessActuator()

@ObsoleteCoroutinesApi
fun start() {
    runBlocking {
        flowActuator.start()
    }
}

@ObsoleteCoroutinesApi
fun stop() {
    runBlocking {
        flowActuator.stop()
    }
}

@ObsoleteCoroutinesApi
internal abstract class SingleThreadLoopActuator(private val name: String) {
    private val isRunning = AtomicBoolean(false)
    private lateinit var job: Job

    open suspend fun start() {
        job = GlobalScope.launch(newSingleThreadContext(name)) {
            while (isRunning.get()) {
                try {
                    handle()
                } catch (e: Exception) {
                    log.error { "Actuator handle error: $e" }
                }
            }
            yield()
        }
    }

    protected abstract suspend fun handle()

    open suspend fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            job.cancelAndJoin()
        }
    }
}

