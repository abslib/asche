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

package com.github.abslib.asche.base.process

import com.github.abslib.asche.base.event.BaseEvent
import com.github.abslib.asche.base.event.EventDispatcher
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

internal val log = KotlinLogging.logger {}

interface BaseProcess {
    suspend fun start()
    suspend fun stop()
    suspend fun finish()
}

interface TriggeredProcess : BaseProcess {
    suspend fun trigger(timeSlice: Long, timeUnit: TimeUnit)
}

interface SuspendableProcess : BaseProcess {
    suspend fun suspend()
    suspend fun resume()
}

interface EventSourcedProcess : BaseProcess, SuspendableProcess, EventDispatcher<ProcessEvent>

interface ProcessEvent : BaseEvent
enum class ProcessCtlAction : ProcessEvent { START, STOP, SUSPEND, RESUME, FINISH }

enum class ProcessState { INIT, WAITING, RUNNING, COMPLETED, STOPPING, STOPPED, SUSPENDING, SUSPENDED }
