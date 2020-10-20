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

import com.github.abslib.asche.base.event.MailBox
import com.github.abslib.asche.base.fsm.SimpleStateMachine
import com.github.abslib.asche.base.fsm.StateBehavior
import com.github.abslib.asche.base.fsm.StateMachine
import com.github.abslib.asche.base.process.ProcessCtlAction
import com.github.abslib.asche.base.process.ProcessEvent
import com.github.abslib.asche.base.process.ProcessState

internal class TransitionalJobProcess(data: Job, mailBox: MailBox<ProcessEvent>) : AbstractJobProcess(data, mailBox) {
    override val id: Long
        get() = id

    override val fsm: StateMachine<ProcessState, ProcessEvent>
        get() {
            return SimpleStateMachine(behavior(ProcessState.valueOf(data.state)))
                    .defTransition(
                            ProcessState.INIT,
                            ProcessState.COMPLETED, behavior(ProcessState.COMPLETED),
                            ProcessCtlAction.START
                    )
        }

    override fun behavior(state: ProcessState): StateBehavior<ProcessState, ProcessEvent> {
        return when (state) {
            ProcessState.COMPLETED -> JobCompleteBehavior(this)
            else -> throw IllegalStateException("unsupported state: $state")
        }
    }
}
