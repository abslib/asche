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

package com.github.abslib.asche.domain.job

import com.github.abslib.asche.base.fsm.StateBehavior
import com.github.abslib.asche.base.process.ProcessEvent
import com.github.abslib.asche.base.process.ProcessState
import com.github.abslib.asche.domain.task.JobFinish
import com.github.abslib.asche.service.dispatcher.sendEvent
import kotlinx.coroutines.ObsoleteCoroutinesApi

internal abstract class JobBehavior(
    protected val host: JobProcess, override val state: ProcessState
) : StateBehavior<ProcessState, ProcessEvent> {
    override suspend fun onEnter(from: ProcessState, action: ProcessEvent) {
        host.data.state = this.state.name
        host.save()
        com.github.abslib.asche.domain.task.log.info { "Job[${host.data.id}] enter state: $state from $from by action: $action" }
    }

    override suspend fun onExit(action: ProcessEvent) {
        com.github.abslib.asche.domain.task.log.info { "Job[${host.data.id}] exit state: $state by action: $action" }
    }

    override suspend fun onAction(action: ProcessEvent) {
        com.github.abslib.asche.domain.task.log.warn { "Job[${host.data.id} ignore action: $action in state: ${state}]" }
    }
}

internal sealed class DeactivatedJobBehavior(host: JobProcess, state: ProcessState) : JobBehavior(host, state) {
    @ObsoleteCoroutinesApi
    override suspend fun onEnter(from: ProcessState, action: ProcessEvent) {
        super.onEnter(from, action)
        sendEvent(host.data.taskId, JobFinish(host.data.id))
    }
}

internal class JobInitBehavior(host: JobProcess) : JobBehavior(host, ProcessState.INIT)
internal class JobWaitingBehavior(host: JobProcess) : JobBehavior(host, ProcessState.WAITING)
internal class JobRunningBehavior(host: JobProcess) : JobBehavior(host, ProcessState.RUNNING)
internal class JobSuspendingBehavior(host: JobProcess) : JobBehavior(host, ProcessState.SUSPENDING)
internal class JobStoppingBehavior(host: JobProcess) : JobBehavior(host, ProcessState.STOPPING)

internal class JobCompleteBehavior(host: JobProcess) : DeactivatedJobBehavior(host, ProcessState.COMPLETED)
internal class JobSuspendedBehavior(host: JobProcess) : DeactivatedJobBehavior(host, ProcessState.SUSPENDED)
internal class JobStoppedBehavior(host: JobProcess) : DeactivatedJobBehavior(host, ProcessState.STOPPED)
