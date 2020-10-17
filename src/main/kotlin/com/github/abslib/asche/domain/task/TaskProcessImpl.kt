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

import com.github.abslib.asche.base.event.MailBox
import com.github.abslib.asche.base.fsm.StateBehavior
import com.github.abslib.asche.base.process.ProcessEvent
import com.github.abslib.asche.base.process.ProcessState
import com.github.abslib.asche.base.process.TriggeredEventProcess
import com.github.abslib.asche.domain.flow.FlowController

internal class TaskProcessImpl(
    override val data: Task,
    override val flow: FlowController,
    mailBox: MailBox<ProcessEvent>
) : TaskProcess, TriggeredEventProcess(ProcessState.valueOf(data.state), mailBox) {
    lateinit var taskRepository: TaskRepository
    override fun save() {
        taskRepository.save(this.data)
    }

    override fun behavior(state: ProcessState): StateBehavior<ProcessState, ProcessEvent> {
        return when (state) {
            ProcessState.INIT -> TaskInitBehavior(this)
            ProcessState.WAITING -> TaskWaitingBehavior(this)
            ProcessState.RUNNING -> TaskRunningBehavior(this)
            ProcessState.COMPLETED -> TaskCompleteBehavior(this)
            ProcessState.STOPPING -> TaskStoppingBehavior(this)
            ProcessState.STOPPED -> TaskStoppedBehavior(this)
            ProcessState.SUSPENDING -> TaskSuspendingBehavior(this)
            ProcessState.SUSPENDED -> TaskSuspendedBehavior(this)
        }
    }
}