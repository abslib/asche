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

package com.github.abslib.asche.domain.task

import com.github.abslib.asche.base.fsm.StateBehavior
import com.github.abslib.asche.base.process.ProcessEvent
import com.github.abslib.asche.base.process.ProcessState
import com.github.abslib.asche.service.dispatcher.sendEvent
import kotlinx.coroutines.ObsoleteCoroutinesApi

internal abstract class TaskBehavior(
    protected val host: TaskProcess, override val state: ProcessState
) : StateBehavior<ProcessState, ProcessEvent> {
    override suspend fun onEnter(from: ProcessState, action: ProcessEvent) {
        host.data.state = this.state.name
        host.save()
        host.flow.send(action)
        log.info { "Task[${host.data.id}] enter state: $state from $from by action: $action" }
    }

    override suspend fun onExit(action: ProcessEvent) {
        log.info { "Task[${host.data.id}] exit state: $state by action: $action" }
    }
}

internal sealed class ActiveTaskBehavior(host: TaskProcess, state: ProcessState) : TaskBehavior(host, state) {
    @ObsoleteCoroutinesApi
    override suspend fun onAction(action: ProcessEvent) {
        when (action) {
            is JobFinish -> {
                host.flow.onNodeFinish(action.jobId)
            }
            is JobUpdate -> {
                sendEvent(action.jobId, action.event)
            }
            else -> {
                log.warn { "Task[${host.data.id} ignore action: $action in state: ${state}]" }
            }
        }
    }
}

internal sealed class DeactivatedTaskBehavior(
    host: TaskProcess, state: ProcessState
) : TaskBehavior(host, state) {
    override suspend fun onAction(action: ProcessEvent) {
        log.warn { "Task[${host.data.id} ignore action: $action in state: ${state}]" }
    }
}

internal class TaskInitBehavior(host: TaskProcess) : DeactivatedTaskBehavior(host, ProcessState.INIT)
internal class TaskWaitingBehavior(host: TaskProcess) : ActiveTaskBehavior(host, ProcessState.WAITING)
internal class TaskRunningBehavior(host: TaskProcess) : ActiveTaskBehavior(host, ProcessState.RUNNING)
internal class TaskCompleteBehavior(host: TaskProcess) : ActiveTaskBehavior(host, ProcessState.COMPLETED)
internal class TaskSuspendedBehavior(host: TaskProcess) : DeactivatedTaskBehavior(host, ProcessState.SUSPENDED)
internal class TaskSuspendingBehavior(host: TaskProcess) : ActiveTaskBehavior(host, ProcessState.SUSPENDING)
internal class TaskStoppingBehavior(host: TaskProcess) : ActiveTaskBehavior(host, ProcessState.STOPPING)
internal class TaskStoppedBehavior(host: TaskProcess) : DeactivatedTaskBehavior(host, ProcessState.STOPPED)

