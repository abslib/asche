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
import com.github.abslib.asche.base.fsm.StateBehavior
import com.github.abslib.asche.base.process.ProcessCtlAction
import com.github.abslib.asche.base.process.ProcessEvent
import com.github.abslib.asche.base.process.ProcessState
import com.github.abslib.asche.domain.dispatcher.sendEvent
import com.github.abslib.asche.domain.executor.RemoveCallEvent
import kotlinx.coroutines.ObsoleteCoroutinesApi

internal class RemoteCallJobProcess(data: Job, mailBox: MailBox<ProcessEvent>) : AbstractJobProcess(data, mailBox) {
    override fun behavior(state: ProcessState): StateBehavior<ProcessState, ProcessEvent> {
        return when (state) {
            ProcessState.INIT -> JobInitBehavior(this)
            ProcessState.WAITING -> JobWaitingBehavior(this)
            ProcessState.COMPLETED -> JobCompleteBehavior(this)
            ProcessState.STOPPED -> JobStoppedBehavior(this)
            ProcessState.SUSPENDED -> JobSuspendedBehavior(this)

            ProcessState.STOPPING -> StoppingBehavior()
            ProcessState.SUSPENDING -> SuspendingBehavior()
            ProcessState.RUNNING -> RunningBehavior()
        }
    }

    abstract inner class AsyncRemoteCallBehavior(
        private val matchedAction: ProcessCtlAction, state: ProcessState
    ) : JobBehavior(this, state) {
        @ObsoleteCoroutinesApi
        override suspend fun onEnter(from: ProcessState, action: ProcessEvent) {
            super.onEnter(from, action)
            sendEvent(RemoveCallEvent(data.type, matchedAction.name, data.context))
        }

        @ObsoleteCoroutinesApi
        override suspend fun onAction(action: ProcessEvent) {
            when (action) {
                is JobResult -> {
                    if (action.action == matchedAction.name) {
                        host.finish()
                    } else {
                        log.warn { "Job[${host.data.id}] ignore result in state: $state, by action: ${action.action}" }
                    }
                }
                else -> super.onAction(action)
            }
        }
    }

    inner class StoppingBehavior : AsyncRemoteCallBehavior(ProcessCtlAction.STOP, ProcessState.STOPPING)
    inner class SuspendingBehavior : AsyncRemoteCallBehavior(ProcessCtlAction.SUSPEND, ProcessState.SUSPENDING)
    inner class RunningBehavior : AsyncRemoteCallBehavior(ProcessCtlAction.START, ProcessState.RUNNING)
}