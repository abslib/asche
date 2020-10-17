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

import com.github.abslib.asche.base.event.EventSourcedTarget
import com.github.abslib.asche.base.event.MailBox
import kotlinx.coroutines.yield
import java.util.concurrent.TimeUnit

abstract class TriggeredEventProcess(
    initState: ProcessState, private val mailBox: MailBox<ProcessEvent>
) : BaseStateEventProcess(initState), TriggeredProcess {

    private val es: EventSourcedTarget<ProcessEvent> by lazy {
        object : EventSourcedTarget<ProcessEvent>(mailBox) {
            override suspend fun onReceive(event: ProcessEvent) {
                fsm.act(event)
            }
        }
    }

    override suspend fun trigger(timeSlice: Long, timeUnit: TimeUnit) {
        val st = System.currentTimeMillis()
        val dt = timeUnit.toMillis(timeSlice)
        while (System.currentTimeMillis() - st < dt) {
            if (!es.receive()) {
                break
            }
            yield()
        }
    }

    override suspend fun send(event: ProcessEvent) {
        es.send(event)
    }
}
